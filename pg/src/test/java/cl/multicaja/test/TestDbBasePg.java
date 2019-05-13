package cl.multicaja.test;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.ConfigUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * @autor vutreras
 */
public class TestDbBasePg extends TestDbBase {

  protected static ConfigUtils configUtils = ConfigUtils.getInstance();

  protected static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");

  protected static final String SCHEMA_ACCOUNTING = ConfigUtils.getInstance().getProperty("schema.acc");

  protected static final String SCHEMA_PARAMETERS = ConfigUtils.getInstance().getProperty("schema.parameters");

  protected static final String SCHEMA_CDT = ConfigUtils.getInstance().getProperty("schema.cdt");

  private static String INSERT_PREPAID_CARD = String.format("INSERT INTO %s.prp_tarjeta(\n" +
    "            pan, pan_encriptado, estado, \n" +
    "            nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, \n" +
    "            uuid, pan_hash, id_cuenta,expiracion)\n" +
    "    VALUES (?, ?, ?, ?,\n" +
    "            ?, ?, ?, ?,\n" +
    "            ?, ?, ?,?);\n",SCHEMA);

  private static final String INSERT_MOVEMENT_SQL
    = String.format("INSERT INTO %s.prp_movimiento (id_movimiento_ref, id_tarjeta, id_tx_externo, tipo_movimiento, monto, " +
    "estado, estado_de_negocio, estado_con_switch,estado_con_tecnocom,origen_movimiento,fecha_creacion,fecha_actualizacion," +
    "codent,centalta,cuenta,clamon,indnorcor,tipofac,fecfac,numreffac,pan,clamondiv,impdiv,impfac,cmbapli,numaut,indproaje," +
    "codcom,codact,impliq,clamonliq,codpais,nompob,numextcta,nummovext,clamone,tipolin,linref,numbencta,numplastico,nomcomred) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", SCHEMA);

  private static final String INSERT_ACCOUNT_SQL
    = String.format("INSERT INTO %s.prp_cuenta (id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion) VALUES(?, ?, ?, ?, ?, ?, ?, ?);", SCHEMA);

  private static final String INSERT_USER = String.format("INSERT INTO prepago.prp_usuario(\n" +
    "            estado, fecha_creacion, fecha_actualizacion, nombre, \n" +
    "            apellido, numero_documento, tipo_documento, nivel, uuid,plan)\n" +
    "    VALUES (?, ?, ?, \n" +
    "            ?, ?, ?, ?, \n" +
    "            ?, ?, ?);\n", SCHEMA);

  public Long insertRandomUser(String status){
    KeyHolder keyHolder = new GeneratedKeyHolder();
    dbUtils.getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection.prepareStatement(INSERT_USER, new String[] {"id"});
      ps.setString(1, status);
      ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setString(4, getRandomString(10));
      ps.setString(5, getRandomString(10));
      ps.setString(6, getRandomString(10));
      ps.setString(7,getRandomString(10));
      ps.setString(8, getRandomString(10));
      ps.setString(9, UUID.randomUUID().toString());
      ps.setString(10, getRandomString(10));
      return ps;
    }, keyHolder);
    return (long) keyHolder.getKey();
  }


  public Long insertRandomAccount(String status) {
    Long userId = insertRandomUser("ACTIVE");

    KeyHolder keyHolder = new GeneratedKeyHolder();
    dbUtils.getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_ACCOUNT_SQL, new String[] {"id"});
      ps.setLong(1, userId);
      ps.setString(2, getRandomString(10));
      ps.setString(3, getRandomString(10));
      ps.setString(4, "");
      ps.setLong(5, 0L);
      ps.setString(6, status);
      ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      return ps;
    }, keyHolder);
    return  (long) keyHolder.getKey();
  }

  public Long insertRandomCard(String status) throws Exception{

    Long idCuenta = insertRandomAccount("ACTIVE");

    KeyHolder keyHolder = new GeneratedKeyHolder();
    dbUtils.getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_PREPAID_CARD, new String[] {"id"});
      ps.setString(1, getRandomString(10));
      ps.setString(2, getRandomString(10));
      ps.setString(3, getRandomString(10));
      ps.setString(4, getRandomString(10));
      ps.setString(5, getRandomString(2));
      ps.setString(6, getRandomString(8));
      ps.setTimestamp(7,Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setString(9, UUID.randomUUID().toString());
      ps.setString(10,getRandomString(10));
      ps.setLong(11, idCuenta);
      ps.setInt(12,1111);
      return ps;
    }, keyHolder);
      return  (long) keyHolder.getKey();
  }

  public Long insertRandomMovement() throws Exception {

    Long idTarjeta = insertRandomCard("ACTIVE");

    KeyHolder keyHolder = new GeneratedKeyHolder();

    dbUtils.getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_MOVEMENT_SQL, new String[] {"id"});
      ps.setLong(1,0);
      ps.setLong(2,idTarjeta);
      ps.setString(3, getRandomString(1));
      ps.setString(4, getRandomString(1));
      ps.setBigDecimal(5, BigDecimal.TEN);
      ps.setString(6, getRandomString(1));
      ps.setString(7, getRandomString(1));
      ps.setString(8, getRandomString(1));
      ps.setString(9, getRandomString(1));
      ps.setString(10, getRandomString(1));
      ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setString(13, getRandomString(1));
      ps.setString(14, getRandomString(1));
      ps.setString(15, getRandomString(1));
      ps.setObject(16, 152);
      ps.setObject(17, 1);
      ps.setObject(18, 311);
      ps.setDate(19, new java.sql.Date(System.currentTimeMillis()));
      ps.setString(20,"");
      ps.setString(21, "");
      ps.setLong(22, 123);
      ps.setBigDecimal(23, BigDecimal.TEN);
      ps.setBigDecimal(24, BigDecimal.TEN);
      ps.setLong(25, 0);
      ps.setString(26, getRandomNumericString(1));
      ps.setObject(27,"");
      ps.setString(28, "");
      ps.setLong(29, 0);
      ps.setBigDecimal(30, BigDecimal.TEN);
      ps.setLong(31, 0);
      ps.setObject(32, 123);
      ps.setString(33, "");
      ps.setLong(34, 0);
      ps.setLong(35, 0);
      ps.setLong(36, 0);
      ps.setString(37, "");
      ps.setLong(38,0);
      ps.setLong(39, 0);
      ps.setLong(40, 0);
      ps.setString(41, "");
      return ps;
    }, keyHolder);
    return (long) keyHolder.getKey();
  }

}
