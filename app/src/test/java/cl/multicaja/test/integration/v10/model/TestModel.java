package cl.multicaja.test.integration.v10.model;

public class TestModel {

  private Integer id;
  private String name;

  public TestModel() {
  }

  public TestModel(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
