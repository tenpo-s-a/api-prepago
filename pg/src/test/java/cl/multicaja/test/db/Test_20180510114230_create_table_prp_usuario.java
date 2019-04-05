package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_20180510114230_create_table_prp_usuario extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_usuario() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_usuario", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_usuario_mc", "int8", 19),
      new ColumnInfo("rut", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("saldo_info", SqlType.TEXT.getGetJavaType()),
      new ColumnInfo("saldo_expiracion", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("intentos_validacion", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("nombre",SqlType.VARCHAR.getGetJavaType(),30),
      new ColumnInfo("apellido",SqlType.VARCHAR.getGetJavaType(),30),
      new ColumnInfo("numero_documento",SqlType.VARCHAR.getGetJavaType(),30),
      new ColumnInfo("tipo_documento",SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("nivel",SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("uuid",SqlType.VARCHAR.getGetJavaType(),100)
    );
    Assert.assertEquals("Existe tabla prp_usuario", true, exists);
  }
}
