package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.utils.PgpUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Test_MastercardIpmFileHelper_decryptFile {


  private String publicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
    "\n" +
    "mQENBFwuYtEBCADcpIs51X/bNhfcpez7dh96pv6CE5g8VItddoRcXXX/pRJ78AlK\n" +
    "xzFKcSHIdmCcRRtCCLluN2s51Ee50K3maoIG0Tve2sFZsXuNGhtqet4a2JzCFMi+\n" +
    "u2niqb4uW4BfvQLB8VGyQtIT8jcgmtM+Rjptin9tWgB7p7cbXrecm1gtWMWLDN1M\n" +
    "9zeuybxy8gfc4SeFKHlGKdfpM9te/fDP8ZQFFU/WJYnL0rW5DA756yj5iu9Ua6Tq\n" +
    "XXYf1tS7SnwUHeqPTZNRjWq+rA5hewN3H79a+Pq+LDlKRi8PQ6qnctVQX41BB6pi\n" +
    "LA4xllUMrazCW9jKW5cvYDBOjVN2qfpSEwe1ABEBAAG0RVRlc3QgcHJlcGFnbyAo\n" +
    "U2UgdXRpbGl6YW4gZW4gbG9zIHRlc3RzIGF1dG9tYXRpemFkb3MpIDx0ZXN0QG1h\n" +
    "aWwuY29tPokBTgQTAQgAOBYhBLGJQwqKoiG41fSID8cWDM1fDTKxBQJcLmLRAhsD\n" +
    "BQsJCAcCBhUKCQgLAgQWAgMBAh4BAheAAAoJEMcWDM1fDTKxdYYIAMFejio9gR3x\n" +
    "6Bk0Ob9U14TLoV6vGB70O6BAf6U5/9yJtZahfnzwS6nnVFaQAHS68KZ3JFNOnWoh\n" +
    "MmGWuRORTT+Znq52gnQ3cr9NcQ2bX8XepOyCHoOsTUWGWdblczFwOjbN/72ODrJP\n" +
    "+z53ZboJW4U7hTFXwJKPsY+/Xuic9OgpKSHrbP2BncPvaDvzU4sWEgJWRecjEhCL\n" +
    "w0VCeyC9kTC2FJ3jhbZj28J9eoGjlLCk+b1GwcrYkgjNIuq8Z/EV3FM147zQt/+8\n" +
    "huIt5y1iFFF7ocwlbghL2zfkEDM8noOBgSr8RK9n49nyrebRtIx5LTZo2MsE6u4j\n" +
    "fjRURtP5v1S5AQ0EXC5i0QEIAKuc+3hyqU/0dUV/jqmeFJvIxj2HvO8YMAAOU49n\n" +
    "lKD011Zl/xOTW3/iIjoSM4r9BaFIpjIgRZs8NlEUopZ0TXi0XFdboINAYcL+Or/5\n" +
    "GDwS39L5LX0B9ps0qhfD7aLDUo59WWSGZR2Zh5gdEgCv83WoIeSKrtDYdIbOmr8W\n" +
    "GhaDpIdBr6r0UztnKBsOZjzBS/IKDJ7RhFvGwPU6PYF8JTrSxNOHRhI9vlErHTt7\n" +
    "gnPqMXNL6WXLAkLBduQQydenFlHG/7YD15eJxffmKJ6+9V0aH2jtUKTOTN0xl8lm\n" +
    "xgtQGFTFBTgXbbAqW4B5ejC5Ui7MaDdxjbVYCJuSHR6FYOsAEQEAAYkBNgQYAQgA\n" +
    "IBYhBLGJQwqKoiG41fSID8cWDM1fDTKxBQJcLmLRAhsMAAoJEMcWDM1fDTKxgEMI\n" +
    "ALxCi2yUvvx/iMQMdZ2GXBHpny3URX5390iTe3IY5VS/OpbYex9A2CcnO+PPTldg\n" +
    "+fO3hdbs1YpudL+2k6uXY13c8V24olpJOeb5KAumtfyqMAGZ7lETanvEyMgC26yG\n" +
    "pmNqaSKu4QRoYBqSR3jWruuGy114lfD19u6b1Gdnn/OsBfYG2tVBDJPyKhw7wIn4\n" +
    "IX7kUXQAlwsYF04jhlOoK/vuSXIzBOZ/ghwpFykMBqwG+aDGQ48k6jlyvPl5Zhfi\n" +
    "ptK+NOmjr8KFscXiphGKp4pptGAZgIThTD/xrRfL4FY2UBc7CRM0x049UyKZ9VSc\n" +
    "8AfFi5x3z7wX5+vMKFLmiOQ=\n" +
    "=zEDq\n" +
    "-----END PGP PUBLIC KEY BLOCK-----";
  private String privateKe = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
    "\n" +
    "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\n" +
    "VJzwB8WLnHfPvBfn68woUuaI5A==\n" +
    "=2Ikf\n" +
    "-----END PGP PRIVATE KEY BLOCK-----";
  private String passphrase = "test";

  @Test
  public void decryptFile_inputFile_null() {
    try {
      PgpUtil.decryptFile(null, null, null, null, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Input File is null]", "Input File is null", e.getMessage());
    }

  }

  @Test
  public void decryptFile_privateKey_null() throws Exception {

    FileInputStream fis = new FileInputStream("src/test/resources/mastercard/files/YTFswitch.log.ori");
    try {
      PgpUtil.decryptFile(fis, null, null, null, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Private Key is null]", "Private Key is null", e.getMessage());
    }
    fis.close();
  }

  @Test
  public void decryptFile_publicKey_null() throws Exception {

    FileInputStream fis = new FileInputStream("src/test/resources/mastercard/files/YTFswitch.log.ori");
    try {
      PgpUtil.decryptFile(fis, "", null, null, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Public Key is null]", "Public Key is null", e.getMessage());
    }
    fis.close();
  }

  @Test
  public void decryptFile_output_null() throws Exception {
    FileInputStream fis = new FileInputStream("src/test/resources/mastercard/files/YTFswitch.log.ori");
    try {
      PgpUtil.decryptFile(fis, "", "", null, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Output file is null]", "Output file is null", e.getMessage());
    }
    fis.close();

  }

  @Test
  public void decryptFile_password_null() throws Exception {

    File f = new File("src/test/resources/mastercard/files/YTFswitch.log.ori");
    FileInputStream fis = new FileInputStream(f);

    try {
      PgpUtil.decryptFile(fis, "", "", f, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Password is null or empty]", "Password is null", e.getMessage());
    }
    fis.close();
  }

  @Test
  public void decryptFile() throws Exception {

    FileInputStream encryptedFileIs = new FileInputStream("src/test/resources/mastercard/files/YTFswitch.log.upld");

    File decryptedFile = new File("src/test/resources/mastercard/files/YTFswitch.log.upld_decrypted");
    File originalFile =  new File("src/test/resources/mastercard/files/YTFswitch.log.ori");

    try {
      PgpUtil.decryptFile(encryptedFileIs, privateKe, publicKey, decryptedFile, passphrase);

      String decryptedFileData = FileUtils.readFileToString(decryptedFile, UTF_8);
      String originalFileData = FileUtils.readFileToString(originalFile, UTF_8);

      Assert.assertTrue("Debe existir el archivo desencriptado", decryptedFile.exists());
      Assert.assertEquals("Deben tener el mismo contenido", originalFileData, decryptedFileData);

    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

    decryptedFile.delete();
  }
}
