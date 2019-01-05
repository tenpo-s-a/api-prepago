package cl.multicaja.prepaid.ejb.v10;

import java.io.InputStream;

public interface TecnocomReconciliationEJB10 {

  /**
   * Procesa el archivo de operaciones diarias enviado por Tecnocom
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  void processFile(InputStream inputStream, String fileName) throws Exception;
}
