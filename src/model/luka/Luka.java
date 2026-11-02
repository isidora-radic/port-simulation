package model.luka;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.evidencija.NaplataLuckeTakse;
import model.plovila.Plovilo;
import model.sluzbe.TipSluzbe;
import util.GeneratorPlovila;
import util.ObracunTakse;

public class Luka implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Terminal> terminali;
    private Map<Integer, Long> evidencijaUlazaka;
    private List<NaplataLuckeTakse> evidencijaNaplata;

    public Luka(int brojTerminala) {
        if (brojTerminala <= 0) {
            throw new IllegalArgumentException("Broj terminala mora biti veći od nule.");
        }

        terminali = new ArrayList<>();

        for (int broj = 1; broj <= brojTerminala; broj++) {
            terminali.add(new Terminal(broj));
        }

        evidencijaUlazaka = new HashMap<>();
        evidencijaNaplata = new ArrayList<>();
    }

    public int getBrojTerminala() {
        return terminali.size();
    }

    public List<Terminal> getTerminali() {
        return Collections.unmodifiableList(terminali);
    }

    public Terminal getTerminal(int brojTerminala) {
        if (brojTerminala < 1 || brojTerminala > terminali.size()) {
            return null;
        }

        return terminali.get(brojTerminala - 1);
    }

    public Terminal pronadjiTerminalPlovila(Plovilo plovilo) {
        if (plovilo == null) {
            return null;
        }

        for (Terminal terminal : terminali) {
            if (terminal.pronadjiPozicijuPlovila(plovilo) != null) {
                return terminal;
            }
        }

        return null;
    }

    public synchronized Pozicija dodajPloviloNaTerminal(Plovilo plovilo, int brojTerminala) {
        if (plovilo == null || pronadjiTerminalPlovila(plovilo) != null) {
            return null;
        }

        Terminal terminal = getTerminal(brojTerminala);

        if (terminal == null || terminal.jePopunjen()) {
            return null;
        }

        Pozicija pozicija = terminal.priveziNaSlucajanDok(plovilo);

        if (pozicija != null) {
            evidentirajUlazak(plovilo);
        }

        return pozicija;
    }

    public synchronized boolean ukloniPlovilo(Plovilo plovilo) {
        Terminal terminal = pronadjiTerminalPlovila(plovilo);

        if (terminal == null) {
            return false;
        }

        return terminal.ukloniPlovilo(plovilo);
    }

    public int getUkupanBrojSlobodnihDokova() {
        int ukupno = 0;

        for (Terminal terminal : terminali) {
            ukupno += terminal.getBrojSlobodnihDokova();
        }

        return ukupno;
    }

    public boolean jePopunjena() {
        return getUkupanBrojSlobodnihDokova() == 0;
    }

    public Terminal pronadjiPrviTerminalSaSlobodnimDokom() {
        for (int brojTerminala = 1; brojTerminala <= getBrojTerminala(); brojTerminala++) {
            Terminal terminal = getTerminal(brojTerminala);

            if (terminal != null && terminal.getBrojSlobodnihDokova() > 0) {
                return terminal;
            }
        }

        return null;
    }

    public Plovilo pronadjiNajblizeSluzbenoPlovilo(TipSluzbe trazenaSluzba,
            int brojTerminalaIncidenta, Pozicija pozicijaIncidenta,
            Collection<Plovilo> iskljucenaPlovila) {

        Plovilo najblize = null;
        int najmanjaUdaljenost = Integer.MAX_VALUE;

        for (Terminal terminal : terminali) {
            for (Plovilo plovilo : terminal.getPlovila()) {
                if (!plovilo.pripadaSluzbi(trazenaSluzba)
                        || plovilo.isUcesnikNezgode()
                        || plovilo.isIdeNaIntervenciju()
                        || iskljucenaPlovila.contains(plovilo)) {
                    continue;
                }

                if (plovilo.getState() != Thread.State.NEW) {
                    continue;
                }

                Pozicija pozicijaPlovila = terminal.pronadjiPozicijuPlovila(plovilo);

                if (pozicijaPlovila == null) {
                    continue;
                }

                int udaljenostTerminala = Math.abs(
                        terminal.getBrojTerminala() - brojTerminalaIncidenta);
                int udaljenost = udaljenostTerminala * 100;

                if (udaljenostTerminala == 0 && pozicijaIncidenta != null) {
                    udaljenost += Math.abs(pozicijaPlovila.getRed() - pozicijaIncidenta.getRed());
                    udaljenost += Math.abs(pozicijaPlovila.getKolona() - pozicijaIncidenta.getKolona());
                }

                if (udaljenost < najmanjaUdaljenost) {
                    najmanjaUdaljenost = udaljenost;
                    najblize = plovilo;
                }
            }
        }

        return najblize;
    }

    public synchronized Pozicija posaljiNaIntervenciju(Plovilo plovilo,
            int brojTerminalaIncidenta, Pozicija pozicijaIncidenta) {

        if (plovilo == null) {
            return null;
        }

        Terminal ciljniTerminal = getTerminal(brojTerminalaIncidenta);
        Terminal trenutniTerminal = pronadjiTerminalPlovila(plovilo);

        if (ciljniTerminal == null) {
            return null;
        }

        if (trenutniTerminal == null || trenutniTerminal == ciljniTerminal) {
            return ciljniTerminal.postaviNaIntervenciju(plovilo, pozicijaIncidenta);
        }

        if (!trenutniTerminal.ukloniPlovilo(plovilo)) {
            return null;
        }

        Pozicija pozicijaIntervencije =
                ciljniTerminal.postaviNaIntervenciju(plovilo, pozicijaIncidenta);

        if (pozicijaIntervencije == null) {
            trenutniTerminal.priveziNaSlucajanDok(plovilo);
            return null;
        }

        return pozicijaIntervencije;
    }

    public synchronized Plovilo pronadjiIliGenerisiSluzbenoPlovilo(TipSluzbe sluzba,
            int brojTerminalaIncidenta, Pozicija pozicijaIncidenta,
            Collection<Plovilo> iskljucenaPlovila) {

        Plovilo plovilo = pronadjiNajblizeSluzbenoPlovilo(
                sluzba, brojTerminalaIncidenta, pozicijaIncidenta, iskljucenaPlovila);

        if (plovilo == null) {
            plovilo = GeneratorPlovila.generisiSluzbenoPlovilo(sluzba);
        }

        if (plovilo != null) {
            plovilo.setIdeNaIntervenciju(true);
            plovilo.setRotacija(true);
        }

        return plovilo;
    }

    public int normalizujPocetnoStanje() {
        int ukupnoVraceno = 0;

        for (Terminal terminal : terminali) {
            ukupnoVraceno += terminal.normalizujPocetnoStanje();
        }

        return ukupnoVraceno;
    }

    public synchronized void evidentirajUlazak(Plovilo plovilo) {
        if (plovilo == null) {
            return;
        }

        plovilo.evidentirajUlazak();
        evidencijaUlazaka.put(plovilo.getImoBroj(), plovilo.getVrijemeUlaska());
    }

    public synchronized Map<Integer, Long> getEvidencijaUlazaka() {
        return Collections.unmodifiableMap(new HashMap<>(evidencijaUlazaka));
    }

    public synchronized NaplataLuckeTakse evidentirajIzlazak(
            Plovilo plovilo, long milisekundiPoSatu) {

        if (plovilo == null) {
            return null;
        }

        for (NaplataLuckeTakse postojeca : evidencijaNaplata) {
            if (postojeca.getImoBroj() == plovilo.getImoBroj()
                    && postojeca.getVrijemeUlaska() == plovilo.getVrijemeUlaska()) {
                return postojeca;
            }
        }

        NaplataLuckeTakse naplata = ObracunTakse.obracunaj(plovilo, milisekundiPoSatu);
        evidencijaNaplata.add(naplata);

        return naplata;
    }

    public synchronized List<NaplataLuckeTakse> getEvidencijaNaplata() {
        return Collections.unmodifiableList(new ArrayList<>(evidencijaNaplata));
    }

    private void readObject(ObjectInputStream ulaz) throws IOException, ClassNotFoundException {
        ulaz.defaultReadObject();

        if (evidencijaUlazaka == null) {
            evidencijaUlazaka = new HashMap<>();

            for (Terminal terminal : terminali) {
                for (Plovilo plovilo : terminal.getPlovila()) {
                    if (plovilo.getVrijemeUlaska() == 0) {
                        plovilo.evidentirajUlazak();
                    }

                    evidencijaUlazaka.put(plovilo.getImoBroj(), plovilo.getVrijemeUlaska());
                }
            }
        }

        if (evidencijaNaplata == null) {
            evidencijaNaplata = new ArrayList<>();
        }
    }

    public synchronized void obrisiEvidencijuNaplata() {
        evidencijaNaplata.clear();
    }

    @Override
    public String toString() {
        StringBuilder prikaz = new StringBuilder();

        for (Terminal terminal : terminali) {
            prikaz.append(terminal);
            prikaz.append(System.lineSeparator());
        }

        return prikaz.toString();
    }
}