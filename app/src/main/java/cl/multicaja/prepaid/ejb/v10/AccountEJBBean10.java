package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.dao.AccountDao;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.model.Timestamps;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountProcessor;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_EXISTE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class AccountEJBBean10 extends PrepaidBaseEJBBean10 implements AccountEJB10 {

  private static Log log = LogFactory.getLog(AccountEJBBean10.class);

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

  @Inject
  private AccountDao accountDao;

  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  public KafkaEventDelegate10 getKafkaEventDelegate10() {
    return kafkaEventDelegate10;
  }

  public void setKafkaEventDelegate10(KafkaEventDelegate10 kafkaEventDelegate10) {
    this.kafkaEventDelegate10 = kafkaEventDelegate10;
  }

  public AccountDao getAccountDao() {
    return accountDao;
  }

  public void setAccountDao(AccountDao accountDao) {
    this.accountDao = accountDao;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  @Override
  public Account insertAccount(Long userId, String accountNumber) throws Exception {
    if(StringUtils.isAllBlank(accountNumber)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountNumber"));
    }
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    Account account = new Account();
    account.setUserId(userId);
    account.setAccount(accountNumber);
    account.setStatus(AccountStatus.ACTIVE.toString());
    account.setBalanceInfo("");
    account.setExpireBalance(0L);
    account.setProcessor(AccountProcessor.TECNOCOM_CL.toString());
    account = accountDao.insert(account);
    return  accountDao.find(account.getId());
  }

  @Override
  public void publishAccountCreatedEvent(Long externalUserId, Account acc) throws Exception {
    if(externalUserId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "externalUserId"));
    }

    if(acc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account"));
    }

    cl.multicaja.prepaid.kafka.events.model.Account account = new cl.multicaja.prepaid.kafka.events.model.Account();
    account.setId(acc.getUuid());
    account.setStatus(acc.getStatus());

    Timestamps timestamps = new Timestamps();
    timestamps.setCreatedAt(acc.getCreatedAt());
    timestamps.setUpdatedAt(acc.getUpdatedAt());
    account.setTimestamps(timestamps);

    AccountEvent accountEvent = new AccountEvent();
    accountEvent.setUserId(externalUserId.toString());
    accountEvent.setAccount(account);

    getKafkaEventDelegate10().publishAccountCreatedEvent(accountEvent);
  }
}
