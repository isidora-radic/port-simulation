package model.evidencija;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NaplataLuckeTakse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int imoBroj;
    private final String nazivPlovila;
    private final long vrijemeUlaska;
    private final long vrijemeIzlaska;
    private final int brojSati;
    private final int iznosKM;
    private final boolean oslobodjenoPlacanja;

    public NaplataLuckeTakse(int imoBroj, String nazivPlovila, long vrijemeUlaska,
            long vrijemeIzlaska, int brojSati, int iznosKM, boolean oslobodjenoPlacanja) {

        this.imoBroj = imoBroj;
        this.nazivPlovila = nazivPlovila;
        this.vrijemeUlaska = vrijemeUlaska;
        this.vrijemeIzlaska = vrijemeIzlaska;
        this.brojSati = brojSati;
        this.iznosKM = iznosKM;
        this.oslobodjenoPlacanja = oslobodjenoPlacanja;
    }

    public int getImoBroj() {
        return imoBroj;
    }

    public String getNazivPlovila() {
        return nazivPlovila;
    }

    public long getVrijemeUlaska() {
        return vrijemeUlaska;
    }

    public long getVrijemeIzlaska() {
        return vrijemeIzlaska;
    }

    public int getBrojSati() {
        return brojSati;
    }

    public int getIznosKM() {
        return iznosKM;
    }

    public boolean isOslobodjenoPlacanja() {
        return oslobodjenoPlacanja;
    }

    public LocalDateTime getDatumVrijemeUlaska() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(vrijemeUlaska), ZoneId.systemDefault());
    }

    public LocalDateTime getDatumVrijemeIzlaska() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(vrijemeIzlaska), ZoneId.systemDefault());
    }

    @Override
    public String toString() {
        return "IMO=" + imoBroj
                + ", naziv=" + nazivPlovila
                + ", sati=" + brojSati
                + ", iznos=" + iznosKM + " KM"
                + ", oslobođeno=" + oslobodjenoPlacanja;
    }
}