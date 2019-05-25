package cl.multicaja.prepaid.utils;

import cl.multicaja.core.utils.encryption.PgpHelper;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class PgpUtil {

  public static void decryptFile(InputStream inputFile, String  privateKey, String publicKey, File outputFile, String pass) throws Exception {
    if(inputFile == null) {
      throw new Exception("Input File is null");
    }
    if(privateKey == null) {
      throw new Exception("Private Key is null");
    }
    if(publicKey == null) {
      throw new Exception("Public Key is null");
    }
    if(outputFile == null) {
      throw new Exception("Output file is null");
    }
    if(pass == null) {
      throw new Exception("Password is null");
    }

    System.out.println("Decrypting file");

    try {
      ByteArrayOutputStream decryptedFile = PgpHelper.getInstance().decrypt(IOUtils.toByteArray(inputFile), pass, PgpHelper.ArmoredKeyPair.of(privateKey, publicKey));

      OutputStream outputStream = new FileOutputStream(outputFile);
      decryptedFile.writeTo(outputStream);

      outputStream.flush();
      outputStream.close();
      decryptedFile.close();

      System.out.println("File decrypted");
    } catch(Exception e) {
      System.out.println("Error decrypting file");
      throw e;
    }
  }
  public static InputStream decryptFileToIs(InputStream inputFile, String  privateKey, String publicKey, File outputFile, String pass) throws Exception {
    if(inputFile == null) {
      throw new Exception("Input File is null");
    }
    if(privateKey == null) {
      throw new Exception("Private Key is null");
    }
    if(publicKey == null) {
      throw new Exception("Public Key is null");
    }
    if(outputFile == null) {
      throw new Exception("Output file is null");
    }
    if(pass == null) {
      throw new Exception("Password is null");
    }

    System.out.println("Decrypting file");

    try {
      ByteArrayOutputStream decryptedFile = PgpHelper.getInstance().decrypt(IOUtils.toByteArray(inputFile), pass, PgpHelper.ArmoredKeyPair.of(privateKey, publicKey));
      decryptedFile.close();
      System.out.println("File decrypted");
      return new ByteArrayInputStream(decryptedFile.toByteArray());
    } catch(Exception e) {
      System.out.println("Error decrypting file");
      throw e;
    }
  }
}
