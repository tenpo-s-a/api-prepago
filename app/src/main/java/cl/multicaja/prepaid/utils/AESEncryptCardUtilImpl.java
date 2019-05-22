package cl.multicaja.prepaid.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;


public class AESEncryptCardUtilImpl implements EncryptCardUtil {
    private static final String CIPHER_INSTANCE_NAME = "AES/CBC/NoPadding";
    private static final String ALGORITHM = "AES";
    private static final byte[] iv = new byte[]{21, -116, 85, -30, 29, -95, 96, -118, -60, 85, -5, -123, -21, -37, 14, -69};

    @Override
    public String encryptPan(String pan, String password) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
            SecretKeySpec sks = new SecretKeySpec(password.getBytes(), ALGORITHM);
            cipher.init(1, sks, new IvParameterSpec(iv));
            byte[] encriptado = cipher.doFinal(pan.getBytes());
            return DatatypeConverter.printBase64Binary(encriptado);
        } catch (Exception e) {
            e.printStackTrace(); // TODO change to some log method
            return null;
        }
    }

    @Override
    public String decryptPan(String cryptedPan, String password) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
            SecretKeySpec sks = new SecretKeySpec(password.getBytes(), ALGORITHM);
            cipher.init(2, sks, new IvParameterSpec(iv));
            byte[] dec = cipher.doFinal(DatatypeConverter.parseBase64Binary(cryptedPan));
            return new String(dec);
        } catch (Exception e) {
            e.printStackTrace(); // TODO change to some log method
            return null;
        }
    }

    private static AESEncryptCardUtilImpl instance;

    public AESEncryptCardUtilImpl() {
        super();
    }

    public static AESEncryptCardUtilImpl getInstance() {
        if (null == instance)
            instance = new AESEncryptCardUtilImpl();
        return instance;
    }
}
