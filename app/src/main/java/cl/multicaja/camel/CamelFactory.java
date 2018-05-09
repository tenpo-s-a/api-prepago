/**
 *
 */
package cl.multicaja.camel;

import cl.multicaja.core.utils.ConfigUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.DeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.SharedDeadLetterStrategy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;


/**
 * Fabrica de utilidades y objetos Camel
 *
 * @author vutreras
 */
public final class CamelFactory {

	private static final Log log = LogFactory.getLog(CamelFactory.class);

	private static final String ACYIVEMQ_COMPONENT_NAME = "remote";

	private static CamelFactory instance;

	/**
	 *
	 * @return
	 */
	public static CamelFactory getInstance() {
		if (instance == null) {
			instance = new CamelFactory();
		}
		return instance;
	}

	/**
	 *
	 */
	public CamelFactory() {
		super();
	}

  /**
   * crea un string con un id unico utilizando UUID
   *
   * @return
   */
  public String createUniqueMessageID() {
    UUID uuid = UUID.randomUUID();
    return String.valueOf(uuid).replaceAll("-", "") + System.nanoTime();
  }

	private PooledConnectionFactory pooledConnectionFactory;

	/**
	 * 	retorna el pool de conexiones
	 * @return
	 */
	public ConnectionFactory getConnectionFactory() {
		if (pooledConnectionFactory == null) {
			try {
				ConfigUtils config = ConfigUtils.getInstance();
				int maxConnections = config.getPropertyInt("activemq.connection.pool.max", 10);
				String activemqUrl = config.getProperty("activemq.url");
				String username = config.getProperty("activemq.username");
				String password = config.getProperty("activemq.password");
				boolean asyncSend = false;//config.getPropertieBoolean("activemq.async.send", false);
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activemqUrl);
				connectionFactory.setUseAsyncSend(asyncSend);
        if (StringUtils.isNotBlank(username)) {
          connectionFactory.setUserName(username);
        }
        if (StringUtils.isNotBlank(username)) {
          connectionFactory.setPassword(password);
        }
				pooledConnectionFactory = new PooledConnectionFactory();
				pooledConnectionFactory.setConnectionFactory(connectionFactory);
				pooledConnectionFactory.setMaxConnections(maxConnections);
				return pooledConnectionFactory;
			} catch (Exception e) {
				log.error("Error createConnectionFactoryActiveMQ", e);
				return null;
			}
		}
		return pooledConnectionFactory;
	}

	/**
	 * libera el pool de conexiones
	 */
	public void releaseConnectionFactory() {
		try {
			if (pooledConnectionFactory != null) {
				pooledConnectionFactory.clear();
				pooledConnectionFactory = null;
			}
		} catch(Exception e) {
			log.error("Error al liberar conexion mq, " + getInfo() + ", causa: " + e.getMessage());
		}
	}

	/**
	 * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ usando el pool de conexiones
	 * por defecto
   *
	 * @return
	 */
	public JMSMessenger createJMSMessenger() {
		return new JMSMessenger(this.getConnectionFactory());
	}

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ usando una conexion especifica
   *
   * @param connectionFactory
   * @return
   */
  public JMSMessenger createJMSMessenger(ConnectionFactory connectionFactory) {
    return new JMSMessenger(connectionFactory);
  }

	/**
	 * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ con configuraciones especificas
   * de timeout
	 *
	 * @param receiveTimeout
	 * @param timeToLive
	 * @return
	 */
	public JMSMessenger createJMSMessenger(long receiveTimeout, long timeToLive) {
		return new JMSMessenger(this.getConnectionFactory(), receiveTimeout, timeToLive);
	}

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ con configuraciones especificas
   * de timeout
   *
   * @param connectionFactory
   * @param receiveTimeout
   * @param timeToLive
   * @return
   */
  public JMSMessenger createJMSMessenger(ConnectionFactory connectionFactory, long receiveTimeout, long timeToLive) {
    return new JMSMessenger(connectionFactory, receiveTimeout, timeToLive);
  }

	/**
	 * retorna un ProducerTemplate nuevo de apache camel asociado al CamelContext por defecto
   *
	 * @return
	 */
	public ProducerTemplate createProducerTemplate() {
		try {
			return this.getCamelContext().createProducerTemplate();
		} catch (Exception e) {
			log.error("Error al crear un ProducerTemplate, " + getInfo() + ", causa: " + e.getMessage());
			return null;
		}
	}

	/**
	 * crea un nuevo contexto camel, con la posibilidad de usar o no activemq
   *
	 * @param useActiveMQ
	 * @return
	 * @throws Exception
	 */
	private CamelContext createCamelContext(boolean useActiveMQ) throws Exception {
		System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
		DefaultCamelContext context = new DefaultCamelContext();
		context.disableJMX();
		try {
			context.setAllowUseOriginalMessage(false);
		} catch (Exception e) {
			log.error("Error al establecer la configuracion de ActiveMQ setAllowUseOriginalMessage, causa: " + e.getMessage());
		}
		if (useActiveMQ) {
			PooledConnectionFactory pooledConnectionFactory = (PooledConnectionFactory)getConnectionFactory();
			pooledConnectionFactory.setIdleTimeout(0);

			JmsConfiguration jmsConfig = new JmsConfiguration();
			jmsConfig.setConnectionFactory(pooledConnectionFactory);

			ActiveMQComponent activemq = new ActiveMQComponent();
			activemq.setConfiguration(jmsConfig);

			context.addComponent(ACYIVEMQ_COMPONENT_NAME, activemq);

			log.info(getInfo() + " con ActiveMQ con pool de conexiones creado");
		}
    return context;
	}


	private CamelContext context;

  /**
   *
   * @return
   */
	private String getInfo() {
		return "CamelFactory[instancia:" + this.hashCode() + "], CamelContext[instancia:" + (context != null ? context.hashCode() : "null") + "]";
	}

	/**
	 * crea un contexto camel reutilizable con las rutas predeterminadas entregadas por getCamelRoutes
   *
	 * @return
	 * @throws Exception
	 */
	public CamelContext getCamelContext() throws Exception {
		return getCamelContext(true);
	}

	/**
	 * crea un contexto camel reutilizable con las rutas predeterminadas entregadas por getCamelRoutes
   *
	 * @param useActiveMQ
	 * @return
	 * @throws Exception
	 */
	public CamelContext getCamelContext(boolean useActiveMQ) throws Exception {
		if (context == null) {
			context = createCamelContext(useActiveMQ);
			log.info("Creado CamelContext por defecto: " + getInfo());
		}
    return context;
	}

  private boolean isCamelRunning = false;

	/**
	 * libera el CamelContext por defecto
	 */
	public void releaseCamelContext() {
		if (context != null) {
			try {
				context.stop();
			} catch (Exception e) {
				log.error("Error al detener: " + getInfo() + ", causa: " + e.getMessage());
			}
		}
		context = null;
		releaseConnectionFactory();
		isCamelRunning = false;
	}

	/**
	 * Agrega rutas camel al CamelContext por defecto
	 *
	 * @param useActiveMQ habilita / deshabilita el uso de activemq
	 * @param routes
	 * @return
	 * @throws Exception
	 */
	public CamelContext startCamelContextWithRoutes(boolean useActiveMQ, RouteBuilder... routes) throws Exception {
		if (!isCamelRunning) {
			getCamelContext(useActiveMQ).start();
			isCamelRunning = true;
		}
		if (isCamelRunning) {
			for (int i = 0; i < routes.length; i++) {
				getCamelContext(useActiveMQ).addRoutes(routes[i]);
			}
		}
		return getCamelContext(useActiveMQ);
	}

	/**
	 * Retorna si el CamelContext por defecto se encuentra en ejecucion
	 * @return
	 */
	public boolean isCamelRunning() {
		return isCamelRunning;
	}

	/**
	 * Crea un queue para el envio o recepcion de mensajes usando JMSMessenger
	 *
	 * @param queueName
	 * @return
	 */
	public Queue createJMSQueue(final String queueName) {
	  return new Queue() {
      @Override
      public String getQueueName() throws JMSException {
        return queueName;
      }
      @Override
      public String toString() {
        return queueName;
      }
    };
	}

	/**
	 * Crea un queue endpoint ser consumido por apache camel
	 *
	 * @param queueName
	 * @return
	 */
	public Endpoint createJMSEndpoint(String queueName) {
    try {
      return this.getCamelContext().getComponent(ACYIVEMQ_COMPONENT_NAME).createEndpoint(ACYIVEMQ_COMPONENT_NAME + ":queue:" + queueName);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private BrokerService broker;

  /**
   *
   * @return
   * @throws Exception
   */
  public BrokerService createBrokerService() throws Exception {
    if (broker == null) {
      log.info("==== Iniciando broker activemq ====");
      ConfigUtils config = ConfigUtils.getInstance();

      String url = config.getProperty("activemq.broker.embedded.url", "broker:(vm://localhost)?persistent=false");
      String name = config.getProperty("activemq.broker.embedded.name", "BrokerServiceCustom");
      String dataDirectory = config.getProperty("activemq.broker.embedded.dataDirectory", "activemq-data");
      //debe ser false, se deja la posibilidad que sea persistente pero no es la idea
      boolean persistent = config.getPropertyBoolean("activemq.broker.embedded.persistent", false);
      boolean deleteAllMessagesOnStartup = config.getPropertyBoolean("activemq.broker.embedded.deleteAllMessagesOnStartup", false);
      boolean addTcpConnector = config.getPropertyBoolean("activemq.broker.embedded.addTcpConnector", false);
      boolean useJmx = config.getPropertyBoolean("activemq.broker.embedded.useJmx", false);

      broker = BrokerFactory.createBroker(new URI(url));
      broker.setBrokerName(name);   //para el conector vm no se requiere nombre al broker dado que usa por defecto "localhost"
      //si se establece un nombre despues en el log los clientes que se conecten al broker podrian lanzar un warning
      //con el texto: Broker localhost not started so using ....
      broker.setDeleteAllMessagesOnStartup(deleteAllMessagesOnStartup);
      broker.setPersistent(persistent);
      broker.setRestartAllowed(true);
      broker.setUseJmx(useJmx);
      broker.setDataDirectory(dataDirectory);
      //un conector estra como por ejemplo un TCP no es necesario, dado esto se deja como opcional por si en algun momento
      //se necesita que el broker embebido pueda aceptar conexiones desde fuera del Jboss
      if (addTcpConnector) {
        broker.addConnector(config.getProperty("activemq.broker.embedded.tcpConnector", "tcp://localhost:61616"));
      }

      log.info("broker dataDirectoryFile: " + (broker.getDataDirectoryFile() != null ? broker.getDataDirectoryFile().getAbsolutePath() : "no definido"));
      log.info("broker brokerDataDirectory: " + (broker.getBrokerDataDirectory() != null ? broker.getBrokerDataDirectory().getAbsolutePath() : "no definido"));
      log.info("broker tmpDataDirectory: " + (broker.getTmpDataDirectory() != null ? broker.getTmpDataDirectory().getAbsolutePath() : "no definido"));

      //Para evitar que la cola DLQ guarde los mensajes indefinidamente:
      //http://activemq.apache.org/message-redelivery-and-dlq-handling.html

      //Se configura el broker para que se eliminen automaticamente los mensajes cuando expiren

      PolicyMap policyMap = new PolicyMap();
      DeadLetterStrategy strategy = new SharedDeadLetterStrategy();
      strategy.setProcessExpired(false);
      PolicyEntry policyEntry = new PolicyEntry();
      policyEntry.setQueue(">");
      policyEntry.setDeadLetterStrategy(strategy);
      policyMap.setPolicyEntries(Arrays.asList(policyEntry));
      broker.setDestinationPolicy(policyMap);

      if (persistent) {
        KahaDBPersistenceAdapter kpa = new KahaDBPersistenceAdapter();
        kpa.setDirectory(new File(".", dataDirectory));
        broker.setPersistenceAdapter(kpa);
      }

    } else {
      log.error("==== Broker activemq ya se encuentra iniciado ====");
    }
    return broker;
  }
}
