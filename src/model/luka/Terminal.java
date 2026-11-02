package model.luka;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import model.incident.PomorskaNezgoda;
import model.plovila.Plovilo;

public class Terminal implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();

    public static final int BROJ_REDOVA = 4;
    public static final int BROJ_KOLONA = 17;
    public static final int BROJ_DOKOVA = 30;

    private final int brojTerminala;
    private final PoljeTerminala[][] polja;
    private volatile boolean blokiran;
    private volatile PomorskaNezgoda aktivnaNezgoda;
    private List<PomorskaNezgoda> nezgode;
    private transient Consumer<PomorskaNezgoda> slusalacNezgode;
    private transient Plovilo[][] rezervacijePreticanja;

    public Terminal(int brojTerminala) {
        this.brojTerminala = brojTerminala;
        this.polja = new PoljeTerminala[BROJ_REDOVA][BROJ_KOLONA];
        this.blokiran = false;
        this.aktivnaNezgoda = null;
        this.nezgode = new ArrayList<>();
        this.slusalacNezgode = null;
        inicijalizujPolja();
        this.rezervacijePreticanja = new Plovilo[BROJ_REDOVA][BROJ_KOLONA];
    }

    private void inicijalizujPolja() {
        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                Pozicija pozicija = new Pozicija(red, kolona);
                TipPolja tip = odrediTipPolja(red, kolona);

                polja[red][kolona] = new PoljeTerminala(pozicija, tip);
            }
        }
    }

    private TipPolja odrediTipPolja(int red, int kolona) {
        if (kolona == 0) {
            return TipPolja.DOLAZNI_KANAL;
        }

        if (kolona == 1) {
            return TipPolja.ODLAZNI_KANAL;
        }

        if (red == 0 || red == 3) {
            return TipPolja.DOK;
        }

        return TipPolja.KRUZNA_RUTA;
    }

    public int getBrojTerminala() {
        return brojTerminala;
    }

    public boolean isBlokiran() {
        return blokiran;
    }

    public PoljeTerminala getPolje(int red, int kolona) {
        if (!ispravnaPozicija(red, kolona)) {
            return null;
        }

        return polja[red][kolona];
    }

    public boolean ispravnaPozicija(int red, int kolona) {
        return red >= 0 && red < BROJ_REDOVA && kolona >= 0 && kolona < BROJ_KOLONA;
    }

    public int getBrojSlobodnihDokova() {
        int brojSlobodnih = 0;

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                PoljeTerminala polje = polja[red][kolona];

                if (polje.getTip() == TipPolja.DOK && polje.jeSlobodno()) {
                    brojSlobodnih++;
                }
            }
        }

        return brojSlobodnih;
    }

    public boolean imaSlobodanDok() {
        return getBrojSlobodnihDokova() > 0;
    }

    public boolean jePopunjen() {
        return getBrojSlobodnihDokova() == 0;
    }

    public synchronized boolean postaviNaUlaz(Plovilo plovilo) {
        if (plovilo == null || blokiran || pronadjiPozicijuPlovila(plovilo) != null) {
            return false;
        }

        PoljeTerminala ulaz = polja[0][0];

        if (!ulaz.jeSlobodno()) {
            return false;
        }

        return ulaz.zauzmi(plovilo);
    }

    public boolean jeUlazSlobodan() {
        return !blokiran && polja[0][0].jeSlobodno();
    }

    public synchronized boolean priveziNaDok(Plovilo plovilo, int red, int kolona) {

        if (plovilo == null || !ispravnaPozicija(red, kolona)) {
            return false;
        }

        PoljeTerminala polje = polja[red][kolona];

        if (polje.getTip() != TipPolja.DOK) {
            return false;
        }

        if (pronadjiPozicijuPlovila(plovilo) != null) {
            return false;
        }

        return polje.zauzmi(plovilo);
    }

    public List<Pozicija> getSlobodniDokovi() {
        List<Pozicija> slobodniDokovi = new ArrayList<>();

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                PoljeTerminala polje = polja[red][kolona];

                if (polje.getTip() == TipPolja.DOK && polje.jeSlobodno()) {
                    slobodniDokovi.add(polje.getPozicija());
                }
            }
        }

        return slobodniDokovi;
    }

    public synchronized Pozicija priveziNaSlucajanDok(Plovilo plovilo) {

        if (plovilo == null || pronadjiPozicijuPlovila(plovilo) != null) {
            return null;
        }

        List<Pozicija> slobodniDokovi = getSlobodniDokovi();

        if (slobodniDokovi.isEmpty()) {
            return null;
        }

        Pozicija izabraniDok = slobodniDokovi.get(RANDOM.nextInt(slobodniDokovi.size()));

        boolean uspjesno = polja[izabraniDok.getRed()]
                [izabraniDok.getKolona()].zauzmi(plovilo);

        return uspjesno ? izabraniDok : null;
    }

    public Pozicija pronadjiPozicijuPlovila(Plovilo plovilo) {
        if (plovilo == null) {
            return null;
        }

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                if (polja[red][kolona].getPlovilo() == plovilo) {
                    return polja[red][kolona].getPozicija();
                }
            }
        }

        return null;
    }

    public synchronized boolean premjestiPlovilo(Plovilo plovilo, int noviRed, int novaKolona) {

        if (plovilo == null || !ispravnaPozicija(noviRed, novaKolona)) {
            return false;
        }
        if (blokiran && !plovilo.isIdeNaIntervenciju()) {
            return false;
        }

        Pozicija staraPozicija = pronadjiPozicijuPlovila(plovilo);

        if (staraPozicija == null) {
            return false;
        }

        if (staraPozicija.getRed() == noviRed && staraPozicija.getKolona() == novaKolona) {
            return true;
        }

        PoljeTerminala staroPolje =
                polja[staraPozicija.getRed()][staraPozicija.getKolona()];

        PoljeTerminala novoPolje = polja[noviRed][novaKolona];

        if (rezervacijePreticanja != null) {

            Plovilo vlasnikRezervacije = rezervacijePreticanja[noviRed][novaKolona];

            if (vlasnikRezervacije != null && vlasnikRezervacije != plovilo) {
                return false;
            }
        }

        if (!novoPolje.jeSlobodno()) {
            return false;
        }

        if (!novoPolje.zauzmi(plovilo)) {
            return false;
        }

        boolean staroOslobodjeno = staroPolje.oslobodi(plovilo);

        if (!staroOslobodjeno) {
            novoPolje.oslobodi(plovilo);
            return false;
        }

        return true;
    }

    public synchronized boolean ukloniPlovilo(Plovilo plovilo) {
        Pozicija pozicija = pronadjiPozicijuPlovila(plovilo);

        if (pozicija == null) {
            return false;
        }

        return polja[pozicija.getRed()]
                [pozicija.getKolona()]
                .oslobodi(plovilo);
    }

    public List<Plovilo> getPlovila() {
        List<Plovilo> plovila = new ArrayList<>();

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                Plovilo plovilo = polja[red][kolona].getPlovilo();

                if (plovilo != null && !plovila.contains(plovilo)) {
                    plovila.add(plovilo);
                }
            }
        }

        return Collections.unmodifiableList(plovila);
    }

    public String prikaziTerminal() {
        StringBuilder prikaz = new StringBuilder();

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                PoljeTerminala polje = polja[red][kolona];

                if (!polje.jeSlobodno()) {
                    prikaz.append(String.format("%-4s", polje));
                } else {
                    prikaz.append(String.format("%-4s", oznakaPraznogPolja(red, kolona)));
                }
            }

            prikaz.append(System.lineSeparator());
        }

        return prikaz.toString();
    }

    private String oznakaPraznogPolja(int red, int kolona) {
        TipPolja tip = polja[red][kolona].getTip();

        if (tip == TipPolja.DOK) {
            return "*";
        }

        if (tip == TipPolja.DOLAZNI_KANAL) {
            return "↓";
        }

        if (tip == TipPolja.ODLAZNI_KANAL) {
            return "↑";
        }

        if (red == 1) {
            return "←";
        }

        return "→";
    }

    public Plovilo getPloviloNaPoziciji(Pozicija pozicija) {

        if (pozicija == null || !ispravnaPozicija(pozicija.getRed(), pozicija.getKolona())) {
            return null;
        }

        return polja[pozicija.getRed()]
                [pozicija.getKolona()]
                .getPlovilo();
    }

    public synchronized List<Pozicija> rezervisiPutanjuPreticanja(
            Plovilo plovilo, Pozicija zauzetaPozicija, Pozicija pozicijaNakonPreticanja) {

        if (blokiran && (plovilo == null || !plovilo.isIdeNaIntervenciju())) {

            return Collections.emptyList();
        }

        Pozicija trenutna = pronadjiPozicijuPlovila(plovilo);

        /*
         * Preticanje je dozvoljeno samo na
         * horizontalnim dijelovima kanala.
         */
        if (trenutna.getRed() != 1 && trenutna.getRed() != 2) {
            return Collections.emptyList();
        }

        if (zauzetaPozicija.getRed() != trenutna.getRed()
                || pozicijaNakonPreticanja.getRed() != trenutna.getRed()) {
            return Collections.emptyList();
        }

        int smjer = zauzetaPozicija.getKolona() - trenutna.getKolona();

        if (Math.abs(smjer) != 1) {
            return Collections.emptyList();
        }

        if (pozicijaNakonPreticanja.getKolona() != zauzetaPozicija.getKolona() + smjer) {
            return Collections.emptyList();
        }

        if (getPloviloNaPoziciji(zauzetaPozicija) == null) {
            return Collections.emptyList();
        }

        int lijeviRed = trenutna.getRed() == 2 ? 1 : 2;

        Pozicija lijevo = new Pozicija(lijeviRed, trenutna.getKolona());

        Pozicija lijevoPored = new Pozicija(lijeviRed, zauzetaPozicija.getKolona());

        List<Pozicija> putanja = new ArrayList<>();

        putanja.add(lijevo);
        putanja.add(lijevoPored);
        putanja.add(pozicijaNakonPreticanja);

        if (rezervacijePreticanja == null) {
            rezervacijePreticanja = new Plovilo[BROJ_REDOVA][BROJ_KOLONA];
        }

        for (Pozicija pozicija : putanja) {

            if (!ispravnaPozicija(pozicija.getRed(), pozicija.getKolona())) {
                return Collections.emptyList();
            }

            PoljeTerminala polje = getPolje(pozicija.getRed(), pozicija.getKolona());

            if (polje.getTip() == TipPolja.DOK || !polje.jeSlobodno()) {
                return Collections.emptyList();
            }

            Plovilo vlasnik = rezervacijePreticanja[pozicija.getRed()][pozicija.getKolona()];

            if (vlasnik != null && vlasnik != plovilo) {
                return Collections.emptyList();
            }
        }

        for (Pozicija pozicija : putanja) {
            rezervacijePreticanja[pozicija.getRed()][pozicija.getKolona()] = plovilo;
        }

        return putanja;
    }

    public synchronized void oslobodiPutanjuPreticanja(Plovilo plovilo, List<Pozicija> putanja) {

        if (plovilo == null || putanja == null || rezervacijePreticanja == null) {
            return;
        }

        for (Pozicija pozicija : putanja) {

            if (!ispravnaPozicija(pozicija.getRed(), pozicija.getKolona())) {
                continue;
            }

            if (rezervacijePreticanja[pozicija.getRed()][pozicija.getKolona()] == plovilo) {
                rezervacijePreticanja[pozicija.getRed()][pozicija.getKolona()] = null;
            }
        }
    }


    public synchronized boolean prijaviNezgodu(PomorskaNezgoda nezgoda) {

        if (nezgoda == null || aktivnaNezgoda != null) {
            return false;
        }

        aktivnaNezgoda = nezgoda;
        blokiran = true;

        /*
         * Prekida se svako započeto preticanje,
         * kako bi interventna vozila mogla prići.
         */
        ponistiSveRezervacijePreticanja();

        System.out.println("NEZGODA: terminal=" + brojTerminala
                + ", prvo=" + nezgoda.getPrvoPlovilo().getNaziv()
                + ", drugo=" + nezgoda.getDrugoPlovilo().getNaziv()
                + ", pozicija=" + nezgoda.getPozicija()
                + ", blokiran=" + blokiran);

        if (nezgode == null) {
            nezgode = new ArrayList<>();
        }
        nezgode.add(nezgoda);

        return true;
    }

    public synchronized boolean zavrsiNezgodu(PomorskaNezgoda nezgoda) {

        if (nezgoda == null || aktivnaNezgoda != nezgoda) {
            return false;
        }

        nezgoda.zavrsiUvidjaj();

        aktivnaNezgoda = null;
        blokiran = false;

        System.out.println("KRAJ NEZGODE: terminal=" + brojTerminala + ", blokiran=" + blokiran);

        return true;
    }

    public PomorskaNezgoda getAktivnaNezgoda() {
        return aktivnaNezgoda;
    }

    public synchronized List<PomorskaNezgoda> getNezgode() {
        if (nezgode == null) {
            nezgode = new ArrayList<>();
        }

        return Collections.unmodifiableList(new ArrayList<>(nezgode));
    }

    public void setSlusalacNezgode(Consumer<PomorskaNezgoda> slusalacNezgode) {

        this.slusalacNezgode = slusalacNezgode;
    }

    public synchronized PomorskaNezgoda pokusajPomorskuNezgodu(
            Plovilo prvoPlovilo, Plovilo drugoPlovilo, Pozicija pozicija) {

        if (prvoPlovilo == null || drugoPlovilo == null
                || prvoPlovilo == drugoPlovilo || aktivnaNezgoda != null) {
            return null;
        }

        if (prebrojSlobodnaPoljaZaIntervenciju() < 3) {
            return null;
        }

        boolean dogodilaSeNezgoda = RANDOM.nextInt(100) < 50;

        if (!dogodilaSeNezgoda) {
            return null;
        }

        PomorskaNezgoda nezgoda =
                new PomorskaNezgoda(brojTerminala, prvoPlovilo, drugoPlovilo, pozicija);

        prvoPlovilo.setUcesnikNezgode(true);
        drugoPlovilo.setUcesnikNezgode(true);

        boolean prijavljena = prijaviNezgodu(nezgoda);

        if (!prijavljena) {
            prvoPlovilo.setUcesnikNezgode(false);
            drugoPlovilo.setUcesnikNezgode(false);
            return null;
        }

        if (slusalacNezgode != null) {
            slusalacNezgode.accept(nezgoda);
        }

        return nezgoda;
    }

    private synchronized void ponistiSveRezervacijePreticanja() {

        if (rezervacijePreticanja == null) {
            return;
        }

        for (int red = 0; red < BROJ_REDOVA; red++) {

            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                rezervacijePreticanja[red][kolona] = null;
            }
        }
    }

    public synchronized Pozicija postaviNaIntervenciju(Plovilo plovilo, Pozicija pozicijaIncidenta) {

        if (plovilo == null || pozicijaIncidenta == null || !plovilo.jeSluzbenoPlovilo()) {
            return null;
        }

        plovilo.setIdeNaIntervenciju(true);

        Pozicija cilj = pronadjiNajblizeSlobodnoPolje(pozicijaIncidenta);

        if (cilj == null) {
            plovilo.setIdeNaIntervenciju(false);
            return null;
        }

        Pozicija trenutna = pronadjiPozicijuPlovila(plovilo);

        boolean uspjesno;

        if (trenutna == null) {
            uspjesno = polja[cilj.getRed()]
                    [cilj.getKolona()]
                    .zauzmi(plovilo);
        } else {
            uspjesno = premjestiPlovilo(plovilo, cilj.getRed(), cilj.getKolona());
        }

        if (!uspjesno) {
            plovilo.setIdeNaIntervenciju(false);
            return null;
        }

        return cilj;
    }

    private synchronized int prebrojSlobodnaPoljaZaIntervenciju() {

        int brojSlobodnih = 0;

        for (int red = 0; red < BROJ_REDOVA; red++) {

            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                PoljeTerminala polje = polja[red][kolona];

                boolean nijeRezervisano = rezervacijePreticanja == null
                        || rezervacijePreticanja[red][kolona] == null;

                if (polje.getTip() != TipPolja.DOK && polje.jeSlobodno() && nijeRezervisano) {

                    brojSlobodnih++;
                }
            }
        }

        return brojSlobodnih;
    }

    private Pozicija pronadjiNajblizeSlobodnoPolje(Pozicija centar) {

        Pozicija najbolja = null;
        int najmanjaUdaljenost = Integer.MAX_VALUE;

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                PoljeTerminala polje = polja[red][kolona];

                boolean rezervisano = rezervacijePreticanja != null
                        && rezervacijePreticanja[red][kolona] != null;

                if (!polje.jeSlobodno() || polje.getTip() == TipPolja.DOK || rezervisano) {

                    continue;
                }

                int udaljenost = Math.abs(red - centar.getRed())
                        + Math.abs(kolona - centar.getKolona());

                if (udaljenost < najmanjaUdaljenost) {
                    najmanjaUdaljenost = udaljenost;
                    najbolja = polje.getPozicija();
                }
            }
        }

        return najbolja;
    }

    public synchronized Pozicija vratiInterventnoNaDok(Plovilo plovilo) {

        if (plovilo == null) {
            return null;
        }

        Pozicija trenutna = pronadjiPozicijuPlovila(plovilo);

        if (trenutna == null) {
            return null;
        }

        List<Pozicija> slobodniDokovi = getSlobodniDokovi();

        if (slobodniDokovi.isEmpty()) {
            plovilo.setIdeNaIntervenciju(false);
            plovilo.setRotacija(false);
            return trenutna;
        }

        Pozicija dok = slobodniDokovi.get(0);

        boolean pomjereno = premjestiPlovilo(plovilo, dok.getRed(), dok.getKolona());

        if (!pomjereno) {
            return null;
        }

        plovilo.setIdeNaIntervenciju(false);
        plovilo.setRotacija(false);

        return dok;
    }

    private void readObject(ObjectInputStream ulaz)
            throws IOException, ClassNotFoundException {

        ulaz.defaultReadObject();

        rezervacijePreticanja = new Plovilo[BROJ_REDOVA][BROJ_KOLONA];

        if (nezgode == null) {
            nezgode = new ArrayList<>();
        }

        if (aktivnaNezgoda != null && !nezgode.contains(aktivnaNezgoda)) {
            nezgode.add(aktivnaNezgoda);
        }

        slusalacNezgode = null;
    }

    public synchronized int normalizujPocetnoStanje() {
        blokiran = false;
        aktivnaNezgoda = null;

        List<Plovilo> plovilaVanDokova = new ArrayList<>();

        for (int red = 0; red < BROJ_REDOVA; red++) {
            for (int kolona = 0; kolona < BROJ_KOLONA; kolona++) {

                PoljeTerminala polje = polja[red][kolona];

                Plovilo plovilo = polje.getPlovilo();

                if (plovilo == null) {
                    continue;
                }

                plovilo.setOdabranoZaIzlazak(false);
                plovilo.setUcesnikNezgode(false);
                plovilo.setIdeNaIntervenciju(false);

                if (polje.getTip() != TipPolja.DOK) {
                    plovilaVanDokova.add(plovilo);
                }
            }
        }

        int vracenoNaDokove = 0;

        for (Plovilo plovilo : plovilaVanDokova) {
            ukloniPlovilo(plovilo);

            Pozicija dok = priveziNaSlucajanDok(plovilo);

            if (dok != null) {
                vracenoNaDokove++;
            }
        }

        return vracenoNaDokove;
    }

    @Override
    public String toString() {
        return "Terminal " + brojTerminala
                + System.lineSeparator()
                + prikaziTerminal();
    }
}