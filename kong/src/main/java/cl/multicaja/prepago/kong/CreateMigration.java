package cl.multicaja.prepago.kong;

import cl.multicaja.prepago.utils.Utils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateMigration extends BaseMigrations {

    public static void main(String[] args) {

        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String id = df.format(new Date());

        File fileMigration = new File(DIR_MIGRATIONS, String.format("%s.kong", id));

        if (fileMigration.exists()) {
            Utils.println("La migración " + fileMigration.getAbsolutePath() + " ya existe, intenta nuevamente", Utils.ANSI_RED);
            return;
        }

        File fileTest = new File(DIR_TEST_MIGRATIONS, String.format("Test_%s.java", id));

        if (fileTest.exists()) {
          Utils.println("La clase de test de la migración " + fileTest.getAbsolutePath() + " ya existe, intenta nuevamente", Utils.ANSI_RED);
          return;
        }

        try {

            fileMigration.createNewFile();

            String textMigration = IOUtils.toString(CreateMigration.class.getResourceAsStream("/template.kong"), "UTF-8");

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileMigration), "UTF-8");
            writer.write(textMigration);
            writer.flush();
            writer.close();

            Utils.println("Migracion creada: " + fileMigration.getAbsolutePath(), Utils.ANSI_GREEN);

            fileTest.createNewFile();

            String textTest = IOUtils.toString(CreateMigration.class.getResourceAsStream("/Test_template.java.txt"), "UTF-8");

            textTest = textTest.replace("Test_template", fileTest.getName().replace(".java", ""));

            OutputStreamWriter writer2 = new OutputStreamWriter(new FileOutputStream(fileTest), "UTF-8");
            writer2.write(textTest);
            writer2.flush();
            writer2.close();

            Utils.println("Test de migracion creada: " + fileTest.getAbsolutePath(), Utils.ANSI_GREEN);

        } catch(Exception ex) {
            Utils.println("Error al crear migracion: " + fileMigration.getAbsolutePath(), Utils.ANSI_RED);
            ex.printStackTrace();
        }
    }

}
