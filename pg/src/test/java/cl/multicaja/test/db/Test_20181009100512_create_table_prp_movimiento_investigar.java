package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181009100512_create_table_prp_movimiento_investigar extends TestDbBasePg {

  //Segun modificacion data por : 20190221130909_create_sp_alter_prp_movimiento_investigar.sql

  @Test
  public void checkIfExistsTable_prp_movimiento_investigar() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_investigar", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_archivo_origen", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("origen",SqlType.VARCHAR.getGetJavaType(),50),
      new ColumnInfo("nombre_archivo", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("fecha_registro", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_de_transaccion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("responsable", SqlType.VARCHAR.getGetJavaType(),50),
      new ColumnInfo("descripcion", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("mov_ref", SqlType.BIGINT.getGetJavaType())
    );

    Assert.assertEquals("Existe tabla prp_movimiento_investigar", true, exists);
  }
}
