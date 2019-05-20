package cl.multicaja.prepaid.utils;

public interface EncryptCardUtil {
    String encryptPan(String pan, String password);
    String decryptPan(String cryptedPan, String password);
}
