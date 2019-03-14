package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ResearchMovement10 extends BaseModel {

  private ObjectMapper objectMapper = null;
  protected ObjectMapper getObjectMapper(){
    if (this.objectMapper == null) {
      this.objectMapper = new ObjectMapper();
    }
    return this.objectMapper;
  }
  private List<ResearchMovementInformationFiles> stringJsonArrayToList(String json, Object object ) throws IOException {
    TypeFactory typeFactory = this.getObjectMapper().getTypeFactory();
    CollectionType collectionType = typeFactory.constructCollectionType(
      List.class, object.getClass());
    return this.getObjectMapper().readValue(json,collectionType);
  }

  private Long id;
  private String filesInfo;
  private ReconciliationOriginType originType;
  private Timestamp createdAt;
  private Timestamp dateOfTransaction;
  private ResearchMovementResponsibleStatusType responsible;
  private ResearchMovementDescriptionType description;
  private BigDecimal movRef;
  private PrepaidMovementType movementType;
  private ResearchMovementSentStatusType sentStatus;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFilesInfo() {
    return filesInfo;
  }

  public void setFilesInfo(String filesInfo) {
    this.filesInfo = filesInfo;
  }

  public ReconciliationOriginType getOriginType() {
    return originType;
  }

  public void setOriginType(ReconciliationOriginType originType) {
    this.originType = originType;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getDateOfTransaction() {
    return dateOfTransaction;
  }

  public void setDateOfTransaction(Timestamp dateOfTransaction) {
    this.dateOfTransaction = dateOfTransaction;
  }

  public ResearchMovementResponsibleStatusType getResponsible() {
    return responsible;
  }

  public void setResponsible(ResearchMovementResponsibleStatusType responsible) {
    this.responsible = responsible;
  }

  public ResearchMovementDescriptionType getDescription() {
    return description;
  }

  public void setDescription(ResearchMovementDescriptionType description) {
    this.description = description;
  }

  public BigDecimal getMovRef() {
    return movRef;
  }

  public void setMovRef(BigDecimal movRef) {
    this.movRef = movRef;
  }

  public PrepaidMovementType getMovementType() {
    return movementType;
  }

  public void setMovementType(PrepaidMovementType movementType) {
    this.movementType = movementType;
  }

  public ResearchMovementSentStatusType getSentStatus() {
    return sentStatus;
  }

  public void setSentStatus(ResearchMovementSentStatusType sentStatus) {
    this.sentStatus = sentStatus;
  }

  //TODO: Mas adelante puede ser con automatización del idioma, o valores se una tabla de settings
  public String[] toMailUse(Boolean isSetFieldNames) throws IllegalAccessException,NoSuchFieldException,IOException{

    List<ResearchMovementInformationFiles> rmifl = null;

    if(!getFilesInfo().isEmpty()) {
      rmifl = stringJsonArrayToList(getFilesInfo(), new ResearchMovementInformationFiles());
    }

    Iterator<ResearchMovementInformationFiles> rmiflIterator = rmifl.iterator();
    HashMap<String, String> fieldNames = new HashMap<>();
    fieldNames.put("idArchivo","Id Archivo #");
    fieldNames.put("idEnArchivo","Tipo Archivo #");
    fieldNames.put("nombreArchivo","Nombre Archivo #");
    fieldNames.put("tipoArchivo","Id en Archivo #");

    HashMap<String, String> rmifMap = new HashMap<>();
    rmifMap.put("Id Unico",getId().toString());

    Field[] fields;
    Class<?> objClass;
    Integer toRplce = 1;
    while (rmiflIterator.hasNext()) {
      ResearchMovementInformationFiles rmiflObject = rmiflIterator.next();

      objClass = rmiflObject.getClass();
      fields = objClass.getDeclaredFields();
      Field fieldSet;
      for (Field field: fields){
        fieldSet = objClass.getDeclaredField(field.getName());
        fieldSet.setAccessible(true);
        rmifMap.put(
          fieldNames.get(field.getName()).replace("#", Integer.valueOf(toRplce).toString()),
          fieldSet.get(rmiflObject).toString());
      }
      toRplce++;

    }

    ZonedDateTime utcDateTime = getDateOfTransaction().toLocalDateTime().atZone(ZoneId.of("UTC"));
    ZonedDateTime chileDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/Santiago"));
    String stringDate = chileDateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    rmifMap.put("Origen",getOriginType().name());
    rmifMap.put("Fecha Transaccion",stringDate);
    rmifMap.put("Responsable",getResponsible().toString());
    rmifMap.put("Descripción",getDescription().getValue());
    rmifMap.put("Id Movimiento",getMovRef().toString());
    rmifMap.put("Tipo de Movimiento",getMovementType().name());

    String[] dataReturn;
    if(!isSetFieldNames){
      dataReturn = rmifMap.values().toArray(new String[0]);
    }else{
      dataReturn = rmifMap.keySet().toArray(new String[0]);
    }

    return dataReturn;
  }

}
