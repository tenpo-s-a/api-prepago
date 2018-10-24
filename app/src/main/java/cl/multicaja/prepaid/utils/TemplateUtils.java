package cl.multicaja.prepaid.utils;

public class TemplateUtils {

  public static String freshDeskTemplateColas1(String template, String proceso, String nombres, String rut, Long idUsuario, String numAut,Long monto){
    template = template.replace("{process}",proceso);
    template = template.replace("{nombres}",nombres);
    template = template.replace("{rut}",rut);
    template = template.replace("{IdUsuario}",String.valueOf(idUsuario));
    template = template.replace("{numaut}",String.valueOf(numAut));
    template = template.replace("{monto}",String.valueOf(monto));
    return template;
  }
  public static String freshDeskTemplateColas2(String template, String proceso, String nombres, String rut, Long idUsuario){
    template = template.replace("{process}",proceso);
    template = template.replace("{nombres}",nombres);
    template = template.replace("{rut}",rut);
    template = template.replace("{IdUsuario}",String.valueOf(idUsuario));
    return template;
  }

}
