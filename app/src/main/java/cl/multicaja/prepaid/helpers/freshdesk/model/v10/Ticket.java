package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ticket extends NewTicket implements Serializable {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private Long id;
  private String createdAt;
  private String updatedAt;

  public Ticket() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getCreatedAtLocalDateTime() {
    return LocalDateTime.parse(getCreatedAt(), formatter);
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
  public LocalDateTime getUpdatedAtLocalDateTime() {
    return LocalDateTime.parse(getUpdatedAt(), formatter);
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  @JsonIgnore
  public Boolean isClosedOrResolved() {
    return (this.getStatus().equals(StatusType.CLOSED) || this.getStatus().equals(StatusType.RESOLVED));
  }
}
