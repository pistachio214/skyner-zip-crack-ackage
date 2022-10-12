import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class UnRar5Util {


    public static boolean decompressFile(String inputFilePath, final String targetFileDir,String pwd) {
        boolean r =false;
        //解压7zip文件
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        try {
            // 判断目标目录是否存在，不存在则创建
            File newdir = new File(targetFileDir);
            if (false == newdir.exists()) {
                newdir.mkdirs();
                newdir = null;
            }
            randomAccessFile = new RandomAccessFile(inputFilePath, "r");
            RandomAccessFileInStream t = new RandomAccessFileInStream(randomAccessFile);
            if(inputFilePath.endsWith("rar")){
                inArchive = SevenZip.openInArchive(ArchiveFormat.RAR5, t,pwd);
            }
            else if(inputFilePath.endsWith("7z")){
                inArchive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, t,pwd);
            }
            else{
                inArchive = SevenZip.openInArchive(ArchiveFormat.ZIP, t,pwd);
            }
            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                final int[] hash = new int[]{0};
                //System.out.println("item.isFolder()==" + item.isFolder());
                if (!item.isFolder()) {
                    ExtractOperationResult result;
                    final long[] sizeArray = new long[1];
                    result = item.extractSlow(new ISequentialOutStream() {
                        public int write(byte[] data) throws SevenZipException {
                            //写入指定文件
                            FileOutputStream fos;
                            try {
                                if (item.getPath().indexOf(File.separator) > 0) {
                                    String path = targetFileDir + File.separator + item.getPath().substring(0, item.getPath().lastIndexOf(File.separator));
                                    File folderExisting = new File(path);
                                    if (!folderExisting.exists()){
                                        new File(path).mkdirs();
                                    }
                                }
                                fos = new FileOutputStream(targetFileDir + File.separator + item.getPath(), true);
                                fos.write(data);
                                fos.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            hash[0] ^= Arrays.hashCode(data); // Consume data
                            sizeArray[0] += data.length;
                            return data.length; // Return amount of consumed data
                        }
                    },pwd);

                    if (result == ExtractOperationResult.OK) {
                        System.out.println(String.format("%9X | %10s | %s",hash[0], sizeArray[0], item.getPath()));
                        r=true;
                    }
                    else {
                        //System.err.println("Error extracting item: " + result);

                    }
                }
            }
        } catch (Exception e) {
            //System.err.println("Error occurs: " + e);
            e.printStackTrace();
            //System.exit(1);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException e) {
                    //System.err.println("Error closing archive: " + e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    //System.err.println("Error closing file: " + e);
                }
            }
        }
        //System.out.println("return false");
        return r;
    }

//    public static void main(String[] args) {
//        decompressFile("/Users/dong4hang/Desktop/7z/Downloads1.7z","/Users/dong4hang/Desktop/7z/","111");
//    }
}
