package com.dsh.crackpackage.util;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

public class FileHashUtil {
    private static final String FAILED_PASSWORDS_DIR = "failed_passwords";
    private static final int BUFFER_SIZE = 8192;
    private static final int WRITE_BATCH_SIZE = 1000; // 每1000个密码写入一次
    private static final int READ_BATCH_SIZE = 10000; // 每次读取10000行
    private static Map<String, Set<String>> failedPasswordsCache = new HashMap<>();
    private static Map<String, Integer> batchCounters = new HashMap<>();

    public static String calculateMD5(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    md.update(buffer, 0, read);
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("计算文件MD5时发生错误：" + e.getMessage());
            return null;
        }
    }

    public static void recordFailedPassword(String fileMD5, String password) {
        if (fileMD5 == null || password == null) return;

        synchronized (failedPasswordsCache) {
            // 获取或创建缓存
            Set<String> cache = failedPasswordsCache.computeIfAbsent(fileMD5, k -> new HashSet<>());
            cache.add(password);

            // 更新计数器
            int count = batchCounters.getOrDefault(fileMD5, 0) + 1;
            batchCounters.put(fileMD5, count);

            // 达到批量大小时写入
            if (count >= WRITE_BATCH_SIZE) {
                flushFailedPasswords(fileMD5);
            }
        }
    }

    public static void flushFailedPasswords(String fileMD5) {
        if (fileMD5 == null || !failedPasswordsCache.containsKey(fileMD5)) return;

        Set<String> newPasswords;
        synchronized (failedPasswordsCache) {
            newPasswords = new HashSet<>(failedPasswordsCache.get(fileMD5));
        }

        try {
            Files.createDirectories(Paths.get(FAILED_PASSWORDS_DIR));
            Path failedPasswordsFile = Paths.get(FAILED_PASSWORDS_DIR, fileMD5 + ".txt");
            
            // 如果文件不存在，直接写入
            if (!Files.exists(failedPasswordsFile)) {
                Files.write(failedPasswordsFile, newPasswords);
            } else {
                // 如果文件存在，分批读取并合并
                Set<String> mergedPasswords = new HashSet<>(newPasswords);
                try (BufferedReader reader = Files.newBufferedReader(failedPasswordsFile)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        mergedPasswords.add(line);
                    }
                }
                Files.write(failedPasswordsFile, mergedPasswords);
            }
            
            // 清除缓存和计数器
            synchronized (failedPasswordsCache) {
                failedPasswordsCache.get(fileMD5).clear();
                batchCounters.put(fileMD5, 0);
            }
        } catch (IOException e) {
            System.out.println("写入失败密码时发生错误：" + e.getMessage());
        }
    }

    public static void flushAllFailedPasswords() {
        Set<String> fileMD5Set;
        synchronized (failedPasswordsCache) {
            fileMD5Set = new HashSet<>(failedPasswordsCache.keySet());
        }
        for (String fileMD5 : fileMD5Set) {
            flushFailedPasswords(fileMD5);
        }
    }

    public static Set<String> getFailedPasswords(String fileMD5) {
        try {
            Path failedPasswordsFile = Paths.get(FAILED_PASSWORDS_DIR, fileMD5 + ".txt");
            if (!Files.exists(failedPasswordsFile)) {
                return new HashSet<>();
            }

            // 获取文件大小
            long fileSize = Files.size(failedPasswordsFile);
            int estimatedLines = (int)(fileSize / 10); // 假设每行平均10个字符
            
            // 创建一个合适大小的HashSet
            Set<String> failedPasswords = new HashSet<>(Math.min(estimatedLines, 1000000));
            
            // 分批读取文件
            try (BufferedReader reader = Files.newBufferedReader(failedPasswordsFile)) {
                String line;
                int lineCount = 0;
                int batchCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    failedPasswords.add(line);
                    lineCount++;
                    
                    // 每读取一定数量的行，显示进度
                    if (lineCount % READ_BATCH_SIZE == 0) {
                        batchCount++;
                        System.out.printf("\r正在加载历史密码记录... 已加载 %d 批（每批 %d 个）", 
                            batchCount, READ_BATCH_SIZE);
                    }
                }
                
                if (lineCount > READ_BATCH_SIZE) {
                    System.out.println(); // 换行
                }
            }

            // 添加缓存中的密码
            if (failedPasswordsCache.containsKey(fileMD5)) {
                failedPasswords.addAll(failedPasswordsCache.get(fileMD5));
            }
            
            return failedPasswords;
        } catch (IOException e) {
            System.out.println("读取失败密码时发生错误：" + e.getMessage());
            return new HashSet<>();
        }
    }

    public static void clearFailedPasswords(String fileMD5) {
        try {
            Path failedPasswordsFile = Paths.get(FAILED_PASSWORDS_DIR, fileMD5 + ".txt");
            Files.deleteIfExists(failedPasswordsFile);
            failedPasswordsCache.remove(fileMD5);
            batchCounters.remove(fileMD5);
        } catch (IOException e) {
            System.out.println("清除失败密码记录时发生错误：" + e.getMessage());
        }
    }

    // 添加关闭钩子，确保程序退出时写入所有缓存的密码
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在保存失败密码记录...");
            flushAllFailedPasswords();
        }));
    }
}
