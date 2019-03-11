package cl.multicaja.test.db;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import cl.multicaja.test.model.InformationFilesModelObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Test_20190305195605_create_sp_busca_movimientos_a_investigar extends TestDbBasePg {

  // Historial de modificación
  // 20181010114034_create_sp_busca_movimientos_a_investigar.sql
  // 20190222153542_create_sp_busca_movimientos_a_investigar_v11.sql
  // 20181010114035_create_sp_busca_movimientos_a_investigar_v12.sql
  // 20190305195605_create_sp_busca_movimientos_a_investigar_v13.sql

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

  private List<InformationFilesModelObject> toList(String json, Object object ) throws IOException {

    TypeFactory typeFactory = this.getObjectMapper().getTypeFactory();
    CollectionType collectionType = typeFactory.constructCollectionType(
      List.class, object.getClass());
    return this.getObjectMapper().readValue(json,collectionType);
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

  @Test
  public void testSearchResearchMovementById_BeginDateTime_EndDateTime_SentStatus_MovRef() throws SQLException,IOException {

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
    String sentStatus = "PENDING";

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    endDateTime = endDateTime.plusHours(1);

    Map<String, Object> resp = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(jsonSent,origen,
      fechaDeTransaccion,responsable,descripcion,
      movRef,tipoMovimiento,sentStatus);

    Assert.assertNotNull("Data no debe ser null", resp);
    Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));

    Map<String, Object> data = searchResearchMovement( numberUtils.toLong(resp.get("_r_id")),
      Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), sentStatus,numberUtils.toBigDecimal(movRef));

    List<Map<String, Object>> results = (List)data.get("result");

    Assert.assertEquals("Se debe encontrar un solo registro ",1,results.size());
    ResearchMovement researchMovement = (ResearchMovement) results.get(0);

    Assert.assertNotNull("No esta vacio ",researchMovement.informacionArchivos);
    Assert.assertEquals("El Json de informacionArchivos es el mismo ",jsonSent,researchMovement.informacionArchivos);
    Assert.assertEquals("El origen es el mismo ",origen,researchMovement.origen);
    Assert.assertEquals("El fechaDeTransaccion es el mismo ",fechaDeTransaccion,researchMovement.fechaDeTransaccion);
    Assert.assertEquals("El responsable es el mismo ",responsable,researchMovement.responsable);
    Assert.assertEquals("El descripcion es el mismo ",descripcion,researchMovement.descripcion);
    Long movRefReturn = researchMovement.movRef.longValue();
    Assert.assertEquals("El movRef es el mismo ",movRef,movRefReturn);
    Assert.assertEquals("El tipoMovimiento es el mismo ",tipoMovimiento,researchMovement.tipoMovimiento);
    Assert.assertEquals("El sentStatus es el mismo ",sentStatus,researchMovement.sentStatus);

  }

  @Test
  public void testSearchResearchMovementById_BeginDateTime_EndDateTime_SentStatus() throws SQLException,IOException {

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
    String sentStatus = "PENDING";

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    endDateTime = endDateTime.plusHours(1);

    Map<String, Object> resp = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(jsonSent,origen,
      fechaDeTransaccion,responsable,descripcion,
      movRef,tipoMovimiento,sentStatus);

    Assert.assertNotNull("Data no debe ser null", resp);
    Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));

    Map<String, Object> data = searchResearchMovement( numberUtils.toLong(resp.get("_r_id")),
      Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), sentStatus,null);
    List<Map<String, Object>> results = (List)data.get("result");

    Assert.assertEquals("Se debe encontrar un solo registro ",1,results.size());
    ResearchMovement researchMovement = (ResearchMovement) results.get(0);

    Assert.assertNotNull("No esta vacio ",researchMovement.informacionArchivos);
    Assert.assertEquals("El Json de informacionArchivos es el mismo ",jsonSent,researchMovement.informacionArchivos);
    Assert.assertEquals("El origen es el mismo ",origen,researchMovement.origen);
    Assert.assertEquals("El fechaDeTransaccion es el mismo ",fechaDeTransaccion,researchMovement.fechaDeTransaccion);
    Assert.assertEquals("El responsable es el mismo ",responsable,researchMovement.responsable);
    Assert.assertEquals("El descripcion es el mismo ",descripcion,researchMovement.descripcion);
    Long movRefReturn = researchMovement.movRef.longValue();
    Assert.assertEquals("El movRef es el mismo ",movRef,movRefReturn);
    Assert.assertEquals("El tipoMovimiento es el mismo ",tipoMovimiento,researchMovement.tipoMovimiento);
    Assert.assertEquals("El sentStatus es el mismo ",sentStatus,researchMovement.sentStatus);

  }

  @Test
  public void testSearchResearchMovementBy_Id() throws SQLException,IOException {

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
    String sentStatus = "PENDING";

    Map<String, Object> resp = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(jsonSent,origen,
      fechaDeTransaccion,responsable,descripcion,
      movRef,tipoMovimiento,sentStatus);

    Assert.assertNotNull("Data no debe ser null", resp);
    Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));

    Map<String, Object> data = searchResearchMovement( numberUtils.toLong(resp.get("_r_id")),
      null,null, null,null);
    List<Map<String, Object>> results = (List)data.get("result");

    Assert.assertEquals("Se debe encontrar un solo registro ",1,results.size());
    ResearchMovement researchMovement = (ResearchMovement) results.get(0);

    Assert.assertNotNull("No esta vacio ",researchMovement.informacionArchivos);
    Assert.assertEquals("El Json de informacionArchivos es el mismo ",jsonSent,researchMovement.informacionArchivos);
    Assert.assertEquals("El origen es el mismo ",origen,researchMovement.origen);
    Assert.assertEquals("El fechaDeTransaccion es el mismo ",fechaDeTransaccion,researchMovement.fechaDeTransaccion);
    Assert.assertEquals("El responsable es el mismo ",responsable,researchMovement.responsable);
    Assert.assertEquals("El descripcion es el mismo ",descripcion,researchMovement.descripcion);
    Long movRefReturn = researchMovement.movRef.longValue();
    Assert.assertEquals("El movRef es el mismo ",movRef,movRefReturn);
    Assert.assertEquals("El tipoMovimiento es el mismo ",tipoMovimiento,researchMovement.tipoMovimiento);
    Assert.assertEquals("El sentStatus es el mismo ",sentStatus,researchMovement.sentStatus);

  }

  @Test
  public void testSearchResearchMovementBy_BeginDateTime_EndDateTime() throws SQLException,IOException {

    ResearchMovement researchMovement = null;
    List<ResearchMovement> researchMovements = new ArrayList<>();

    InformationFilesModelObject informationFilesModelObjectSent = new InformationFilesModelObject();
    informationFilesModelObjectSent.setIdArchivo(Long.valueOf(1));
    informationFilesModelObjectSent.setIdEnArchivo("idEnArchivi_1");
    informationFilesModelObjectSent.setNombreArchivo("nombreArchivo_1");
    informationFilesModelObjectSent.setTipoArchivo("tipoArchivo_1");

    String jsonSent = this.toJson(informationFilesModelObjectSent);
    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));

    String sentStatus = "PENDING_TEST_1";
    Long numRecords = 10L;

    for(int i=0;i<numRecords;i++){

      endDateTime = endDateTime.plusHours(1);

      researchMovement = new ResearchMovement();
      researchMovement.setFechaDeTransaccion(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
      researchMovement.setInformacionArchivos(jsonSent);
      researchMovement.setOrigen(getRandomString(10));
      researchMovement.setResponsable(getRandomString(10));
      researchMovement.setDescripcion(getRandomString(10));
      researchMovement.setMovRef(BigDecimal.valueOf(100));
      researchMovement.setTipoMovimiento(getRandomString(10));
      researchMovement.setSentStatus(sentStatus);

      Map<String, Object> resp = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(
        jsonSent,
        researchMovement.getOrigen(),
        researchMovement.getFechaDeTransaccion(),
        researchMovement.getResponsable(),
        researchMovement.getDescripcion(),
        researchMovement.getMovRef().longValue(),
        researchMovement.getTipoMovimiento(),
        sentStatus);

      researchMovement.setId(numberUtils.toLong(resp.get("_r_id")));
      researchMovements.add(researchMovement);

      Assert.assertNotNull("Data no debe ser null", resp);
      Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));
    }

    Map<String, Object> data = searchResearchMovement( null,
      Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), null,null);

    List<Map<String, Object>> results = (List)data.get("result");

    Long resultRecords = Long.valueOf(results.size());
    Assert.assertEquals("Se debe encontrar "+numRecords+" registros",numRecords,resultRecords);
    System.out.println("testSearchResearchMovement_by_beginDateTime_endDateTime_sentStatus: "+results.size());

    for(int i=0; i<resultRecords;i++){
      researchMovements.sort(new IdSorterDesc());
      ResearchMovement rmSent = researchMovements.get(i);
      ResearchMovement rmReturn = (ResearchMovement) results.get(i);

      Assert.assertNotNull("No esta vacio ",rmReturn.informacionArchivos);
      Assert.assertEquals("El Json de informacionArchivos es el mismo ",jsonSent,rmReturn.informacionArchivos);
      Assert.assertEquals("El origen es el mismo ",rmSent.origen,rmReturn.origen);
      Assert.assertEquals("El fechaDeTransaccion es el mismo ",rmSent.fechaDeTransaccion,rmReturn.fechaDeTransaccion);
      Assert.assertEquals("El responsable es el mismo ",rmSent.responsable,rmReturn.responsable);
      Assert.assertEquals("El descripcion es el mismo ",rmSent.descripcion,rmReturn.descripcion);
      Assert.assertEquals("El movRef es el mismo ",rmSent.movRef.longValue(),rmReturn.movRef.longValue());
      Assert.assertEquals("El tipoMovimiento es el mismo ",rmSent.tipoMovimiento,rmReturn.tipoMovimiento);
      Assert.assertEquals("El sentStatus es el mismo ",rmSent.sentStatus,rmReturn.sentStatus);
    }

  }

  @Test
  public void testSearchResearchMovementBy_SentStatus() throws SQLException,IOException {

    ResearchMovement researchMovement = null;
    List<ResearchMovement> researchMovements = new ArrayList<>();

    InformationFilesModelObject informationFilesModelObjectSent = new InformationFilesModelObject();
    informationFilesModelObjectSent.setIdArchivo(Long.valueOf(1));
    informationFilesModelObjectSent.setIdEnArchivo("idEnArchivi_1");
    informationFilesModelObjectSent.setNombreArchivo("nombreArchivo_1");
    informationFilesModelObjectSent.setTipoArchivo("tipoArchivo_1");

    String jsonSent = this.toJson(informationFilesModelObjectSent);
    String sentStatus = "PENDING_TEST_2";
    Long numRecords = 10L;

    for(int i=0;i<numRecords;i++){

      researchMovement = new ResearchMovement();
      researchMovement.setFechaDeTransaccion(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
      researchMovement.setInformacionArchivos(jsonSent);
      researchMovement.setOrigen(getRandomString(10));
      researchMovement.setResponsable(getRandomString(10));
      researchMovement.setDescripcion(getRandomString(10));
      researchMovement.setMovRef(BigDecimal.valueOf(100));
      researchMovement.setTipoMovimiento(getRandomString(10));
      researchMovement.setSentStatus(sentStatus);

      Map<String, Object> resp = Test_20181009113614_create_sp_crea_movimiento_investigar.setResearchMovement(
        jsonSent,
        researchMovement.getOrigen(),
        researchMovement.getFechaDeTransaccion(),
        researchMovement.getResponsable(),
        researchMovement.getDescripcion(),
        researchMovement.getMovRef().longValue(),
        researchMovement.getTipoMovimiento(),
        sentStatus);

      researchMovement.setId(numberUtils.toLong(resp.get("_r_id")));
      researchMovements.add(researchMovement);

      Assert.assertNotNull("Data no debe ser null", resp);
      Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));
    }

    Map<String, Object> data = searchResearchMovement( null,
      null,null, sentStatus,null);
    List<Map<String, Object>> results = (List)data.get("result");

    Long resultRecords = Long.valueOf(results.size());
    Assert.assertEquals("Se debe encontrar "+numRecords+" registros",numRecords,resultRecords);
    System.out.println("testSearchResearchMovement_by_sentStatus: "+results.size());

    for(int i=0; i<numRecords;i++){
      researchMovements.sort(new IdSorterDesc());
      ResearchMovement rmSent = researchMovements.get(i);
      ResearchMovement rmReturn = (ResearchMovement) results.get(i);

      Assert.assertNotNull("No esta vacio ",rmReturn.informacionArchivos);
      Assert.assertEquals("El Json de informacionArchivos es el mismo ",jsonSent,rmReturn.informacionArchivos);
      Assert.assertEquals("El origen es el mismo ",rmSent.origen,rmReturn.origen);
      Assert.assertEquals("El fechaDeTransaccion es el mismo ",rmSent.fechaDeTransaccion,rmReturn.fechaDeTransaccion);
      Assert.assertEquals("El responsable es el mismo ",rmSent.responsable,rmReturn.responsable);
      Assert.assertEquals("El descripcion es el mismo ",rmSent.descripcion,rmReturn.descripcion);
      Assert.assertEquals("El movRef es el mismo ",rmSent.movRef.longValue(),rmReturn.movRef.longValue());
      Assert.assertEquals("El tipoMovimiento es el mismo ",rmSent.tipoMovimiento,rmReturn.tipoMovimiento);
      Assert.assertEquals("El sentStatus es el mismo ",rmSent.sentStatus,rmReturn.sentStatus);
    }

  }

  @Test
  public void testSearchResearchMovementBy_DatesOffRange() throws SQLException {

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));

    beginDateTime = beginDateTime.plusMonths(1);
    endDateTime = endDateTime.plusMonths(3);

    Map<String, Object> data = searchResearchMovement(null,
      Timestamp.valueOf(beginDateTime), Timestamp.valueOf(endDateTime), null,null);
    Assert.assertNull("El resultado es nulo ", data.get("result"));

  }

  @Test
  public void testSearchResearchMovementBy_InvalidId() throws SQLException {

    {
      Map<String, Object> data = searchResearchMovement(
        Long.valueOf(-1),null,null,null,null);
      Assert.assertNull("El resultado es nulo ", data.get("result"));
    }
    {
      Map<String, Object> data = searchResearchMovement(
        Long.valueOf(0),null,null,null,null);
      Assert.assertNull("El resultado es nulo ", data.get("result"));
    }

  }

  @Test
  public void testSearchResearchMovementBy_IdNull() throws SQLException {

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    String sentStatus = "PENDING_TEST_2";

    Map<String, Object> data = searchResearchMovement( null,
      Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), sentStatus, NumberUtils.getInstance().toBigDecimal(0L));
    Assert.assertNull("El resultado es nulo ",data.get("result"));

  }

  @Test
  public void testSearchResearchMovementBy_SentStatusNull() throws SQLException,IOException {

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    String sentStatus = "PENDING_TEST_2";

    Map<String, Object> data = searchResearchMovement( Long.valueOf(1),
      Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), null,numberUtils.toBigDecimal(100));
    Assert.assertNull("El resultado es nulo ",data.get("result"));

  }

  @Test
  public void testSearchResearchMovementBy_MovRefNull() throws SQLException,IOException {

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);
    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    String sentStatus = "PENDING_TEST_2";

    Map<String, Object> data = searchResearchMovement(
      Long.valueOf(1),Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), sentStatus,null);
    Assert.assertNull("El resultado es nulo ",data.get("result"));

  }

  @Test
  public void testSearchResearchMovementBy_AllNull() throws SQLException,IOException {

    Map<String, Object> data = searchResearchMovement( null,null,null, null,null);
    Assert.assertNull("El resultado es nulo ",data.get("result"));

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

    public ResearchMovement(
      Long id,
      String informacionArchivos,
      String origen,
      Timestamp fechaDeTransaccion,
      String responsable,
      String descripcion,
      BigDecimal movRef,
      String tipoMovimiento,
      String sentStatus){

      this.id = id;
      this.informacionArchivos = informacionArchivos;
      this.origen = origen;
      this.fechaDeTransaccion = fechaDeTransaccion;
      this.responsable = responsable;
      this.descripcion = descripcion;
      this.movRef = movRef;
      this.tipoMovimiento = tipoMovimiento;
      this.sentStatus = sentStatus;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getInformacionArchivos() {
      return informacionArchivos;
    }

    public void setInformacionArchivos(String informacionArchivos) {
      this.informacionArchivos = informacionArchivos;
    }

    public String getOrigen() {
      return origen;
    }

    public void setOrigen(String origen) {
      this.origen = origen;
    }

    public Timestamp getFechaRegistro() {
      return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
      this.fechaRegistro = fechaRegistro;
    }

    public Timestamp getFechaDeTransaccion() {
      return fechaDeTransaccion;
    }

    public void setFechaDeTransaccion(Timestamp fechaDeTransaccion) {
      this.fechaDeTransaccion = fechaDeTransaccion;
    }

    public String getResponsable() {
      return responsable;
    }

    public void setResponsable(String responsable) {
      this.responsable = responsable;
    }

    public String getDescripcion() {
      return descripcion;
    }

    public void setDescripcion(String descripcion) {
      this.descripcion = descripcion;
    }

    public BigDecimal getMovRef() {
      return movRef;
    }

    public void setMovRef(BigDecimal movRef) {
      this.movRef = movRef;
    }

    public String getTipoMovimiento() {
      return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
      this.tipoMovimiento = tipoMovimiento;
    }

    public String getSentStatus() {
      return sentStatus;
    }

    public void setSentStatus(String sentStatus) {
      this.sentStatus = sentStatus;
    }

    @Override
    public String toString() {
      return "ResearchMovement{" +
        "id=" + id +
        ", informacionArchivos='" + informacionArchivos + '\'' +
        ", origen='" + origen + '\'' +
        ", fechaRegistro=" + fechaRegistro +
        ", fechaDeTransaccion=" + fechaDeTransaccion +
        ", responsable='" + responsable + '\'' +
        ", descripcion='" + descripcion + '\'' +
        ", movRef=" + movRef +
        ", tipoMovimiento='" + tipoMovimiento + '\'' +
        ", sentStatus='" + sentStatus + '\'' +
        '}';
    }
  }

  public class IdSorterDesc implements Comparator<ResearchMovement>
  {
    @Override
    public int compare(ResearchMovement o1, ResearchMovement o2) {
      return o2.getId().compareTo(o1.getId());
    }
  }

}
