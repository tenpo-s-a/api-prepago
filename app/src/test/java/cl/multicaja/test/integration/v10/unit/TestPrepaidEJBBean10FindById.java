package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;

import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Movement;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.*;

public class TestPrepaidEJBBean10FindById extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void findById_movementId_null() throws Exception {
    try {
      getPrepaidMovementEJBBean10().findById(null);
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = ValidationException.class)
  public void findById_movement_null() throws Exception {
    try {
      getPrepaidMovementEJBBean10().findById(Long.MAX_VALUE);
    } catch(ValidationException vex) {
      Assert.assertEquals(TRANSACCION_ERROR_EN_CONSULTA_DE_MOVIMIENTO.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void findById() throws Exception {



    User user = registerUser();
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
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 movement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard, cdtTransaction, PrepaidMovementType.WITHDRAW);
    movement.setConSwitch(ReconciliationStatusType.RECONCILED);
    movement.setConTecnocom(ReconciliationStatusType.RECONCILED);
    movement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    movement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);


    PrepaidMovement10 movement10 = getPrepaidMovementEJBBean10().insertMovement(123L, "nadas", movement);


    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().findById(movement.getId());



    Assert.assertEquals("Debe ser el mismo movimiento", movement10.getId(), dbMovement.getId());
    Assert.assertEquals("Debe ser el mismo movimiento", movement10.getCentalta(), dbMovement.getCentalta());
    Assert.assertEquals("Debe ser el mismo movimiento", movement10.getIdTxExterno(), dbMovement.getIdTxExterno());
    Assert.assertEquals("Debe ser el mismo movimiento", movement10.getNumaut(), dbMovement.getNumaut());
    Assert.assertEquals("Debe ser el mismo movimiento", movement10.getPan(), dbMovement.getPan());

  }
}
