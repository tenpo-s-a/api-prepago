package cl.multicaja.cdt.ejb.v10;

import cl.multicaja.cdt.model.v10.CdtTransaction10;

import java.util.Map;

public interface CdtEJB10 {
  CdtTransaction10 addCdtTransaction(Map<String, Object> headers, CdtTransaction10 cdtTransaction10) throws Exception;
}