package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import model.luka.Luka;

public final class SerijalizacijaLuke {

    private static final Path PUTANJA = Path.of("luka.ser");

    private SerijalizacijaLuke() {
    }

    public static void sacuvaj(Luka luka) {
        if (luka == null) {
            throw new IllegalArgumentException("Luka ne smije biti null.");
        }

        try (ObjectOutputStream izlaz = new ObjectOutputStream(Files.newOutputStream(PUTANJA))) {
            izlaz.writeObject(luka);
        } catch (IOException e) {
            throw new IllegalStateException("Greška prilikom čuvanja luke.", e);
        }
    }

    public static Luka ucitajIliKreiraj(int brojTerminala) {
        if (!Files.exists(PUTANJA)) {
            return new Luka(brojTerminala);
        }

        try (ObjectInputStream ulaz = new ObjectInputStream(Files.newInputStream(PUTANJA))) {
            Object objekat = ulaz.readObject();

            if (!(objekat instanceof Luka)) {
                throw new IllegalStateException("Fajl ne sadrži ispravan objekat Luka.");
            }

            Luka luka = (Luka) objekat;

            if (luka.getBrojTerminala() != brojTerminala) {
                throw new IllegalStateException(
                        "Broj terminala u luka.ser ne odgovara broju iz properties fajla.");
            }

            return luka;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Greška prilikom učitavanja luke.", e);
        }
    }

    public static boolean postojiSacuvanaLuka() {
        return Files.exists(PUTANJA);
    }
}