package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180726123938_create_sp_prp_actualiza_valor_usd.setUsdValue;

public class Test_20180726122133_create_sp_mc_prp_buscar_valor_usd extends TestDbBasePg {

  // Historial de modificación
  // 20180726122133_create_sp_mc_prp_buscar_valor_usd_v10.sql
  // 20190320170012_create_sp_mc_prp_buscar_valor_usd_v11.sql

  private static final String SP_SEARCH_DOLAR_USD_VALUE_NAME = SCHEMA + ".mc_prp_buscar_valor_usd_v11";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_valor_usd", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_valor_usd", SCHEMA));
  }


  public Map<String, Object> searchUsdValue() throws SQLException {

    Object[] params = {};

    RowMapper rm = (Map<String, Object> row) -> {
      UsdValue usdValue = new UsdValue();
      usdValue.id = numberUtils.toLong(row.get("_id"));
      usdValue.nombreArchivo = String.valueOf(row.get("_nombre_archivo"));
      usdValue.fechaCreacion = (Timestamp) row.get("_fecha_creacion");
      usdValue.fechaTermino = (Timestamp) row.get("_fecha_termino");
      usdValue.fechaExpiracionUsd = (Timestamp) row.get("_fecha_expiracion_usd");
      usdValue.precioVenta = BigDecimal.valueOf(numberUtils.toDouble(row.get("_precio_venta")));
      usdValue.precioCompra = BigDecimal.valueOf(numberUtils.toDouble(row.get("_precio_compra")));
      usdValue.precioMedio = BigDecimal.valueOf(numberUtils.toDouble(row.get("_precio_medio")));
      usdValue.exponente = BigDecimal.valueOf(numberUtils.toDouble(row.get("_exponente")));
      usdValue.precioDia = BigDecimal.valueOf(numberUtils.toDouble(row.get("_precio_dia")));

      return usdValue;
    };

    return dbUtils.execute(SP_SEARCH_DOLAR_USD_VALUE_NAME, rm, params);
  }

  @Test
  public void testSearchUsdValue() throws SQLException{

    Timestamp fechaExpiracionUsd = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));

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

    Map<String, Object> data = searchUsdValue();
    List<Map<String, Object>> results = (List)data.get("result");

    Assert.assertEquals("Se debe encontrar un solo registro ",1,results.size());
    UsdValue usdValueResponse = (UsdValue) results.get(0);

    Assert.assertNotNull("No esta vacio ",usdValueResponse);
    Assert.assertEquals("NombreArchivo Deben ser iguales ",usdValueSent.getNombreArchivo(),usdValueResponse.getNombreArchivo());
    Assert.assertEquals("FechaExpiracionUsd Deben ser iguales ",usdValueSent.getFechaExpiracionUsd(),usdValueResponse.getFechaExpiracionUsd());
    Assert.assertEquals("PrecioVenta Deben ser iguales ",usdValueSent.getPrecioVenta(),usdValueResponse.getPrecioVenta());
    Assert.assertEquals("PrecioCompra Deben ser iguales ",usdValueSent.getPrecioCompra(),usdValueResponse.getPrecioCompra());
    Assert.assertEquals("PrecioMedio Deben ser iguales ",usdValueSent.getPrecioMedio(),usdValueResponse.getPrecioMedio());
    Assert.assertEquals("Exponente Deben ser iguales ",usdValueSent.getExponente(),usdValueResponse.getExponente());
    Assert.assertEquals("PrecioDia Deben ser iguales ",usdValueSent.getPrecioDia(),usdValueResponse.getPrecioDia());

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

    public UsdValue() {
      super();
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

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
