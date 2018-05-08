package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class Place {

  private Integer countryIso3266Code;
  private String countryDescription;
  private String placeName;

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
