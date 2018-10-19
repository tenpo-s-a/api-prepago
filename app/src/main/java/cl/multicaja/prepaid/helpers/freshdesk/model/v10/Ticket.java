package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import java.io.Serializable;

public class Ticket extends BaseModel implements Serializable {

  private Integer source;
  private Integer status;
  private Long id;

  public Integer getSource() {
    return source;
  }

  public void setSource(Integer source) {
    this.source = source;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
