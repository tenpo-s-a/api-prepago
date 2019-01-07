package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.CurrencyUsd;

import java.io.InputStream;

public interface MastercardCurrencyUpdateEJB10 {

  /**
   * Obtiene valor actual del DOLAR
   *
   * @throws Exception
   */
  CurrencyUsd getCurrencyUsd() throws Exception;

  /**
   * Actualiza valor DOLAR
   *
   * @throws Exception
   */
  void updateUsdValue(CurrencyUsd currencyUsd) throws Exception;

  /**
   * Procesa el archivo enviado por Mastercard para obtener el valor del dolar
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  void processMastercardUsdFile(InputStream inputStream, String fileName) throws Exception;
}
