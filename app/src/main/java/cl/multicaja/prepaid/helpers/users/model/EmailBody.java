package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmailBody extends BaseModel {

  private String address;
	private String template;
	private Map<String, Object> templateData;
  private List<Attached> attachments;


  public EmailBody(){
    super();
  }

  public EmailBody(String template, String address) {
    this.template = template;
    this.address = address;
  }

  public EmailBody(String template, Map<String, Object> templateData, String address) {
    this.template = template;
    this.templateData = templateData;
    this.address = address;
  }


  public List<Attached> getAttachments() {
    return attachments;
  }
  public void setAttachments(List<Attached> attachments) {
    this.attachments = attachments;
  }
  public String getTemplate() {
    return template;
  }
  public void setTemplate(String template) {
    this.template = template;
  }
  public Map<String, Object> getTemplateData() {
    return templateData;
  }
  public void setTemplateData(Map<String, Object> templateData) {
    this.templateData = templateData;
  }
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }

  public void addAttached(String contentFile, String mimeType, String fileName) {
    if (attachments == null) {
      attachments = new ArrayList<>();
    }
    Attached attached = new Attached();
    attached.setContentFile(contentFile);
    attached.setMimeType(mimeType);
    attached.setFileName(fileName);
    attachments.add(attached);
  }
  
}
