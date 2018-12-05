package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20180503142212_create_sp_mc_cdt_crea_movimiento_cuenta_v10 extends TestDbBasePg {


  private JdbcTemplate jdbcTempate = dbUtils.getJdbcTemplate();

  /***********************************************
   * NUEVO USUARIO PREPAGO
   * PRIMERA CARGA
   * VERIFICACION DE LIMITES
   * @throws SQLException
   **********************************************/
  @Test
  public void nuevoUsuarioPrimeraCarga() throws SQLException {
    Movimiento movimiento;

    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    //================================================================================
    // 1 -Monto	$1000    // Deberia fallar limite Monto debe ser mayor a $3.000
    //================================================================================
    //Movimiento(Integer idFaseMovimiento, String idCuenta, Integer idMovimientoRef, String idExterno, String glosa, BigDecimal monto, boolean bSimulacion)
    movimiento = new Movimiento(SOLICITUD_PRIMERA_CARGA, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud primera carga", new BigDecimal(1000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError == 108203", movimiento.getNumError().equals("108203"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // 2 -Monto	$55000   // Deberia fallar limite Monto carga ser menor a $50.000
    //================================================================================
    movimiento.setIdMovimientoRef(0);
    movimiento.setMonto(new BigDecimal(55000));
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError == 108206", movimiento.getNumError().equals("108206"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // 3 -Monto 	$45000   // Deberia pasar sin errores.
    //================================================================================
    movimiento.setIdMovimientoRef(0);
    movimiento.setMonto(new BigDecimal(45000));
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    // CONFIRMACION PRIMERA CARGA
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_PRIMERA_CARGA);
    movimiento.setGlosa("Confirmacion Primera Carga");
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // 4 -Monto 	$45000   // Deberia fallar xq cantidad de primeras cargas es >1
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_PRIMERA_CARGA, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud primera carga", new BigDecimal(3000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);

    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("108001"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // Verificar Acumuladores Primera Carga
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_PRIMERA_CARGA, 45000L, 1L, 0L);
  }

  /***********************************************
   * USUARIO N2 PREPAGO
   * CARGA WEB
   * VERIFICACION DE LIMITES
   * @throws SQLException
   **********************************************/
  @Test
  public void usaurioN2CargaWeb() throws SQLException {

    Movimiento movimiento;
    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    //================================================================================
    // 1 -Monto	$1000    // Deberia fallar limite Monto debe ser mayor a $3.000
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga WEB", new BigDecimal(2999), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertFalse("NumError == 10009", movimiento.getNumError().equals("10009"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // 2 -Monto	$55000   // Deberia fallar limite Monto carga ser menor a $500.000
    //================================================================================
    movimiento.setMonto(new BigDecimal(500010));
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertFalse("NumError == 10008", movimiento.getNumError().equals("10008"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    int iMonto = 200000;
    for (int i = 1; i <= 5; i++) {
      //================================================================================
      // Monto	$200.000   // Deberia Pasar OK y sumar Acumuladores (CARGA)
      //================================================================================
      movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga WEB", new BigDecimal(200000), false);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      //================================================================================
      // Monto	$200.000   // Deberia Pasar OK y sumar Acumuladores (CONFIRMACION)
      //================================================================================
      movimiento.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, iMonto, i, 0);

      iMonto = iMonto + 200000;
    }
     verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 1000000, 5, 0);
    //================================================================================
    // 10 Deberia fallar por el limite mensual de 1000000
    //================================================================================
    movimiento.setIdFaseMovimiento(SOLICITUD_CARGA_WEB);
    movimiento.setIdMovimientoRef(0);
    movimiento.setMonto(new BigDecimal(3000));
    movimiento.setIdExterno(getRandomNumericString(20));
    movimiento = callCreaMovimientoCuenta(movimiento);

    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError == 108204", movimiento.getNumError().equals("108204"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

  }

  /***********************************************
   * USUARIO N2 PREPAGO
   * CARGA WEB
   * VERIFICACION DE LIMITES
   * @throws SQLException
   **********************************************/
  @Test
  public void usaurioN2CargaPos() throws SQLException {

    Movimiento movimiento;
    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    //================================================================================
    // 1 -Monto	$1000    // Deberia fallar limite Monto debe ser mayor a $3.000
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_POS, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(2999), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError == 108203", movimiento.getNumError().equals("108203"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // 2 -Monto	$100.000   // Deberia fallar limite Monto carga ser menor a $100.000
    //================================================================================
    movimiento.setMonto(new BigDecimal(100010));
    movimiento.setIdMovimientoRef(0);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError == 108202", movimiento.getNumError().equals("108202"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    int iMonto = 100000;
    for (int i = 1; i <= 10; i++) {
      //================================================================================
      // 3 -Monto	 $100.000 // DEBERIA PASAR (TOTAL = 1000000) (SOLICITUD)
      //================================================================================
      movimiento = new Movimiento(SOLICITUD_CARGA_POS, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(100000), false);
      movimiento = callCreaMovimientoCuenta(movimiento);
      //respuestaMovimiento = callCreaMovimientoCuenta(cuentaUsuario, idFaseMovimiento, 0, ""+getUniqueLong(), descCuenta, ,"N");
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));
      // CONFIRMACION SOLICITUD
      movimiento.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_POS);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_POS, iMonto, i, 0);

      iMonto = iMonto + 100000;
    }
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_POS, 1000000, 10, 0);
    //================================================================================
    // 3 -Deberia Fallar por Monto supera el 1000000 mensual
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_POS, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(30000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError == 108204", movimiento.getNumError().equals("108204"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

  }


  /****
   * Teste encargado de verificar que la simulacion no afecte los acumuladores.
   */
  @Test
  public void verificaSimulacion() throws SQLException {

    Movimiento movimiento;

    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();

    //================================================================================
    // SOLICITUD CARGA
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(45000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError == 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("sjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // CONFIRMACION CARGA
    //================================================================================
    movimiento.setIdExterno(movimiento.getIdExterno());
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError == 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("sjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // VERIFICA QUE LOS ACUMULADORES SUMARON
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 0);

    //================================================================================
    //  PRUEBA DE CARGA SIMULADA ( CON PROBLEMA DE LIMITE)
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(999999), true);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertFalse("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertFalse("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // VERIFICA QUE LOS ACUMULADORES NO SUMARON (SIMULACION OK)
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 0);

    //================================================================================
    //  PRUEBA DE CARGA SIMULADA ( SIN  PROBLEMA DE LIMITE)
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(10000), true);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // VERIFICA QUE LOS ACUMULADORES NO SUMARON (SIMULACION OK)
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 0);


  }

  @Test
  public void retiroWeb() throws SQLException {

    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();

    Movimiento movimiento;

    int iMonto = 100000;
    for (int i = 1; i <= 5; i++) {
      //================================================================================
      // Monto	$200.000   // Deberia Pasar OK y sumar Acumuladores (CARGA)
      //================================================================================
      movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga WEB", new BigDecimal(100000), false);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      //================================================================================
      // Monto	$200.000   // Deberia Pasar OK y sumar Acumuladores (CONFIRMACION)
      //================================================================================
      movimiento.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, iMonto, i, 0);

      iMonto = iMonto + 100000;
    }

    //================================================================================
    //PRIMER RETIRO
    //================================================================================

    movimiento = new Movimiento(SOLICITUD_RETIRO_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de retiro WEB", new BigDecimal(10000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // PRIMERA CONFIRMACION RETIRO
    //================================================================================
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_RETIRO_WEB);
    movimiento.setIdExterno(movimiento.getIdExterno());
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    //SEGUNDO RETIRO
    //================================================================================

    movimiento = new Movimiento(SOLICITUD_RETIRO_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de retiro WEB", new BigDecimal(20000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // SEGUNDA CONFIRMACION RETIRO
    //================================================================================
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_RETIRO_WEB);
    movimiento.setIdExterno(movimiento.getIdExterno());
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));
    // TOTAL DE RETIROS
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), CONFIRMACIÓN_RETIRO_WEB, 30000, 2, 0);

  }


  @Test
  public void retiroPos() throws SQLException {

    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    Movimiento movimiento;

    int iMonto = 100000;
    for (int i = 1; i <= 5; i++) {
      //================================================================================
      // Monto	$200.000   // Deberia Pasar OK y sumar Acumuladores (CARGA)
      //================================================================================
      movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga WEB", new BigDecimal(100000), false);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      //================================================================================
      // Monto	$200.000   // Deberia Pasar OK y sumar Acumuladores (CONFIRMACION)
      //================================================================================
      movimiento.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
      movimiento = callCreaMovimientoCuenta(movimiento);
      Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
      Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
      Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

      verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, iMonto, i, 0);

      iMonto = iMonto + 100000;
    }

    //================================================================================
    //PRIMER RETIRO
    //================================================================================

    movimiento = new Movimiento(SOLICITUD_RETIRO_POS, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de retiro WEB", new BigDecimal(10000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // PRIMERA CONFIRMACION RETIRO
    //================================================================================
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_RETIRO_POS);
    movimiento.setIdExterno(movimiento.getIdExterno());
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    //SEGUNDO RETIRO
    //================================================================================

    movimiento = new Movimiento(SOLICITUD_RETIRO_POS, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de retiro WEB", new BigDecimal(20000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));

    //================================================================================
    // SEGUNDA CONFIRMACION RETIRO
    //================================================================================
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_RETIRO_POS);
    movimiento.setIdExterno(movimiento.getIdExterno());
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError != 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError != vacio", StringUtils.isBlank(movimiento.getMsjError()));
    // TOTAL DE RETIROS
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), CONFIRMACIÓN_RETIRO_POS, 30000, 2, 0);

  }




  /***********************************************
   * Verifica que se haga una reversa
   * confirmaciones o reversas de una misma
   * transaccion de solicitud
   * @throws SQLException
   **********************************************/
  @Test
  public void pruebaReversa() throws Exception {

    Movimiento movimiento;
    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    //================================================================================
    // SOLICITUD DE CARGA
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_CARGA_WEB, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(45000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("[Movimiento 1] Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("[Movimiento 1] NumError == 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("[Movimiento 1] MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 45000);

    //================================================================================
    // CONFIRMACION DE CARGA
    //================================================================================
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("[Movimiento 1] Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("[Movimiento 1] NumError == 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("[Movimiento 1] MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 0);

    //================================================================================
    // SOLICITUD DE REVERSA
    //================================================================================
    movimiento.setIdFaseMovimiento(SOLICITUD_REVERSA_CARGA);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("[Movimiento 1] Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("[Movimiento 1] NumError == 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("[Movimiento 1] MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    //================================================================================
    // CONFIRMACION DE REVERSA
    //================================================================================
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_REVERSA_CARGA);
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("[Movimiento 1] Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("[Movimiento 1] NumError == 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("[Movimiento 1] MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 0, 0, 0);

  }


  /***********************************************
   * Verifica que las solicitudes y confirmaciones
   * se puedan realizar en distinto orden
   * @throws SQLException
   **********************************************/
  @Test
  public void pruebasSolicitudesDesorden() throws Exception {

    //================================================================================
    // SOLICITUD DE CARGA USUARIO 1
    //================================================================================
    CuentaUsuarioCdt user1 = creaCuentaUsuarioCdt();
    Movimiento movimiento1 = new Movimiento(SOLICITUD_CARGA_WEB, user1.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(30000), false);
    movimiento1 = callCreaMovimientoCuenta(movimiento1);
    Assert.assertTrue("[Movimiento 1] Id Movimiento Cta debe ser > 0", movimiento1.getIdMovimientoRef() > 0);
    Assert.assertEquals("[Movimiento 1] NumError == 0", movimiento1.getNumError(), "0");
    Assert.assertTrue("[Movimiento 1] MsjError = vacio", StringUtils.isBlank(movimiento1.getMsjError()));
    verificaAcumuladores(user1.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 30000, 1, 30000);
    //================================================================================
    // SOLICITUD DE CARGA USUARIO 2
    //================================================================================
    CuentaUsuarioCdt user2 = creaCuentaUsuarioCdt();
    Movimiento movimiento2 = new Movimiento(SOLICITUD_CARGA_WEB, user2.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(45000), false);
    movimiento2 = callCreaMovimientoCuenta(movimiento2);
    Assert.assertTrue("[Movimiento 2] Id Movimiento Cta debe ser > 0", movimiento2.getIdMovimientoRef() > 0);
    Assert.assertEquals("[Movimiento 2] NumError == 0", movimiento2.getNumError(), "0");
    Assert.assertTrue("[Movimiento 2] MsjError = vacio", StringUtils.isBlank(movimiento2.getMsjError()));
    verificaAcumuladores(user2.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 45000);

    //================================================================================
    // SOLICITUD DE CARGA USUARIO 3
    //================================================================================
    CuentaUsuarioCdt user3 = creaCuentaUsuarioCdt();
    Movimiento movimiento3 = new Movimiento(SOLICITUD_CARGA_WEB, user3.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud de carga", new BigDecimal(65000), false);
    movimiento3 = callCreaMovimientoCuenta(movimiento3);
    Assert.assertTrue("[Movimiento 3] Id Movimiento Cta debe ser > 0", movimiento3.getIdMovimientoRef() > 0);
    Assert.assertTrue("[Movimiento 3] NumError == 0", movimiento3.getNumError().equals("0"));
    Assert.assertTrue("[Movimiento 3] MsjError = vacio", StringUtils.isBlank(movimiento3.getMsjError()));
    verificaAcumuladores(user3.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 65000, 1, 65000);

    //================================================================================
    // CONFIRMACION DE CARGA USUARIO 2
    //================================================================================
    movimiento2.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
    movimiento2.setGlosa("Confirmacion Movimiento");
    movimiento2 = callCreaMovimientoCuenta(movimiento2);
    Assert.assertTrue("[Movimiento 2] Id Movimiento Cta debe ser > 0", movimiento2.getIdMovimientoRef() > 0);
    Assert.assertEquals("[Movimiento 2] NumError == 0", movimiento2.getNumError(), "0");
    Assert.assertTrue("[Movimiento 2] MsjError = vacio", StringUtils.isBlank(movimiento2.getMsjError()));
    verificaAcumuladores(user2.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 45000, 1, 0);

    //================================================================================
    // CONFIRMACION DE CARGA USUARIO 1
    //================================================================================
    user1 = creaCuentaUsuarioCdt();
    movimiento1.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
    movimiento1 = callCreaMovimientoCuenta(movimiento1);
    Assert.assertTrue("[Movimiento 1] Id Movimiento Cta debe ser > 0", movimiento1.getIdMovimientoRef() > 0);
    Assert.assertEquals("[Movimiento 1] NumError == 0", movimiento1.getNumError(), "0");
    Assert.assertTrue("[Movimiento 1] MsjError = vacio", StringUtils.isBlank(movimiento1.getMsjError()));
    verificaAcumuladores(user1.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 30000, 1, 0);

    //================================================================================
    // SOLICITUD DE CARGA USUARIO 3
    //================================================================================
    movimiento3.setIdFaseMovimiento(CONFIRMACIÓN_CARGA_WEB);
    movimiento3 = callCreaMovimientoCuenta(movimiento3);
    Assert.assertTrue("[Movimiento 3] Id Movimiento Cta debe ser > 0", movimiento3.getIdMovimientoRef() > 0);
    Assert.assertTrue("[Movimiento 3] NumError == 0", movimiento3.getNumError().equals("0"));
    Assert.assertTrue("[Movimiento 3] MsjError = vacio", StringUtils.isBlank(movimiento3.getMsjError()));
    verificaAcumuladores(user3.getIdCuentaInterno(), SOLICITUD_CARGA_WEB, 65000, 1, 0);

  }


  /***********************************************
   * PRUENA DOBLE CONFIRMACION PMC
   * PRIMERA CARGA
   * VERIFICACION DE LIMITES
   * @throws SQLException
   **********************************************/
  @Test
  public void dobleConfirmacionPrimeraCarga() throws SQLException {
    Movimiento movimiento;
    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    //================================================================================
    // 3 -Monto 	$45000   // Deberia pasar sin errores.
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_PRIMERA_CARGA, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud primera carga", new BigDecimal(45000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    int idMovOriginal = movimiento.getIdMovimientoRef();
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    // CONFIRMACION PRIMERA CARGA
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_PRIMERA_CARGA);
    movimiento.setGlosa("Confirmacion Primera Carga");
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // CONFIRMACION PRIMERA CARGA REPETIDA
    movimiento.setIdMovimientoRef(idMovOriginal);
    movimiento = callCreaMovimientoCuenta(movimiento);
    System.out.println("dobleConfirmacionPrimeraCarga: "+movimiento.getMsjError());
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertFalse("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertFalse("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // Verificar Acumuladores Primera Carga
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_PRIMERA_CARGA, 45000L, 1L, 0L);
  }

  /***********************************************
   *Test confirmación seguido de reversa
   * PRIMERA CARGA
   * VERIFICACION DE LIMITES
   * @throws SQLException
   **********************************************/
  @Test
  public void confirmacionPrimeraCargaConfReversa() throws SQLException {
    Movimiento movimiento;
    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();
    //================================================================================
    // 3 -Monto 	$45000   // Deberia pasar sin errores.
    //================================================================================
    movimiento = new Movimiento(SOLICITUD_PRIMERA_CARGA, cuentaUsuario.getIdCuentaExterno(), 0, getRandomNumericString(20), "Solicitud primera carga", new BigDecimal(45000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);
    int idMovOriginal = movimiento.getIdMovimientoRef();
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));
    // CONFIRMACION PRIMERA CARGA
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_PRIMERA_CARGA);
    movimiento.setGlosa("Confirmacion Primera Carga");
    movimiento = callCreaMovimientoCuenta(movimiento);
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() > 0);
    Assert.assertTrue("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertTrue("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // CONFIRMACION PRIMERA CARGA REPETIDA
    movimiento.setIdFaseMovimiento(CONFIRMACIÓN_REVERSA_CARGA);
    movimiento.setIdMovimientoRef(idMovOriginal);
    movimiento = callCreaMovimientoCuenta(movimiento);
    System.out.println("confirmacionPrimeraCargaConfReversa: "+movimiento.getMsjError());
    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertFalse("NumError = 0", movimiento.getNumError().equals("0"));
    Assert.assertFalse("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

    // Verificar Acumuladores Primera Carga
    verificaAcumuladores(cuentaUsuario.getIdCuentaInterno(), SOLICITUD_PRIMERA_CARGA, 45000L, 1L, 0L);
  }

  /***********************************************
   *ID Externo Vacio
   * PRIMERA CARGA
   * VERIFICACION DE LIMITES
   * @throws SQLException
   **********************************************/
  @Test
  public void idExternoVacio() throws SQLException {
    Movimiento movimiento;
    CuentaUsuarioCdt cuentaUsuario = creaCuentaUsuarioCdt();

    movimiento = new Movimiento(SOLICITUD_PRIMERA_CARGA, cuentaUsuario.getIdCuentaExterno(), 0, "", "Solicitud primera carga", new BigDecimal(45000), false);
    movimiento = callCreaMovimientoCuenta(movimiento);

    Assert.assertTrue("Id Movimiento Cta debe ser > 0", movimiento.getIdMovimientoRef() == 0);
    Assert.assertTrue("NumError = MC003", movimiento.getNumError().equals("MC003"));
    Assert.assertFalse("MsjError = vacio", StringUtils.isBlank(movimiento.getMsjError()));

  }

  private void verificaAcumuladores(long idCuenta, long idFaseMovimiento, long montoSuma, long countMovimientos, long montoTx) {
    List lstAcumuladores = getCuentaAcumulador(idCuenta, idFaseMovimiento);
    System.out.println(lstAcumuladores);
    //================================================================================
    // 4 -Verifica los Acumuladores
    //================================================================================
    for (Object data : lstAcumuladores) {
      Map<String, Object> aData = (Map<String, Object>) data;
      if (((String) aData.get("CODOPE")).equalsIgnoreCase("SUM") && !((String) aData.get("PERIOCIDAD")).equalsIgnoreCase("VIDA")) {
        Assert.assertEquals("MsjError != vacio", aData.get("MONTO"), new BigDecimal(montoSuma));
      } else if (((String) aData.get("CODOPE")).equalsIgnoreCase("COUNT")) {
        Assert.assertEquals("MsjError != vacio", aData.get("MONTO"), new BigDecimal(countMovimientos));
      } else if (((String) aData.get("CODOPE")).equalsIgnoreCase("SUM") || ((String) aData.get("PERIOCIDAD")).equalsIgnoreCase("VIDA")) {
        Assert.assertEquals("MsjError != vacio", aData.get("MONTO"), new BigDecimal(montoTx));
      }
    }
  }


  private List getCuentaAcumulador(long idCuenta, long idFase) {
    String sQuery;
    sQuery =
      "select\n" +
        " CAC.id AS ID,\n" +
        "     RAC.periocidad AS PERIOCIDAD,\n" +
        "     CAC.monto AS MONTO,\n" +
        "     CAC.codigo_operacion AS CODOPE\n" +
        " from\n" +
        " " + SCHEMA_CDT + "." + Constants.Tables.CUENTA_ACUMULADOR.getName() + " cac \n" +
        " inner join " + SCHEMA_CDT + "." + Constants.Tables.REGLA_ACUMULACION.getName() + " rac on rac.id = cac.id_regla_acumulacion\n" +
        " inner join " + SCHEMA_CDT + "." + Constants.Tables.FASE_ACUMULADOR.getName() + " fac on fac.id_regla_acumulacion = cac.id_regla_acumulacion and\n" +
        " fac.id_regla_acumulacion = rac.id\n" +
        " where\n" +
        " cac.id_cuenta = " + idCuenta + " and \n" +
        " fac.id_fase_movimiento = " + idFase + " ";
    return jdbcTempate.queryForList(
      sQuery
    );
  }

  private Movimiento callCreaMovimientoCuenta(Movimiento movimiento) throws SQLException {
    Object[] params = {
      movimiento.getIdCuenta(),
      movimiento.getIdFaseMovimiento(),
      movimiento.getIdMovimientoRef(),
      movimiento.getIdExterno(),
      movimiento.getGlosa(),
      movimiento.getMonto(),
      movimiento.isbSimulacion() ? "S" : "N",
      new OutParam("_id_movimiento_cuenta", Types.NUMERIC),
      new OutParam("_numerror", Types.VARCHAR),
      new OutParam("_msjerror", Types.VARCHAR)};

    Map<String, Object> outputData = dbUtils.execute(SCHEMA_CDT + Constants.Procedures.SP_CREA_MOVIMIENTO_CUENTA.getName(), params);

    BigDecimal id_movimiento_cuenta = (BigDecimal) outputData.get("_id_movimiento_cuenta");
    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");
    System.out.println("[MOVIMIENTO_CUENTA/CALL_CREA_MOVIMIENTO_CUENTA] NumError: " + numError + " MsjError: " + msjError);
    movimiento.setNumError(numError);
    movimiento.setMsjError(msjError);

    if (numError.equals("0")) {
      movimiento.setIdMovimientoRef(id_movimiento_cuenta.intValue());
    } else {
      movimiento.setIdMovimientoRef(0);
    }
    return movimiento;
  }

  private CuentaUsuarioCdt creaCuentaUsuarioCdt() throws SQLException {
    //================================================================================
    // CREA CUENTA
    //================================================================================
    String cuentaUsuario = "PREPAGO" + getUniqueRutNumber();
    String descCuenta = "Nueva cuenta " + cuentaUsuario;

    Object[] params = {cuentaUsuario, descCuenta, new OutParam("_id_cuenta", Types.NUMERIC), new OutParam("_numerror", Types.VARCHAR), new OutParam("_msjerror", Types.VARCHAR)};

    Map<String, Object> outputData = dbUtils.execute(SCHEMA_CDT + Constants.Procedures.SP_CREA_CUENTA.getName(), params);

    BigDecimal idCuenta = (BigDecimal) outputData.get("_id_cuenta");
    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");
    System.out.println("[MOVIMIENTO_CUENTA/CREA_CUENTA] NumError: " + numError + " MsjError: " + msjError);
    // Pruebas de Creacion de Cuenta.
    Assert.assertTrue("[Crea Cuenta] Numero de cuenta debe ser < 0", idCuenta.intValue() > 0);
    Assert.assertTrue("[Crea Cuenta] Numero de error 0 creacion correcta", numError.equals("0"));
    Assert.assertTrue("[Crea Cuenta] Msj de error vacio creacion correcta", StringUtils.isBlank(msjError));
    if (numError.equals("0")) {
      CuentaUsuarioCdt cuentaUsuarioCdt = new CuentaUsuarioCdt();
      cuentaUsuarioCdt.setIdCuentaExterno(cuentaUsuario);
      cuentaUsuarioCdt.setIdCuentaInterno(idCuenta.longValue());
      return cuentaUsuarioCdt;
    }
    return new CuentaUsuarioCdt();
  }


  private static int SOLICITUD_PRIMERA_CARGA = 1;
  private static int CONFIRMACIÓN_PRIMERA_CARGA = 2;
  private static int SOLICITUD_REVERSA_PRIMERA_CARGA = 3;
  private static int CONFIRMACIÓN_REVERSA_PRIMERA_CARGA = 4;
  private static int SOLICITUD_CARGA_WEB = 5;
  private static int CONFIRMACIÓN_CARGA_WEB = 6;
  private static int SOLICITUD_CARGA_POS = 7;
  private static int CONFIRMACIÓN_CARGA_POS = 8;
  private static int SOLICITUD_REVERSA_CARGA = 9;
  private static int CONFIRMACIÓN_REVERSA_CARGA = 10;
  private static int SOLICITUD_RETIRO_WEB = 11;
  private static int CONFIRMACIÓN_RETIRO_WEB = 12;
  private static int SOLICITUD_RETIRO_POS = 13;
  private static int CONFIRMACIÓN_RETIRO_POS = 14;
  private static int SOLICITUD_REVERSA_DE_RETIRO = 15;
  private static int CONFIRMACION_REVERSA_DE_RETIRO = 16;

}


class CuentaUsuarioCdt{
  private String idCuentaExterno;
  private Long idCuentaInterno;

  public String getIdCuentaExterno() {
    return idCuentaExterno;
  }

  public void setIdCuentaExterno(String idCuentaExterno) {
    this.idCuentaExterno = idCuentaExterno;
  }

  public Long getIdCuentaInterno() {
    return idCuentaInterno;
  }

  public void setIdCuentaInterno(Long idCuentaInterno) {
    this.idCuentaInterno = idCuentaInterno;
  }
}

class Movimiento {

  private Integer idFaseMovimiento;
  private String idCuenta;
  private Integer idMovimientoRef;
  private String idExterno;
  private String glosa;
  private BigDecimal monto;
  private boolean bSimulacion;

  private String numError;
  private String msjError;

  public Movimiento() {
  }

  public Movimiento(Integer idFaseMovimiento, String idCuenta, Integer idMovimientoRef, String idExterno, String glosa, BigDecimal monto, boolean bSimulacion) {
    this.idFaseMovimiento = idFaseMovimiento;
    this.idCuenta = idCuenta;
    this.idMovimientoRef = idMovimientoRef;
    this.idExterno = idExterno;
    this.glosa = glosa;
    this.monto = monto;
    this.bSimulacion = bSimulacion;
  }

  public String getIdCuenta() {
    return idCuenta;
  }

  public Integer getIdFaseMovimiento() {
    return idFaseMovimiento;
  }

  public void setIdFaseMovimiento(Integer idFaseMovimiento) {
    this.idFaseMovimiento = idFaseMovimiento;
  }

  public void setIdCuenta(String idCuenta) {
    this.idCuenta = idCuenta;
  }

  public Integer getIdMovimientoRef() {
    return idMovimientoRef;
  }

  public void setIdMovimientoRef(Integer idMovimientoRef) {
    this.idMovimientoRef = idMovimientoRef;
  }

  public String getIdExterno() {
    return idExterno;
  }

  public void setIdExterno(String idExterno) {
    this.idExterno = idExterno;
  }

  public String getGlosa() {
    return glosa;
  }

  public void setGlosa(String glosa) {
    this.glosa = glosa;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal monto) {
    this.monto = monto;
  }

  public boolean isbSimulacion() {
    return bSimulacion;
  }

  public void setbSimulacion(boolean bSimulacion) {
    this.bSimulacion = bSimulacion;
  }

  public String getNumError() {
    return numError;
  }

  public void setNumError(String numError) {
    this.numError = numError;
  }

  public String getMsjError() {
    return msjError;
  }

  public void setMsjError(String msjError) {
    this.msjError = msjError;
  }
}
