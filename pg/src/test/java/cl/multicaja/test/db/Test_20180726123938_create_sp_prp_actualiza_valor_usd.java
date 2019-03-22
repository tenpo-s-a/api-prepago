package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class Test_20180726123938_create_sp_prp_actualiza_valor_usd extends TestDbBasePg {

  // Historial de modificación
  // 20180726123938_create_sp_prp_actualiza_valor_usd_v10.sql
  // 20190320161201_create_sp_prp_actualiza_valor_usd_v11.sql

  private static final String SP_INSERT_DOLAR_USD_VALUE_NAME = SCHEMA + ".mc_prp_actualiza_valor_usd_v11";
  private Log log = LogFactory.getLog(Test_20180726123938_create_sp_prp_actualiza_valor_usd.class);

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_valor_usd", SCHEMA));
  }

  private static Object[] buildParams(
    String nombreArchivo,
    Timestamp fechaExpiracionUsd,
    BigDecimal precioVenta,
    BigDecimal precioCompra,
    BigDecimal precioMedio,
    BigDecimal exponente,
    BigDecimal precioDia
  ){
    Object[] params = {
      new InParam(nombreArchivo, Types.VARCHAR),
      new InParam(fechaExpiracionUsd,Types.TIMESTAMP),
      new InParam(precioVenta,Types.NUMERIC),
      new InParam(precioCompra,Types.NUMERIC),
      new InParam(precioMedio,Types.NUMERIC),
      new InParam(exponente,Types.NUMERIC),
      new InParam(precioDia,Types.NUMERIC),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return params;
  }

  public static Map<String, Object> setUsdValue (
    String nombreArchivo,
    Timestamp fechaExpiracionUsd,
    BigDecimal precioVenta,
    BigDecimal precioCompra,
    BigDecimal precioMedio,
    BigDecimal exponente,
    BigDecimal precioDia
  ) throws SQLException {

    Object[] params = buildParams(
      nombreArchivo,
      fechaExpiracionUsd,
      precioVenta,
      precioCompra,
      precioMedio,
      exponente,
      precioDia
    );

    return dbUtils.execute(SP_INSERT_DOLAR_USD_VALUE_NAME, params);
  }

  @Test
  public void testInsertValorUsd() throws SQLException {

    final String nombreArchivo = "Test";
    final Timestamp fechaExpiracionUsd = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));

    {
      log.info("TEST 1 nombre_archivo obligatorio");
      Map<String, Object> data = setUsdValue(
        null,
        null,
        null,
        null,
        null,
        null,
        null
      );
      log.info(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","MC001",data.get("_error_code"));
    }

    {
      log.info("TEST 2 precio_venta obligatorio");
      Map<String, Object> data = setUsdValue(
        nombreArchivo,
        fechaExpiracionUsd,
        null,
        BigDecimal.valueOf(651),
        BigDecimal.valueOf(325),
        BigDecimal.valueOf(1),
        BigDecimal.valueOf(650)
      );
      log.info(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","MC002",data.get("_error_code"));
    }

    {
      log.info("TEST 3 precio_compra obligatorio");
      Map<String, Object> data = setUsdValue(
        nombreArchivo,
        fechaExpiracionUsd,
        BigDecimal.valueOf(650),
        null,
        BigDecimal.valueOf(325),
        BigDecimal.valueOf(1),
        BigDecimal.valueOf(650)
      );
      log.info(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","MC003",data.get("_error_code"));
    }

    {
      log.info("TEST 4 precio_medio obligatorio");
      Map<String, Object> data = setUsdValue(
        nombreArchivo,
        fechaExpiracionUsd,
        BigDecimal.valueOf(650),
        BigDecimal.valueOf(651),
        null,
        BigDecimal.valueOf(1),
        BigDecimal.valueOf(650)
      );
      log.info(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","MC004",data.get("_error_code"));
    }

    {
      log.info("TEST 5 exponente obligatorio");
      Map<String, Object> data = setUsdValue(
        nombreArchivo,
        fechaExpiracionUsd,
        BigDecimal.valueOf(650),
        BigDecimal.valueOf(651),
        BigDecimal.valueOf(325),
        null,
        BigDecimal.valueOf(650)
      );
      log.info(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","MC005",data.get("_error_code"));
    }

    {
      log.info("TEST 6 precio_dia obligatorio");
      Map<String, Object> data = setUsdValue(
        nombreArchivo,
        fechaExpiracionUsd,
        BigDecimal.valueOf(650),
        BigDecimal.valueOf(651),
        BigDecimal.valueOf(325),
        BigDecimal.valueOf(1),
        null
      );
      log.info(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","MC006",data.get("_error_code"));
    }

    {

      UsdValue usdValueSent = new UsdValue();
      usdValueSent.setNombreArchivo("Test");
      usdValueSent.setFechaExpiracionUsd(fechaExpiracionUsd);
      usdValueSent.setPrecioVenta(BigDecimal.valueOf(650.21));
      usdValueSent.setPrecioCompra(BigDecimal.valueOf(651.34));
      usdValueSent.setPrecioMedio(BigDecimal.valueOf(325.105));
      usdValueSent.setExponente(BigDecimal.valueOf(1.1));

      Double usdVariacion = usdValueSent.dayCurrencyVariation * usdValueSent.getPrecioVenta().doubleValue();

      usdValueSent.setPrecioDia(BigDecimal.valueOf(usdVariacion));

      Map<String, Object> resp = setUsdValue(
        usdValueSent.getNombreArchivo(),
        usdValueSent.getFechaExpiracionUsd(),
        usdValueSent.getPrecioVenta(),
        usdValueSent.getPrecioCompra(),
        usdValueSent.getPrecioMedio(),
        usdValueSent.getExponente(),
        usdValueSent.getPrecioDia()
      );

      Assert.assertNotNull("Data no debe ser null", resp);
      Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));

      Map<String, Object> data = new Test_20180726122133_create_sp_mc_prp_buscar_valor_usd().searchUsdValue();
      List<Map<String, Object>> results = (List)data.get("result");

      Assert.assertEquals("Se debe encontrar un solo registro ",1,results.size());
      Test_20180726122133_create_sp_mc_prp_buscar_valor_usd.UsdValue usdValueResponse = (Test_20180726122133_create_sp_mc_prp_buscar_valor_usd.UsdValue) results.get(0);

      Assert.assertNotNull("No esta vacio ",usdValueResponse);
      Assert.assertEquals("NombreArchivo Deben ser iguales ",usdValueSent.getNombreArchivo(),usdValueResponse.getNombreArchivo());
      Assert.assertEquals("FechaExpiracionUsd Deben ser iguales ",usdValueSent.getFechaExpiracionUsd(),usdValueResponse.getFechaExpiracionUsd());
      Assert.assertEquals("PrecioCompra Deben ser iguales ",usdValueSent.getPrecioCompra(),usdValueResponse.getPrecioCompra());
      Assert.assertEquals("PrecioMedio Deben ser iguales ",usdValueSent.getPrecioMedio(),usdValueResponse.getPrecioMedio());
      Assert.assertEquals("Exponente Deben ser iguales ",usdValueSent.getExponente(),usdValueResponse.getExponente());
      Assert.assertEquals("PrecioDia Deben ser iguales ",usdValueSent.getPrecioDia(),usdValueResponse.getPrecioDia());
    }

  }

  class UsdValue {

    public final Double dayCurrencyVariation = 1.025;

    private Long id;
    private String nombreArchivo;
    private Timestamp fechaCreacion;
    private Timestamp fechaTermino;
    private Timestamp fechaExpiracionUsd;
    private BigDecimal precioVenta;
    private BigDecimal precioCompra;
    private BigDecimal precioMedio;
    private BigDecimal exponente;
    private BigDecimal precioDia;

    public String getNombreArchivo() {
      return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
      this.nombreArchivo = nombreArchivo;
    }

    public Timestamp getFechaExpiracionUsd() {
      return fechaExpiracionUsd;
    }

    public void setFechaExpiracionUsd(Timestamp fechaExpiracionUsd) {
      this.fechaExpiracionUsd = fechaExpiracionUsd;
    }

    public BigDecimal getPrecioVenta() {
      return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
      this.precioVenta = precioVenta;
    }

    public BigDecimal getPrecioCompra() {
      return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
      this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioMedio() {
      return precioMedio;
    }

    public void setPrecioMedio(BigDecimal precioMedio) {
      this.precioMedio = precioMedio;
    }

    public BigDecimal getExponente() {
      return exponente;
    }

    public void setExponente(BigDecimal exponente) {
      this.exponente = exponente;
    }

    public BigDecimal getPrecioDia() {
      return precioDia;
    }

    public void setPrecioDia(BigDecimal precioDia) {
      this.precioDia = precioDia;
    }
  }

}
