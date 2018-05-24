package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180518151953_create_table_prp_movimiento extends TestDbBasePg {
  @Test
  public void checkTableMovimiento(){
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_movimiento_ref",SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("id_usuario", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("tipo_movimiento", SqlType.VARCHAR.getGetJavaType(), 10),
      new ColumnInfo("monto", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("moneda", SqlType.VARCHAR.getGetJavaType(), 3),
      new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(), 10),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("cod_entidad", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("cen_alta", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("cuenta", SqlType.VARCHAR.getGetJavaType(), 12),
      new ColumnInfo("cod_moneda", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("ind_norcor", SqlType.NUMERIC.getGetJavaType(), 1),
      new ColumnInfo("tipo_factura", SqlType.NUMERIC.getGetJavaType(), 4),
      new ColumnInfo("fecha_factura", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("num_factura_ref", SqlType.VARCHAR.getGetJavaType(), 23),
      new ColumnInfo("pan", SqlType.VARCHAR.getGetJavaType(),22),
      new ColumnInfo("cod_mondiv", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("imp_div",  SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("imp_fac",  SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("cmp_apli",  SqlType.NUMERIC.getGetJavaType(),9),
      new ColumnInfo("num_autorizacion",  SqlType.VARCHAR.getGetJavaType(), 6),
      new ColumnInfo("ind_proaje",  SqlType.VARCHAR.getGetJavaType(), 1),
      new ColumnInfo("cod_comercio",  SqlType.VARCHAR.getGetJavaType(),15),
      new ColumnInfo("cod_actividad",  SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("imp_liq", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("cod_monliq", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("cod_pais", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nom_poblacion",  SqlType.VARCHAR.getGetJavaType(), 26),
      new ColumnInfo("num_extracto", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("num_mov_extracto", SqlType.NUMERIC.getGetJavaType(), 7),
      new ColumnInfo("clave_moneda", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("tipo_linea",  SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("referencia_linea", SqlType.NUMERIC.getGetJavaType(), 8),
      new ColumnInfo("num_benef_cta", SqlType.NUMERIC.getGetJavaType(), 5),
      new ColumnInfo("numero_plastico", SqlType.NUMERIC.getGetJavaType(), 12)
    );
    Assert.assertEquals("Existe tabla prp_movimientoas", true, exists);
  }
}
