import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DictionaryUtil {
    private static final String DICTIONARY_DIR = "dictionary";
    private static final String COMMON_PASSWORDS_FILE = "common_passwords.txt";
    private static final String NUMERIC_PASSWORDS_FILE = "numeric_passwords.txt";
    private static final String PATTERN_PASSWORDS_FILE = "pattern_passwords.txt";
    private static final String SUCCESSFUL_PASSWORDS_FILE = "successful_passwords.txt";

    private List<String> commonPasswords;
    private List<String> numericPasswords;
    private List<String> patternPasswords;
    private Set<String> successfulPasswords;

    public DictionaryUtil() {
        initializeDictionaries();
    }

    private void initializeDictionaries() {
        try {
            // 创建字典目录
            Files.createDirectories(Paths.get(DICTIONARY_DIR));

            // 初始化密码列表
            commonPasswords = loadOrCreatePasswordList(COMMON_PASSWORDS_FILE, getDefaultCommonPasswords());
            numericPasswords = loadOrCreatePasswordList(NUMERIC_PASSWORDS_FILE, getDefaultNumericPasswords());
            patternPasswords = loadOrCreatePasswordList(PATTERN_PASSWORDS_FILE, getDefaultPatternPasswords());
            successfulPasswords = new HashSet<>(loadOrCreatePasswordList(SUCCESSFUL_PASSWORDS_FILE, new ArrayList<>()));
        } catch (IOException e) {
            System.out.println("初始化密码字典时发生错误：" + e.getMessage());
            // 使用默认密码
            commonPasswords = getDefaultCommonPasswords();
            numericPasswords = getDefaultNumericPasswords();
            patternPasswords = getDefaultPatternPasswords();
            successfulPasswords = new HashSet<>();
        }
    }

    private List<String> loadOrCreatePasswordList(String filename, List<String> defaultPasswords) throws IOException {
        Path filePath = Paths.get(DICTIONARY_DIR, filename);
        if (Files.exists(filePath)) {
            return new ArrayList<>(Files.readAllLines(filePath));
        } else {
            Files.write(filePath, defaultPasswords);
            return new ArrayList<>(defaultPasswords);
        }
    }

    private List<String> getDefaultCommonPasswords() {
        return Arrays.asList(
                "123456", "password", "12345678", "qwerty", "abc123",
                "111111", "123123", "admin", "letmein", "welcome",
                "monkey", "password1", "123456789", "football", "123qwe",
                "baseball", "dragon", "master", "login", "princess",
                "solo", "abc", "123", "666666", "888888"
        );
    }

    private List<String> getDefaultNumericPasswords() {
        List<String> passwords = new ArrayList<>();
        // 添加常见的数字组合
        for (int i = 0; i <= 9999; i++) {
            passwords.add(String.format("%04d", i));
        }
        // 添加年份
        for (int year = 1950; year <= 2025; year++) {
            passwords.add(String.valueOf(year));
        }
        return passwords;
    }

    private List<String> getDefaultPatternPasswords() {
        return Arrays.asList(
                "a1b2c3", "q1w2e3", "abc123", "qwe123", "aaa111",
                "abc@123", "admin123", "pass123", "root123", "test123",
                "password123", "123abc", "123qwe", "1q2w3e", "1a2b3c"
        );
    }

    public String[] getCommonPasswords() {
        Set<String> allPasswords = new LinkedHashSet<>();
        // 首先添加成功的密码
        allPasswords.addAll(successfulPasswords);
        // 然后添加常用密码
        allPasswords.addAll(commonPasswords);
        return allPasswords.toArray(new String[0]);
    }

    public String[] getNumericPasswords() {
        return numericPasswords.toArray(new String[0]);
    }

    public String[] getPatternPasswords() {
        return patternPasswords.toArray(new String[0]);
    }

    public void addSuccessfulPassword(String password) {
        if (password != null && !password.isEmpty()) {
            successfulPasswords.add(password);
            try {
                Path filePath = Paths.get(DICTIONARY_DIR, SUCCESSFUL_PASSWORDS_FILE);
                Files.write(filePath, successfulPasswords);
            } catch (IOException e) {
                System.out.println("保存成功密码时发生错误：" + e.getMessage());
            }
        }
    }
}
