package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import model.evidencija.NaplataLuckeTakse;

public final class IzvozNaplataCSV {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

    private IzvozNaplataCSV() {
    }

    public static void izvezi(List<NaplataLuckeTakse> naplate, Path putanja) {
        if (naplate == null || putanja == null) {
            throw new IllegalArgumentException("Naplate i putanja su obavezni.");
        }

        try (BufferedWriter pisac = Files.newBufferedWriter(putanja, StandardCharsets.UTF_8)) {
            pisac.write('\uFEFF');
            pisac.write("IMO broj,Naziv plovila,Vrijeme ulaska,Vrijeme izlaska,"
                    + "Broj sati,Iznos KM,Oslobođeno plaćanja");
            pisac.newLine();

            for (NaplataLuckeTakse naplata : naplate) {
                String red = naplata.getImoBroj()
                        + "," + csvVrijednost(naplata.getNazivPlovila())
                        + "," + naplata.getDatumVrijemeUlaska().format(FORMAT)
                        + "," + naplata.getDatumVrijemeIzlaska().format(FORMAT)
                        + "," + naplata.getBrojSati()
                        + "," + naplata.getIznosKM()
                        + "," + (naplata.isOslobodjenoPlacanja() ? "DA" : "NE");

                pisac.write(red);
                pisac.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nije moguće izvesti CSV evidenciju.", e);
        }
    }

    private static String csvVrijednost(String vrijednost) {
        if (vrijednost == null) {
            return "";
        }

        String ocisceno = vrijednost.replace("\"", "\"\"");
        return "\"" + ocisceno + "\"";
    }
}