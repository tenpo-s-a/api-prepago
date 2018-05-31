package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.util.Map;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidMovementEJBBean10 implements PrepaidMovementEJB10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean10.class);
  private ConfigUtils configUtils;
  private DBUtils dbUtils;

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
    return this.getConfigUtils().getProperty("schema");
  }

  @Override
  public PrepaidMovement10 addPrepaidMovement(Map<String, Object> header, PrepaidMovement10 data) throws Exception {

    String SP_CREATE_MOV = getSchema()+".mc_prp_crea_movimiento_v10";

    Object[] params = {
      new InParam(data.getIdMovimientoRef(),Types.NUMERIC), //id_mov_ref
      new InParam(data.getIdPrepaidUser(),Types.NUMERIC), //id_usuario
      data.getIdTxExterno(),
      data.getTipoMovimiento().toString(), //Movimiento
      new InParam(data.getMonto(),Types.NUMERIC),
      data.getEstado().toString(),
      data.getCodent(),//_codent
      data.getCentalta(),//_centalta
      data.getCuenta(),//_cuenta
      new InParam(data.getClamon().getValue(), Types.NUMERIC),//_clamon NUMERIC
      new InParam(data.getIndnorcor().getValue(),Types.NUMERIC),//_indnorcor NUMERIC
      new InParam(data.getTipofac().getCode(),Types.NUMERIC),//_tipofac NUMERIC
      new Date(data.getFecfac().getTime()),//_fecfac
      data.getNumreffac(),//_numreffac VARCHAR
      data.getPan(),// _pan            VARCHAR,
      new InParam(data.getClamondiv(), Types.NUMERIC),//_clamondiv      NUMERIC,
      new InParam(data.getImpdiv(), Types.NUMERIC),//_impdiv           NUMERIC,
      new InParam(data.getImpfac(), Types.NUMERIC),//_impfac           NUMERIC,
      new InParam(data.getCmbapli(), Types.NUMERIC),//_cmbapli            NUMERIC,
      data.getNumaut(),//_numaut    VARCHAR,
      data.getIndproaje().getValue(),//_indproaje          VARCHAR,
      data.getCodcom(),//_codcom        VARCHAR,
      data.getCodact(),//_codact       VARCHAR,
      new InParam(data.getImpliq(), Types.NUMERIC),//_impliq             NUMERIC,
      new InParam(data.getClamonliq(),Types.NUMERIC), //_clamonliq          NUMERIC,
      new InParam(data.getCodpais().getValue(), Types.NUMERIC), //_codpais            NUMERIC,
      data.getNompob(),//_nompob       VARCHAR,
      new InParam(data.getNumextcta(),Types.NUMERIC),//_numextcta        NUMERIC,
      new InParam(data.getNummovext(),Types.NUMERIC),//_nummovext    NUMERIC,
      new InParam(data.getClamone(),Types.NUMERIC),// _clamone        NUMERIC,
      data.getTipolin(),//_tipolin         VARCHAR,
      new InParam(data.getLinref(), Types.NUMERIC),//_linref    NUMERIC,
      new InParam(data.getNumbencta(),Types.NUMERIC),//_numbencta       NUMERIC,
      new InParam(data.getNumplastico(),Types.NUMERIC),//_numplastico     NUMERIC,
      new OutParam("_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp =  getDbUtils().execute(SP_CREATE_MOV, params);

    if(resp == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "resp == null"));
    }

    String numError = (String)resp.get("_error_code");
    String msjError = (String)resp.get("_error_msg");

    if(StringUtils.isBlank(numError) || !numError.equals("0") ){
      log.error("Num Error: "+numError+ " MsjError: "+msjError);
      throw new ValidationException(101004).setData(new KeyValue("value", "numError: " + numError + ", msjError: " + msjError));
    }

    BigDecimal id = (BigDecimal) resp.get("_id");

    if(id == null  || id.longValue() == 0 ) {
      throw new ValidationException(101004).setData(new KeyValue("value", "id == null o id == 0"));
    }

    data.setId(id.longValue());
    return data;
  }

  @Override
  public void updatePrepaidMovement(Map<String, Object> header, Long id, Integer numextcta, Integer nummovext, Integer clamone, PrepaidMovementStatus status) throws Exception {

    String SP_UPDATE_MOV = getSchema()+".mc_prp_actualiza_movimiento_v10";

    if(id == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "id"));
    }

    if(status == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      new InParam(id,Types.NUMERIC),
      numextcta == null ? new NullParam(Types.NUMERIC) : new InParam(numextcta, Types.NUMERIC),
      nummovext == null ? new NullParam(Types.NUMERIC) : new InParam(nummovext, Types.NUMERIC),
      clamone == null ? new NullParam(Types.NUMERIC) : new InParam(clamone, Types.NUMERIC),
      status.toString(),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(SP_UPDATE_MOV,params);

    log.info("Resp updatePrepaidMovement: " + resp);

    if(resp == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "resp == null"));
    }

    String sNumError = (String)resp.get("_error_code");

    if(StringUtils.isBlank(sNumError) || !sNumError.equals("0") ){
      throw new ValidationException(101004).setData(new KeyValue("value", "sNumError: " + sNumError));
    }
  }

  @Override
  public void updatePrepaidMovement(Map<String, Object> header, Long id, PrepaidMovementStatus status) throws Exception {
    this.updatePrepaidMovement(null, id, null, null, null, status);
  }
}
