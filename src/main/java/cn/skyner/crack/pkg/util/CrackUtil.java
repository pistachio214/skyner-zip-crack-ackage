package com.dsh.crackpackage.util;

import com.dsh.crackpackage.api.IDictionaryService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CrackUtil {
    private static final int REPORT_INTERVAL = 100;  // 每尝试100次密码报告一次进度
    private static final AtomicInteger totalAttempts = new AtomicInteger(0);
    private static long startTime = System.currentTimeMillis();
    private static String currentFileMD5;
    private static Set<String> failedPasswords;
    private static int skippedAttempts = 0;
    private static int stageAttempts = 0;
    private static int stageSkipped = 0;

    public static String crack(String source, String dest, int minLen, int maxLen, IDictionaryService dictionary) {
        com.dsh.crackpackage.util.LogUtil.info("开始智能破解密码...");
        totalAttempts.set(0);
        skippedAttempts = 0;
        startTime = System.currentTimeMillis();

        // 阶段0：加载历史密码记录
        com.dsh.crackpackage.util.LogUtil.info("\n[阶段0] 加载历史密码记录...");
        // 计算文件的 MD5
        currentFileMD5 = com.dsh.crackpackage.util.FileHashUtil.calculateMD5(source);
        if (currentFileMD5 == null) {
            com.dsh.crackpackage.util.LogUtil.error("无法计算文件的 MD5，将不会记录失败的密码");
            failedPasswords = new HashSet<>();
            com.dsh.crackpackage.util.LogUtil.info("完成 - MD5 计算失败，跳过历史记录加载");
        } else {
            failedPasswords = com.dsh.crackpackage.util.FileHashUtil.getFailedPasswords(currentFileMD5);
            com.dsh.crackpackage.util.LogUtil.info("完成 - 已加载 " + failedPasswords.size() + " 个历史失败密码记录");
        }

        String result = null;
        try {
            // 阶段1：尝试常用密码列表
            com.dsh.crackpackage.util.LogUtil.info("\n[阶段1] 尝试常用密码列表...");
            resetStageCounters();
            result = tryPasswordList(source, dest, dictionary.getCommonPasswords());
            if (result != null) {
                com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
                com.dsh.crackpackage.util.FileHashUtil.clearFailedPasswords(currentFileMD5); // 清除失败记录
                com.dsh.crackpackage.util.LogUtil.info("");
                com.dsh.crackpackage.util.LogUtil.info("破解成功！密码是: " + result);
                com.dsh.crackpackage.util.LogUtil.info("文件已解压到: " + dest);
                return result;
            }
            com.dsh.crackpackage.util.FileHashUtil.flushFailedPasswords(currentFileMD5); // 阶段1结束时写入
            com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
            com.dsh.crackpackage.util.LogUtil.info("完成 - 尝试了 " + stageAttempts + " 个常用密码，跳过了 " + stageSkipped + " 个已知密码");

            // 阶段2：尝试简单数字组合
            com.dsh.crackpackage.util.LogUtil.info("\n[阶段2] 尝试简单数字组合...");
            resetStageCounters();
            result = tryPasswordList(source, dest, dictionary.getNumericPasswords());
            if (result != null) {
                com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
                com.dsh.crackpackage.util.FileHashUtil.clearFailedPasswords(currentFileMD5); // 清除失败记录
                com.dsh.crackpackage.util.LogUtil.info("");
                com.dsh.crackpackage.util.LogUtil.info("破解成功！密码是: " + result);
                com.dsh.crackpackage.util.LogUtil.info("文件已解压到: " + dest);
                return result;
            }
            com.dsh.crackpackage.util.FileHashUtil.flushFailedPasswords(currentFileMD5); // 阶段2结束时写入
            com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
            com.dsh.crackpackage.util.LogUtil.info("完成 - 尝试了 " + stageAttempts + " 个数字组合，跳过了 " + stageSkipped + " 个已知密码");

            // 阶段3：尝试常见密码模式
            com.dsh.crackpackage.util.LogUtil.info("\n[阶段3] 尝试常见密码模式...");
            resetStageCounters();
            result = tryPasswordList(source, dest, dictionary.getPatternPasswords());
            if (result != null) {
                com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
                com.dsh.crackpackage.util.FileHashUtil.clearFailedPasswords(currentFileMD5); // 清除失败记录
                com.dsh.crackpackage.util.LogUtil.info("");
                com.dsh.crackpackage.util.LogUtil.info("破解成功！密码是: " + result);
                com.dsh.crackpackage.util.LogUtil.info("文件已解压到: " + dest);
                return result;
            }
            com.dsh.crackpackage.util.FileHashUtil.flushFailedPasswords(currentFileMD5); // 阶段3结束时写入
            com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
            com.dsh.crackpackage.util.LogUtil.info("完成 - 尝试了 " + stageAttempts + " 个模式密码，跳过了 " + stageSkipped + " 个已知密码");

            // 阶段4：智能暴力破解
            com.dsh.crackpackage.util.LogUtil.info("\n[阶段4] 开始智能暴力破解...");
            resetStageCounters();
            result = smartBruteForce(source, dest, minLen, maxLen);
            if (result != null) {
                com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
                com.dsh.crackpackage.util.FileHashUtil.clearFailedPasswords(currentFileMD5); // 清除失败记录
                com.dsh.crackpackage.util.LogUtil.info("");
                com.dsh.crackpackage.util.LogUtil.info("破解成功！密码是: " + result);
                com.dsh.crackpackage.util.LogUtil.info("文件已解压到: " + dest);
            } else {
                com.dsh.crackpackage.util.FileHashUtil.flushFailedPasswords(currentFileMD5); // 破解失败时写入
                com.dsh.crackpackage.util.LogUtil.clearProgress(); // 清除进度行
                com.dsh.crackpackage.util.LogUtil.info("完成 - 尝试了 " + stageAttempts + " 个暴力破解密码，跳过了 " + stageSkipped + " 个已知密码");
                com.dsh.crackpackage.util.LogUtil.info("");
                com.dsh.crackpackage.util.LogUtil.info("破解失败 - 总共尝试了 " + totalAttempts.get() + " 个密码，跳过了 " + skippedAttempts + " 个已知密码，耗时 " +
                        String.format("%.1f", (System.currentTimeMillis() - startTime) / 1000.0) + " 秒");
            }

        } catch (Exception e) {
            com.dsh.crackpackage.util.LogUtil.error("破解过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private static void resetStageCounters() {
        stageAttempts = 0;
        stageSkipped = 0;
    }

    private static String tryPasswordList(String source, String dest, String[] passwords) {
        for (String password : passwords) {
            // 跳过已知失败的密码
            if (failedPasswords.contains(password)) {
                skippedAttempts++;
                stageSkipped++;
                continue;
            }

            if (tryPassword(source, dest, password)) {
                return password;
            }

            // 记录失败的密码
            if (currentFileMD5 != null) {
                com.dsh.crackpackage.util.FileHashUtil.recordFailedPassword(currentFileMD5, password);
            }

            stageAttempts++;
            int attempts = totalAttempts.incrementAndGet();
            if (attempts % REPORT_INTERVAL == 0) {
                reportProgress(password, attempts);
            }
        }
        return null;
    }

    private static boolean tryPassword(String source, String dest, String password) {
        return com.dsh.crackpackage.util.CompressUtil.extract(source, dest, password);
    }

    private static String smartBruteForce(String source, String dest, int minLen, int maxLen) {
        String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()";

        for (int len = minLen; len <= maxLen; len++) {
            char[] current = new char[len];
            for (int i = 0; i < len; i++) {
                current[i] = charset.charAt(0);
            }

            while (true) {
                String password = new String(current);

                // 跳过已知失败的密码
                if (!failedPasswords.contains(password)) {
                    if (tryPassword(source, dest, password)) {
                        com.dsh.crackpackage.util.LogUtil.info(""); // 清除进度行
                        return password;
                    }

                    // 记录失败的密码
                    if (currentFileMD5 != null) {
                        com.dsh.crackpackage.util.FileHashUtil.recordFailedPassword(currentFileMD5, password);
                    }

                    stageAttempts++;
                    int attempts = totalAttempts.incrementAndGet();
                    if (attempts % REPORT_INTERVAL == 0) {
                        reportProgress(password, attempts, len);
                    }
                } else {
                    skippedAttempts++;
                    stageSkipped++;
                }

                // 生成下一个密码组合
                int pos = len - 1;
                while (pos >= 0) {
                    int index = charset.indexOf(current[pos]);
                    if (index < charset.length() - 1) {
                        current[pos] = charset.charAt(index + 1);
                        break;
                    } else {
                        current[pos] = charset.charAt(0);
                        pos--;
                    }
                }
                if (pos < 0) break;
            }
        }
        com.dsh.crackpackage.util.LogUtil.info(""); // 清除进度行
        return null;
    }

    private static void reportProgress(String lastTried, int totalTries) {
        double timeElapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        double speed = totalTries / timeElapsed;
        com.dsh.crackpackage.util.LogUtil.progress(String.format("\r尝试: %-20s [总尝试: %d] [已跳过: %d] [速度: %.1f次/秒] [耗时: %.1f秒]",
                lastTried, totalTries, skippedAttempts, speed, timeElapsed));
    }

    private static void reportProgress(String lastTried, int totalTries, int currentLen) {
        double timeElapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        double speed = totalTries / timeElapsed;
        com.dsh.crackpackage.util.LogUtil.progress(String.format("\r尝试: %-20s [长度: %d] [总尝试: %d] [已跳过: %d] [速度: %.1f次/秒] [耗时: %.1f秒]",
                lastTried, currentLen, totalTries, skippedAttempts, speed, timeElapsed));
    }
}
