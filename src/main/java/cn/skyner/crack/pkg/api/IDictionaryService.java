package cn.skyner.crack.pkg.api;

public interface IDictionaryService {
    String[] getCommonPasswords();

    String[] getNumericPasswords();

    String[] getPatternPasswords();

    void addSuccessfulPassword(String password);
}
