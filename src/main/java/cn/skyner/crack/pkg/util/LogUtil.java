package cn.skyner.crack.pkg.util;

public class LogUtil {
    public static void info(String message) {
        System.out.println(message);
    }

    public static void warning(String message) {
        System.out.println("[警告] " + message);
    }

    public static void error(String message) {
        System.out.println("[错误] " + message);
    }

    public static void progress(String message) {
        System.out.print("\r" + message);
    }

    public static void clearProgress() {
        System.out.print("\r");
        for (int i = 0; i < 120; i++) {
            System.out.print(" ");
        }
        System.out.print("\r");
    }
}
