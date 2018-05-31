package cl.multicaja.test.api.unit;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10 extends TestBaseUnit {

  @Test
  public void testeEjbAddMovement() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser();

    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(prepaidUser);

    createPrepaidMovement(prepaidMovement10);
  }

  @Test
  public void testeEjbUpdate() throws Exception {

    // CREA USUARIOS
    PrepaidUser10 prepaidUser = buildPrepaidUser();

    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(prepaidUser);

    prepaidMovement10 = createPrepaidMovement(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,prepaidMovement10.getId(),null,null,null,PrepaidMovementStatus.IN_PROCESS);

    List lstMov = buscaMovimiento(prepaidMovement10.getId());

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser :"+PrepaidMovementStatus.IN_PROCESS, PrepaidMovementStatus.IN_PROCESS.toString(), fila.get("estado"));
  }

  @Test
  public void testeEjbUpdate2() throws Exception {

    // CREA USUARIOS
    PrepaidUser10 prepaidUser = buildPrepaidUser();

    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(prepaidUser);

    prepaidMovement10 = createPrepaidMovement(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(),1,2,CodigoMoneda.CHILE_CLP.getValue(), PrepaidMovementStatus.PROCESS_OK);

    List lstMov = buscaMovimiento(prepaidMovement10.getId());

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser :" + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK.toString(), fila.get("estado"));
    Assert.assertEquals("El Num Extracto debe ser 1",1,((BigDecimal)fila.get("numextcta")).intValue());
    Assert.assertEquals("El Num Extracto debe ser 1",2,((BigDecimal)fila.get("nummovext")).intValue());
    Assert.assertEquals("El Num Extracto debe ser 1", CodigoMoneda.CHILE_CLP.getValue().intValue(),((BigDecimal)fila.get("clamone")).intValue());
  }

  public List buscaMovimiento(Object idMovimiento)  {
    ConfigUtils configUtils = ConfigUtils.getInstance();
    String SCHEMA = configUtils.getProperty("schema");
    String SP_NAME = SCHEMA + ".prp_movimiento";
    return dbUtils.getJdbcTemplate().queryForList("SELECT * FROM "+SP_NAME+" WHERE ID ="+idMovimiento);
  }

}
