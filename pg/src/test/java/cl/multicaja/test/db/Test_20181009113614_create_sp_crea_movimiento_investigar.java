package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import cl.multicaja.test.model.InformationFilesModelObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


public class Test_20181009113614_create_sp_crea_movimiento_investigar extends TestDbBasePg {

  // Historial de modificación
  // 20181009113614_create_sp_crea_movimiento_investigar.sql
  // 20190222153718_create_sp_update_sp_insert_prp_movimiento_investigar_v11.sql
  // 20190306084027_update_sp_insert_prp_movimiento_investigar_v12.sql

  private static final String SP_INSERT_RESEARCH_MOVEMENT_NAME = SCHEMA + ".mc_prp_crea_movimiento_investigar_v12";
  private static final String SP_SEARCH_RESEARCH_MOVEMENT_NAME = SCHEMA + ".mc_prp_busca_movimientos_a_investigar_v13";

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", SCHEMA));
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

  protected <T> T fromJson(String json, Class<T> cls) throws IOException {
    return this.getObjectMapper().readValue(json,cls);
  }

  private List<InformationFilesModelObject> toList(String json,Object object ) throws IOException{

    TypeFactory typeFactory = this.getObjectMapper().getTypeFactory();
    CollectionType collectionType = typeFactory.constructCollectionType(
      List.class, object.getClass());
    return this.getObjectMapper().readValue(json,collectionType);
  }

  private static Object[] buildParams(
    String informacionArchivos,
    String origen,
    Timestamp fechaDeTransaccion,
    String responsable,
    String descripcion,
    Long movRef,
    String tipoMovimiento,
    String sentStatus
  ){
    Object[] params = {
      new InParam(informacionArchivos,Types.VARCHAR),
      new InParam(origen,Types.VARCHAR),
      new InParam(fechaDeTransaccion,Types.TIMESTAMP),
      new InParam(responsable,Types.VARCHAR),
      new InParam(descripcion,Types.VARCHAR),
      new InParam(movRef,Types.NUMERIC),
      new InParam(tipoMovimiento,Types.VARCHAR),
      new InParam(sentStatus,Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return params;
  }

  public static Map<String, Object> setResearchMovement (
    String informacionArchivos,
    String origen,
    Timestamp fechaDeTransaccion,
    String responsable,
    String descripcion,
    Long movRef,
    String tipoMovimiento,
    String sentStatus
  ) throws SQLException {

    Object[] params = buildParams(informacionArchivos,origen,fechaDeTransaccion,responsable,descripcion,movRef,tipoMovimiento,sentStatus);

    return dbUtils.execute(SP_INSERT_RESEARCH_MOVEMENT_NAME, params);
  }

  public Map<String, Object> searchResearchMovement(Long id,  Timestamp beginDateTime, Timestamp endDateTime, String sentStatus) throws SQLException {

    Object[] params = {
      id != null ? new InParam(id, Types.BIGINT) : new NullParam(Types.BIGINT),
      beginDateTime != null ? new InParam(beginDateTime, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      endDateTime != null ? new InParam(endDateTime, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      sentStatus != null ? new InParam(sentStatus, Types.VARCHAR) : new NullParam(Types.VARCHAR),
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
  public void testCreateResearchMovement() throws SQLException,IOException {

    {
      System.out.println("TEST 1 origen obligatorio");
      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),null,
        null,null,null,null,null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 2 fecha de transaccio obligatoria");
      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        null,null,null,null,null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 3 responsable oblitagorio");
      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),null,null,
        null,null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 4 descripcion obligatorio");
      System.out.println(new Timestamp((new Date()).getTime()).toString());
      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),getRandomString(10),null,
        null,null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 5 movRef obligatorio");
      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),getRandomString(10),getRandomString(10),
        null,null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 6 movRef en 0, tipoMovimiento Obligatorio");

      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),getRandomString(10),getRandomString(10),
        Long.valueOf(0),null,null);
      System.out.println("Se ingreso satisfactoriamente un valor igual a 0");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 7 movRef < 0");

      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),getRandomString(10),getRandomString(10),
        Long.valueOf(-1),null,null);
      System.out.println("Se ingreso satisfactoriamente un valor igual a 0");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 8 movRef en 0, sentStatus obligatorio");

      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),getRandomString(10),getRandomString(10),
        Long.valueOf(0),getRandomString(10),null);
      System.out.println("Se ingreso satisfactoriamente un valor igual a 0");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
    }

    {
      System.out.println("TEST 9 Inserción OK, con todos los parámetros");
      Map<String, Object> data = setResearchMovement(this.toJson("dato_tipo_json"),getRandomString(10),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),getRandomString(10),getRandomString(10),
        Long.valueOf(100),getRandomString(10),getRandomString(10));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("Debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 10 Inserción OK, InformationFilesModelObject json");

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
      String sentStatus = getRandomString(10);

      LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
      beginDateTime = beginDateTime.minusHours(1);
      LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
      endDateTime = endDateTime.plusHours(1);

      Map<String, Object> resp = setResearchMovement(jsonSent,origen,
        fechaDeTransaccion,responsable,descripcion,
        movRef,tipoMovimiento,sentStatus);

      Assert.assertNotNull("Data no debe ser null", resp);
      Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));
      System.out.println("Movimiento ingresado satisfactoriamente");

      Map<String, Object> data = searchResearchMovement( numberUtils.toLong(resp.get("_r_id")),
        Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), sentStatus);
      List<Map<String, Object>> results = (List)data.get("result");
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

      InformationFilesModelObject informationFilesModelObjectReturn = this.fromJson(researchMovement.informacionArchivos,InformationFilesModelObject.class);

      Assert.assertEquals("IdArchivo es igual ",informationFilesModelObjectSent.getIdArchivo(),informationFilesModelObjectReturn.getIdArchivo());
      Assert.assertEquals("IdEnArchivo es igual ",informationFilesModelObjectSent.getIdEnArchivo(),informationFilesModelObjectReturn.getIdEnArchivo());
      Assert.assertEquals("NombreArchivo es igual ",informationFilesModelObjectSent.getNombreArchivo(),informationFilesModelObjectReturn.getNombreArchivo());
      Assert.assertEquals("TipoArchivo es igual",informationFilesModelObjectSent.getTipoArchivo(),informationFilesModelObjectReturn.getTipoArchivo());

    }

    {
      System.out.println("TEST 11 Inserción OK, InformationFilesModelObject jsonArray");

      InformationFilesModelObject informationFilesModelObjectSent = new InformationFilesModelObject();
      informationFilesModelObjectSent.setIdArchivo(Long.valueOf(1));
      informationFilesModelObjectSent.setIdEnArchivo("idEnArchivi_1");
      informationFilesModelObjectSent.setNombreArchivo("nombreArchivo_1");
      informationFilesModelObjectSent.setTipoArchivo("tipoArchivo_1");

      String origen = getRandomString(10);
      Timestamp fechaDeTransaccion = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
      String responsable = getRandomString(10);
      String descripcion = getRandomString(10);
      Long movRef = Long.valueOf(100);
      String tipoMovimiento = getRandomString(10);
      String sentStatus = getRandomString(10);

      LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
      beginDateTime = beginDateTime.minusHours(1);

      LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
      endDateTime = endDateTime.plusHours(1);

      List<InformationFilesModelObject> informationFilesModelObjectListSent = new ArrayList<>();
      informationFilesModelObjectListSent.add(informationFilesModelObjectSent);

      String jsonSent = this.toJson(informationFilesModelObjectListSent);

      Map<String, Object> resp = setResearchMovement(jsonSent,origen,fechaDeTransaccion,responsable,descripcion,movRef,tipoMovimiento,sentStatus);
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
      Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);
      System.out.println("Movimiento ingresado satisfactoriamente");

      Map<String, Object> data = searchResearchMovement( numberUtils.toLong(resp.get("_r_id")),Timestamp.valueOf(beginDateTime),Timestamp.valueOf(endDateTime), sentStatus);
      List<Map<String, Object>> results = (List)data.get("result");
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

      List<InformationFilesModelObject> informationFilesModelObjectListReturn = this.toList(researchMovement.informacionArchivos, new InformationFilesModelObject());
      Iterator<InformationFilesModelObject> informationFilesModelObjectIterator = informationFilesModelObjectListReturn.iterator();
      while (informationFilesModelObjectIterator.hasNext()) {
        InformationFilesModelObject informationFilesModelObjectReturn = informationFilesModelObjectIterator.next();

        Assert.assertEquals("IdArchivo es Igual ",informationFilesModelObjectSent.getIdArchivo(),informationFilesModelObjectReturn.getIdArchivo());
        Assert.assertEquals("IdEnArchivo es Igual ",informationFilesModelObjectSent.getIdEnArchivo(),informationFilesModelObjectReturn.getIdEnArchivo());
        Assert.assertEquals("IdEnArchivo es Igual ",informationFilesModelObjectSent.getNombreArchivo(),informationFilesModelObjectReturn.getNombreArchivo());
        Assert.assertEquals("TipoArchivo es Igual ",informationFilesModelObjectSent.getTipoArchivo(),informationFilesModelObjectReturn.getTipoArchivo());
      }
    }

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
  }
}
