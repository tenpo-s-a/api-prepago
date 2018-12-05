package cl.multicaja.cuentatraspaso.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.db.ColumnInfo;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180402193618_create_changelog extends TestDbBase {

  @Test
  public void checkTableChangelog() {
    boolean exists = dbUtils.tableExists("public", "changelog_cuenta_de_traspaso", true,
      new ColumnInfo("id", "numeric", 20),
      new ColumnInfo("applied_at", "varchar", 25),
      new ColumnInfo("description", "varchar", 255));
    Assert.assertEquals("Existe tabla changelog", true, exists);
  }
}
