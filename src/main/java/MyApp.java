
import java.io.File;
import java.util.Scanner;

public class MyApp {
    public static void main(String[] args) {
        String source = "";//文件路径
        String dest="";//解压路径
        boolean includeNumber=true;//是否包含数字
        boolean includealphabet=true;//是否包含小写字母
        boolean includeAlphabet=true;//是否包含大写字母
        boolean includeSymbol=true;//是否包含符号
        int length=1;//最长密码长度
        String userDir = System.getProperties().getProperty("user.dir");
        System.out.println(userDir);
        System.out.println("要破解的文件：");
        Scanner scanner = new Scanner(System.in);
        source= userDir + File.separator+ scanner.nextLine();
//        System.out.println("破解到：");
//        Scanner scanner2 = new Scanner(System.in);
        dest=userDir;
        System.out.println("是否包含数字？（Y/N）:");
        Scanner scanner3 = new Scanner(System.in);
        if(scanner3.nextLine().equals("N")){
            includeNumber=false;
        }
        System.out.println("是否包含小写字母？（Y/N）:");
        Scanner scanner4 = new Scanner(System.in);
        if(scanner4.nextLine().equals("N")){
            includealphabet=false;
        }
        System.out.println("是否包含大写字母？（Y/N）:");
        Scanner scanner5 = new Scanner(System.in);
        if(scanner5.nextLine().equals("N")){
            includeAlphabet=false;
        }
        System.out.println("是否包含符号？（Y/N）:");
        Scanner scanner6 = new Scanner(System.in);
        if(scanner6.nextLine().equals("N")){
            includeSymbol=false;
        }
        //System.out.println("密码最长长度:");
        //Scanner scanner7 = new Scanner(System.in);
        //length=Integer.parseInt(scanner7.nextLine());
        length=20;//最高破解20位的密码
        // 开始时间
        long stime = System.currentTimeMillis();
        // 程序执行
        String[] pwd = CrackUtil.getStr(includeNumber,includealphabet,includeAlphabet,includeSymbol);
        // 循环密码长度
        for(int i=1;i<=length;i++){
            // 执行破解程序
            CrackUtil.recursion(pwd,0,i,source,dest,stime);
        }

    }
}
