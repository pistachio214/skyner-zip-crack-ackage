


public class StringUtil {
    /**
     * 获取文件名后缀
     */
    public static String getSuffix(String fileName){
        String[] s = fileName.split("\\.");
        String suffix = s[s.length-1];
        return suffix;
    }

//    public static void main(String[] args) {
//        String suffix=getSuffix("/Users/dong4hang/Desktop/rar/cxnx++6960.rar");
//        System.out.println(suffix);
//    }

}

