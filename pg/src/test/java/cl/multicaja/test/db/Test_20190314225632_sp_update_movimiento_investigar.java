package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import cl.multicaja.test.model.InformationFilesModelObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class Test_20190314225632_sp_update_movimiento_investigar extends TestDbBasePg {

  private static final String SP_UPDATE_RESEARCH_MOVEMENT_NAME = SCHEMA + ".mc_prp_actualiza_movimiento_investigar_v10";
  private static final String SP_SEARCH_RESEARCH_MOVEMENT_NAME = SCHEMA + ".mc_prp_busca_movimientos_a_investigar_v13";

  @Before
  public void before() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  @After
  public void after() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  private ObjectMapper objectMapper = null;

  protected ObjectMapper getObjectMapper(){
    if (this.objectMapper == null) {
      this.objectMapper = new ObjectMapper();
    }
    return this.objectMapper;
  }

  protected String toJson(Object obj) throws JsonProcessingException {
    return this.getObjectMapper().writeValueAsString(obj);
  }

  public Map<String, Object> searchResearchMovement(
    Long id, Timestamp beginDateTime, Timestamp endDateTime, String sentStatus, BigDecimal movRef) throws SQLException {

    Object[] params = {
      id != null ? new InParam(id, Types.BIGINT) : new NullParam(Types.BIGINT),
      beginDateTime != null ? new InParam(beginDateTime, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      endDateTime != null ? new InParam(endDateTime, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      sentStatus != null ? new InParam(sentStatus, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      movRef != null ? new InParam(movRef, Types.DECIMAL) : new NullParam(Types.DECIMAL)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ResearchMovement researchMovement = new ResearchMovement();
      researchMovement.id = numberUtils.toLong(row.get("_id"));
      researchMovement.informacionArchivos = String.valueOf(row.get("_informacion_archivos"));
      researchMovement.origen = String.valueOf(row.get("_origen"));
      researchMovement.fechaRegistro = (Timestamp) row.get("_fecha_registro");
      researchMovement.fechaDeTransaccion = (Timestamp) row.get("_fecha_de_transaccion");
      researchMovement.responsable = String.valueOf(row.get("_responsable"));
      researchMovement.descripcion = String.valueOf(row.get("_descripcion"));
      researchMovement.movRef = BigDecimal.valueOf(numberUtils.toDouble(row.get("_mov_ref")));
      researchMovement.tipoMovimiento = String.valueOf(row.get("_tipo_movimiento"));
      researchMovement.sentStatus = String.valueOf(row.get("_sent_status"));

      return researchMovement;
    };

    return dbUtils.execute(SP_SEARCH_RESEARCH_MOVEMENT_NAME, rm, params);
  }


  public Map<String, Object> updateResearchMovement(Long id,String sentStatus) throws SQLException{
    Object[] params = {
      id != null ? new InParam(id, Types.BIGINT) : new NullParam(Types.BIGINT),
      sentStatus != null ? new InParam(sentStatus, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return dbUtils.execute(SP_UPDATE_RESEARCH_MOVEMENT_NAME,params);
  }


  @Test
  public void testUpdateById() throws SQLException,JsonProcessingException{

    InformationFilesModelObject informationFilesModelObjectSent = new InformationFilesModelObject();
    informationFilesModelObjectSent.setIdArchivo(Long.valueOf(1));
    informationFilesModelObjectSent.setIdEnArchivo("idEnArchivi_1");
    informationFilesModelObjectSent.setNombreArchivo("nombreArchivo_1");
    informationFilesModelObjectSent.setTipoArchivo("tipoArchivo_1");

    String jsonSent = this.toJson(informationFilesModelObjectSent);

    Timestamp fechaDeTransaccion = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
    String origen = getRandomString(10);
    String responsable = getRandomString(10);
    String descripcion = getRandomString(10);
    Long movRef = 100L;
    String tipoMovimiento = getRandomString(10);
    String sentStatus = "SENT_PENDING";
    String sentStatusChng = "SENT_OK";

    Map<String, Object> respCreate = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(jsonSent,origen,
      fechaDeTransaccion,responsable,descripcion,
      movRef,tipoMovimiento,sentStatus);

    Assert.assertNotNull("Data no debe ser null", respCreate);
    Assert.assertEquals("Debe ser 0","0",respCreate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",respCreate.get("_error_msg"));

    Long idForUpdate = numberUtils.toLong(respCreate.get("_r_id"));

    Map<String, Object> respUpdate = updateResearchMovement(idForUpdate,sentStatusChng);
    Assert.assertNotNull("Data no debe ser null", respUpdate);
    Assert.assertEquals("Debe ser 0","0",respUpdate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",respUpdate.get("_error_msg"));

    Map<String, Object> data = searchResearchMovement( idForUpdate,null,null, null,null);
    List<Map<String, Object>> results = (List)data.get("result");

    Assert.assertEquals("Se debe encontrar un solo registro ",1,results.size());
    ResearchMovement researchMovement = (ResearchMovement) results.get(0);

    Assert.assertNotNull("No esta vacio ",researchMovement.informacionArchivos);
    Assert.assertEquals("El sentStatus es el mismo ",sentStatusChng,researchMovement.sentStatus);

  }

  @Test
  public void testUpdateByNotFoundId() throws SQLException,JsonProcessingException{
    InformationFilesModelObject informationFilesModelObjectSent = new InformationFilesModelObject();
    informationFilesModelObjectSent.setIdArchivo(Long.valueOf(1));
    informationFilesModelObjectSent.setIdEnArchivo("idEnArchivi_1");
    informationFilesModelObjectSent.setNombreArchivo("nombreArchivo_1");
    informationFilesModelObjectSent.setTipoArchivo("tipoArchivo_1");

    String jsonSent = this.toJson(informationFilesModelObjectSent);

    Timestamp fechaDeTransaccion = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
    String origen = getRandomString(10);
    String responsable = getRandomString(10);
    String descripcion = getRandomString(10);
    Long movRef = 101L;
    String tipoMovimiento = getRandomString(10);
    String sentStatus = "SENT_PENDING";
    String sentStatusChng = "SENT_OK";

    Map<String, Object> respCreate = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(jsonSent,origen,
      fechaDeTransaccion,responsable,descripcion,
      movRef,tipoMovimiento,sentStatus);

    Assert.assertNotNull("Data no debe ser null", respCreate);
    Assert.assertEquals("Debe ser 0","0",respCreate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",respCreate.get("_error_msg"));

    Long idForUpdate = Long.valueOf(100000);

    Map<String, Object> respUpdate = updateResearchMovement(idForUpdate,sentStatusChng);
    Assert.assertNotNull("Data no debe ser null", respUpdate);
    Assert.assertEquals("Debe ser 0","101000",respUpdate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","[mc_prp_actualiza_movimiento_investigar_v10] El id no se encuentra, el registro no se pudo actualizar",respUpdate.get("_error_msg"));

  }

  @Test
  public void testUpdateByInvalidId() throws SQLException,JsonProcessingException{
    InformationFilesModelObject informationFilesModelObjectSent = new InformationFilesModelObject();
    informationFilesModelObjectSent.setIdArchivo(Long.valueOf(1));
    informationFilesModelObjectSent.setIdEnArchivo("idEnArchivi_1");
    informationFilesModelObjectSent.setNombreArchivo("nombreArchivo_1");
    informationFilesModelObjectSent.setTipoArchivo("tipoArchivo_1");

    String jsonSent = this.toJson(informationFilesModelObjectSent);

    Timestamp fechaDeTransaccion = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
    String origen = getRandomString(10);
    String responsable = getRandomString(10);
    String descripcion = getRandomString(10);
    Long movRef = 101L;
    String tipoMovimiento = getRandomString(10);
    String sentStatus = "SENT_PENDING";
    String sentStatusChng = "SENT_OK";

    Map<String, Object> respCreate = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(jsonSent,origen,
      fechaDeTransaccion,responsable,descripcion,
      movRef,tipoMovimiento,sentStatus);

    Assert.assertNotNull("Data no debe ser null", respCreate);
    Assert.assertEquals("Debe ser 0","0",respCreate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",respCreate.get("_error_msg"));

    Long idForUpdate = Long.valueOf(-1);

    Map<String, Object> respUpdate = updateResearchMovement(idForUpdate,sentStatusChng);
    Assert.assertNotNull("Data no debe ser null", respUpdate);
    Assert.assertEquals("Debe ser 0","101000",respUpdate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","[mc_prp_actualiza_movimiento_investigar_v10] El id no se encuentra, el registro no se pudo actualizar",respUpdate.get("_error_msg"));

  }

  class ResearchMovement {
    private Long id;
    private String informacionArchivos;
    private String origen;
    private Timestamp fechaRegistro;
    private Timestamp fechaDeTransaccion;
    private String responsable;
    private String descripcion;
    private BigDecimal movRef;
    private String tipoMovimiento;
    private String sentStatus;

    public ResearchMovement() {
      super();
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

  }

}
