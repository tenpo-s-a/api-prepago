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
      new ColumnInfo("id_tx_externo",SqlType.VARCHAR.getGetJavaType(),50),
      new ColumnInfo("tipo_movimiento", SqlType.VARCHAR.getGetJavaType(), 10),
      new ColumnInfo("monto", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(), 10),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("codent", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("centalta", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("cuenta", SqlType.VARCHAR.getGetJavaType(), 12),
      new ColumnInfo("clamon", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("indnorcor", SqlType.NUMERIC.getGetJavaType(), 1),
      new ColumnInfo("tipofac", SqlType.NUMERIC.getGetJavaType(), 4),
      new ColumnInfo("fecfac", SqlType.DATE.getGetJavaType()),
      new ColumnInfo("numreffac", SqlType.VARCHAR.getGetJavaType(), 23),
      new ColumnInfo("pan", SqlType.VARCHAR.getGetJavaType(),22),
      new ColumnInfo("clamondiv", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("impdiv",  SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("impfac",  SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("cmbapli",  SqlType.NUMERIC.getGetJavaType(),9),
      new ColumnInfo("numaut",  SqlType.VARCHAR.getGetJavaType(), 6),
      new ColumnInfo("indproaje",  SqlType.VARCHAR.getGetJavaType(), 1),
      new ColumnInfo("codcom",  SqlType.VARCHAR.getGetJavaType(),15),
      new ColumnInfo("codact",  SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("impliq", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("clamonliq", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("codpais", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nompob",  SqlType.VARCHAR.getGetJavaType(), 26),
      new ColumnInfo("numextcta", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nummovext", SqlType.NUMERIC.getGetJavaType(), 7),
      new ColumnInfo("clamone", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("tipolin",  SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("linref", SqlType.NUMERIC.getGetJavaType(), 8),
      new ColumnInfo("numbencta", SqlType.NUMERIC.getGetJavaType(), 5),
      new ColumnInfo("numplastico", SqlType.NUMERIC.getGetJavaType(), 12)
    );
    Assert.assertEquals("Existe tabla prp_movimientoas", true, exists);
  }
}
