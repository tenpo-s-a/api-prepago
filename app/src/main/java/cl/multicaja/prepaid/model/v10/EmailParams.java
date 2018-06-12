package cl.multicaja.prepaid.model.v10;

import java.io.Serializable;

public class EmailParams implements Serializable {

  private String templateData;
  private String mailFrom;
  private String mailSubject;

  public String getTemplateData() {
    return templateData;
  }

  public void setTemplateData(String templateData) {
    this.templateData = templateData;
  }

  public String getMailFrom() {
    return mailFrom;
  }

  public void setMailFrom(String mailFrom) {
    this.mailFrom = mailFrom;
  }

  public String getMailSubject() {
    return mailSubject;
  }

  public void setMailSubject(String mailSubject) {
    this.mailSubject = mailSubject;
  }
}
