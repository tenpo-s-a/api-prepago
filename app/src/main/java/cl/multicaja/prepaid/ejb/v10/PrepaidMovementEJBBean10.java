package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStateType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.sql.Types;
import java.util.Map;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidMovementEJBBean10 implements PrepaidMovementEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);
  private ConfigUtils configUtils;
  private DBUtils dbUtils;
  private NumberUtils numberUtils = NumberUtils.getInstance();
  private final String SP_UPDATE_MOV = getSchema()+".mc_prp_actualiza_movimiento_v10";
  private final String SP_CREATE_MOV = getSchema()+".mc_prp_crea_movimiento_v10";

  @Override
  public PrepaidMovement10 addPrepaidMovement(Map<String, Object> header, PrepaidMovement10 data) throws Exception {

    //TODO: REVISAR QUE DATOS SON OBLIGATORIOS Y CUALES NO
    Object[] params = {
      data.getIdMovimientoRef(), //id_mov_ref
      data.getIdUsuario(), //id_usuario
      data.getTipoMovimiento(), //Movimiento
      data.getMonto(),
      data.getMoneda(),
      data.getEstado().getState(),
      data.getCodEntidad(),//_cod_entidad
      data.getCenAlta(),//_cen_alta
      data.getCuenta(),//_cuenta
      new InParam(data.getCodMoneda(),Types.NUMERIC),//_cod_moneda NUMERIC
      new InParam(data.getIndNorcor(),Types.NUMERIC),//_ind_norcor NUMERIC
      new InParam(data.getTipoFactura(),Types.NUMERIC),//_tipo_factura NUMERIC
      data.getFechaFactura(),//_fecha_factura
      data.getNumFacturaRef(),//_num_factura_ref VARCHAR
      data.getPan(),// _pan            VARCHAR,
      new InParam(data.getCodMondiv(),Types.NUMERIC),//_cod_mondiv      NUMERIC,
      new InParam(data.getImpDiv(),Types.NUMERIC),//_imp_div           NUMERIC,
      new InParam(data.getImpFac(),Types.NUMERIC),//_imp_fac           NUMERIC,
      new InParam(data.getCmpApli(),Types.NUMERIC),//_cmp_apli            NUMERIC,
      data.getNumAutorizacion(),//_num_autorizacion    VARCHAR,
      data.getIndProaje(),//_ind_proaje          VARCHAR,
      data.getCodComercio(),//_cod_comercio        VARCHAR,
      data.getCodActividad(),//_cod_actividad       VARCHAR,
      new InParam(data.getImpLiq(),Types.NUMERIC),//_imp_liq             NUMERIC,
      new InParam(data.getCodMonliq(),Types.NUMERIC), //_cod_monliq          NUMERIC,
      new InParam(data.getCodPais(),Types.NUMERIC), //_cod_pais            NUMERIC,
      data.getNomPoblacion(),//_nom_poblacion       VARCHAR,
      new InParam(data.getNumExtracto(),Types.NUMERIC),//_num_extracto        NUMERIC,
      new InParam(data.getNumMovExtracto(),Types.NUMERIC),//_num_mov_extracto    NUMERIC,
      new InParam(data.getClaveMoneda(),Types.NUMERIC),// _clave_moneda        NUMERIC,
      data.getTipoLinea(),//_tipo_linea          VARCHAR,
      new InParam(data.getReferenciaLinea(),Types.NUMERIC),//_referencia_linea    NUMERIC,
      new InParam(data.getNumBenefCta(),Types.NUMERIC),//_num_benef_cta       NUMERIC,
      new InParam(data.getNumeroPlastico(),Types.NUMERIC),//_numero_plastico     NUMERIC,
      new OutParam("_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp =  getDbUtils().execute(SP_CREATE_MOV, params);

    if(resp == null){
      // TODO: Modificar por lo correspondiente
      throw new ValidationException(1111);
    }
    String sNumError = (String)resp.get("_error_code");
    if(StringUtils.isBlank(sNumError) || !sNumError.equals("0") ){
      // TODO: Modificar por lo correspondiente
      throw new ValidationException(1111);
    }
    Long id = (Long) resp.get("_id");
    if(id == null  || id == 0 ){
      throw new ValidationException(1111);
    }
    data.setId(id);

    return data;
  }

  @Override
  public void updatePrepaidMovement(Map<String, Object> header, Long id, Integer numExtracto, Integer numMovExtracto, Integer claveMoneda, PrepaidMovementStateType state) throws Exception {

    if(id == null){
      throw  new ValidationException(1111);
    }
    if(state == null){
      throw  new ValidationException(1111);
    }
    //TODO: Implementar llamada al SP
    Object[] params = {
      new InParam(id,Types.NUMERIC),
      numExtracto == null ? new NullParam(Types.NUMERIC):new InParam(numExtracto, Types.NUMERIC),
      numMovExtracto == null ? new NullParam(Types.NUMERIC):new InParam(numMovExtracto, Types.NUMERIC),
      claveMoneda == null ? new NullParam(Types.NUMERIC):new InParam(claveMoneda, Types.NUMERIC),
      state,
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String,Object> resp =  getDbUtils().execute(SP_UPDATE_MOV,params);
    if(resp == null){
      // TODO: Modificar por lo correspondiente
      throw new ValidationException(1111);
    }
    String sNumError = (String)resp.get("_error_code");
    if(StringUtils.isBlank(sNumError) || !sNumError.equals("0") ){
      // TODO: Modificar por lo correspondiente
      throw new ValidationException(1111);
    }
  }

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
}
