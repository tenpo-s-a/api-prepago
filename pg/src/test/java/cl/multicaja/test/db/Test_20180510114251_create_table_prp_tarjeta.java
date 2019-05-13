package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_20180510114251_create_table_prp_tarjeta extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_tarjeta() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_tarjeta", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("pan", SqlType.VARCHAR.getGetJavaType(), 16),
      new ColumnInfo("pan_encriptado", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("expiracion", "int4", 10),
      new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("nombre_tarjeta", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("producto", SqlType.VARCHAR.getGetJavaType(), 2),
      new ColumnInfo("numero_unico", SqlType.VARCHAR.getGetJavaType(), 8),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion",  SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("uuid",SqlType.VARCHAR.getGetJavaType(),50),
      new ColumnInfo("pan_hash", SqlType.VARCHAR.getGetJavaType(),200),
      new ColumnInfo("id_cuenta",SqlType.BIGINT.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla prp_tarjeta", true, exists);
  }
}
