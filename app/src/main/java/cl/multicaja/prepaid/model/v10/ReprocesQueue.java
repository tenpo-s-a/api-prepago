package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

public class ReprocesQueue extends BaseModel {

  private String idQueue;
  private QueuesNameType lastQueue;

  public String getIdQueue() {
    return idQueue;
  }

  public void setIdQueue(String idQueue) {
    this.idQueue = idQueue;
  }

  public QueuesNameType getLastQueue() {
    return lastQueue;
  }

  public void setLastQueue(QueuesNameType lastQueue) {
    this.lastQueue = lastQueue;
  }
}
