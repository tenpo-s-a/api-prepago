package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.omg.CORBA.NO_IMPLEMENT;

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
  public List<ResearchMovementInformationFiles> stringJsonArrayToList(String json, Object object ) throws IOException {
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

  public String[] toMailUse(Boolean isSetFieldNames) throws IOException{

    String FileId="FileId";
    String IdOnFile="IdOnFile";
    String  FileName = "FileName";
    String  TypeOfFile = "TypeOfFile";
    

    List<ResearchMovementInformationFiles> rmifl;
    List<String> keys = new ArrayList<>();
    List<String> values = new ArrayList<>();

    HashMap<String, String> fieldNames = new HashMap<>();

    fieldNames.put(id.getClass().getName(),"Id Unico");
    fieldNames.put(originType.getClass().getName(),"Origen");
    fieldNames.put(createdAt.getClass().getName(),"Fecha Creación");
    fieldNames.put(dateOfTransaction.getClass().getName(),"Fecha Transaccion");
    fieldNames.put(responsible.getClass().getName(),"Responsable");
    fieldNames.put(description.getClass().getName(),"Descripción");
    fieldNames.put(movRef.getClass().getName(),"Id Movimiento");
    fieldNames.put(movementType.getClass().getName(),"Tipo de Movimiento");
    fieldNames.put(sentStatus.getClass().getName(),"Estado de Envío");
    fieldNames.put(FileId,"Id Archivo #");
    fieldNames.put(IdOnFile,"Id en Archivo #");
    fieldNames.put(FileName,"Nombre Archivo #");
    fieldNames.put(TypeOfFile,"Tipo Archivo #");

    if(!getFilesInfo().isEmpty()) {

      rmifl = stringJsonArrayToList(getFilesInfo(), new ResearchMovementInformationFiles());
      if (rmifl.size() > 0) {

        //===== key values
        keys.add(fieldNames.get(id.getClass().getName()));
        values.add(getId().toString());

        Integer toRplce = 1;
        for(Integer i=0;i<rmifl.size(); i++){

          keys.add(fieldNames.get(FileName).replace("#", Integer.valueOf(toRplce).toString()));
          values.add(rmifl.get(i).getNombreArchivo());
          keys.add(fieldNames.get(IdOnFile).replace("#", Integer.valueOf(toRplce).toString()));
          values.add(rmifl.get(i).getIdEnArchivo());

          toRplce++;
        }

        if (rmifl.size() == 1) {
          keys.add(fieldNames.get(FileName).replace("#", String.valueOf(2)));
          values.add(" ");
          keys.add(fieldNames.get(IdOnFile).replace("#",  String.valueOf(2)));
          values.add(" ");
        }

        ZonedDateTime utcDateTime = getDateOfTransaction().toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime chileDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/Santiago"));
        String stringDate = chileDateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        keys.add(fieldNames.get(originType.getClass().getName()));
        values.add(getOriginType().name());

        keys.add(fieldNames.get(dateOfTransaction.getClass().getName()));
        values.add(stringDate);

        keys.add(fieldNames.get(responsible.getClass().getName()));
        values.add(getResponsible().toString());

        keys.add(fieldNames.get(description.getClass().getName()));
        values.add(getDescription().getValue());

        keys.add(fieldNames.get(movRef.getClass().getName()));
        values.add(getMovRef().toString());

        keys.add(fieldNames.get(movementType.getClass().getName()));
        values.add(getMovementType().name());

        //===== key values end

      }
    }

    String[] dataReturn;
    if(!isSetFieldNames){
      dataReturn = values.toArray(new String[values.size()]);
    }else{
      dataReturn = keys.toArray(new String[keys.size()]);
    }

    return dataReturn;
  }

}
