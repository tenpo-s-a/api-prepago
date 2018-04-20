package cl.multicaja.prepago.kong;

import cl.multicaja.prepago.utils.Utils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class ExecuteMigrations extends BaseMigrations {

    public static void main(String[] args) {

        HashMap<String, String> mapParams = createMenu(args);

        if (mapParams == null) {
            return;
        }

        KONG_HOST = mapParams.get("kong_host");
        API_HOST = mapParams.get("api_host");

        boolean silence = mapParams.get("silence").trim().toLowerCase().equals("true");

        File[] files = getFilesMigrations();

        if (files != null && files.length > 0) {

            Utils.println("Iniciando ejecución de migraciones", Utils.ANSI_GREEN);

            for (File f : files) {
                if (f.getName().endsWith(".kong")) {
                    execute(f, silence);
                }
            }

            Utils.println("Ejecución de migraciones finalizada", Utils.ANSI_GREEN);

        } else {
            Utils.println("No existen migraciones para ejecutar", Utils.ANSI_RED);
        }
    }

    /**
     *
     * @param args
     * @return
     */
    private static HashMap<String, String> createMenu(String[] args) {

        System.out.println("Argumentos: " + Arrays.asList(args));

        Options options = new Options();

        try {

            options.addOption("kong_host", true,  "Host de kong, EJ: http://localhost:8001");
            options.addOption("api_host", true,  "Host del api, EJ: http://localhost:3200");
            options.addOption("silence", true,  "true: Ejecuta las migraciones sin arrojar error, false: Ejecuta las migraciones arrojando error");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmdLine = parser.parse(options, args);

            String kongHostValue =  cmdLine.getOptionValue("kong_host");
            if (kongHostValue == null){
                throw new org.apache.commons.cli.ParseException("El parametro kong_host es requerido");
            }

            String apiHostValue =  cmdLine.getOptionValue("api_host");
            if (apiHostValue == null){
                throw new org.apache.commons.cli.ParseException("El parametro api_host es requerido");
            }

            String silenceValue = cmdLine.getOptionValue("silence");
            if (silenceValue == null){
                silenceValue = "false";
            }

            HashMap<String, String> map = new HashMap<>();
            map.put("kong_host", kongHostValue);
            map.put("api_host", apiHostValue);
            map.put("silence", silenceValue);

            System.out.println("Argumentos map: " + map);

            return map;

        } catch(Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            new HelpFormatter().printHelp(ExecuteMigrations.class.getSimpleName(), options );
            return null;
        }
    }

    /**
     * ejecuta una migracion
     * @param file
     * @return
     */
    private static void execute(File file, boolean silence) {
        Utils.println("======== Ejecutando migración: " + file.getName() + " ========", Utils.ANSI_GREEN);
        String text = Utils.clearText(Utils.readFile(file));
        if (StringUtils.isNotBlank(text)) {
            String[] s = text.split("rollback:");
            if (s != null && s.length > 0) {
                String commands_text = s[0].replaceAll("execute:", "").trim();
                if (StringUtils.isNotBlank(commands_text)) {
                    String[] commands = commands_text.split("cmd:");
                    for (String cmd : commands) {
                        ResultCmd resultCmd = executeCmd(cmd);
                        if (resultCmd != null) {
                            int statusCode = resultCmd.getStatusCode();
                            if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
                                Utils.println("Resultado comando: " + resultCmd, Utils.ANSI_GREEN);
                            } else {
                                Utils.println("Resultado comando: " + resultCmd, Utils.ANSI_RED);
                                if (!silence) {
                                    throw new RuntimeException(resultCmd.toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
