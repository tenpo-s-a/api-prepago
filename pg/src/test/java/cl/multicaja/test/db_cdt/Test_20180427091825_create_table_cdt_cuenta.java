package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180427091825_create_table_cdt_cuenta extends TestDbBasePg {

  /***************************************
    id              BIGSERIAL NOT NULL,
    id_externo      BIGSERIAL NOT NULL,
    descripcion     VARCHAR(100) NOT NULL,
    estado          VARCHAR(5) NOT NULL,
    fecha_estado    TIMESTAMP NOT NULL,
    fecha_creacion  TIMESTAMP NOT NULL,
   ****************************************/

  @Test
  public void CheckTableCuenta() {

    boolean exists = dbUtils.tableExists(SCHEMA_CDT,Constants.Tables.CUENTA.getName(), true,
    new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
    new ColumnInfo("id_externo", SqlType.VARCHAR.getGetJavaType(), 50),
    new ColumnInfo("descripcion", SqlType.VARCHAR.getGetJavaType(), 100),
    new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(), 10),
    new ColumnInfo("fecha_estado", SqlType.TIMESTAMP.getGetJavaType()),
    new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()));
    Assert.assertEquals("Existe tabla "+SCHEMA_CDT+"."+Constants.Tables.CUENTA.getName(), true, exists);
  }

}
