package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190219130505_create_table_intermediate_reconciliation_files extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_reconciliation_files() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_reconciliation_files", true,
      new ColumnInfo("id", "bigserial", 19),
      new ColumnInfo("nombre_de_archivo", "varchar", 255),
      new ColumnInfo("proceso", "varchar", 50),
      new ColumnInfo("fecha_recepcion", "timestamp", 29),
      new ColumnInfo("tipo", "varchar", 50),
      new ColumnInfo("status", "varchar", 50)
    );
    Assert.assertEquals("Existe tabla prp_reconciliation_files", true, exists);
  }
}
