package cl.multicaja.test.integration.v10.async;


import cl.multicaja.core.utils.encryption.PgpHelper;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.junit.Assert;
import org.junit.Test;
import cl.multicaja.core.utils.encryption.RSAKeyPairGenerator;

import java.io.*;
import java.security.*;
import java.util.Map;

public class Test_PendingMastercardAccountingFile10 extends TestBaseUnit {
  private static Log log = LogFactory.getLog(Test_PendingMastercardAccountingFile10.class);

  private String pubKeyFileName = "src/test/resources/mastercard/files/public_key.dat";
  private String privKeyFileName = "src/test/resources/mastercard/files/private_key.dat";

  @Test
  public void processAccountingFile() throws Exception {
    String fileName = "reporte_mastercard";
    String sourceDir = "src/test/resources/mastercard/files/";
    String destDir = getConfigUtils().getProperty("sftp.mastercard.accounting.received.folder").concat("/").substring(1);

    //genKeyPair();

    createEncryptedFile(sourceDir + fileName, destDir + fileName, pubKeyFileName);

    //decrypt();

    Thread.sleep(1500); // Esperar que lo agarre el metodo async

    System.out.println("Termino test");
  }

  private String id = "damico";
  private String passwd = "******";

  private void genKeyPair() throws Exception {

    RSAKeyPairGenerator rkpg = new RSAKeyPairGenerator();
    Security.addProvider(new BouncyCastleProvider());
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
    kpg.initialize(1024);

    KeyPair kp = kpg.generateKeyPair();
    FileOutputStream out1 = new FileOutputStream(privKeyFileName);
    FileOutputStream out2 = new FileOutputStream(pubKeyFileName);

    rkpg.exportKeyPair(out1, out2, kp.getPublic(), kp.getPrivate(), id, passwd.toCharArray(), false);
  }

  private void createEncryptedFile(String sourceFile, String destinationFile, String publicKey) throws Exception {
    FileInputStream pubKeyIs = new FileInputStream(publicKey);
    FileOutputStream cipheredFileIs = new FileOutputStream(destinationFile);
    PgpHelper.getInstance().encryptFile(cipheredFileIs, sourceFile, PgpHelper.getInstance().readPublicKey(pubKeyIs), false, false);
    cipheredFileIs.close();
    pubKeyIs.close();
  }


  public void decrypt() throws Exception {
    String encryptedFile = "src/test/resources/mastercard/accounting/reporte_mastercard";
    FileInputStream cipheredFileIs = new FileInputStream(encryptedFile);
    FileInputStream privKeyIn = new FileInputStream(privKeyFileName);
    FileOutputStream plainTextFileIs = new FileOutputStream("src/test/resources/mastercard/accounting/reporte_mastercard_traducido");
    PgpHelper.getInstance().decryptFile(cipheredFileIs, plainTextFileIs, privKeyIn, passwd.toCharArray());
    cipheredFileIs.close();
    plainTextFileIs.close();
    privKeyIn.close();
  }
}
