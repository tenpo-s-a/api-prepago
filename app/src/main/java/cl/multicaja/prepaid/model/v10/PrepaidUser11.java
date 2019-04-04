package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.model.v11.UserStatus;
import cl.multicaja.prepaid.model.v11.DocumentType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class PrepaidUser11 {

  private Long id;
  private String uiid;
  private Long idUserMc;
  private UserStatus status;
  private String name;
  private String lastName;
  private String documentNumber;
  private DocumentType documentType;
  private String level;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Integer rut = 0;
  private String infoBalance = "";
  private Long expirationBalance = 0L;
  private Long attemptsValidation = 0L;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUiid() {
    return uiid;
  }

  public void setUiid(String uiid) {
    this.uiid = uiid;
  }

  public Long getIdUserMc() {
    return idUserMc;
  }

  public void setIdUserMc(Long idUserMc) {
    this.idUserMc = idUserMc;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(DocumentType documentType) {
    this.documentType = documentType;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public String getInfoBalance() {
    return infoBalance;
  }

  public void setInfoBalance(String infoBalance) {
    this.infoBalance = infoBalance;
  }

  public Long getExpirationBalance() {
    return expirationBalance;
  }

  public void setExpirationBalance(Long expirationBalance) {
    this.expirationBalance = expirationBalance;
  }

  public Long getAttemptsValidation() {
    return attemptsValidation;
  }

  public void setAttemptsValidation(Long attemptsValidation) {
    this.attemptsValidation = attemptsValidation;
  }

  public String snakeCaseToCamelCase(String fieldToSearch){ //Al usar hibernate o un orm similar sería innecesario.
    Map<String,String> originalTableNames = new HashMap<>();
    originalTableNames.put("id","id");
    originalTableNames.put("id_usuario_mc","idUserMc");
    originalTableNames.put("estado","status");
    originalTableNames.put("nombre","name");
    originalTableNames.put("apellido","lastName");
    originalTableNames.put("numero_documento","documentNumber");
    originalTableNames.put("tipo_documento","documentType");
    originalTableNames.put("nivel","level");
    originalTableNames.put("fecha_creacion","createdAt");
    originalTableNames.put("fecha_actualizacion","updatedAt");
    originalTableNames.put("rut","rut");
    originalTableNames.put("saldo_info","infoBalance");
    originalTableNames.put("saldo_expiracion","expirationBalance");
    originalTableNames.put("intentos_validacion","attemptsValidation");
    originalTableNames.put("uiid","uiid");

    return originalTableNames.get(fieldToSearch);
  }

  public Object castData(String fieldName, String data){

    Object object = null;

    switch (snakeCaseToCamelCase(fieldName)){
      case "id":
        object = Long.valueOf(data);
        break;
      case "idUserMc":
        object = Long.valueOf(data);
        break;
      case "status":
        object = UserStatus.valueOfEnum(data);
        break;
      case "name":
        object = data;
        break;
      case "lastName":
        object = data;
        break;
      case "documentNumber":
        object = data;
        break;
      case "documentType":
        object = DocumentType.valueOfEnum(data);
        break;
      case "level":
        object = data;
        break;
      case "createdAt":
        Timestamp timestamp1 = Timestamp.valueOf(data);
        object = timestamp1.toLocalDateTime();
        break;
      case "updatedAt":
        Timestamp timestamp2 = Timestamp.valueOf(data);
        object = timestamp2.toLocalDateTime();
        break;
      case "rut":
        object = Integer.valueOf(data);
        break;
      case "infoBalance":
        object = data;
        break;
      case "expirationBalance":
        object = Long.valueOf(data);
        break;
      case "attemptsValidation":
        object = Long.valueOf(data);
        break;
      case "uiid":
        object = data;
        break;
    }
    return object;
  }

}
