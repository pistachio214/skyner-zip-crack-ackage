package cn.skyner.crack.pkg;

import cn.skyner.crack.pkg.impl.DictionaryService;
import cn.skyner.crack.pkg.util.CrackUtil;
import cn.skyner.crack.pkg.util.FileUtil;
import cn.skyner.crack.pkg.util.LogUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Application {
    private static final String ZIP_DIR = "zip_files";
    private static final String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        try {
            // 初始化目录
            FileUtil.initDirectories();

            // 扫描压缩文件
            List<String> zipFiles = FileUtil.scanZipFiles();
            if (zipFiles.isEmpty()) {
                LogUtil.error("未找到任何压缩文件，请将文件放入 zip_files 目录中");
                return;
            }

            // 选择要破解的文件
            String selectedFile = selectFile(zipFiles);
            if (selectedFile == null) {
                return;
            }

            // 开始破解
            crack(selectedFile);

        } catch (Exception e) {
            LogUtil.error("程序执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String selectFile(List<String> zipFiles) {
        // 显示文件列表
        LogUtil.info("发现以下压缩文件：");
        for (int i = 0; i < zipFiles.size(); i++) {
            File file = new File(zipFiles.get(i));
            LogUtil.info(String.format("%d. %s (%s)",
                    i + 1,
                    file.getName(),
                    FileUtil.formatFileSize(file.length())
            ));
        }

        // 选择要破解的文件
        Scanner scanner = new Scanner(System.in);
        LogUtil.info("");
        System.out.print("请选择要破解的文件编号 (1-" + zipFiles.size() + "): ");
        int choice = scanner.nextInt();

        if (choice < 1 || choice > zipFiles.size()) {
            LogUtil.error("无效的选择！");
            return null;
        }

        String selectedFile = zipFiles.get(choice - 1);
        LogUtil.info("");
        LogUtil.info("已选择: " + new File(selectedFile).getName());

        // 输入密码长度范围
        System.out.print("请输入最小密码长度: ");
        int minLen = scanner.nextInt();
        System.out.print("请输入最大密码长度: ");
        int maxLen = scanner.nextInt();

        if (minLen < 1 || maxLen < minLen) {
            LogUtil.error("无效的密码长度范围！");
            return null;
        }

        return selectedFile;
    }

    private static void crack(String selectedFile) {
        LogUtil.info("开始智能破解密码...");
        LogUtil.info("");

        // 开始破解
        String outputPath = Paths.get(OUTPUT_DIR,
                selectedFile.replaceFirst("[.][^.]+$", "")).toString();

        String password = CrackUtil.crack(
                selectedFile,
                outputPath,
                1,
                10,
                new DictionaryService()
        );

        // 显示结果
        if (password != null) {
            LogUtil.info("破解成功！密码是: " + password);
            LogUtil.info("文件已解压到: " + outputPath);
        } else {
            LogUtil.error("未能破解密码。");
            LogUtil.info("建议：");
            LogUtil.info("1. 尝试更大的密码长度范围");
            LogUtil.info("2. 检查文件是否已损坏");
            LogUtil.info("3. 确认文件是否有密码保护");
        }
    }
}
