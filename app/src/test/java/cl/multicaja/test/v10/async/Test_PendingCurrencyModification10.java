package cl.multicaja.test.v10.async;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test_PendingCurrencyModification10 {

  @Test
  public void test() throws Exception {
    String text = "D0088402MD000001044288860000001044550000000001044811140999999999999999";
    Pattern pattern = Pattern.compile("^[D]\\d{7}(MD|MF)\\d{60}$");

    Matcher matcher = pattern.matcher(text);
    System.out.println(matcher.matches());

  }

}
