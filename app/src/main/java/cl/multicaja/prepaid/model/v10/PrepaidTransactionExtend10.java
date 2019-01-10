package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import java.util.List;

public class PrepaidTransactionExtend10 extends BaseModel {

  public class InnerPrepaidTransactionException extends Exception{

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
  }

  private List<PrepaidTransaction10> ListPrepaidTransactions10;
  private Integer errorCode;
  private String errorMessage;

  public List<PrepaidTransaction10> getListPrepaidTransactions10() {
    return ListPrepaidTransactions10;
  }

  public void setListPrepaidTransactions10(List<PrepaidTransaction10> listPrepaidTransactions10) {
    ListPrepaidTransactions10 = listPrepaidTransactions10;
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

  public InnerPrepaidTransactionException invokeInner (String errorMessage, String errorCode) throws Exception{
    return new InnerPrepaidTransactionException(errorMessage, errorCode);
  }

}
