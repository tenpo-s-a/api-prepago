package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190220161809_create_table_prp_movimientos_tecnocom extends TestDbBasePg {

  @Test
  public void checkIfTableExists_movimientos_tecnocom() {

    // Checkea tabla prp_movimientos_tecnocom
    Boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimientos_tecnocom", Boolean.TRUE,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("idArchivo", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("cuenta", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("pan", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("codent", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("centalta", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("clamon", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("indnorcor", SqlType.NUMERIC.getGetJavaType(), 1),
      new ColumnInfo("tipofac", SqlType.NUMERIC.getGetJavaType(), 4),
      new ColumnInfo("fecfac", SqlType.DATE.getGetJavaType()),
      new ColumnInfo("numreffac", SqlType.VARCHAR.getGetJavaType(), 23),
      new ColumnInfo("clamondiv", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("impdiv", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("impfac", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("cmbapli", SqlType.NUMERIC.getGetJavaType(), 9),
      new ColumnInfo("numaut", SqlType.VARCHAR.getGetJavaType(), 6),
      new ColumnInfo("indproaje", SqlType.VARCHAR.getGetJavaType(), 1),
      new ColumnInfo("codcom", SqlType.VARCHAR.getGetJavaType(), 15),
      new ColumnInfo("codact", SqlType.NUMERIC.getGetJavaType(), 4),
      new ColumnInfo("impliq", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("clamonliq", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("codpais", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nompob", SqlType.VARCHAR.getGetJavaType(), 26),
      new ColumnInfo("numextcta", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nummovext", SqlType.NUMERIC.getGetJavaType(), 7),
      new ColumnInfo("clamone", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("tipolin", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("linref", SqlType.NUMERIC.getGetJavaType(), 8),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertTrue("Existe la tabla prp_movimientos_tecnocom", exists);

  }

  @Test
  public void checkIfTableExists_movimientos_tecnocom_his() {

    // Checkea tabla prp_movimientos_tecnocom_hist
    Boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimientos_tecnocom_hist", Boolean.TRUE,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("idArchivo", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("cuenta", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("pan", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("codent", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("centalta", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("clamon", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("indnorcor", SqlType.NUMERIC.getGetJavaType(), 1),
      new ColumnInfo("tipofac", SqlType.NUMERIC.getGetJavaType(), 4),
      new ColumnInfo("fecfac", SqlType.DATE.getGetJavaType()),
      new ColumnInfo("numreffac", SqlType.VARCHAR.getGetJavaType(), 23),
      new ColumnInfo("clamondiv", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("impdiv", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("impfac", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("cmbapli", SqlType.NUMERIC.getGetJavaType(), 9),
      new ColumnInfo("numaut", SqlType.VARCHAR.getGetJavaType(), 6),
      new ColumnInfo("indproaje", SqlType.VARCHAR.getGetJavaType(), 1),
      new ColumnInfo("codcom", SqlType.VARCHAR.getGetJavaType(), 15),
      new ColumnInfo("codact", SqlType.NUMERIC.getGetJavaType(), 4),
      new ColumnInfo("impliq", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("clamonliq", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("codpais", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nompob", SqlType.VARCHAR.getGetJavaType(), 26),
      new ColumnInfo("numextcta", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("nummovext", SqlType.NUMERIC.getGetJavaType(), 7),
      new ColumnInfo("clamone", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("tipolin", SqlType.VARCHAR.getGetJavaType(), 4),
      new ColumnInfo("linref", SqlType.NUMERIC.getGetJavaType(), 8),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertTrue("Existe la tabla prp_movimientos_tecnocom_hist", exists);

  }


}
