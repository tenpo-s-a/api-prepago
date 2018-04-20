package cl.multicaja.prepago.kong;

import cl.multicaja.prepago.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class RollbackMigrations extends BaseMigrations {

    public static void main(String[] args) {

        File[] files = getFilesMigrations();

        if (files != null && files.length > 0) {

            Utils.println("Iniciando ejecución de rollback de migraciones", Utils.ANSI_GREEN);

            for (File f : files) {
                if (f.getName().endsWith(".kong")) {
                    rollback(f);
                }
            }

        } else {
            Utils.println("No existen migraciones para ejecutar", Utils.ANSI_RED);
        }
    }

    /**
     * ejecuta un rollback
     * @param file
     * @return
     */
    private static boolean rollback(File file) {
        Utils.println("======== Ejecutando rollback migración: " + file.getName() + " ========", Utils.ANSI_GREEN);
        boolean ok = false;
        String text = Utils.clearText(Utils.readFile(file));
        if (StringUtils.isNotBlank(text)) {
            String[] s = text.split("rollback:");
            if (s != null && s.length > 0) {
                String commands_text = s[1].replaceAll("execute:", "").trim();
                if (StringUtils.isNotBlank(commands_text)) {
                    String[] commands = commands_text.split("cmd:");
                    for (String cmd : commands) {
                        ResultCmd resultCmd = executeCmd(cmd);
                        int statusCode = resultCmd.getStatusCode();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
                            Utils.println("Resultado comando: " + resultCmd, Utils.ANSI_GREEN);
                        } else {
                            Utils.println("Resultado comando: " + resultCmd, Utils.ANSI_RED);
                        }
                    }
                }
            }
        }
        return ok;
    }
}
