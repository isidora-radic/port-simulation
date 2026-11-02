package util;

import model.evidencija.NaplataLuckeTakse;
import model.plovila.Plovilo;

public final class ObracunTakse {

    private ObracunTakse() {
    }

    public static NaplataLuckeTakse obracunaj(Plovilo plovilo, long milisekundiPoSatu) {
        if (plovilo == null) {
            throw new IllegalArgumentException("Plovilo ne smije biti null.");
        }

        if (milisekundiPoSatu <= 0) {
            throw new IllegalArgumentException("Skala vremena mora biti pozitivna.");
        }

        long vrijemeIzlaska = System.currentTimeMillis();
        long vrijemeUlaska = plovilo.getVrijemeUlaska();

        if (vrijemeUlaska <= 0) {
            vrijemeUlaska = vrijemeIzlaska;
        }

        long trajanje = Math.max(0, vrijemeIzlaska - vrijemeUlaska);
        int brojSati = (int) Math.max(1, Math.ceil((double) trajanje / milisekundiPoSatu));
        boolean oslobodjeno = plovilo.jeSluzbenoPlovilo();
        int iznos = oslobodjeno ? 0 : izracunajIznos(brojSati);

        return new NaplataLuckeTakse(plovilo.getImoBroj(), plovilo.getNaziv(), vrijemeUlaska,
                vrijemeIzlaska, brojSati, iznos, oslobodjeno);
    }

    private static int izracunajIznos(int brojSati) {
        if (brojSati <= 1) {
            return 100;
        }

        int brojPerioda = (int) Math.ceil(brojSati / 12.0);
        return brojPerioda * 1000;
    }
}