package cl.multicaja.test.db_cdt;

public class Constants {

  public enum Tables
  {
    CUENTA("cdt_cuenta"),
    BOLSA("cdt_bolsa"),
    CUENTA_ACUMULADOR("cdt_cuenta_acumulador"),
    LIMITE("cdt_limite"),
    FASE_MOVIMIENTO("cdt_fase_movimiento"),
    //CATEGORIA_MOVIMIENTO("cdt_categoria_movimiento"),
    MOVIMIENTO_CUENTA("cdt_movimiento_cuenta"),
    REGLA_ACUMULACION("cdt_regla_acumulacion"),
    FASE_ACUMULADOR("cdt_fase_acumulador");
    //CATEGORIA_MOV_FASE("cdt_categoria_mov_fase"),
    //CONFIRMACION_MOV("cdt_confirmacion_movimiento");

    private String name;

    Tables(String name) {
      this.name = name;
    }
    public String getName() {
      return name;
    }

  }
  public enum Procedures
  {
    SP_CREA_BOLSA(".mc_cdt_crea_bolsa_v10"),
    SP_CREA_CUENTA(".mc_cdt_crea_cuenta_v10"),
    SP_CREA_LIMITE(".mc_cdt_crea_limite_v10"),
    SP_CREA_FASE_MOVIMIENTO(".mc_cdt_crea_fase_movimiento_v10"),
   // SP_CREA_CATEGORIA_MOVIMIENTO(".mc_cdt_crea_categoria_movimiento_v10"),
    //SP_CREA_MOV_TIPO_MOV(".mc_cdt_crea_categoria_mov_fase_v10"),
    SP_CREA_MOVIMIENTO_CUENTA(".mc_cdt_crea_movimiento_cuenta_v10"),
    SP_CREA_REGLA_ACUMULACION(".mc_cdt_crea_regla_acumulacion_v10"),
    SP_CARGA_FASES_MOVIMIENTOS(".mc_cdt_carga_fases_movimientos_v10");

    private String name;
    Procedures(String name) {
      this.name = name;
    }
    public String getName() {
      return name;
    }

  }

}
