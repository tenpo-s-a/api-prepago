package cl.multicaja.prepaid.ejb.v10;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * Ejb para todos los procesos de un servicio async.
 * Con el tiempo la idea es migrar los procesos que se realizan en la parte asyncrona a este ejb para
 * mejorar los test (Separar procesos).
 *
 * @author JOG
 */

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class AsyncProcessEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean10.class);



}
