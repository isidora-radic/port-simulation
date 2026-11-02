package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class CitacPotjernica {

    private CitacPotjernica() {
    }

    public static Set<Integer> ucitajImoBrojeve(String putanjaFajla) {
        Set<Integer> imoBrojevi = new HashSet<>();

        if (putanjaFajla == null || putanjaFajla.isBlank()) {
            return imoBrojevi;
        }

        Path putanja = Path.of(putanjaFajla);

        if (!Files.exists(putanja) || !Files.isRegularFile(putanja)) {
            return imoBrojevi;
        }

        try {
            String sadrzaj = Files.readString(putanja);
            String[] dijelovi = sadrzaj.split("[^0-9]+");

            for (String dio : dijelovi) {
                if (dio.isBlank()) {
                    continue;
                }

                try {
                    imoBrojevi.add(Integer.parseInt(dio));
                } catch (NumberFormatException e) {
                    // Neispravan broj se preskače.
                }
            }

            return imoBrojevi;
        } catch (IOException e) {
            throw new IllegalStateException("Nije moguće učitati fajl potjernica: " + putanja, e);
        }
    }
}