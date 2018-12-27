package cl.multicaja.prepaid.ejb.v10;

import java.io.InputStream;

public interface McRedReconciliationEJB10 {

  /**
   * Procesa archivo de conciliacion McRed
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  void processFile(InputStream inputStream, String fileName) throws Exception;
}
