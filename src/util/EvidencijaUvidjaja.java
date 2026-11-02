package util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import model.incident.PomorskaNezgoda;
import model.incident.SlucajPotjernice;

public final class EvidencijaUvidjaja {

    private EvidencijaUvidjaja() {
    }

    public static Path sacuvaj(PomorskaNezgoda nezgoda) {
        if (nezgoda == null) {
            throw new IllegalArgumentException("Nezgoda ne smije biti null.");
        }

        String nazivFajla = "uvidjaj-" + nezgoda.getIdentifikator() + ".bin";
        Path putanja = Path.of(System.getProperty("user.home"), nazivFajla);

        try (ObjectOutputStream izlaz = new ObjectOutputStream(Files.newOutputStream(putanja))) {
            izlaz.writeObject(nezgoda);
            return putanja;
        } catch (IOException e) {
            throw new IllegalStateException("Nije moguće sačuvati uviđaj u " + putanja, e);
        }
    }

    public static Path sacuvaj(SlucajPotjernice slucaj) {
        if (slucaj == null) {
            throw new IllegalArgumentException("Slučaj potjernice ne smije biti null.");
        }

        String nazivFajla = "potjernica-" + slucaj.getIdentifikator() + ".bin";
        Path putanja = Path.of(System.getProperty("user.home"), nazivFajla);

        try (ObjectOutputStream izlaz = new ObjectOutputStream(Files.newOutputStream(putanja))) {
            izlaz.writeObject(slucaj);
            return putanja;
        } catch (IOException e) {
            throw new IllegalStateException("Nije moguće sačuvati slučaj potjernice u " + putanja, e);
        }
    }
}