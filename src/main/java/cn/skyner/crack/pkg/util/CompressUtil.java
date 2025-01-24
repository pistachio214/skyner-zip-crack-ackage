package cn.skyner.crack.pkg.util;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CompressUtil {
    public static CompressType getCompressType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".zip")) return CompressType.ZIP;
        if (lowerPath.endsWith(".rar")) return CompressType.RAR;
        if (lowerPath.endsWith(".7z")) return CompressType._7Z;
        if (lowerPath.endsWith(".tar")) return CompressType.TAR;
        if (lowerPath.endsWith(".gz")) return CompressType.GZ;
        if (lowerPath.endsWith(".bz2")) return CompressType.BZ2;
        if (lowerPath.endsWith(".xz")) return CompressType.XZ;
        return CompressType.UNKNOWN;
    }

    public static boolean extract(String source, String dest, String password) {
        try {
            CompressType type = getCompressType(source);
            switch (type) {
                case ZIP:
                    return unZip(source, dest, password);
                case RAR:
                    return unRar(source, dest, password);
                case _7Z:
                    return un7z(source, dest, password);
                case TAR:
                    return unTar(source, dest);
                case GZ:
                    return unGzip(source, dest);
                case BZ2:
                    return unBzip2(source, dest);
                case XZ:
                    return unXz(source, dest);
                default:
                    System.out.println("不支持的文件类型");
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unZip(String source, String dest, String password) {
        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(dest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unRar(String source, String dest, String password) {
        try (Archive archive = new Archive(new File(source), password)) {
            if (!archive.isPasswordProtected() || password != null) {
                List<FileHeader> fileHeaders = archive.getFileHeaders();
                for (FileHeader fileHeader : fileHeaders) {
                    if (!fileHeader.isDirectory()) {
                        File outFile = new File(dest, fileHeader.getFileName());
                        outFile.getParentFile().mkdirs();
                        try (OutputStream os = new FileOutputStream(outFile)) {
                            archive.extractFile(fileHeader, os);
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static boolean un7z(String source, String dest, String password) {
        // 使用ProcessBuilder调用7z命令行工具
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "7z", "x", source, "-o" + dest, "-p" + password, "-y"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unTar(String source, String dest) {
        try (InputStream is = Files.newInputStream(Paths.get(source));
             ArchiveInputStream ais = new ArchiveStreamFactory()
                     .createArchiveInputStream(ArchiveStreamFactory.TAR, is)) {

            ArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) {
                    continue;
                }
                File outFile = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (OutputStream os = Files.newOutputStream(outFile.toPath())) {
                        IOUtils.copy(ais, os);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unGzip(String source, String dest) {
        try (InputStream in = Files.newInputStream(Paths.get(source));
             CompressorInputStream gzIn = new CompressorStreamFactory()
                     .createCompressorInputStream(CompressorStreamFactory.GZIP, in);
             OutputStream out = Files.newOutputStream(Paths.get(dest))) {

            IOUtils.copy(gzIn, out);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unBzip2(String source, String dest) {
        try (InputStream in = Files.newInputStream(Paths.get(source));
             CompressorInputStream bzIn = new CompressorStreamFactory()
                     .createCompressorInputStream(CompressorStreamFactory.BZIP2, in);
             OutputStream out = Files.newOutputStream(Paths.get(dest))) {

            IOUtils.copy(bzIn, out);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unXz(String source, String dest) {
        try (InputStream in = Files.newInputStream(Paths.get(source));
             CompressorInputStream xzIn = new CompressorStreamFactory()
                     .createCompressorInputStream(CompressorStreamFactory.XZ, in);
             OutputStream out = Files.newOutputStream(Paths.get(dest))) {

            IOUtils.copy(xzIn, out);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public enum CompressType {
        ZIP, RAR, _7Z, TAR, GZ, BZ2, XZ, UNKNOWN
    }
}
