package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class Place10 extends BaseModel {

  private Integer countryIso3266Code;
  private String countryDescription;
  private String placeName;

  public Place10() {
    super();
  }

  public Integer getCountryIso3266Code() {
    return countryIso3266Code;
  }

  public void setCountryIso3266Code(Integer countryIso3266Code) {
    this.countryIso3266Code = countryIso3266Code;
  }

  public String getCountryDescription() {
    return countryDescription;
  }

  public void setCountryDescription(String countryDescription) {
    this.countryDescription = countryDescription;
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }
}
