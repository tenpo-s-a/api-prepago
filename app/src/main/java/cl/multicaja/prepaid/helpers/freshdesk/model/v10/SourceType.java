package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum  SourceType {

  //Antiguos items - No borrar
  /*EMAIL(1),
  PORTAL(2),
  PHONE(3),
  CHAT(7),
  MOBIHELP(8),
  FEEDBACKWIDGET(9),
  OUTBOUNDEMAIL(10);*/

  //TODO: Cambiaron algunos items, por ende su id difiere,
  // cuando se invoca la api con un id no registrado,
  // retorna un listado menor de id´s al que aparece en freshdesk en en el filtro origen
  // ACTION: Investigar por que eso sucede. PRIORITY: BAJA
  //Nuevos Items
  //1,2,3,5,6,7,8,9,10
  EMAIL(1),
  PORTAL(2),
  //4 no existe, los id son unicos desaparecen cuando se borra el item que lo contenia.
  PHONE(3),
  FORUM(5),
  TWITTER(6),
  FACEBOOK(7),
  CHAT(8),
  MOBIHELP(9),
  WIDGET_COMMENTS(10),
  OUTBOUNDEMAIL(11), //Solo aparece en el filtro de freshdesk
  ECOMERCE(12), //Solo aparece en el filtro de freshdesk
  BOT(13); //Solo aparece en el filtro de freshdesk

  private Integer value;

  SourceType(Integer value) {
    this.value = value;
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }

  private static Map<Integer, SourceType> FORMAT_MAP = Stream
    .of(SourceType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static SourceType fromValue(Integer value) {
    return FORMAT_MAP.get(value);
  }

}
