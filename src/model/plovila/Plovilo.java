package model.plovila;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.luka.Pozicija;
import model.luka.Terminal;
import model.sluzbe.TipSluzbe;
import util.GeneratorImoBroja;

public abstract class Plovilo extends Thread implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String naziv;
    protected int imoBroj;
    protected int brojMotora;
    protected String putanjaFotografije;
    protected String registarskiBroj;
    protected int brzina;
    protected Set<TipSluzbe> sluzbe;
    protected boolean rotacija;
    protected String putanjaFajlaPotjernica;
    protected boolean odabranoZaIzlazak;
    protected boolean dodatoTokomSimulacije;
    protected long vrijemeUlaska;
    protected transient Terminal aktivniTerminal;
    protected transient List<Pozicija> rutaKretanja;
    protected transient Runnable nakonPomjeranja;
    protected transient Runnable nakonZavrsetkaEtape;
    protected transient volatile boolean imaNastavakKretanja;
    private static final Set<String> PAROVI_PRETICANJA = ConcurrentHashMap.newKeySet();

    protected transient volatile boolean prekiniKretanje;
    protected transient volatile boolean zavrsiloKretanje;
    protected transient boolean ukloniPoZavrsetku;
    protected transient volatile long zaustavljenoDo;
    protected volatile boolean ucesnikNezgode;
    protected transient volatile boolean ideNaIntervenciju;
    protected transient Plovilo posljednjeProvjerenoPlovilo;
    protected volatile boolean podPotjernicom;

    protected static final Random RANDOM = new Random();

    protected Plovilo(String naziv, int brojMotora, String putanjaFotografije, String registarskiBroj) {

        this.naziv = naziv;
        this.brojMotora = brojMotora;
        this.putanjaFotografije = putanjaFotografije;
        this.registarskiBroj = registarskiBroj;

        this.imoBroj = GeneratorImoBroja.generisi();
        this.brzina = generisiBrzinu();
        this.sluzbe = EnumSet.noneOf(TipSluzbe.class);
        this.rotacija = false;
        this.putanjaFajlaPotjernica = null;
        this.odabranoZaIzlazak = false;
        this.dodatoTokomSimulacije = false;
        this.vrijemeUlaska = System.currentTimeMillis();
        this.rutaKretanja = new ArrayList<>();
        this.nakonZavrsetkaEtape = null;
        this.imaNastavakKretanja = false;
        this.prekiniKretanje = false;
        this.zavrsiloKretanje = false;
        this.ukloniPoZavrsetku = true;
        this.zaustavljenoDo = 0L;
        this.ucesnikNezgode = false;
        this.ideNaIntervenciju = false;
        this.posljednjeProvjerenoPlovilo = null;
        this.podPotjernicom = false;
    }

    private int generisiBrzinu() {
        return 1 + RANDOM.nextInt(5);
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public int getImoBroj() {
        return imoBroj;
    }

    public int getBrojMotora() {
        return brojMotora;
    }

    public void setBrojMotora(int brojMotora) {
        this.brojMotora = brojMotora;
    }

    public String getPutanjaFotografije() {
        return putanjaFotografije;
    }

    public void setPutanjaFotografije(String putanjaFotografije) {
        this.putanjaFotografije = putanjaFotografije;
    }

    public String getRegistarskiBroj() {
        return registarskiBroj;
    }

    public void setRegistarskiBroj(String registarskiBroj) {
        this.registarskiBroj = registarskiBroj;
    }

    public int getBrzina() {
        return brzina;
    }

    public Set<TipSluzbe> getSluzbe() {
        return Collections.unmodifiableSet(sluzbe);
    }

    public abstract boolean mozePripadatiSluzbi(TipSluzbe sluzba);

    public boolean dodajSluzbu(TipSluzbe sluzba) {
        if (sluzba != null && mozePripadatiSluzbi(sluzba)) {
            return sluzbe.add(sluzba);
        }

        return false;
    }

    public void ukloniSluzbu(TipSluzbe sluzba) {
        sluzbe.remove(sluzba);

        if (sluzba == TipSluzbe.OBALSKA_STRAZA) {
            putanjaFajlaPotjernica = null;
        }

        if (sluzbe.isEmpty()) {
            rotacija = false;
        }
    }

    public boolean pripadaSluzbi(TipSluzbe sluzba) {
        return sluzbe.contains(sluzba);
    }

    public boolean jeSluzbenoPlovilo() {
        return !sluzbe.isEmpty();
    }

    public boolean isRotacija() {
        return rotacija;
    }

    public void setRotacija(boolean rotacija) {
        this.rotacija = rotacija && jeSluzbenoPlovilo();
    }

    public int getPrioritet() {
        if (!rotacija) {
            return 0;
        }

        int najveciPrioritet = 0;

        for (TipSluzbe sluzba : sluzbe) {
            if (sluzba.getPrioritet() > najveciPrioritet) {
                najveciPrioritet = sluzba.getPrioritet();
            }
        }

        return najveciPrioritet;
    }

    public String getOznakaSluzbe() {
        StringBuilder oznaka = new StringBuilder();

        for (TipSluzbe sluzba : sluzbe) {
            oznaka.append(sluzba.getOznaka());
        }

        if (rotacija) {
            oznaka.append("R");
        }

        return oznaka.toString();
    }

    public String getPutanjaFajlaPotjernica() {
        return putanjaFajlaPotjernica;
    }

    public boolean setPutanjaFajlaPotjernica(String putanjaFajlaPotjernica) {
        if (!pripadaSluzbi(TipSluzbe.OBALSKA_STRAZA)) {
            return false;
        }

        if (putanjaFajlaPotjernica == null
                || putanjaFajlaPotjernica.isBlank()) {
            return false;
        }

        this.putanjaFajlaPotjernica = putanjaFajlaPotjernica;
        return true;
    }

    public boolean isOdabranoZaIzlazak() {
        return odabranoZaIzlazak;
    }

    public void setOdabranoZaIzlazak(boolean odabranoZaIzlazak) {
        this.odabranoZaIzlazak = odabranoZaIzlazak;
    }

    public boolean isDodatoTokomSimulacije() {
        return dodatoTokomSimulacije;
    }

    public void setDodatoTokomSimulacije(boolean dodatoTokomSimulacije) {
        this.dodatoTokomSimulacije = dodatoTokomSimulacije;
    }

    public long getVrijemeUlaska() {
        return vrijemeUlaska;
    }

    public void evidentirajUlazak() {
        this.vrijemeUlaska = System.currentTimeMillis();
    }

    public void pripremiKretanje(Terminal terminal, List<Pozicija> ruta, Runnable nakonPomjeranja) {
        pripremiKretanje(terminal, ruta, nakonPomjeranja, true);
    }

    public void pripremiKretanje(Terminal terminal, List<Pozicija> ruta, Runnable nakonPomjeranja,
            boolean ukloniPoZavrsetku) {
        this.aktivniTerminal = terminal;
        this.rutaKretanja = ruta == null ? new ArrayList<>() : new ArrayList<>(ruta);
        this.nakonPomjeranja = nakonPomjeranja;
        this.ukloniPoZavrsetku = ukloniPoZavrsetku;
        this.prekiniKretanje = false;
        this.zavrsiloKretanje = false;
        this.zaustavljenoDo = 0L;
        this.ideNaIntervenciju = false;
        this.posljednjeProvjerenoPlovilo = null;
    }

    public synchronized void setNakonZavrsetkaEtape(Runnable nakonZavrsetkaEtape) {
        this.nakonZavrsetkaEtape = nakonZavrsetkaEtape;

        if (nakonZavrsetkaEtape == null) {
            this.imaNastavakKretanja = false;
        }
    }

    private String formirajKljucPreticanja(Plovilo drugoPlovilo) {
        int prviImo = Math.min(getImoBroj(), drugoPlovilo.getImoBroj());
        int drugiImo = Math.max(getImoBroj(), drugoPlovilo.getImoBroj());

        return prviImo + ":" + drugiImo;
    }

    private boolean rezervisiParPreticanja(Plovilo drugoPlovilo) {
        if (drugoPlovilo == null || drugoPlovilo == this) {
            return false;
        }

        return PAROVI_PRETICANJA.add(formirajKljucPreticanja(drugoPlovilo));
    }

    private void oslobodiParPreticanja(Plovilo drugoPlovilo) {
        if (drugoPlovilo == null) {
            return;
        }

        PAROVI_PRETICANJA.remove(formirajKljucPreticanja(drugoPlovilo));
    }

    public synchronized void pripremiNastavakKretanja(Terminal terminal, List<Pozicija> ruta, boolean ukloniPoZavrsetku) {
        this.aktivniTerminal = terminal;
        this.rutaKretanja = ruta == null ? new ArrayList<>() : new ArrayList<>(ruta);
        this.ukloniPoZavrsetku = ukloniPoZavrsetku;
        this.imaNastavakKretanja = true;
        this.zavrsiloKretanje = false;
        this.posljednjeProvjerenoPlovilo = null;
    }

    public long getPauzaKretanja() {
        return 1750L - brzina * 250L;
    }

    public void zaustaviPrivremeno(long trajanjeMilisekunde) {
        if (trajanjeMilisekunde <= 0) {
            return;
        }

        long novoVrijeme = System.currentTimeMillis() + trajanjeMilisekunde;

        if (novoVrijeme > zaustavljenoDo) {
            zaustavljenoDo = novoVrijeme;
        }
    }

    public boolean jePrivremenoZaustavljeno() {
        return System.currentTimeMillis() < zaustavljenoDo;
    }

    public boolean isUcesnikNezgode() {
        return ucesnikNezgode;
    }

    public void setUcesnikNezgode(boolean ucesnikNezgode) {
        this.ucesnikNezgode = ucesnikNezgode;
    }

    public boolean isIdeNaIntervenciju() {
        return ideNaIntervenciju;
    }

    public void setIdeNaIntervenciju(boolean ideNaIntervenciju) {
        this.ideNaIntervenciju = ideNaIntervenciju;

        if (ideNaIntervenciju && jeSluzbenoPlovilo()) {
            setRotacija(true);
        }
    }

    @Override
    public void run() {
        if (aktivniTerminal == null || rutaKretanja == null || rutaKretanja.isEmpty()) {
            return;
        }

        try {
            while (!prekiniKretanje) {

                Terminal terminalEtape = aktivniTerminal;
                List<Pozicija> rutaEtape = new ArrayList<>(rutaKretanja);

                boolean ukloniNaKrajuEtape = ukloniPoZavrsetku;

                imaNastavakKretanja = false;

                for (int indeks = 0; indeks < rutaEtape.size(); indeks++) {

                    Pozicija sljedecaPozicija = rutaEtape.get(indeks);
                    if (prekiniKretanje) {
                        break;
                    }

                    while (jePrivremenoZaustavljeno() && !prekiniKretanje) {
                        Thread.sleep(100);
                    }

                    boolean pomjereno = false;

                    while (!pomjereno && !prekiniKretanje) {
                        if (jePrivremenoZaustavljeno()) {
                            Thread.sleep(100);
                            continue;
                        }

                        pomjereno = terminalEtape.premjestiPlovilo(
                                this,
                                sljedecaPozicija.getRed(),
                                sljedecaPozicija.getKolona()
                        );

                        if (!pomjereno) {

                            Plovilo ploviloIspred = terminalEtape.getPloviloNaPoziciji(sljedecaPozicija);

                            if (ploviloIspred != null && ploviloIspred != posljednjeProvjerenoPlovilo) {

                                posljednjeProvjerenoPlovilo = ploviloIspred;

                                terminalEtape.pokusajPomorskuNezgodu(
                                        this,
                                        ploviloIspred,
                                        sljedecaPozicija
                                );
                            }

                            Pozicija pozicijaNakonPreticanja =
                                    indeks + 1 < rutaEtape.size()
                                            ? rutaEtape.get(indeks + 1)
                                            : null;

                            boolean imaPrednost =
                                    ploviloIspred != null
                                    && getPrioritet() > 0
                                    && getPrioritet()
                                            > ploviloIspred.getPrioritet();

                            if (imaPrednost) {
                                ploviloIspred.zaustaviPrivremeno(
                                        getPauzaKretanja() * 2
                                );
                            }

                            boolean parPreticanjaRezervisan =
                                    ploviloIspred != null
                                    && pozicijaNakonPreticanja != null
                                    && rezervisiParPreticanja(
                                            ploviloIspred
                                    );

                            List<Pozicija> putanjaPreticanja =
                                    parPreticanjaRezervisan
                                            ? terminalEtape
                                                    .rezervisiPutanjuPreticanja(
                                                            this,
                                                            sljedecaPozicija,
                                                            pozicijaNakonPreticanja
                                                    )
                                            : Collections.emptyList();

                            /*
                             * Ako manevar trenutno nije moguć, par se
                             * oslobađa da bi kasnije mogao ponovo pokušati.
                             */
                            if (parPreticanjaRezervisan && putanjaPreticanja.isEmpty()) {
                                oslobodiParPreticanja(ploviloIspred);
                            }

                            if (!putanjaPreticanja.isEmpty()) {

                                try {
                                    for (Pozicija korakPreticanja : putanjaPreticanja) {

                                        boolean izvrsenKorak = false;

                                        while (!izvrsenKorak && !prekiniKretanje) {

                                            izvrsenKorak =
                                                    terminalEtape.premjestiPlovilo(
                                                            this,
                                                            korakPreticanja.getRed(),
                                                            korakPreticanja.getKolona()
                                                    );

                                            if (!izvrsenKorak) {
                                                Thread.sleep(100);
                                            }
                                        }

                                        if (izvrsenKorak) {
                                            obavijestiPrikaz();
                                            Thread.sleep(getPauzaKretanja());
                                        }
                                    }

                                    indeks++;
                                    pomjereno = true;

                                } finally {
                                    terminalEtape.oslobodiPutanjuPreticanja(
                                            this,
                                            putanjaPreticanja
                                    );
                                }

                            } else {
                                Thread.sleep(150);
                            }
                        }
                    }

                    if (pomjereno) {
                        posljednjeProvjerenoPlovilo = null;
                        obavijestiPrikaz();
                        Thread.sleep(getPauzaKretanja());
                    }
                }

                if (prekiniKretanje) {
                    break;
                }

                if (ukloniNaKrajuEtape) {
                    terminalEtape.ukloniPlovilo(this);
                }

                if (nakonZavrsetkaEtape != null) {
                    nakonZavrsetkaEtape.run();
                }

                if (imaNastavakKretanja) {
                    obavijestiPrikaz();
                    continue;
                }

                zavrsiloKretanje = true;
                obavijestiPrikaz();
                break;
            }

        } catch (InterruptedException e) {
            prekiniKretanje = true;
            Thread.currentThread().interrupt();
        }
    }

    private void obavijestiPrikaz() {
        if (nakonPomjeranja != null) {
            nakonPomjeranja.run();
        }
    }

    public void zaustaviKretanje() {
        prekiniKretanje = true;
        interrupt();
    }

    public boolean isZavrsiloKretanje() {
        return zavrsiloKretanje;
    }

    public void zavrsiKretanjeNakonIncidenta() {
        prekiniKretanje = true;
        zavrsiloKretanje = true;
        interrupt();
    }

    public boolean isPodPotjernicom() {
        return podPotjernicom;
    }

    public void setPodPotjernicom(boolean podPotjernicom) {
        this.podPotjernicom = podPotjernicom;
    }

    private void readObject(ObjectInputStream ulaz)
            throws IOException, ClassNotFoundException {

        ulaz.defaultReadObject();

        boolean jedinstven = GeneratorImoBroja.registruj(imoBroj);

        if (!jedinstven) {
            imoBroj = GeneratorImoBroja.generisi();
        }

        aktivniTerminal = null;
        rutaKretanja = new ArrayList<>();
        nakonPomjeranja = null;
        nakonZavrsetkaEtape = null;
        imaNastavakKretanja = false;
        prekiniKretanje = false;
        zavrsiloKretanje = false;
        ukloniPoZavrsetku = true;
        zaustavljenoDo = 0L;
        ideNaIntervenciju = false;
        posljednjeProvjerenoPlovilo = null;
    }

    @Override
    public String toString() {
        return "naziv=" + naziv
                + ", IMO=" + imoBroj
                + ", registarski broj=" + registarskiBroj
                + ", brzina=" + brzina
                + ", službe=" + sluzbe
                + ", rotacija=" + rotacija
                + ", izlazi=" + odabranoZaIzlazak;
    }
}