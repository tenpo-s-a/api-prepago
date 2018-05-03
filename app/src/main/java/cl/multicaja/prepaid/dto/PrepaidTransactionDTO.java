package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class PrepaidTransactionDTO {

  private TransactionType transaction_type;
  private String auth_code;
  private String processor_transaction_id;
  private String pan;
  private String real_date;
  private String accounting_date;
  private String processing_date;
  private Amount amount_primary;
  private Amount amount_foreign;
  private Place place;
  private Merchant merchant;

  public TransactionType getTransaction_type() {
    return transaction_type;
  }

  public void setTransaction_type(TransactionType transaction_type) {
    this.transaction_type = transaction_type;
  }

  public String getAuth_code() {
    return auth_code;
  }

  public void setAuth_code(String auth_code) {
    this.auth_code = auth_code;
  }

  public String getProcessor_transaction_id() {
    return processor_transaction_id;
  }

  public void setProcessor_transaction_id(String processor_transaction_id) {
    this.processor_transaction_id = processor_transaction_id;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getReal_date() {
    return real_date;
  }

  public void setReal_date(String real_date) {
    this.real_date = real_date;
  }

  public String getAccounting_date() {
    return accounting_date;
  }

  public void setAccounting_date(String accounting_date) {
    this.accounting_date = accounting_date;
  }

  public String getProcessing_date() {
    return processing_date;
  }

  public void setProcessing_date(String processing_date) {
    this.processing_date = processing_date;
  }

  public Amount getAmount_primary() {
    return amount_primary;
  }

  public void setAmount_primary(Amount amount_primary) {
    this.amount_primary = amount_primary;
  }

  public Amount getAmount_foreign() {
    return amount_foreign;
  }

  public void setAmount_foreign(Amount amount_foreign) {
    this.amount_foreign = amount_foreign;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }
}

