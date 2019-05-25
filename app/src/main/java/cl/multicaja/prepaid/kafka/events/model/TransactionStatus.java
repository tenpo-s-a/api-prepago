package cl.multicaja.prepaid.kafka.events.model;

public enum TransactionStatus {
  AUTHORIZED,
  REJECTED,
  REVERSED,
  PAID;
}
