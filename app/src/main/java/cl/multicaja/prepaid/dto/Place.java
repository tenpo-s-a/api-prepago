package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class Place {
  private Integer country_iso_3266_code;
  private String country_description;
  private String place_name;

  public Integer getCountry_iso_3266_code() {
    return country_iso_3266_code;
  }

  public void setCountry_iso_3266_code(Integer country_iso_3266_code) {
    this.country_iso_3266_code = country_iso_3266_code;
  }

  public String getCountry_description() {
    return country_description;
  }

  public void setCountry_description(String country_description) {
    this.country_description = country_description;
  }

  public String getPlace_name() {
    return place_name;
  }

  public void setPlace_name(String place_name) {
    this.place_name = place_name;
  }
}
