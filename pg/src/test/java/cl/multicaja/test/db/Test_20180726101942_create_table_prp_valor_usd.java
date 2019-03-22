package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180726101942_create_table_prp_valor_usd extends TestDbBasePg {

  private final static String tableName = "prp_valor_usd";

  @Test
  public void checkTablaValorUsd(){
    Boolean exists = dbUtils.tableExists(SCHEMA, tableName, Boolean.TRUE,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType(), 19),
      new ColumnInfo("nombre_archivo", SqlType.VARCHAR.getGetJavaType(), 50),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType(), 29),
      new ColumnInfo("fecha_termino", SqlType.TIMESTAMP.getGetJavaType(), 29),
      new ColumnInfo("fecha_expiracion_usd", SqlType.TIMESTAMP.getGetJavaType(), 29),
      new ColumnInfo("precio_venta", SqlType.NUMERIC.getGetJavaType(),15),
      new ColumnInfo("precio_compra", SqlType.NUMERIC.getGetJavaType(),15),
      new ColumnInfo("precio_medio", SqlType.NUMERIC.getGetJavaType(),15),
      new ColumnInfo("exponente", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("precio_dia", SqlType.NUMERIC.getGetJavaType(),15)
    );

    Assert.assertTrue("Existe la tabla: ", exists);

  }

}
