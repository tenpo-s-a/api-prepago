package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import static cl.multicaja.core.model.Errors.CUENTA_NO_EXISTE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class IpmEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(IpmEJBBean10.class);

  private static final String FIND_IPM_DATA_BY_RECONCILIATION_SIMILARITY = String.format("SELECT * FROM %s.prp_cuenta WHERE id = ?", getSchema());

  public Account findByReconciliationSimilarity(Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    log.info(String.format("[findByReconciliationSimilarity] Buscando cuenta/contrato por id [%d]", id));
    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_IPM_DATA_BY_RECONCILIATION_SIMILARITY, this.getAccountMapper(), id);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[findByReconciliationSimilarity]  Cuenta/contrato con id [%d] no existe", id));
      throw new ValidationException(CUENTA_NO_EXISTE);
    }
  }
}
