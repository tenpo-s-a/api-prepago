package cl.multicaja.accounting.helpers.mastercard.model;

import cl.multicaja.core.model.BaseModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpmFile extends BaseModel {

  private IpmMessage header;
  private IpmMessage trailer;

  private List<IpmMessage> transactions;
  private List<IpmMessage> otherMessages;

  private Map<Integer, Integer> currencyExponents;

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

  public void addTransaction(IpmMessage message) {
    this.transactions.add(message);
  }

  public void addOtherMessage(IpmMessage message) {
    this.otherMessages.add(message);
  }

  public void addCurrencyExponent(String currencyExponent) {
    //TODO interpretar el currency exponent
  }
}
