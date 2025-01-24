package com.dsh.crackpackage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;
    private static final String ZIP_DIR = "zip_files";
    private static final String OUTPUT_DIR = "output";
    private static final String[] SUPPORTED_EXTENSIONS = {
        ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz"
    };

    public static void initDirectories() throws IOException {
        Files.createDirectories(Paths.get(ZIP_DIR));
        Files.createDirectories(Paths.get(OUTPUT_DIR));
    }

    public static List<String> scanZipFiles() {
        List<String> zipFiles = new ArrayList<>();
        File dir = new File(ZIP_DIR);
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (lower.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });

        if (files != null) {
            for (File file : files) {
                zipFiles.add(file.getAbsolutePath());
            }
        }

        return zipFiles;
    }

    public static String formatFileSize(long size) {
        if (size < KB) {
            return size + " B";
        } else if (size < MB) {
            return String.format("%.2f KB", (float) size / KB);
        } else if (size < GB) {
            return String.format("%.2f MB", (float) size / MB);
        } else {
            return String.format("%.2f GB", (float) size / GB);
        }
    }
}
