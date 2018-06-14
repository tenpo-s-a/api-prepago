package cl.multicaja.cdt.ejb.v10;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
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
public class CdtEJBBean10 implements CdtEJB10{

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

  private ConfigUtils configUtils;
  private DBUtils dbUtils;

  private final String SP_CARGA_FASES_MOVIMIENTOS  = ".mc_cdt_carga_fases_movimientos_v10";
  private final String SP_CREA_MOVIMIENTO_CUENTA = ".mc_cdt_crea_movimiento_cuenta_v10";

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  /**
   *
   * @return
   */
  public DBUtils getDbUtils() {
    if (this.dbUtils == null) {
      this.dbUtils = new DBUtils(this.getConfigUtils());
    }
    return this.dbUtils;
  }

  /**
   *
   * @return
   */
  private String getSchema() {
    return this.getConfigUtils().getProperty("schema.cdt");
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

    Object[] params = {cdtTransaction10.getTransactionType().getName() , new NullParam(Types.NUMERIC)};
    Map<String,Object> outputData = getDbUtils().execute(getSchema() + SP_CARGA_FASES_MOVIMIENTOS,params);

    List lstFases = (List) outputData.get("result");

    if(lstFases == null || lstFases.isEmpty()) {
      throw new ValidationException(PARAMETRO_ILEGIBLE_$VALUE).setData(new KeyValue("value", "lstFases == null o lstFases.isEmpty"));
    }

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

    if(numError.equals("0")){
      cdtTransaction10.setTransactionReference(((BigDecimal)outputData.get("IdMovimiento")).longValue());
    } else {
      log.error("[CdtEJBBean10][addCdtTransaction] NumError: "+numError+" MsjError: "+msjError);
      cdtTransaction10.setNumError(numError);
      cdtTransaction10.setMsjError(msjError);
    }
    return cdtTransaction10;
  }
}
