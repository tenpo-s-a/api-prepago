package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import static cl.multicaja.test.db.Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard;

public class Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_crea_movimiento_v10";
  private static final String TABLE_NAME = SCHEMA + ".prp_movimiento";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }
  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }

  public static InParam setInParam(Object o){
    return new InParam(o,Types.NUMERIC);
  }
  @Test
  public void llamadaSpCreaMovimientoOk() throws SQLException
  {
    Map<String, Object> resp = insertaMovimiento();
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
  }

  @Test
  public void llamadaSpCreaMovimientoNoOk() throws SQLException
  {
    Map<String, Object> mapCard = insertCard("ACTIVA");
    Object[] params = {
      setInParam(mapCard.get("id_usuario")), //id_mov_ref
      setInParam(mapCard.get("id_usuario")), //id_usuario
      ""+getUniqueLong(),
      "CARGA", //estado
      setInParam(getUniqueLong()),
      "USD",
      "ENPRO",
      "AA",//_cod_entidad
      "CA",//_cen_alta
      "CTA312312312",//_cuenta
      setInParam(152),//_cod_moneda NUMERIC
      setInParam(1),//_ind_norcor NUMERIC
      setInParam(2),//_tipo_factura NUMERIC
      new Timestamp(System.currentTimeMillis()),//_fecha_factura
      "123",//_num_factura_ref VARCHAR
      mapCard.get("pan"),// _pan            VARCHAR,
      setInParam(90),//_cod_mondiv      NUMERIC,
      setInParam(10),//_imp_div           NUMERIC,
      setInParam(10),//_imp_fac           NUMERIC,
      setInParam(10),//_cmp_apli            NUMERIC,
      "1231",//_num_autorizacion    VARCHAR,
      "AD",//_ind_proaje          VARCHAR,
      "123",//_cod_comercio        VARCHAR,
      "AFQ",//_cod_actividad       VARCHAR,
      setInParam(1),//_imp_liq             NUMERIC,
      setInParam(1), //_cod_monliq          NUMERIC,
      setInParam(1), //_cod_pais            NUMERIC,
      "ASDA",//_nom_poblacion       VARCHAR,
      setInParam(22),//_num_extracto        NUMERIC,
      setInParam(22),//_num_mov_extracto    NUMERIC,
      setInParam(123),// _clave_moneda        NUMERIC,
      "V",//_tipo_linea          VARCHAR,
      setInParam( 2),//_referencia_linea    NUMERIC,
      setInParam(2),//_num_benef_cta       NUMERIC,
      setInParam(2),//_numero_plastico     NUMERIC,
      new OutParam("_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
    System.out.println(resp);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  public static Map<String, Object> insertaMovimiento() throws SQLException {
    Map<String, Object> mapCard = insertCard("ACTIVA");
    Object[] params = {
      setInParam(mapCard.get("id_usuario")), //id_mov_ref
      setInParam(mapCard.get("id_usuario")), //id_usuario
      ""+getUniqueLong(),
      "CARGA", //estado
      setInParam(getUniqueLong()),
      "USD",
      "ENPRO",
      "AA",//_cod_entidad
      "CA",//_cen_alta
      "CTA312312312",//_cuenta
      setInParam(152),//_cod_moneda NUMERIC
      setInParam(1),//_ind_norcor NUMERIC
      setInParam(2),//_tipo_factura NUMERIC
      new Timestamp(System.currentTimeMillis()),//_fecha_factura
      "123",//_num_factura_ref VARCHAR
      mapCard.get("pan"),// _pan            VARCHAR,
      setInParam(90),//_cod_mondiv      NUMERIC,
      setInParam(10),//_imp_div           NUMERIC,
      setInParam(10),//_imp_fac           NUMERIC,
      setInParam(10),//_cmp_apli            NUMERIC,
      "1231",//_num_autorizacion    VARCHAR,
      "A",//_ind_proaje          VARCHAR,
      "123",//_cod_comercio        VARCHAR,
      "AFQ",//_cod_actividad       VARCHAR,
      setInParam(1),//_imp_liq             NUMERIC,
      setInParam(1), //_cod_monliq          NUMERIC,
      setInParam(1), //_cod_pais            NUMERIC,
      "ASDA",//_nom_poblacion       VARCHAR,
      setInParam(22),//_num_extracto        NUMERIC,
      setInParam(22),//_num_mov_extracto    NUMERIC,
      setInParam(123),// _clave_moneda        NUMERIC,
      "V",//_tipo_linea          VARCHAR,
      setInParam( 2),//_referencia_linea    NUMERIC,
      setInParam(2),//_num_benef_cta       NUMERIC,
      setInParam(2),//_numero_plastico     NUMERIC,
      new OutParam("_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
    System.out.println(resp);
    return resp;
  }

}
