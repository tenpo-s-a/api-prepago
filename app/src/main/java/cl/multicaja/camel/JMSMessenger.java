package cl.multicaja.camel;

import org.apache.activemq.command.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import javax.jms.Message;
import java.io.Serializable;

/**
 * Clase para enviar y recibir mensaje MQ, implementacion de messenger JMS usando spring JMSTemplate
 *
 * @author vutreras
 */
public final class JMSMessenger {

	private static final Log log = LogFactory.getLog(JMSMessenger.class);

	private JmsTemplate jmsTemplate;
	private ConnectionFactory connectionFactory;
	private long receiveTimeout = 15000;
	private long timeToLive;

	/**
	 *
	 * @param connectionFactory
	 */
	public JMSMessenger(ConnectionFactory connectionFactory) {
		super();
		this.connectionFactory = connectionFactory;
	}

	/**
	 *
	 * @param connectionFactory
	 * @param receiveTimeout
	 * @param timeToLive
	 */
	public JMSMessenger(ConnectionFactory connectionFactory, long receiveTimeout, long timeToLive) {
		super();
		this.connectionFactory = connectionFactory;
		this.receiveTimeout = receiveTimeout;
		this.timeToLive = timeToLive;
	}

	public long getReceiveTimeout() {
		return this.receiveTimeout;
	}

	public long getTimeToLive() {
		return this.timeToLive;
	}

	/**
	 *
	 * @return
	 */
	public JmsTemplate getJmsTemplate() {
		if (this.jmsTemplate == null) {
			this.setJmsTemplate(new JmsTemplate(this.getConnectionFactory()));
		}
		return this.jmsTemplate;
	}

	/**
	 *
	 * @param jmsTemplate
	 */
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
		if (receiveTimeout > 0) {
			this.jmsTemplate.setReceiveTimeout(receiveTimeout);
		}
		this.jmsTemplate.setExplicitQosEnabled(true);
		this.jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
		if (timeToLive > 0) {
			this.jmsTemplate.setTimeToLive(timeToLive);
		}
	}

	/**
	 * @return the connectionFactory
	 */
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 *
	 * @param connectionFactory
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

  /**
   * permite enviar un mensaje a una cola
   *
   * @param queueName
   * @param messageId
   * @param messageObj
   * @param headers
   * @return
   */
	public boolean putMessage(Queue queueName, final String messageId, final Serializable messageObj, JMSHeader... headers) {
		boolean enviado = false;
		try {
			if (log.isDebugEnabled()) {
				log.debug("sendMessage queueName: " + queueName + ", messageId: " + messageId + ", headers: " + headers);
			}
      this.getJmsTemplate().send(queueName, new MessageCreator() {
        @Override
        public Message createMessage(Session session) throws JMSException {
          Message message = (messageObj instanceof String) ? session.createTextMessage((String)messageObj) : session.createObjectMessage(messageObj);
          if (messageId != null) {
            message.setJMSCorrelationID(messageId.trim());
          }
          if (headers != null && headers.length > 0) {
            for (int i = 0; i < headers.length; i++){
              JMSHeader mh = headers[i];
              message.setStringProperty(mh.getName(), mh.getValue());
            }
          }
          return message;
        }
      });
			enviado = true;
		} catch(Exception ex) {
			log.error("sendMessage queueName: " + queueName + ", id: " + messageId, ex);
		}
		if (!enviado) {
			log.error("Mensaje no enviado a la cola: " + queueName);
		}
		return enviado;
	}

  /**
   * permite enviar un mensaje a una cola
   *
   * @param queueName
   * @param messageId
   * @param messageObj
   * @return
   */
  public boolean putMessage(Queue queueName, String messageId, Serializable messageObj) {
    JMSHeader[] headers = null;
    return this.putMessage(queueName, messageId, messageObj, headers);
  }

  /**
   * permite obtener un mensaje desde una cola
   *
   * @param queueName
   * @param messageId
   * @return
   */
	public Object getMessage(Queue queueName, String messageId) {
		if (log.isDebugEnabled()) {
			log.debug("receiveMessage queueName: " + queueName + ", messageId: " + messageId);
		}
		Object msgObj = null;
		try {
			String selector = "JMSCorrelationID='" + messageId.trim() + "'";
			Message msg = this.getJmsTemplate().receiveSelected(queueName, selector);
			if (msg != null) {
				if (msg instanceof ActiveMQBytesMessage) {
					msgObj = new String(((ActiveMQBytesMessage)msg).getContent().data);
				} else if(msg instanceof ActiveMQTextMessage) {
					msgObj = ((ActiveMQTextMessage)msg).getText();
				} else if(msg instanceof ActiveMQObjectMessage) {
					msgObj = ((ActiveMQObjectMessage)msg).getObject();
				} else if(msg instanceof ActiveMQMapMessage) {
          msgObj = ((ActiveMQMapMessage) msg).getContentMap();
        } else if(msg instanceof ActiveMQMessage) {
          msgObj = ((ActiveMQMessage) msg).getContent();
				} else {
					msgObj = ((TextMessage)msg).getText();
				}
			}
		} catch(Exception ex) {
			log.error("receiveMessage queueName: " + queueName + ", messageId: " + messageId, ex);
		}
		if (msgObj instanceof String) {
			return msgObj != null && !"".equals(msgObj.toString().trim()) ? msgObj : null;
		} else {
			return msgObj;
		}
	}

  /**
   * permite enviar un mensaje a una cola y luego obtener el mensaje de respuesta desde una cola
   *
   * @param queueNameReq
   * @param queueNameResp
   * @param messageId
   * @param messageObj
   * @return
   */
	public Object putAndGetMessage(Queue queueNameReq, Queue queueNameResp, String messageId, Serializable messageObj) {
    JMSHeader[] headers = null;
		boolean ok = putMessage(queueNameReq, messageId, messageObj, headers);
		return ok ? getMessage(queueNameResp, messageId) : null;
	}

  /**
   * permite enviar un mensaje a una cola y luego obtener el mensaje de respuesta desde una cola
   *
   * @param queueNameReq
   * @param queueNameResp
   * @param messageId
   * @param messageObj
   * @param headers
   * @return
   */
	public Object putAndGetMessage(Queue queueNameReq, Queue queueNameResp, String messageId, Serializable messageObj, JMSHeader... headers) {
		boolean ok = putMessage(queueNameReq, messageId, messageObj, headers);
		return ok ? getMessage(queueNameResp, messageId) : null;
	}
}
