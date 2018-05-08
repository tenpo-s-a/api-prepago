package cl.multicaja.camel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.Serializable;

/**
 * Representa informacion de contexto durante el proceso de procesamiento de mensajes en camel
 *
 * @autor vutreras
 */
public final class ExchangeContext implements Serializable {

  private long idTrx;

  private Exception exception;

  private long timestampStart;

  private long timestampEnd;

  private String timeProcess;

  public ExchangeContext() {
    super();
  }

  public long getIdTrx() {
    return idTrx;
  }

  public void setIdTrx(long idTrx) {
    this.idTrx = idTrx;
  }

  public Exception exception() {
    return exception;
  }

  public String getExceptionMessage() {
    return this.exception != null ? this.exception.getMessage() : null;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public long getTimestampStart() {
    return timestampStart;
  }

  public void setTimestampStart(long timestampStart) {
    this.timestampStart = timestampStart;
  }

  public long getTimestampEnd() {
    return timestampEnd;
  }

  public void setTimestampEnd(long timestampEnd) {
    this.timestampEnd = timestampEnd;
  }

  public String getTimeProcess() {
    this.calcTimeProcess();
    return timeProcess;
  }

  public void calcTimeProcess() {
    if (this.timeProcess == null) {
      try {
        long millis = (this.getTimestampEnd() - this.getTimestampStart());
        this.timeProcess = (DurationFormatUtils.formatDuration(millis, "HH:mm:ss,SSS"));
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
