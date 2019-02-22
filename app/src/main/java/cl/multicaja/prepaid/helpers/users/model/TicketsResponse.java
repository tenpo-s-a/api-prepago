package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.Ticket;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketsResponse extends BaseModel implements Serializable {

  private List<Ticket> results;
  private Long total;

  public TicketsResponse() {
    super();
  }

  public List<Ticket> getResults() {
    return results;
  }

  public void setResults(List<Ticket> results) {
    this.results = results;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

}
