package cl.multicaja.prepaid.async.v10.processors;


import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author abarazarte
 **/
public class ReconciliationScheduler10 extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(ReconciliationScheduler10.class);

  public ReconciliationScheduler10(BaseRoute10 route) {
    super(route);
  }

  public Processor processReconciliation() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info(String.format("Running scheduled task - %s", LocalDateTime.now()));
        List<PrepaidMovement10> lstPrepaidMovement10s = getRoute().getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
        for(PrepaidMovement10 mov :lstPrepaidMovement10s) {
          try {
            getRoute().getPrepaidMovementEJBBean10().processReconciliation(mov);
          }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
            continue;
          }
        }
      }
    };
  }
}

