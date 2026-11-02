package model.incident;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import model.luka.Pozicija;
import model.plovila.Plovilo;

public class PomorskaNezgoda implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();

    private final String identifikator;
    private final int brojTerminala;
    private final Plovilo prvoPlovilo;
    private final Plovilo drugoPlovilo;
    private final Pozicija pozicija;
    private final LocalDateTime vrijemeNezgode;
    private final int trajanjeUvidjajaSekunde;

    private boolean zavrsena;

    public PomorskaNezgoda(int brojTerminala, Plovilo prvoPlovilo,
            Plovilo drugoPlovilo, Pozicija pozicija) {

        identifikator = UUID.randomUUID().toString();
        this.brojTerminala = brojTerminala;
        this.prvoPlovilo = prvoPlovilo;
        this.drugoPlovilo = drugoPlovilo;
        this.pozicija = pozicija;
        vrijemeNezgode = LocalDateTime.now();
        trajanjeUvidjajaSekunde = 3 + RANDOM.nextInt(8);
        zavrsena = false;
    }

    public String getIdentifikator() {
        return identifikator;
    }

    public int getBrojTerminala() {
        return brojTerminala;
    }

    public Plovilo getPrvoPlovilo() {
        return prvoPlovilo;
    }

    public Plovilo getDrugoPlovilo() {
        return drugoPlovilo;
    }

    public Pozicija getPozicija() {
        return pozicija;
    }

    public LocalDateTime getVrijemeNezgode() {
        return vrijemeNezgode;
    }

    public int getTrajanjeUvidjajaSekunde() {
        return trajanjeUvidjajaSekunde;
    }

    public boolean isZavrsena() {
        return zavrsena;
    }

    public void zavrsiUvidjaj() {
        zavrsena = true;
    }

    @Override
    public String toString() {
        return "Pomorska nezgoda " + identifikator
                + ", terminal=" + brojTerminala
                + ", pozicija=" + pozicija
                + ", prvo plovilo=" + prvoPlovilo.getNaziv()
                + ", drugo plovilo=" + drugoPlovilo.getNaziv()
                + ", trajanje uviđaja=" + trajanjeUvidjajaSekunde + " s";
    }
}