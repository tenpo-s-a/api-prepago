package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import java.util.List;

public class PrepaidTransactionExtend10 extends BaseModel {

  /*public class InnerPrepaidTransactionException extends Exception{

    private PrepaidTransactionExtend10 prepaidTransactionExtend10;

    public InnerPrepaidTransactionException(String message, String code)
    {
      super(message+" : "+code);
    }

    public PrepaidTransactionExtend10 getPrepaidTransactionExtend10() {
      return prepaidTransactionExtend10;
    }

    public void setPrepaidTransactionExtend10(PrepaidTransactionExtend10 prepaidTransactionExtend10) {
      this.prepaidTransactionExtend10 = prepaidTransactionExtend10;
    }
  }*/

  private List<PrepaidTransaction10> data;
  private Integer errorCode;
  private String errorMessage;
  private Boolean success;

  public List<PrepaidTransaction10> getData() {
    return data;
  }

  public void setData(List<PrepaidTransaction10> data) {
    this.data = data;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

/*public InnerPrepaidTransactionException invokeInner (String errorMessage, String errorCode) throws Exception{
    return new InnerPrepaidTransactionException(errorMessage, errorCode);
  }*/

}
