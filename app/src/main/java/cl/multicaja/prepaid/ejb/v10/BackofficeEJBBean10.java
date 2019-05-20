package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.async.v10.BackofficeDelegate10;
import cl.multicaja.prepaid.external.freshdesk.model.Ticket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.FreshdeskServiceHelper;
import com.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class BackofficeEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(BackofficeEJBBean10.class);

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @Inject
  private BackofficeDelegate10 backofficeDelegate10;

  public BackofficeEJBBean10() {
    super();
  }

  public MailPrepaidEJBBean10 getMailPrepaidEJBBean10() {
    return mailPrepaidEJBBean10;
  }

  public void setMailPrepaidEJBBean10(MailPrepaidEJBBean10 mailPrepaidEJBBean10) {
    this.mailPrepaidEJBBean10 = mailPrepaidEJBBean10;
  }

  //TODO Esto para que es?.
  /*public TicketsResponse getEmergencyTickets(Integer page, LocalDateTime from, LocalDateTime to) throws Exception {
    return UserClient.getInstance().getEmergencyTickets(null, page, from, to);
  }*/

  private static final DateTimeFormatter fileDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

  public File generateE06Report(ZonedDateTime date) throws Exception {
/*
    LocalDateTime firstDayForSearch = ZonedDateTime.ofInstant(date
      .with(TemporalAdjusters.firstDayOfMonth())
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0).toInstant(), ZoneId.of("UTC")).toLocalDateTime();

    LocalDateTime lastDayForSearch = ZonedDateTime.ofInstant(date
      .with(TemporalAdjusters.lastDayOfMonth())
      .withHour(23)
      .withMinute(59)
      .withSecond(59)
      .withNano(999999999).toInstant(), ZoneId.of("UTC")).toLocalDateTime();

    Integer page = 1;

    List<Ticket> tickets = new ArrayList<>();

    TicketsResponse response = this.getEmergencyTickets(page, firstDayForSearch, lastDayForSearch);

    Long total = response.getTotal();

    log.info("Total tickets = " + total);

    if (total > 300) {
      log.info("Enviar mail de proceso manual");
      Map<String, Object> templateData = new HashMap<>();

      templateData.put("description", String.format("La cantidad de tickets de Emergencia del mes %s de %s es de %s. Se debe generar manualmente el archivo a utilizar para la generacion del reporte E06.",
        firstDayForSearch.getMonthValue(),
        firstDayForSearch.getYear(),
        total));

      // Enviamos el archivo al mail de reportes diarios
      EmailBody emailBodyToSend = new EmailBody();
      emailBodyToSend.setTemplateData(templateData);
      emailBodyToSend.setTemplate(MailTemplates.TEMPLATE_MAIL_E06_REPORT);
      emailBodyToSend.setAddress("e06_report@multicaja.cl");
      getMailPrepaidEJBBean10().sendMailAsync(null, emailBodyToSend);
      return null;
    }

    tickets.addAll(response.getResults());

    if (total > 30) {
      double totalIterations = (total.intValue() / 30) + 1;
      log.info("Iterations = " + totalIterations);

      for (int i = 2; i <= totalIterations; i++) {
        TicketsResponse resp = this.getEmergencyTickets(i, firstDayForSearch, lastDayForSearch);
        tickets.addAll(resp.getResults());
      }
    }

    ZonedDateTime firstDayOfMonth = date
      .with(TemporalAdjusters.firstDayOfMonth())
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0);

    ZonedDateTime lastDayOfMonth = date
      .with(TemporalAdjusters.lastDayOfMonth())
      .withHour(23)
      .withMinute(59)
      .withSecond(59)
      .withNano(999999999);

    // Se filtran solo los que estan dentro del mes en hora Chile
    List<Ticket> localTickets = tickets.stream()
      .filter(t -> {
        ZonedDateTime utc = t.getCreatedAtLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime local = ZonedDateTime.ofInstant(utc.toInstant(), ZoneId.of("America/Santiago"));
        return local.isBefore(lastDayOfMonth) && local.isAfter(firstDayOfMonth);
      })
      .collect(Collectors.toList());


    if(localTickets.isEmpty()){
      return null;
    }

    String directoryName = "report_e06";
    File directory = new File(directoryName);
    if (! directory.exists()){
      directory.mkdir();
    }

    String fileId = date.format(DateTimeFormatter.ofPattern("MM_yyyy"));
    String fileName = String.format("E06_%s.CSV", fileId);

    return this.createReportCsv(directoryName + "/" + fileName, localTickets);

 */return null;
  }

  private File createReportCsv(String filename, List<Ticket> tickets) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    String[] header = new String[]{"RUT","Número del Reclamo", "Clasificación del Reclamo", "Vía de Ingreso", "Fecha de recepción", "Fecha de cierre"};
    writer.writeNext(header);

    for (Ticket t : tickets) {

      ZonedDateTime utcCreated = t.getCreatedAtLocalDateTime().atZone(ZoneId.of("UTC"));
      ZonedDateTime localCreated = ZonedDateTime.ofInstant(utcCreated.toInstant(), ZoneId.of("America/Santiago"));

      String closedDate = "19000101";

      /**
       * Se evalua el status del ticket:
       *  - Si el ticket esta CLOSED o RESOLVED, se agrega fecha de cierre.
       */
      if(FreshdeskServiceHelper.getInstance().isClosedOrResolved(t.getStatus())) {
        ZonedDateTime utcUpdated = t.getUpdatedAtLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime localUpdated = ZonedDateTime.ofInstant(utcUpdated.toInstant(), ZoneId.of("America/Santiago"));
        closedDate = localUpdated.format(fileDateFormatter);
      }

      Map<String, Object> customFields = t.getCustomFields();

      String[] clasificacion = String.valueOf(customFields.get("cf_clasificacin")).split(" ");
      String[] viaIngreso = String.valueOf(String.valueOf(customFields.get("cf_va_de_ingreso"))).split(" ");

      String[] data = new String[]{
        String.valueOf(customFields.get("cf_rut_usuario")), //RUT,
        t.getId().toString(), //Número del Reclamo(ID ticket),
        clasificacion[0] + clasificacion[1], //Clasificación del Reclamo
        viaIngreso[0], //Vía de Ingreso
        localCreated.format(fileDateFormatter), //Fecha de recepción
        closedDate, //Fecha de cierre
      };
      writer.writeNext(data);
    }
    writer.close();
    return file;
  }

  public void uploadE06Report(String fileName) {
    backofficeDelegate10.uploadE06ReportFile(fileName);
  }
}
