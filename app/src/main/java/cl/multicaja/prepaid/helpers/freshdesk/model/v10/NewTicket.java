package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.io.Serializable;

public class NewTicket extends BaseModel implements Serializable {

  private String name;
  private Long requesterId;
  private String email;
  private String phone;
  private String uniqueExternalId;
  private String subject;
  private TicketType type;
  private StatusType status;
  private PriorityType priority;
  private Long productId;
  private SourceType source;
  private Long emailConfigId;
  private Long groupId;
  private String description;
  private Long responderId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(Long requesterId) {
    this.requesterId = requesterId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getUniqueExternalId() {
    return uniqueExternalId;
  }

  public void setUniqueExternalId(String uniqueExternalId) {
    this.uniqueExternalId = uniqueExternalId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public TicketType getType() {
    return type;
  }

  public void setType(TicketType type) {
    this.type = type;
  }

  public StatusType getStatus() {
    return status;
  }

  public void setStatus(StatusType status) {
    this.status = status;
  }

  public PriorityType getPriority() {
    return priority;
  }

  public void setPriority(PriorityType priority) {
    this.priority = priority;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public SourceType getSource() {
    return source;
  }

  public void setSource(SourceType source) {
    this.source = source;
  }

  public Long getEmailConfigId() {
    return emailConfigId;
  }

  public void setEmailConfigId(Long emailConfigId) {
    this.emailConfigId = emailConfigId;
  }

  public Long getGroupId() {
    return groupId;
  }

  public void setGroupId(Long groupId) {
    this.groupId = groupId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getResponderId() {
    return responderId;
  }

  public void setResponderId(Long responderId) {
    this.responderId = responderId;
  }
}
