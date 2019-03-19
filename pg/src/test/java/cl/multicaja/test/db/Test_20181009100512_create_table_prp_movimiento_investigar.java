package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181009100512_create_table_prp_movimiento_investigar extends TestDbBasePg {

  // Historial de modificación
  // 20190221130909_create_sp_alter_prp_movimiento_investigar.sql
  // 20190305151144_create_sp_alter_prp_movimiento_investigar_v11.sql

  @Test
  public void checkIfExistsTable_prp_movimiento_investigar() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_investigar", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("informacion_archivos", "json",2147483647),
      new ColumnInfo("origen",SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("fecha_registro", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_de_transaccion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("responsable", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("descripcion", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("mov_ref", SqlType.NUMERIC.getGetJavaType(),100),
      new ColumnInfo("tipo_movimiento", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("sent_status", SqlType.VARCHAR.getGetJavaType(),100)
    );

    Assert.assertEquals("Existe tabla prp_movimiento_investigar", true, exists);
  }
}
