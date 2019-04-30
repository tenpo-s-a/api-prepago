package cl.multicaja.prepaid.kafka.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class User extends BaseModel{

  @JsonProperty(value = "documentNumber",required = true)
  @NotEmpty(message = "Document Number cannot be empty")
  @NotBlank(message = "Document Number cannot be blank")
  @NotNull(message = "Document Number cannot be null")
  private String documentNumber;

  @JsonProperty(value = "firstName",required = true)
  @NotEmpty(message = "First Name cannot be empty")
  @NotBlank(message = "First Name cannot be blank")
  @NotNull(message = "First Name cannot be null")
  private String firstName;

  @JsonProperty(value = "id",required = true)
  @NotEmpty(message = "id or uuid cannot be empty")
  @NotBlank(message = "id or uuid cannot be blank")
  @NotNull(message = "id or uuid cannot be null")
  private String id;

  @JsonProperty(value = "lastName",required = true)
  @NotEmpty(message = "Last Name cannot be empty")
  @NotBlank(message = "Last Name cannot be blank")
  @NotNull(message = "Last Name cannot be null")
  private String lastName;

  @JsonProperty(value = "level",required = true)
  @NotEmpty(message = "Level cannot be empty")
  @NotBlank(message = "Level cannot be blank")
  @NotNull(message = "Level cannot be null")
  private String level;

  @JsonProperty(value = "state",required = true)
  @NotEmpty(message = "State cannot be empty")
  @NotBlank(message = "State cannot be blank")
  @NotNull(message = "State cannot be null")
  private String state;

  @JsonProperty(value = "plan",required = true)
  @NotEmpty(message = "plan cannot be empty")
  @NotBlank(message = "plan cannot be blank")
  @NotNull(message = "plan cannot be null")
  private String plan;

  @JsonProperty(value = "tributaryIdentifier",required = true)
  @NotEmpty(message = "tributaryIdentifier cannot be empty")
  @NotBlank(message = "tributaryIdentifier cannot be blank")
  @NotNull(message = "tributaryIdentifier cannot be null")
  private String tributaryIdentifier;


  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getPlan() {
    return plan;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }

  public String getTributaryIdentifier() {
    return tributaryIdentifier;
  }

  public void setTributaryIdentifier(String tributaryIdentifier) {
    this.tributaryIdentifier = tributaryIdentifier;
  }
}
