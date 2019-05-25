package cl.multicaja.accounting.helpers.mastercard.model;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.model.v10.Timestamps;

import java.util.*;

public class IpmFile extends BaseModel {

  private Long id;

  private String fileName;
  private String fileId;

  private IpmMessage header;
  private IpmMessage trailer;

  private List<IpmMessage> transactions;
  private List<IpmMessage> otherMessages;

  private Map<Integer, Integer> currencyExponents;

  private IpmFileStatus status;

  private Timestamps timestamps;

  public IpmFile() {
    super();
    transactions = new ArrayList<>();
    otherMessages = new ArrayList<>();

    currencyExponents = new HashMap<>();
  }

  public IpmMessage getHeader() {
    return header;
  }

  public void setHeader(IpmMessage header) {
    this.header = header;
  }

  public IpmMessage getTrailer() {
    return trailer;
  }

  public void setTrailer(IpmMessage trailer) {
    this.trailer = trailer;
  }

  public List<IpmMessage> getTransactions() {
    return transactions;
  }

  public List<IpmMessage> getOtherMessages() {
    return otherMessages;
  }

  public Map<Integer, Integer> getCurrencyExponents() {
    return currencyExponents;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public IpmFileStatus getStatus() {
    return status;
  }

  public void setStatus(IpmFileStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public void addTransaction(IpmMessage message) {
    this.transactions.add(message);
  }

  public void addOtherMessage(IpmMessage message) {
    this.otherMessages.add(message);
  }

  public void addCurrencyExponent(String currencyExponent) {
    String[] currencies = splitStringEvery(currencyExponent, 4);

    Arrays.asList(currencies)
      .forEach(c -> {
        Integer currencyCode = NumberUtils.getInstance().toInteger(c.substring(0, 3));
        Integer exponent = NumberUtils.getInstance().toInteger(c.substring(3));

        if(!currencyExponents.containsKey(currencyCode)) {
          currencyExponents.put(currencyCode, exponent);
        }
      });
  }

  public Integer getMessageCount() {
    return (this.getHeader() == null ? 0 : 1) + (this.getTrailer() == null ? 0 : 1) + this.getOtherMessages().size()  + this.getTransactions().size();
  }

  private String[] splitStringEvery(String s, int interval) {
    int arrayLength = (int) Math.ceil(((s.length() / (double)interval)));
    String[] result = new String[arrayLength];

    int j = 0;
    int lastIndex = result.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      result[i] = s.substring(j, j + interval);
      j += interval;
    } //Add the last bit
    result[lastIndex] = s.substring(j);

    return result;
  }
}
