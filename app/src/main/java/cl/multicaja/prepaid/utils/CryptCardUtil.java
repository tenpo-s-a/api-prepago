package cl.multicaja.prepaid.utils;

public interface CryptCardUtil {
    String encryptPan(String pan, String password);
    String decryptPan(String cryptedPan, String password);
}
