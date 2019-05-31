package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.model.v10.IpmMovement10;
import cl.multicaja.prepaid.model.v10.Timestamps;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;

import static cl.multicaja.core.model.Errors.CUENTA_NO_EXISTE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class IpmEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(IpmEJBBean10.class);

  private static final String FIND_IPM_MOVEMENT_BY_RECONCILIATION_SIMILARITY =
    String.format("SELECT * FROM " +
                     "  %s.ipm_file_data " +
                     "WHERE " +
                     "  pan = ? AND " +
                     "  merchant_code = ? AND " +
                     "  approval_code = ? AND " +
                     "  reconciled = FALSE AND " +
                     "  cardholder_billing_amount >= ? AND cardholder_billing_amount <= ?", getSchemaAccounting());

  private static final String UPDATE_IPM_MOVEMENT_RECONCILED_STATUS = "UPDATE %s.ipm_file_data SET reconciled = %b WHERE id = %s";

  /**
   * Busca un movimiento similar, que tenga:
   *      - Mismo pan
   *      - Mismo codcom
   *      - Mismo numaut
   *      - No este conciliado
   *      - Tenga 3.5% de diferencia en el monto (Entre 96.5% y 103.5%)
   *      - Si hay mas de uno, elige el mas cercano al monto original (100.0%)
   * @param truncatedPan
   * @param codcom
   * @param amount
   * @param numaut
   * @return
   * @throws Exception
   */
  public IpmMovement10 findByReconciliationSimilarity(String truncatedPan, String codcom, BigDecimal amount, String numaut) throws Exception {
    if (truncatedPan == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "truncatedPan"));
    }
    if (codcom == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "codcom"));
    }
    if (amount == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if (numaut == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "numaut"));
    }

    BigDecimal offset = new BigDecimal(0.035); // 3.5%
    BigDecimal lowAmount = amount.multiply(BigDecimal.ONE.subtract(offset)); // 3.5% inferior
    BigDecimal highAmount = amount.multiply(BigDecimal.ONE.add(offset)); // 3.5% superior

    log.info(String.format("[findByReconciliationSimilarity] Buscando ipmMovement no conciliado por truncatedPan [%s] codcom [%s] amount [%s] numaut [%s]", truncatedPan, codcom, amount.toString(), numaut));
    try {
      // Obtener lista de entradas similares
      List<IpmMovement10> ipmMovement10List = getDbUtils().getJdbcTemplate().query(FIND_IPM_MOVEMENT_BY_RECONCILIATION_SIMILARITY, this.getIpmMovementMapper(), truncatedPan, codcom, numaut, lowAmount, highAmount);

      if (ipmMovement10List == null || ipmMovement10List.isEmpty()) {
        return null;
      }

      // De los encontrados, buscar el monto que se acerque mas al monto buscado
      BigDecimal minDelta = BigDecimal.valueOf(Double.MAX_VALUE);
      IpmMovement10 minFound = null;
      for (IpmMovement10 foundMovement : ipmMovement10List) {
        BigDecimal amountDelta = amount.subtract(foundMovement.getCardholderBillingAmount()).abs(); // Buscar diferencia con el cardholder_billing_amount
        if (amountDelta.compareTo(minDelta) < 0) { // Esta mas cerca que el actual?
          minDelta = amountDelta;
          minFound = foundMovement; // Nuevo candidato
        }
      }

      return minFound;
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[findByReconciliationSimilarity]  No se encontro MovimientoIpm no conciliado con truncatedPan [%s] codcom [%s] amount [%s] numaut [%s]", truncatedPan, codcom, amount.toString(), numaut));
      return null;
    }
  }

  public void updateIpmMovementReconciledStatus(Long id, boolean reconciledStatus) throws Exception {
    if (id == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    String updateQuery = String.format(UPDATE_IPM_MOVEMENT_RECONCILED_STATUS, getSchemaAccounting(), reconciledStatus, id);
    try {
      getDbUtils().getJdbcTemplate().execute(updateQuery);
    } catch(Exception e) {
      log.error(String.format("[updateIpmMovementReconciledStatus]  No se pudo actualizar el estado reconciliado a [%b] del movimiento [%s]", reconciledStatus, id));
      throw new Exception("No se pudo actualizar el saldo");
    }
  }

  public RowMapper<IpmMovement10> getIpmMovementMapper() {
    return (ResultSet rs, int rowNum) -> {
      IpmMovement10 ipmMovement = new IpmMovement10();
      ipmMovement.setId(rs.getLong("id"));
      ipmMovement.setFileId(rs.getLong("file_id"));
      ipmMovement.setMessageType(rs.getInt("message_type"));
      ipmMovement.setFunctionCode(rs.getInt("function_code"));
      ipmMovement.setMessageReason(rs.getInt("message_reason"));
      ipmMovement.setMessageNumber(rs.getInt("message_number"));
      ipmMovement.setPan(rs.getString("pan"));
      ipmMovement.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
      ipmMovement.setReconciliationAmount(rs.getBigDecimal("reconciliation_amount"));
      ipmMovement.setCardholderBillingAmount(rs.getBigDecimal("cardholder_billing_amount"));
      ipmMovement.setReconciliationConversionRate(rs.getBigDecimal("reconciliation_conversion_rate"));
      ipmMovement.setCardholderBillingConversionRate(rs.getBigDecimal("cardholder_billing_conversion_rate"));
      ipmMovement.setTransactionLocalDate(rs.getTimestamp("transaction_local_date").toLocalDateTime());
      ipmMovement.setApprovalCode(rs.getString("approval_code"));
      ipmMovement.setTransactionCurrencyCode(rs.getInt("transaction_currency_code"));
      ipmMovement.setReconciliationCurrencyCode(rs.getInt("reconciliation_currency_code"));
      ipmMovement.setCardholderBillingCurrencyCode(rs.getInt("cardholder_billing_currency_code"));
      ipmMovement.setMerchantCode(rs.getString("merchant_code"));
      ipmMovement.setMerchantName(rs.getString("merchant_name"));
      ipmMovement.setMerchantState(rs.getString("merchant_state"));
      ipmMovement.setMerchantCountry(rs.getString("merchant_country"));
      ipmMovement.setTransactionLifeCycleId(rs.getString("transaction_life_cycle_id"));
      ipmMovement.setReconciled(rs.getBoolean("reconciled"));

      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
      timestamps.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
      ipmMovement.setTimestamps(timestamps);

      return ipmMovement;
    };
  }
}
