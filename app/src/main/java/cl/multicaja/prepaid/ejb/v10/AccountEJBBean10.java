package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.dao.AccountDao;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountProcessor;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;

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

  public Account insertAccount(String accountNumber, Long userId) throws Exception {
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
  public void publishAccountCreatedEvent(Account account) throws Exception {
    if(account == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account"));
    }

    getKafkaEventDelegate10().publishAccountCreatedEvent(account);
  }
}
