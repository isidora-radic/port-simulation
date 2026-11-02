package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Konfiguracija {

    private static final String PUTANJA = "/config/luka.properties";
    private static final String KLJUC_BROJ_TERMINALA = "broj.terminala";
    private static final String KLJUC_MILISEKUNDI_PO_SATU = "milisekundi.po.satu";

    private Konfiguracija() {
    }

    public static int ucitajBrojTerminala() {
        String vrijednost = ucitajProperties().getProperty(KLJUC_BROJ_TERMINALA);

        if (vrijednost == null || vrijednost.isBlank()) {
            throw new IllegalStateException("Nije definisan broj terminala.");
        }

        try {
            int brojTerminala = Integer.parseInt(vrijednost.trim());

            if (brojTerminala <= 0) {
                throw new IllegalStateException("Broj terminala mora biti veći od nule.");
            }

            return brojTerminala;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Broj terminala nije ispravan: " + vrijednost, e);
        }
    }

    public static long ucitajMilisekundiPoSatu() {
        String vrijednost = ucitajProperties().getProperty(KLJUC_MILISEKUNDI_PO_SATU);

        if (vrijednost == null || vrijednost.isBlank()) {
            throw new IllegalStateException("Nije definisana skala vremena.");
        }

        try {
            long milisekundi = Long.parseLong(vrijednost.trim());

            if (milisekundi <= 0) {
                throw new IllegalStateException("Skala vremena mora biti veća od nule.");
            }

            return milisekundi;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Neispravna skala vremena: " + vrijednost, e);
        }
    }

    private static Properties ucitajProperties() {
        Properties properties = new Properties();

        try (InputStream ulaz = Konfiguracija.class.getResourceAsStream(PUTANJA)) {
            if (ulaz == null) {
                throw new IllegalStateException("Nije pronađen fajl: " + PUTANJA);
            }

            properties.load(ulaz);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Greška prilikom učitavanja konfiguracije.", e);
        }
    }
}