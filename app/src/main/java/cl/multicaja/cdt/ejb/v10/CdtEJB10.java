package cl.multicaja.cdt.ejb.v10;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;

import java.util.List;
import java.util.Map;

public interface CdtEJB10 {

  CdtTransaction10 addCdtTransaction(Map<String, Object> headers, CdtTransaction10 cdtTransaction10) throws Exception;
  CdtTransaction10 buscaMovimientoReferencia(Map<String, Object> headers, Long idRef) throws Exception;
  CdtTransaction10 buscaMovimientoByIdExterno(Map<String, Object> headers, String idRef) throws Exception;
  CdtTransaction10 buscaMovimientoByIdExternoAndTransactionType(Map<String, Object> headers, String idRef, CdtTransactionType type) throws Exception;
  List buscaListaMovimientoByIdExterno(Map<String, Object> headers, String idExterno) throws Exception;

}
