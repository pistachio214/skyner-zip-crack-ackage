import java.util.Random;
import java.util.Stack;

public class CrackUtil {


    /**
     * 数字
     */
    public static final String NUMBER = "0123456789";
    /**
     * 字母
     */
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwyxz";

    /**
     * 符号
     */
    public static final String SYMBOL = "~!@#$%^&*()_+[]{};,.<>?-=";


    static Stack<String> stack = new Stack<String>();
    static int cnt = 0;


    /**
     * 获取指定的字符
     *
     * @param includeNumber   是否包含数字
     * @param includealphabet 是否包含小写字母
     * @param includeAlphabet 是否包含大写字母
     * @param includeSymbol   是否包含字符
     * @return
     */
    public static String[] getStr(boolean includeNumber,boolean includealphabet, boolean includeAlphabet, boolean includeSymbol) {

        StringBuffer sb = new StringBuffer();
        if (includeNumber) {
            sb.append(NUMBER);
        }
        if (includealphabet) {
            sb.append(ALPHABET);
        }
        if(includeAlphabet){
            sb.append(ALPHABET.toUpperCase());
        }
        if (includeSymbol) {
            sb.append(SYMBOL);
        }
        char[] chars = sb.toString().toCharArray();
        String[] strings = new String[chars.length];
        for (int i = 0; i < chars.length; i++) {
            strings[i] = String.valueOf(chars[i]);
        }
        shuffle(strings);//打乱数组

        return strings;
    }
    //打乱数组
    private static void shuffle(String[] arr) {
        Random mRandom = new Random();
        for (int i = arr.length; i > 0; i--) {
            int rand = mRandom.nextInt(i);
            swap(arr, rand, i - 1);
        }
    }

    //交换两个值
    private static void swap(String[] a, int i, int j) {
        String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    /**
     * 递归方法，当实际选取的数目与要求选取的小目相同时，跳出递归
     *
     * @param array  - 数组
     * @param curnum - 当前已经确定的个数
     * @param maxnum - 要选取的数目
     * @source source - 要破解的文件路径
     * @source dest - 破解后存放到哪里
     * @source stime - 程序开始时间
     */
    public static void recursion(String[] array, int curnum, int maxnum,String source,String dest,long stime) {
        if (curnum == maxnum) {
            cnt++;
            String pwd ="";
            for(String item : stack){
                pwd=pwd+item;
            }
            //获取文件名后缀，判断是破解zip文件还是破解rar文件
            String suffix = StringUtil.getSuffix(source);
            if(suffix.equals("zip")){
                //解压zip
                boolean z = UnZipUtil.unZip(source,dest,pwd);
                System.out.println("密码："+pwd+"  破解结果："+z);
                if(z){
                    // 结束时间
                    long etime = System.currentTimeMillis();
                    // 计算执行时间
                    System.out.printf("执行时长：%d 毫秒.", (etime - stime));
                    System.exit(0);
                }
                return;
            }
            if(suffix.equals("rar")||suffix.equals("7z")){
                //破解rar
                boolean z = UnRar5Util.decompressFile(source,dest,pwd);
                System.out.println("密码："+pwd+"  破解结果："+z);
                if(z){
                    // 结束时间
                    long etime = System.currentTimeMillis();
                    // 计算执行时间
                    System.out.printf("执行时长：%d 毫秒.", (etime - stime));
                    System.exit(0);
                }
                return;
            }


        }
        for (String item : array) {
            stack.push(item);
            recursion(array, curnum + 1, maxnum, source, dest,stime);
            stack.pop();
        }
    }

//    public static void main(String[] args) {
//        String[] s=getStr(true,false,false,false);
//        for(String item : s){
//            System.out.println(item);
//        }
//    }



}
