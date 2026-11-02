package model.incident;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import model.plovila.Plovilo;

public class SlucajPotjernice implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();

    private final String identifikator;
    private final int brojTerminala;
    private final Plovilo obalskaStraza;
    private final Plovilo trazenoPlovilo;
    private final LocalDateTime vrijemeOtkrivanja;
    private final int trajanjeUvidjajaSekunde;

    private boolean zavrsen;

    public SlucajPotjernice(int brojTerminala, Plovilo obalskaStraza, Plovilo trazenoPlovilo) {
        identifikator = UUID.randomUUID().toString();
        this.brojTerminala = brojTerminala;
        this.obalskaStraza = obalskaStraza;
        this.trazenoPlovilo = trazenoPlovilo;
        vrijemeOtkrivanja = LocalDateTime.now();
        trajanjeUvidjajaSekunde = 3 + RANDOM.nextInt(3);
        zavrsen = false;
    }

    public String getIdentifikator() {
        return identifikator;
    }

    public int getBrojTerminala() {
        return brojTerminala;
    }

    public Plovilo getObalskaStraza() {
        return obalskaStraza;
    }

    public Plovilo getTrazenoPlovilo() {
        return trazenoPlovilo;
    }

    public LocalDateTime getVrijemeOtkrivanja() {
        return vrijemeOtkrivanja;
    }

    public int getTrajanjeUvidjajaSekunde() {
        return trajanjeUvidjajaSekunde;
    }

    public boolean isZavrsen() {
        return zavrsen;
    }

    public void zavrsi() {
        zavrsen = true;
    }

    @Override
    public String toString() {
        return "Potjernica " + identifikator
                + ", terminal=" + brojTerminala
                + ", obalska straža=" + obalskaStraza.getNaziv()
                + ", traženo plovilo=" + trazenoPlovilo.getNaziv()
                + ", IMO=" + trazenoPlovilo.getImoBroj()
                + ", trajanje=" + trajanjeUvidjajaSekunde + " s";
    }
}