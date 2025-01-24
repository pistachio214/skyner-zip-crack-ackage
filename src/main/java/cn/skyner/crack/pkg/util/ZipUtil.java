import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;

public class ZipUtil {
    public static void zip(String sourcePath, String zipPath, String password) {
        try {
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

            ZipFile zipFile = new ZipFile(zipPath, password.toCharArray());
            File sourceFile = new File(sourcePath);
            zipFile.addFile(sourceFile, zipParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
