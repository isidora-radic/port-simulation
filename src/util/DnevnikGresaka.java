package util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class DnevnikGresaka {

    private static final Logger LOGGER = Logger.getLogger("LukaLogger");

    static {
        try {
            FileHandler fileHandler = new FileHandler("error.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Logger nije moguće pokrenuti: " + e.getMessage());
        }
    }

    private DnevnikGresaka() {
    }

    public static void inicijalizuj() {
        // Poziv ove metode aktivira statički blok.
    }

    public static void evidentiraj(String poruka, Throwable greska) {
        LOGGER.log(Level.SEVERE, poruka, greska);
    }
}