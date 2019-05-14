package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190513100546_create_table_ipm_file_data extends TestDbBasePg {

  @Test
  public void checkIfExistsTable() {
    boolean exists = dbUtils.tableExists("prepaid_accounting", "ipm_file_data", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("file_id", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("message_type", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("function_code", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("message_reason", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("message_number", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("pan", SqlType.VARCHAR.getGetJavaType(), 19),
      new ColumnInfo("transaction_amount", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("reconciliation_amount", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("cardholder_billing_amount", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("reconciliation_conversion_rate", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("cardholder_billing_conversion_rate", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("transaction_local_date", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("approval_code", SqlType.VARCHAR.getGetJavaType(), 6),
      new ColumnInfo("transaction_currency_code", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("reconciliation_currency_code", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("cardholder_billing_currency_code", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("merchant_code", SqlType.VARCHAR.getGetJavaType(), 15),
      new ColumnInfo("merchant_name", SqlType.VARCHAR.getGetJavaType(), 22),
      new ColumnInfo("merchant_state", SqlType.VARCHAR.getGetJavaType(), 13),
      new ColumnInfo("merchant_country", SqlType.VARCHAR.getGetJavaType(), 3),
      new ColumnInfo("transaction_life_cycle_id", SqlType.VARCHAR.getGetJavaType(), 16),
      new ColumnInfo("reconciled", SqlType.BOOLEAN.getGetJavaType()),
      new ColumnInfo("created_at", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("updated_at", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla ipm_file_data", true, exists);
  }
}
