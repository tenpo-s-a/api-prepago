package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidAccountingMovement;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PendingStoreWithdrawToAccounting10 extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(AccountingScheduler10.class);

  public PendingStoreWithdrawToAccounting10(BaseRoute10 route) {
    super(route);
  }

  public Processor storeWithdrawToAccounting() throws Exception {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("Storing web withdraw in accounting and clearing tables");
        PrepaidTopupData10 data = req.getData();
        PrepaidMovement10 prepaidWithdraw = data.getPrepaidMovement10();
        UserAccount userAccount = data.getUserAccount();
        System.out.print("Recibi una user account con id: " + userAccount.getId());

        if (prepaidWithdraw == null) {
          log.error("Error movement es null");
          return null;
        }

        if (userAccount == null || userAccount.getId() == null) {
          log.error("Error userAccountId es null");
          return null;
        }

        PrepaidAccountingMovement mov = new PrepaidAccountingMovement();
        mov.setPrepaidMovement10(prepaidWithdraw);
        mov.setReconciliationDate(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()));

        // Insertar en accounting como PENDING
        AccountingData10 accounting10 = getRoute().getPrepaidAccountingEJBBean10().buildAccounting10(mov, AccountingStatusType.PENDING);
        accounting10 = getRoute().getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

        // Insertar en clearing
        ClearingData10 clearing10 = new ClearingData10();
        clearing10.setStatus(AccountingStatusType.PENDING);
        clearing10.setUserBankAccount(userAccount);
        clearing10.setAccountingId(accounting10.getId());
        getRoute().getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);

        return req;
      }
    };
  }
}
