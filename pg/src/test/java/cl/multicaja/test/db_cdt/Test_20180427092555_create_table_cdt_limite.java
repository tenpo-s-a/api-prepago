package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180427092555_create_table_cdt_limite extends TestDbBasePg {

  /********************************************************
   *       id                    BIGSERIAL NOT NULL,
   *       id_fase_movimiento    BIGSERIAL NOT NULL,
   *       id_regla_acumulacion  BIGSERIAL,
   *       descripcion           VARCHAR(100) NOT NULL,
   *       valor                 DECIMAL NOT NULL,
   *       cod_operacion         VARCHAR(10) NOT NULL,
   *       estado                VARCHAR(5) NOT NULL,
   *       fecha_estado          TIMESTAMP NOT NULL,
   *       fecha_creacion        TIMESTAMP NOT NULL,
   *       CONSTRAINT cdt_limite_pk PRIMARY KEY(id)
   *******************************************************/

  @Test
  public void CheckTableLimite() {
    boolean exists = dbUtils.tableExists(SCHEMA_CDT, Constants.Tables.LIMITE.getName(), true,
      new ColumnInfo("id", "BIGSERIAL",19),
      new ColumnInfo("id_fase_movimiento", "INT8", 19),
      new ColumnInfo("id_regla_acumulacion", "INT8", 19),
      new ColumnInfo("descripcion", "VARCHAR", 100),
      new ColumnInfo("valor", "NUMERIC", 131089),
      new ColumnInfo("cod_operacion", "VARCHAR", 10),
      new ColumnInfo("cod_error", "NUMERIC", 10),
      new ColumnInfo("estado", "VARCHAR", 10),
      new ColumnInfo("fecha_estado", "TIMESTAMP", 29),
      new ColumnInfo("fecha_creacion", "TIMESTAMP", 29));
      Assert.assertEquals("Existe tabla "+SCHEMA_CDT+"."+Constants.Tables.LIMITE.getName(), true, exists);
  }


}
