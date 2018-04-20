package cl.multicaja.prepago.kong;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class BaseMigrations {

    protected static File DIR_MIGRATIONS = new File("./migrations");
    protected static File DIR_TEST_MIGRATIONS = new File("./src/test/java/cl/multicaja/prepago/test/kong");

    protected static String KONG_HOST = "http://localhost:8001";
    protected static String API_HOST = "http://localhost:3200";

    protected static File[] getFilesMigrations() {
        File[] files = DIR_MIGRATIONS.listFiles();
        Arrays.sort(files);
        return files;
    }

    /**
     * ejecuta un comando para ek api de kong
     * @param cmd
     * @return
     */
    protected static ResultCmd executeCmd(String cmd) {

        ResultCmd resultCmdReturn = null;

        cmd = cmd.replaceAll("\\{KONG_HOST}", KONG_HOST).replaceAll("\\{API_HOST}", API_HOST).replaceAll("\n", " ").replaceAll("'", "").trim();

        if (StringUtils.isNotBlank(cmd) && cmd.startsWith("curl ")) {

            //String[] params = commandToArrayParams(cmd);
            System.out.println("Ejecutando comando: " + cmd);

            try {

                ResultCmd resultCmd = new ResultCmd();
                Process proc = Runtime.getRuntime().exec(cmd);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    s = s.trim();
                    if (s.contains("HTTP/")) {
                        resultCmd.setStatusCode(NumberUtils.toInt(s.split(" ")[1], -1));
                        resultCmd.setStatus(s);
                    } else if (s.startsWith("{")){
                        resultCmd.setMessage(s);
                    }
                }

                resultCmdReturn = resultCmd;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return resultCmdReturn;
    }
}
