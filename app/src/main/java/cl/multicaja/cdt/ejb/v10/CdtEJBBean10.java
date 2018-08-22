package cl.multicaja.cdt.ejb.v10;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class CdtEJBBean10 implements CdtEJB10 {

  private static Log log = LogFactory.getLog(CdtEJBBean10.class);

  private static NumberUtils numberUtils = NumberUtils.getInstance();
  private static ConfigUtils configUtils;
  private static DBUtils dbUtils;

  private final String SP_CARGA_FASES_MOVIMIENTOS  = ".mc_cdt_carga_fases_movimientos_v10";
  private final String SP_CREA_MOVIMIENTO_CUENTA = ".mc_cdt_crea_movimiento_cuenta_v10";

  /**
   *
   * @return
   */
  public static ConfigUtils getConfigUtils() {
    if (configUtils == null) {
      configUtils = new ConfigUtils("api-prepaid");
    }
    return configUtils;
  }

  /**
   *
   * @return
   */
  public static DBUtils getDbUtils() {
    if (dbUtils == null) {
      dbUtils = new DBUtils(getConfigUtils());
    }
    return dbUtils;
  }

  /**
   *
   * @return
   */
  public static String getSchema() {
    return getConfigUtils().getProperty("schema.cdt");
  }

  @Override
  public CdtTransaction10 addCdtTransaction(Map<String, Object> headers, CdtTransaction10 cdtTransaction10) throws Exception {

    if(cdtTransaction10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cdtTransaction"));
    }
    if(cdtTransaction10.getAccountId() == null || StringUtils.isAllBlank(cdtTransaction10.getAccountId().trim())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }
    if(cdtTransaction10.getTransactionType() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "transactionType"));
    }
    if(cdtTransaction10.getExternalTransactionId() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "externalTransactionId"));
    }
    if(cdtTransaction10.getAmount() == null || cdtTransaction10.getAmount().doubleValue() == 0){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount o amount == 0"));
    }
    if(cdtTransaction10.getIndSimulacion() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "indSimulacion"));
    }

    Object[] params = {
      cdtTransaction10.getTransactionType().getName(),
      new NullParam(Types.NUMERIC)
    };

    Map<String,Object> outputData = getDbUtils().execute(getSchema() + SP_CARGA_FASES_MOVIMIENTOS, params);

    List lstFases = (List) outputData.get("result");

    if(lstFases == null || lstFases.isEmpty()) {
      throw new ValidationException(PARAMETRO_ILEGIBLE_$VALUE).setData(new KeyValue("value", "lstFases == null o lstFases.isEmpty"));
    }

    log.info("Registrando en CDT: " + cdtTransaction10);

    HashMap<String,Object> mapFase = (HashMap<String, Object>) lstFases.get(0);

    Long faseId = (Long) mapFase.get("_id");

    params = new Object[] {
      cdtTransaction10.getAccountId(),
      faseId,
      cdtTransaction10.getTransactionReference(),
      cdtTransaction10.getExternalTransactionId(),
      cdtTransaction10.getGloss(),
      cdtTransaction10.getAmount(),
      cdtTransaction10.getIndSimulacion() ? "S" : "N",
      new OutParam("IdMovimiento", Types.NUMERIC),
      new OutParam("NumError", Types.VARCHAR),
      new OutParam("MsjError", Types.VARCHAR)
    };

    outputData = getDbUtils().execute( getSchema() + SP_CREA_MOVIMIENTO_CUENTA, params);

    String numError = (String) outputData.get("NumError");
    String msjError = (String) outputData.get("MsjError");

    if("0".equals(numError)){
      cdtTransaction10.setTransactionReference(numberUtils.toLong(outputData.get("IdMovimiento")));
    } else {
      log.error("addCdtTransaction resp: " + outputData + " - " + cdtTransaction10);
      cdtTransaction10.setNumError(numError);
      cdtTransaction10.setMsjError(msjError);
    }
    return cdtTransaction10;
  }

  @Override
  public CdtTransaction10 buscaMovimientoReferencia(Map<String, Object> headers, Long idRef) throws Exception{

    if(idRef == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idCuenta"));
    }


    Object[] params = new Object[] {
      idRef
    };

    RowMapper rm = (Map<String, Object> row) -> {
      CdtTransaction10 tx = new CdtTransaction10();
      tx.setTransactionReference(numberUtils.toLong(row.get("_id"), null));
      tx.setTransactionType(CdtTransactionType.valueOfEnum(String.valueOf(row.get("_nombre_fase"))));
      return tx;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_cdt_busca_movimiento_referencia_v10", rm, params);
    List<CdtTransaction10> lst = (List)resp.get("result");

    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }


}
