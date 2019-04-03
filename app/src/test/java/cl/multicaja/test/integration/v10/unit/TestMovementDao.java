package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.dao.MovementDao;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Movement;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoPais;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

public class TestMovementDao extends TestBaseUnit {

  private MovementDao movementDao = new MovementDao();

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete  from %s.%s",getSchema(),"prp_movimiento"));

  }

  @Test
  public void testInsert() throws Exception {

    /*User user = registerUser();
    UserAccount userAccount = createBankAccount(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankAccountId(userAccount.getId());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);*/

    movementDao.setEm(createEntityManager());
    Movement movement = new Movement();

    //movement = buildMovement11(prepaidUser, prepaidWithdraw, prepaidCard, cdtTransaction, PrepaidMovementType.WITHDRAW);
    movement.setConSwitch(ReconciliationStatusType.RECONCILED);
    movement.setConTecnocom(ReconciliationStatusType.RECONCILED);
    movement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    movement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    movement.setCentalta("sdas");
    movement.setClamon(CodigoMoneda.CHILE_CLP);
    movement.setClamondiv(1);
    movement.setClamone(1);
    movement.setClamonliq(1);
    movement.setCmbapli(1);
    movement.setCodact(1);
    movement.setCodcom("dsa");
    movement.setCodent("dsa");
    movement.setCodpais(CodigoPais.CHILE);
    movement.setCuenta("dsad");
    movement.setFecfac(Date.from(Instant.now()));
    movement.setFechaActualizacion(Timestamp.from(Instant.now()));
    movement.setFechaCreacion(Timestamp.from(Instant.now()));
    movement.setIdMovimientoRef(123L);
    movement.setIdPrepaidUser(123L);
    movement.setIdTxExterno("dsad");
    movement.setImpdiv(new BigDecimal(123));
    movement.setImpfac(new BigDecimal(123));
    movement.setImpliq(new BigDecimal(123));

    movement = movementDao.insert(movement);
    Assert.assertNotNull("No debe ser null",movement);
    Assert.assertNotNull("No debe ser null",movement.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,movement.getId().longValue());


  }


}
