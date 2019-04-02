package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.json.JsonParser;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.*;
import cl.multicaja.prepaid.model.v10.*;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10.*;

public final class KafkaEventDelegate10 {

  private static Log log = LogFactory.getLog(KafkaEventDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private JsonParser jsonParser;

  private ProducerTemplate producerTemplate;

  private JsonParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = JsonUtils.getJsonParser();
    }

    return this.jsonParser;
  }

  private String toJson(Object obj) {
    return this.getJsonParser().toJson(obj);
  }

  public KafkaEventDelegate10() {
    super();
  }

  private ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  /**
   * Envia un evento de cuenta creada
   *
   */
  //FIXME: debe recibir la informacion de la cuenta
  public void publishAccountCreatedEvent(PrepaidCard10 prepaidCard10) {

    if(prepaidCard10 == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, prepaidCard -> null =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      headers.put("JMSCorrelationID", prepaidCard10.getProcessorUserId());
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      Account account = new Account();
      //FIXME: debe ser el UUID de la cuenta/contrato
      account.setId(prepaidCard10.getProcessorUserId());
      //FIXME: debe ser el status de la cuenta/contrato
      account.setStatus(PrepaidCardStatus.ACTIVE.toString());

      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(prepaidCard10.getTimestamps().getCreatedAt().toLocalDateTime());
      timestamps.setUpdatedAt(prepaidCard10.getTimestamps().getUpdatedAt().toLocalDateTime());
      account.setTimestamps(timestamps);

      AccountEvent accountEvent = new AccountEvent();
      //FIXME: deberia ser el ID de usuario externo
      accountEvent.setUserId(prepaidCard10.getIdUser().toString());
      accountEvent.setAccount(account);

      ExchangeData<String> req = new ExchangeData<>(toJson(accountEvent));
      req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_ACCOUNT_CREATED_EVENT));

      this.getProducerTemplate().sendBodyAndHeaders(SEDA_ACCOUNT_CREATED_EVENT, req, headers);
    }
  }

  /**
   * Envia un evento de tarjeta creada
   *
   */
  public void publishCardCreatedEvent(PrepaidCard10 prepaidCard10) {

    if(prepaidCard10 == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, prepaidCard -> null =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      headers.put("JMSCorrelationID", prepaidCard10.getProcessorUserId());
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      Card card = new Card();
      //FIXME: el id deberia ser el UUID de la tarjeta
      card.setId(prepaidCard10.getId().toString());
      card.setPan(prepaidCard10.getPan());
      card.setStatus(prepaidCard10.getStatus().toString());

      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(prepaidCard10.getTimestamps().getCreatedAt().toLocalDateTime());
      timestamps.setUpdatedAt(prepaidCard10.getTimestamps().getUpdatedAt().toLocalDateTime());

      card.setTimestamps(timestamps);

      CardEvent cardEvent = new CardEvent();
      cardEvent.setCard(card);
      //FIXME: deberia ser el UUID del contrato/cuenta
      cardEvent.setAccountId(prepaidCard10.getProcessorUserId());
      //FIXME: deberia ser el ID de usuario externo
      cardEvent.setUserId(prepaidCard10.getIdUser().toString());

      ExchangeData<String> req = new ExchangeData<>(toJson(cardEvent));
      req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_CARD_CREATED_EVENT));

      this.getProducerTemplate().sendBodyAndHeaders(SEDA_CARD_CREATED_EVENT, req, headers);
    }
  }

  /**
   * Envia un evento de transaccion
   *
   */
  public void publishTransactionEvent(NewAmountAndCurrency10 newFee, PrepaidMovement10 prepaidMovement10, String type, String status) {

    if(prepaidMovement10 == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, PrepaidTopup10 -> null =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      headers.put("JMSCorrelationID", prepaidMovement10.getId());
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      Transaction transaction = new Transaction();
      //transaction.setId(prepaidTopup10.getId().toString());
      transaction.setRemoteTransactionId(prepaidMovement10.getIdTxExterno());
      transaction.setAuthCode(prepaidMovement10.getCodact().toString());
      transaction.setCountryCode(prepaidMovement10.getCodpais().getValue());
      Merchant merchant = new Merchant();
      merchant.setCategory(prepaidMovement10.getCodact());
      merchant.setCode(prepaidMovement10.getCodcom());
      merchant.setName(prepaidMovement10.getNomcomred());
      transaction.setMerchant(merchant);
      NewAmountAndCurrency10 newAmountAndCurrency10 = new NewAmountAndCurrency10();
      newAmountAndCurrency10.setValue(prepaidMovement10.getMonto());
      newAmountAndCurrency10.setCurrencyCode(prepaidMovement10.getClamon());
      transaction.setPrimaryAmount(newAmountAndCurrency10);
      transaction.setType(type);
      transaction.setStatus(status);
      List<Fee> fees = new ArrayList<>();
      Fee fee = new Fee();
      fee.setAmount(newFee);
      fees.add(fee);
      transaction.setFees(fees);

      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(prepaidMovement10.getFechaCreacion().toLocalDateTime());
      timestamps.setUpdatedAt(prepaidMovement10.getFechaActualizacion().toLocalDateTime());

      transaction.setTimestamps(timestamps);
      log.error("prueba de estructura" + transaction);

      TransactionEvent transactionEvent = new TransactionEvent();
      transactionEvent.setTransaction(transaction);

      ExchangeData<String> req = new ExchangeData<>(toJson(transactionEvent));
      if(status.equals("AUTHORIZED")){
        req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_TRANSACTION_AUTHORIZED_EVENT));
        this.getProducerTemplate().sendBodyAndHeaders(SEDA_TRANSACTION_AUTHORIZED_EVENT, req, headers);
      }

    }
  }
}
