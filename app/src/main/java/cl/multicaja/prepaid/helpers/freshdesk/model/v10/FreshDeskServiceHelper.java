package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import cl.multicaja.prepaid.external.freshdesk.FreshdeskService;
import cl.multicaja.prepaid.external.freshdesk.FreshdeskServiceImpl;
import cl.multicaja.prepaid.external.freshdesk.model.*;
import cl.multicaja.prepaid.external.freshdesk.model.Ticket;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class FreshDeskServiceHelper {

  //TODO Reemplazar por properties.
  private final String apiUrl = "https://multicaja.freshdesk.com";
  private final String apiUser = "CEFMK1T4XuXYrBhbuN";
  private final String apiPassword = "X";
  private final Boolean isEnabled = Boolean.TRUE;

  private FreshdeskService freshdeskService = new FreshdeskServiceImpl(apiUrl,apiUser,apiPassword,isEnabled);

  public FreshdeskService getFreshdeskService() {
    return freshdeskService;
  }


  public Ticket createTicketInFreshdeskByFreshdeskContact(PrepaidUser10 prepaidUser10, NewTicket newTicket){

    Ticket ticket = null;
    //Contact contact = freshdeskService.findContact(user.getFreshDeskId()); //TODO: Prepaid User debería tener este id, el cual pueda consumir la capa C y viceversa
    Contact contact = null;

    if(contact.getActive()){
      if(contact.getId() != null && contact.getId() > 0){
        ticket = getFreshdeskService().createTicket(newTicket);
      }else{
        //Create Contact then create ticket
      }
    }else{
      //Exception
    }
    return ticket;
  }

  public Ticket createTicketInFreshdesk(NewTicket newTicket){
    return getFreshdeskService().createTicket(newTicket);
  }

  public TicketsResponse getTicketsByTypeAndCreatedDate(Integer page, LocalDateTime from, LocalDateTime to, String type) throws Exception{
    return getFreshdeskService().getTicketsByTypeAndCreatedDate(page,from,to,type);
  }

  public GroupsResponse getGroups() throws IOException {
    return getFreshdeskService().listGroups();
  }

  public ProductsResponse getProducts() throws IOException {
    return getFreshdeskService().listProducts();
  }

  public Boolean isClosedOrResolved(Long status) {
    return (status.equals(StatusType.CLOSED) || status.equals(StatusType.RESOLVED));
  }

}
