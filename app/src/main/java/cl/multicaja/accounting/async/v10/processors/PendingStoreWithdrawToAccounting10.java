package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.Clearing10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author abarazarte
 **/
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

        // Insertar en accounting como PENDING
        System.out.println("Get prepaid accounting ejb: " + getRoute().getPrepaidAccountingEJBBean10());
        System.out.println("prepaidwithdraw: " + prepaidWithdraw);
        Accounting10 accounting10 = getRoute().getPrepaidAccountingEJBBean10().buildAccounting10(prepaidWithdraw, AccountingStatusType.PENDING);
        accounting10 = getRoute().getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

        // Insertar en clearing
        Clearing10 clearing10 = new Clearing10();
        clearing10.setClearingStatus(AccountingStatusType.PENDING);
        clearing10.setUserAccount(userAccount);
        clearing10.setId(accounting10.getId());
        getRoute().getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);

        return req;
      }
    };
  }
}
