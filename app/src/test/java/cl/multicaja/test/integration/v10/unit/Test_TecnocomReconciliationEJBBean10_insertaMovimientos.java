package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.model.v10.MovimientoTecnocom10;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.OriginOpeType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoPais;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Test_TecnocomReconciliationEJBBean10_insertaMovimientos extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void beforeClass(){
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_archivos_conciliacion CASCADE", getSchema()));
  }

  @Test
  public void testInsertMovimientoOk() throws Exception {

    Map<String, Object> respFile = insertArchivoReconcialicionLog(getRandomString(10),"TEST","TEST","PEND");
    Long fileId = numberUtils.toLong(respFile.get("_r_id"));
    MovimientoTecnocom10 movTec = buildRandomTcMov(fileId);

    Assert.assertNotNull("Movimiento Tc no debe ser null",movTec);
    // Inserta movimiento
    movTec = inserTcMov(movTec);

    Assert.assertNotNull("Movimiento Tc despues de insertar no debe ser null",movTec);
    Assert.assertNotEquals("Id debe ser != 0",0,movTec.getId().intValue());

    List<MovimientoTecnocom10> movTec2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN);
    Assert.assertEquals("Debe existir un movimiento",1,movTec2.size());

    Assert.assertEquals("Deben ser iguales getIdArchivo",movTec.getIdArchivo(),movTec2.get(0).getIdArchivo());
    Assert.assertEquals("Deben ser iguales getPan",movTec.getPan(),movTec2.get(0).getPan());
    Assert.assertEquals("Deben ser iguales getCuenta",movTec.getCuenta(),movTec2.get(0).getCuenta());
    Assert.assertEquals("Deben ser iguales getNumAut",movTec.getNumAut(),movTec2.get(0).getNumAut());
    Assert.assertEquals("Deben ser iguales getImpFac",movTec.getImpFac().getValue().longValue(),movTec2.get(0).getImpFac().getValue().longValue());
    Assert.assertEquals("Deben ser iguales getFecFac",movTec.getFecFac(),movTec2.get(0).getFecFac());
    Assert.assertEquals("Deben ser iguales getNomComRed",movTec.getNomcomred(),movTec2.get(0).getNomcomred());
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f1() throws BadRequestException {
    try{
      inserTcMov(null);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"empty");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f2() throws BadRequestException {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(null);
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"IdArchivo");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f3() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta(null);
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"Cuenta");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f3_cuenta_empty() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("   ");
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"Cuenta");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f4() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("AA");
      movimientoTecnocom10.setPan(null);
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"Pan");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f5() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("AA");
      movimientoTecnocom10.setPan("asfaojsfjasof");
      movimientoTecnocom10.setTipoFac(null);
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"TipoFac");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f6() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("AA");
      movimientoTecnocom10.setPan("asfaojsfjasof");
      movimientoTecnocom10.setTipoFac(TipoFactura.CARGA_TRANSFERENCIA);
      movimientoTecnocom10.setImpFac(null);
      inserTcMov(movimientoTecnocom10);

    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"ImpFac");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f7() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("AA");
      movimientoTecnocom10.setPan("asfaojsfjasof");
      movimientoTecnocom10.setTipoFac(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA);
      movimientoTecnocom10.setImpFac(null);
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ", "ImpFac", e.getData()[0].getValue());
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }



  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f8() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("AA");
      movimientoTecnocom10.setPan("asfaojsfjasof");
      movimientoTecnocom10.setTipoFac(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA);
      NewAmountAndCurrency10 impfac = new NewAmountAndCurrency10(new BigDecimal(10000L));
      movimientoTecnocom10.setImpFac(impfac);
      movimientoTecnocom10.setNumAut(null);
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ", "NumAut", e.getData()[0].getValue());
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testInsertMovimientoNoOK_f8_numaut_empty() throws Exception {
    try{
      MovimientoTecnocom10 movimientoTecnocom10= new MovimientoTecnocom10();
      movimientoTecnocom10.setIdArchivo(1L);
      movimientoTecnocom10.setCuenta("AA");
      movimientoTecnocom10.setPan("asfaojsfjasof");
      movimientoTecnocom10.setTipoFac(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA);
      NewAmountAndCurrency10 impfac = new NewAmountAndCurrency10(new BigDecimal(10000L));
      movimientoTecnocom10.setImpFac(impfac);
      movimientoTecnocom10.setNumAut("   ");
      inserTcMov(movimientoTecnocom10);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ", "NumAut", e.getData()[0].getValue());
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  public static MovimientoTecnocom10 inserTcMov(MovimientoTecnocom10 data)throws Exception{
    return getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(data);
  }

  public static MovimientoTecnocom10 buildRandomTcMov(Long fileId) {
    MovimientoTecnocom10 movimientoTecnocom10 = new MovimientoTecnocom10();
    movimientoTecnocom10.setIdArchivo(fileId);
    movimientoTecnocom10.setLinRef(1);
    movimientoTecnocom10.setNumMovExt(1L);
    movimientoTecnocom10.setNumExtCta(1L);
    movimientoTecnocom10.setNumAut(getRandomNumericString(6));
    movimientoTecnocom10.setCodCom(getRandomNumericString(5));
    movimientoTecnocom10.setPan(getRandomNumericString(20));
    movimientoTecnocom10.setCuenta(getRandomNumericString(10));
    movimientoTecnocom10.setTipoFac(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA);
    movimientoTecnocom10.setIndNorCor(TipoFactura.CARGA_TRANSFERENCIA.getCorrector());
    movimientoTecnocom10.setFecFac(LocalDate.now());
    movimientoTecnocom10.setNomPob(getRandomNumericString(10));
    movimientoTecnocom10.setCodCom("");
    movimientoTecnocom10.setCodEnt("");
    movimientoTecnocom10.setCentAlta("");
    movimientoTecnocom10.setNumRefFac(getRandomNumericString(3));
    movimientoTecnocom10.setCmbApli(new BigDecimal(23));
    movimientoTecnocom10.setIndProaje("");
    movimientoTecnocom10.setCodAct(1);
    movimientoTecnocom10.setCodPais(CodigoPais.CHILE.getValue());
    movimientoTecnocom10.setClamone(CodigoMoneda.NONE);
    movimientoTecnocom10.setTipoLin("");

    NewAmountAndCurrency10 impFac = new NewAmountAndCurrency10();
    impFac.setValue(new BigDecimal(getUniqueInteger()));
    impFac.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    movimientoTecnocom10.setImpFac(impFac);

    NewAmountAndCurrency10 impDiv = new NewAmountAndCurrency10();
    impDiv.setValue(new BigDecimal(getUniqueInteger()));
    impDiv.setCurrencyCode(CodigoMoneda.USA_USD);
    movimientoTecnocom10.setImpDiv(impDiv);

    NewAmountAndCurrency10 imLiq = new NewAmountAndCurrency10();
    imLiq.setValue(new BigDecimal(getUniqueInteger()));
    imLiq.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    movimientoTecnocom10.setImpLiq(imLiq);

    movimientoTecnocom10.setContrato(getRandomNumericString(10));
    movimientoTecnocom10.setOriginOpe("ONLI");
    movimientoTecnocom10.setImpautcon(imLiq);
    movimientoTecnocom10.setFecTrn(new Timestamp(System.currentTimeMillis()));

    return movimientoTecnocom10;
  }
  public static Map<String, Object> insertArchivoReconcialicionLog(String nombreArchivo, String proceso, String tipo, String status) throws SQLException {
    Object[] params = {
      nombreArchivo != null ? nombreArchivo : new NullParam(Types.VARCHAR),
      proceso != null ? proceso : new NullParam(Types.VARCHAR),
      tipo != null ? tipo : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return getDbUtils().execute(getSchema() + ".prp_inserta_archivo_conciliacion", params);
  }
}
