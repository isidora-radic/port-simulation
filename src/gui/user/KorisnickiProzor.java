package gui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.luka.Luka;
import model.luka.PoljeTerminala;
import model.luka.Terminal;
import model.luka.TipPolja;
import util.GeneratorPlovila;
import util.SerijalizacijaLuke;
import model.luka.Pozicija;
import model.plovila.Plovilo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.GeneratorRute;
import javafx.application.Platform;
import gui.admin.FormaPlovila;
import model.incident.PomorskaNezgoda;
import model.sluzbe.TipSluzbe;
import util.EvidencijaUvidjaja;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import util.DnevnikGresaka;

import model.incident.SlucajPotjernice;
import util.CitacPotjernica;
import util.Konfiguracija;

public class KorisnickiProzor {

    private final Luka luka;
    private final Stage stage;

    private ComboBox<Integer> izborTerminala;
    private TextField poljeMinimalniBroj;
    private GridPane prikazTerminala;
    private Label status;
    private Button dugmePokreni;
    private boolean simulacijaPokrenuta;
    private final List<Plovilo> plovilaKojaIzlaze;
    private Button dugmeDodajPlovilo;
    private Plovilo aktivnoDolaznoPlovilo;
    private boolean simulacijaZavrsena;
    private final AtomicInteger aktivnePotjernice;
    private Label porukaDogadjaja; 
    private final long milisekundiPoSatu;
    private final Set<Integer> plovilaIzaslaUTekucojSimulaciji;
    
    public KorisnickiProzor(Luka luka) {
        this.luka = luka;
        this.stage = new Stage();
        this.simulacijaPokrenuta = false;
        this.plovilaKojaIzlaze = new ArrayList<>();
        this.aktivnoDolaznoPlovilo = null;
        this.simulacijaZavrsena = false;
        this.aktivnePotjernice = new AtomicInteger(0);
        this.milisekundiPoSatu = Konfiguracija.ucitajMilisekundiPoSatu();
        this.plovilaIzaslaUTekucojSimulaciji = ConcurrentHashMap.newKeySet();
        try {
            throw new IllegalStateException(
                    "PROBNI TEST LOGGERA"
            );
        } catch (IllegalStateException e) {
            DnevnikGresaka.evidentiraj(
                    "Provjera upisa u error.log",
                    e
            );
        }

    }

    public void prikazi() {
        BorderPane root = new BorderPane();

        root.setTop(kreirajZaglavlje());
        root.setCenter(kreirajSadrzaj());

        Scene scena = new Scene(root, 1100, 650);

        stage.setTitle("Luka – korisnička simulacija");
        stage.setScene(scena);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.show();
    }

    private void evidentirajIzlazakIzLuke(
            Plovilo plovilo) {

        if (plovilo == null) {
            return;
        }

        luka.evidentirajIzlazak(
                plovilo,
                milisekundiPoSatu
        );

        /*
         * Set pamti IMO broj samo jednom,
         * bez obzira iz kojeg je terminala
         * plovilo krenulo.
         */
        plovilaIzaslaUTekucojSimulaciji.add(
                plovilo.getImoBroj()
        );
    }

    private VBox kreirajZaglavlje() {
        Label naslov = new Label("SIMULACIJA LUKE");

        naslov.setStyle(
                "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: white;"
        );

        Label opis = new Label(
                "Prikaz terminala i kretanja plovila"
        );

        opis.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-text-fill: white;"
        );

        VBox zaglavlje = new VBox(5, naslov, opis);
        zaglavlje.setAlignment(Pos.CENTER);
        zaglavlje.setPadding(new Insets(20));
        zaglavlje.setStyle(
                "-fx-background-color: #174A6E;"
        );

        return zaglavlje;
    }

    private VBox kreirajSadrzaj() {
        izborTerminala = new ComboBox<>();

        for (int broj = 1;
                broj <= luka.getBrojTerminala();
                broj++) {
            izborTerminala.getItems().add(broj);
        }

        izborTerminala.setPrefWidth(100);

        poljeMinimalniBroj = new TextField();
        poljeMinimalniBroj.setPromptText(
                "Broj po terminalu"
        );
        poljeMinimalniBroj.setPrefWidth(150);

        dugmePokreni = new Button("Pokreni simulaciju");

        dugmePokreni.setStyle(
                "-fx-background-color: #2E7D32;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );

        dugmeDodajPlovilo =
                new Button("Dodaj novo plovilo");

        dugmeDodajPlovilo.setStyle(
                "-fx-background-color: #E68A00;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );

        dugmeDodajPlovilo.setDisable(true);

        dugmeDodajPlovilo.setOnAction(
                dogadjaj -> dodajPloviloTokomSimulacije()
        );

        HBox kontrole = new HBox(
                10,
                new Label("Terminal:"),
                izborTerminala,
                new Label("Minimalni broj plovila:"),
                poljeMinimalniBroj,
                dugmePokreni,
                dugmeDodajPlovilo
        );

        kontrole.setAlignment(Pos.CENTER_LEFT);

        prikazTerminala = new GridPane();
        prikazTerminala.setAlignment(Pos.CENTER);
        prikazTerminala.setHgap(2);
        prikazTerminala.setVgap(2);
        prikazTerminala.setPadding(new Insets(20));

        status = new Label();
        status.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );

        porukaDogadjaja = new Label();

        porukaDogadjaja.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #C62828;"
        );

        porukaDogadjaja.setWrapText(true);

        izborTerminala.setOnAction(dogadjaj -> {
            osvjeziPrikazTerminala();
        });

        dugmePokreni.setOnAction(
                dogadjaj -> provjeriMinimalniBroj()
        );

        izborTerminala.setValue(1);
        osvjeziPrikazTerminala();

        VBox sadrzaj = new VBox(
                20,
                kontrole,
                status,
                porukaDogadjaja,
                prikazTerminala
        );

        sadrzaj.setPadding(new Insets(20));

        return sadrzaj;
    }
    

    private void osvjeziPrikazTerminala() {
        Integer brojTerminala =
                izborTerminala.getValue();

        if (brojTerminala == null) {
            return;
        }

        Terminal terminal =
                luka.getTerminal(brojTerminala);

        prikazTerminala.getChildren().clear();

        for (int red = 0;
                red < Terminal.BROJ_REDOVA;
                red++) {

            for (int kolona = 0;
                    kolona < Terminal.BROJ_KOLONA;
                    kolona++) {

                PoljeTerminala polje =
                        terminal.getPolje(red, kolona);

                Label celija = kreirajCeliju(
                        polje,
                        red,
                        kolona
                );

                prikazTerminala.add(
                        celija,
                        kolona,
                        red
                );
            }
        }

        status.setText(
                "Terminal " + brojTerminala
                + " – slobodni dokovi: "
                + terminal.getBrojSlobodnihDokova()
                + "/" + Terminal.BROJ_DOKOVA
        );
    }

    private Label kreirajCeliju(
            PoljeTerminala polje,
            int red,
            int kolona) {

        String tekst;

        if (!polje.jeSlobodno()) {
            Plovilo plovilo = polje.getPlovilo();

            tekst = polje.toString();

            if (plovilo.isOdabranoZaIzlazak()) {
                tekst += "I";
            }
            if (plovilo.isUcesnikNezgode()) {
                tekst += "X";
            }
            if (plovilo.isPodPotjernicom()) {
                tekst += "P";
            }
        } else if (polje.getTip() == TipPolja.DOK) {
            tekst = "*";
        } else if (polje.getTip()
                == TipPolja.DOLAZNI_KANAL) {
            tekst = "↓";
        } else if (polje.getTip()
                == TipPolja.ODLAZNI_KANAL) {
            tekst = "↑";
        } else if (red == 1) {
            tekst = "←";
        } else {
            tekst = "→";
        }

        Label celija = new Label(tekst);

        celija.setAlignment(Pos.CENTER);
        celija.setMinSize(52, 52);
        celija.setMaxSize(52, 52);

        String boja;

        if (!polje.jeSlobodno()) {
            boja = "#FFCC80";
        } else if (polje.getTip() == TipPolja.DOK) {
            boja = "#B3E5FC";
        } else {
            boja = "#E0E0E0";
        }

        celija.setStyle(
                "-fx-background-color: " + boja + ";"
                + "-fx-border-color: #455A64;"
                + "-fx-border-width: 1px;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );

        return celija;
    }

    private void provjeriMinimalniBroj() {
        if (simulacijaPokrenuta) {
            return;
        }

        try {
            int minimalniBroj = Integer.parseInt(
                    poljeMinimalniBroj.getText().trim()
            );

            if (minimalniBroj < 0
                    || minimalniBroj > Terminal.BROJ_DOKOVA) {

                status.setText(
                        "Minimalni broj mora biti od 0 do "
                        + Terminal.BROJ_DOKOVA + "."
                );
                return;
            }

            int brojNormalizovanih =
                    luka.normalizujPocetnoStanje();

            if (brojNormalizovanih > 0) {
                SerijalizacijaLuke.sacuvaj(luka);
            }


            int brojDodanihPlovila =
                    dopuniTerminale(minimalniBroj);

            int brojPlovilaZaIzlazak =
                    izaberiPlovilaZaIzlazak();

            plovilaIzaslaUTekucojSimulaciji.clear();
            simulacijaPokrenuta = true;

            poljeMinimalniBroj.setDisable(true);
            dugmePokreni.setDisable(true);
            dugmeDodajPlovilo.setDisable(false);

            SerijalizacijaLuke.sacuvaj(luka);

            osvjeziPrikazTerminala();

            status.setText(
                    "Simulacija je pokrenuta. Dodano: "
                    + brojDodanihPlovila
                    + ", za izlazak izabrano: "
                    + brojPlovilaZaIzlazak
                    + "."
            );

            poveziSlusaoceNezgoda();
            pokreniProvjerePotjernica();
            pokreniPlovilaZaIzlazak();

        } catch (NumberFormatException e) {
            status.setText(
                    "Unesite ispravan minimalni broj plovila."
            );
        }
    }

    private int dopuniTerminale(int minimalniBroj) {
        int ukupnoDodano = 0;

        for (int brojTerminala = 1;
                brojTerminala <= luka.getBrojTerminala();
                brojTerminala++) {

            Terminal terminal =
                    luka.getTerminal(brojTerminala);

            int trenutniBroj =
                    terminal.getPlovila().size();

            while (trenutniBroj < minimalniBroj) {
                var novoPlovilo =
                        GeneratorPlovila.generisiPlovilo();

                var pozicija =
                        luka.dodajPloviloNaTerminal(
                                novoPlovilo,
                                brojTerminala
                        );

                if (pozicija == null) {
                    break;
                }

                trenutniBroj++;
                ukupnoDodano++;
            }
        }

        return ukupnoDodano;
    }

    private int izaberiPlovilaZaIzlazak() {
        int ukupnoIzabrano = 0;

        for (Terminal terminal : luka.getTerminali()) {
            List<Plovilo> plovila = new ArrayList<>(
                    terminal.getPlovila()
            );

            for (Plovilo plovilo : plovila) {
                plovilo.setOdabranoZaIzlazak(false);

                luka.evidentirajUlazak(plovilo);
            }

            if (plovila.isEmpty()) {
                continue;
            }

            Collections.shuffle(plovila);

            int brojZaIzlazak = (int) Math.ceil(
                    plovila.size() * 0.15
            );

            for (int i = 0; i < brojZaIzlazak; i++) {
                plovila.get(i).setOdabranoZaIzlazak(true);
                ukupnoIzabrano++;
            }
        }
        return ukupnoIzabrano;
    }

    private void pokreniPlovilaZaIzlazak() {

        plovilaKojaIzlaze.clear();

        for (Terminal terminal : luka.getTerminali()) {

            List<Plovilo> plovila =
                    new ArrayList<>(terminal.getPlovila());

            for (Plovilo plovilo : plovila) {
                if (!plovilo.isOdabranoZaIzlazak()) {
                    continue;
                }

                List<Pozicija> ruta =
                        GeneratorRute.kreirajRutuZaIzlazak(
                                terminal,
                                plovilo
                        );

                if (ruta.isEmpty()) {
                    continue;
                }


                plovilo.pripremiKretanje(
                        terminal,
                        ruta,
                        () -> Platform.runLater(() -> {
                            if (plovilo.isZavrsiloKretanje()) {
                                evidentirajIzlazakIzLuke(plovilo);
                            }

                            osvjeziPrikazTerminala();
                            provjeriZavrsetakIzlazaka();
                        })
                );

                plovilo.setNakonZavrsetkaEtape(
                        () -> nastaviIzlazakKrozNaredniTerminal(
                                plovilo,
                                terminal.getBrojTerminala()
                        )
                );

                plovilaKojaIzlaze.add(plovilo);

                if (plovilo.getState() == Thread.State.NEW) {
                    plovilo.start();
                }
            }
        }
    }

    private void nastaviIzlazakKrozNaredniTerminal(
            Plovilo plovilo,
            int brojTrenutnogTerminala) {

        int brojNarednogTerminala =
                brojTrenutnogTerminala + 1;

        if (brojNarednogTerminala
                > luka.getBrojTerminala()) {

            plovilo.setNakonZavrsetkaEtape(null);
            return;
        }

        Terminal naredniTerminal =
                luka.getTerminal(brojNarednogTerminala);

        boolean postavljeno = false;

        while (!postavljeno
                && !Thread.currentThread().isInterrupted()) {

            postavljeno =
                    naredniTerminal.postaviNaUlaz(plovilo);

            if (!postavljeno) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (!postavljeno) {
            return;
        }

        List<Pozicija> novaRuta;

        if (naredniTerminal.jePopunjen()) {
            novaRuta =
                    GeneratorRute
                            .kreirajRutuPravoKrozTerminal();
        } else {
            novaRuta =
                    GeneratorRute
                            .kreirajKruznuRutuKrozTerminal();
        }

        Platform.runLater(() -> {
            Integer prikazaniTerminal =
                    izborTerminala.getValue();

            if (prikazaniTerminal != null
                    && prikazaniTerminal
                            == brojNarednogTerminala) {

                osvjeziPrikazTerminala();
            }

            status.setText(
                    plovilo.getNaziv()
                    + " je prešlo iz terminala "
                    + brojTrenutnogTerminala
                    + " u terminal "
                    + brojNarednogTerminala
                    + "."
            );
        });


        plovilo.setNakonZavrsetkaEtape(
                () -> nastaviIzlazakKrozNaredniTerminal(
                        plovilo,
                        brojNarednogTerminala
                )
        );

        plovilo.pripremiNastavakKretanja(
                naredniTerminal,
                novaRuta,
                true
        );
    }

    private void provjeriZavrsetakIzlazaka() {
        if (!simulacijaPokrenuta
                || simulacijaZavrsena
                || plovilaKojaIzlaze.isEmpty()) {
            return;
        }

        for (Plovilo plovilo : plovilaKojaIzlaze) {
            if (!plovilo.isZavrsiloKretanje()) {
                return;
            }
        }

        if (aktivnoDolaznoPlovilo != null
                && !aktivnoDolaznoPlovilo
                        .isZavrsiloKretanje()) {
            return;
        }

        if (aktivnePotjernice.get() > 0) {
            return;
        }
        
        /*
         * Simulacija se ne smije završiti dok na
         * bilo kojem terminalu traje nezgoda ili uviđaj.
         */
        for (Terminal terminal : luka.getTerminali()) {

            if (terminal.getAktivnaNezgoda() != null
                    || terminal.isBlokiran()) {

                return;
            }
        }
        
        simulacijaZavrsena = true;
        dugmeDodajPlovilo.setDisable(true);

        SerijalizacijaLuke.sacuvaj(luka);


        status.setText(
                "Simulacija je završena. Ukupan broj plovila "
                + "koja su izašla iz cijele luke: "
                + plovilaIzaslaUTekucojSimulaciji.size()
                + "."
        );
    }


    private void dodajPloviloTokomSimulacije() {

        if (simulacijaZavrsena) {
            status.setText(
                    "Simulacija je već završena."
            );
            return;
        }

        if (!simulacijaPokrenuta) {
            status.setText(
                    "Prvo pokrenite simulaciju."
            );
            return;
        }

        if (aktivnoDolaznoPlovilo != null
                && !aktivnoDolaznoPlovilo
                        .isZavrsiloKretanje()) {

            status.setText(
                    "Sačekajte da se prethodno novo plovilo "
                    + "priveže ili napusti luku."
            );
            return;
        }

        /*
         * Ulazak se ne dozvoljava ako su svi dokovi
         * u svim terminalima zauzeti.
         */
        if (luka.jePopunjena()) {
            status.setText(
                    "Svi dokovi su popunjeni. "
                    + "Ulaz u luku nije dozvoljen."
            );
            return;
        }

        FormaPlovila forma =
                new FormaPlovila(stage);

        Plovilo novoPlovilo = forma.prikazi();

        if (novoPlovilo == null) {
            return;
        }

        Terminal prviTerminal =
                luka.getTerminal(1);

        if (!prviTerminal.jeUlazSlobodan()) {
            status.setText(
                    "Ulaz Terminala 1 je trenutno zauzet. "
                    + "Pokušajte ponovo."
            );
            return;
        }

        boolean postavljeno =
                prviTerminal.postaviNaUlaz(novoPlovilo);

        if (!postavljeno) {
            status.setText(
                    "Novo plovilo nije moguće postaviti "
                    + "na ulaz Terminala 1."
            );
            return;
        }

        novoPlovilo.setDodatoTokomSimulacije(true);
        novoPlovilo.setOdabranoZaIzlazak(false);

        luka.evidentirajUlazak(novoPlovilo);

        aktivnoDolaznoPlovilo = novoPlovilo;

        pripremiDolazakUTerminal(
                novoPlovilo,
                1,
                true
        );
    }

    private Pozicija pronadjiPrviSlobodanDok(
            Terminal terminal) {

        List<Pozicija> slobodniDokovi =
                new ArrayList<>(
                        terminal.getSlobodniDokovi()
                );

        if (slobodniDokovi.isEmpty()) {
            return null;
        }

        slobodniDokovi.sort(
                java.util.Comparator
                        .comparingInt(
                                (Pozicija pozicija) ->
                                        pozicija.getRed() == 3
                                                ? 0
                                                : 1
                        )
                        .thenComparingInt(
                                pozicija ->
                                        pozicija.getRed() == 3
                                                ? pozicija.getKolona()
                                                : -pozicija.getKolona()
                        )
        );

        return slobodniDokovi.get(0);
    }

    private void pripremiDolazakUTerminal(
            Plovilo plovilo,
            int brojTerminala,
            boolean prvaEtapa) {

        Terminal terminal =
                luka.getTerminal(brojTerminala);

        Pozicija ciljniDok =
                pronadjiPrviSlobodanDok(terminal);

        List<Pozicija> ruta;
        boolean ukloniPoZavrsetku;

        if (ciljniDok != null) {
            /*
             * Terminal ima slobodan dok:
             * plovilo kruži samo do prvog slobodnog doka.
             */
            ruta = GeneratorRute.kreirajRutuDoDoka(
                    terminal,
                    ciljniDok
            );

            ukloniPoZavrsetku = false;

            plovilo.setNakonZavrsetkaEtape(
                    () -> zavrsiDolazakNaDok(
                            plovilo,
                            terminal,
                            ciljniDok
                    )
            );

        } else {
            /*
             * Terminal je pun:
             * plovilo pravi cijeli krug i zatim
             * prelazi u naredni terminal.
             */
            ruta =
                    GeneratorRute
                            .kreirajKruznuRutuKrozTerminal();

            ukloniPoZavrsetku = true;

            plovilo.setNakonZavrsetkaEtape(
                    () -> nastaviDolazakKrozNaredniTerminal(
                            plovilo,
                            brojTerminala
                    )
            );
        }

        if (ruta.isEmpty()) {
            terminal.ukloniPlovilo(plovilo);
            aktivnoDolaznoPlovilo = null;

            Platform.runLater(() ->
                    status.setText(
                            "Nije moguće formirati rutu "
                            + "za novo plovilo."
                    )
            );
            return;
        }

        if (prvaEtapa) {
            plovilo.pripremiKretanje(
                    terminal,
                    ruta,
                    () -> Platform.runLater(
                            this::osvjeziPrikazTerminala
                    ),
                    ukloniPoZavrsetku
            );

            plovilo.start();

        } else {
            plovilo.pripremiNastavakKretanja(
                    terminal,
                    ruta,
                    ukloniPoZavrsetku
            );
        }

        Platform.runLater(() -> {
            Integer prikazaniTerminal =
                    izborTerminala.getValue();

            if (prikazaniTerminal != null
                    && prikazaniTerminal == brojTerminala) {
                osvjeziPrikazTerminala();
            }

            if (ciljniDok == null) {
                status.setText(
                        "Terminal " + brojTerminala
                        + " je pun. Novo plovilo kruži "
                        + "prema narednom terminalu."
                );
            } else {
                status.setText(
                        "Novo plovilo prolazi Terminal "
                        + brojTerminala
                        + " i ide prema prvom slobodnom doku "
                        + ciljniDok + "."
                );
            }
        });
    }

    private void nastaviDolazakKrozNaredniTerminal(
            Plovilo plovilo,
            int brojTrenutnogTerminala) {

        int brojNarednogTerminala =
                brojTrenutnogTerminala + 1;

        /*
         * Prošlo je i posljednji terminal,
         * a nije pronašlo slobodan dok.
         */
        if (brojNarednogTerminala
                > luka.getBrojTerminala()) {

            plovilo.setNakonZavrsetkaEtape(null);
            aktivnoDolaznoPlovilo = null;

            Platform.runLater(() -> {
                status.setText(
                        "Novo plovilo nije pronašlo slobodan dok "
                        + "i napustilo je luku."
                );

                SerijalizacijaLuke.sacuvaj(luka);
                provjeriZavrsetakIzlazaka();
            });

            return;
        }

        Terminal naredniTerminal =
                luka.getTerminal(brojNarednogTerminala);

        boolean postavljeno = false;

        while (!postavljeno
                && !Thread.currentThread().isInterrupted()) {

            postavljeno =
                    naredniTerminal.postaviNaUlaz(plovilo);

            if (!postavljeno) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (!postavljeno) {
            return;
        }

        pripremiDolazakUTerminal(
                plovilo,
                brojNarednogTerminala,
                false
        );
    }

    private void zavrsiDolazakNaDok(
            Plovilo plovilo,
            Terminal terminal,
            Pozicija ciljniDok) {

        plovilo.setNakonZavrsetkaEtape(null);
        aktivnoDolaznoPlovilo = null;

        Platform.runLater(() -> {
            osvjeziPrikazTerminala();

            SerijalizacijaLuke.sacuvaj(luka);

            status.setText(
                    "Novo plovilo "
                    + plovilo.getNaziv()
                    + " privezano je na prvi slobodan dok "
                    + ciljniDok
                    + " Terminala "
                    + terminal.getBrojTerminala()
                    + "."
            );

            provjeriZavrsetakIzlazaka();
        });
    }

    private void poveziSlusaoceNezgoda() {
        for (Terminal terminal : luka.getTerminali()) {
            terminal.setSlusalacNezgode(
                    nezgoda -> pokreniObraduNezgode(
                            nezgoda
                    )
            );
        }
    }

    private void pokreniObraduNezgode(
            PomorskaNezgoda nezgoda) {

        Thread obrada = new Thread(
                () -> obradiNezgodu(nezgoda),
                "Obrada-nezgode-"
                        + nezgoda.getIdentifikator()
        );

        obrada.setDaemon(true);
        obrada.start();
    }

    private void obradiNezgodu(
            PomorskaNezgoda nezgoda) {

        Terminal terminal =
                luka.getTerminal(
                        nezgoda.getBrojTerminala()
                );

        if (terminal == null) {
            return;
        }

        List<Plovilo> interventnaPlovila =
                new ArrayList<>();

        /*
         * Sve tri službe moraju stići prije
         * početka uviđaja.
         */
        Plovilo obalskaStraza =
                posaljiSluzbu(
                        TipSluzbe.OBALSKA_STRAZA,
                        nezgoda,
                        interventnaPlovila
                );

        Plovilo carina =
                posaljiSluzbu(
                        TipSluzbe.CARINA,
                        nezgoda,
                        interventnaPlovila
                );

        Plovilo vatrogasci =
                posaljiSluzbu(
                        TipSluzbe.VATROGASCI,
                        nezgoda,
                        interventnaPlovila
                );

        if (obalskaStraza == null
                || carina == null
                || vatrogasci == null) {

            /*
             * Uklanjamo službe koje su eventualno
             * stigle prije neuspjeha intervencije.
             */
            for (Plovilo interventno
                    : interventnaPlovila) {

                interventno.setIdeNaIntervenciju(false);
                interventno.setRotacija(false);

                terminal.ukloniPlovilo(
                        interventno
                );
            }

            nezgoda.getPrvoPlovilo()
                    .setUcesnikNezgode(false);

            nezgoda.getDrugoPlovilo()
                    .setUcesnikNezgode(false);

            terminal.zavrsiNezgodu(nezgoda);

            Platform.runLater(() -> {
                osvjeziPorukuAktivnihNezgoda();
                osvjeziPrikazTerminala();

                status.setText(
                        "Intervencija na Terminalu "
                        + nezgoda.getBrojTerminala()
                        + " nije mogla biti završena."
                );
            });

            return;
        }

        /*
         * Poruka na početku uviđaja.
         */
        Platform.runLater(() -> {
            osvjeziPorukuAktivnihNezgoda();
            osvjeziPrikazTerminala();

            status.setText(
                    "Obalska straža, carina i vatrogasci "
                    + "stigli su na mjesto nezgode na Terminalu "
                    + nezgoda.getBrojTerminala()
                    + ". Uviđaj traje "
                    + nezgoda.getTrajanjeUvidjajaSekunde()
                    + " sekundi."
            );
        });

        System.out.println(
                "UVIĐAJ POČEO: terminal="
                + nezgoda.getBrojTerminala()
                + ", broj službi="
                + interventnaPlovila.size()
                + ", trajanje="
                + nezgoda.getTrajanjeUvidjajaSekunde()
                + " sekundi"
        );

        /*
         * Trajanje uviđaja od 3 do 10 sekundi.
         */
        try {
            Thread.sleep(
                    nezgoda.getTrajanjeUvidjajaSekunde()
                    * 1000L
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        Plovilo prvo =
                nezgoda.getPrvoPlovilo();

        Plovilo drugo =
                nezgoda.getDrugoPlovilo();

        /*
         * Zaustavljamo niti učesnika nezgode.
         */
        prvo.zavrsiKretanjeNakonIncidenta();
        drugo.zavrsiKretanjeNakonIncidenta();

        /*
         * Učesnici nezgode odmah nestaju.
         */
        terminal.ukloniPlovilo(prvo);
        terminal.ukloniPlovilo(drugo);

        prvo.setUcesnikNezgode(false);
        drugo.setUcesnikNezgode(false);

        evidentirajIzlazakIzLuke(prvo);
        evidentirajIzlazakIzLuke(drugo);

        /*
         * Nakon uviđaja nestaju i sva tri
         * interventna plovila.
         */
        for (Plovilo interventno
                : interventnaPlovila) {

            interventno.setIdeNaIntervenciju(false);
            interventno.setRotacija(false);

            terminal.ukloniPlovilo(
                    interventno
            );
        }

        /*
         * Završavamo nezgodu i odblokiramo
         * samo pogođeni terminal.
         */
        terminal.zavrsiNezgodu(nezgoda);

        Path putanjaUvidjaja =
                EvidencijaUvidjaja.sacuvaj(
                        nezgoda
                );

        SerijalizacijaLuke.sacuvaj(luka);

        /*
         * Završna poruka ide tek nakon što su
         * nezgoda i uviđaj završeni.
         */
        Platform.runLater(() -> {
            osvjeziPorukuAktivnihNezgoda();
            osvjeziPrikazTerminala();

            status.setText(
                    "Uviđaj na Terminalu "
                    + nezgoda.getBrojTerminala()
                    + " je završen. Terminal je ponovo otvoren. "
                    + "Evidencija: "
                    + putanjaUvidjaja.getFileName()
            );

            provjeriZavrsetakIzlazaka();
        });

        System.out.println(
                "UVIĐAJ ZAVRŠEN: terminal="
                + nezgoda.getBrojTerminala()
                + ", terminal otvoren="
                + !terminal.isBlokiran()
        );
    }


    private void osvjeziPorukuAktivnihNezgoda() {

        StringBuilder poruka =
                new StringBuilder();

        for (Terminal terminal : luka.getTerminali()) {

            PomorskaNezgoda nezgoda =
                    terminal.getAktivnaNezgoda();

            if (nezgoda == null) {
                continue;
            }

            if (poruka.length() > 0) {
                poruka.append(System.lineSeparator());
            }

            poruka.append("⚠ POMORSKA NEZGODA – Terminal ")
                    .append(nezgoda.getBrojTerminala())
                    .append(" | Učesnici: ")
                    .append(
                            nezgoda.getPrvoPlovilo()
                                    .getNaziv()
                    )
                    .append(" i ")
                    .append(
                            nezgoda.getDrugoPlovilo()
                                    .getNaziv()
                    )
                    .append(" | Pozicija: ")
                    .append(nezgoda.getPozicija())
                    .append(" | Uviđaj u toku: ")
                    .append(
                            nezgoda
                                    .getTrajanjeUvidjajaSekunde()
                    )
                    .append(" s. Saobraćaj na terminalu je blokiran.");
        }

        if (poruka.length() == 0) {
            porukaDogadjaja.setText("");
        } else {
            porukaDogadjaja.setText(
                    poruka.toString()
            );
        }
    }

    private Plovilo posaljiSluzbu(
            TipSluzbe sluzba,
            PomorskaNezgoda nezgoda,
            List<Plovilo> interventnaPlovila) {

        Plovilo plovilo =
                luka.pronadjiIliGenerisiSluzbenoPlovilo(
                        sluzba,
                        nezgoda.getBrojTerminala(),
                        nezgoda.getPozicija(),
                        interventnaPlovila
                );

        if (plovilo == null) {
            return null;
        }

        Pozicija pozicija = null;

        /*
         * Uviđaj neće početi dok služba ne bude
         * uspješno postavljena pored incidenta.
         */
        while (pozicija == null
                && !Thread.currentThread().isInterrupted()) {

            pozicija =
                    luka.posaljiNaIntervenciju(
                            plovilo,
                            nezgoda.getBrojTerminala(),
                            nezgoda.getPozicija()
                    );

            if (pozicija == null) {

            	System.out.println(
            	        "SLUŽBA ČEKA: "
            	        + sluzba
            	        + ", terminal="
            	        + nezgoda.getBrojTerminala()
            	        + ", nema slobodnog interventnog polja."
            	);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        if (pozicija == null) {
            return null;
        }

        interventnaPlovila.add(plovilo);

        System.out.println(
                "SLUŽBA STIGLA: "
                + sluzba
                + ", plovilo="
                + plovilo.getNaziv()
                + ", terminal="
                + nezgoda.getBrojTerminala()
                + ", pozicija="
                + pozicija
                + ", rotacija="
                + plovilo.isRotacija()
        );

        return plovilo;
    }

    private void pokreniProvjerePotjernica() {
        Set<Integer> vecObradjeniImoBrojevi =
                new HashSet<>();

        for (Terminal terminal : luka.getTerminali()) {
            List<Plovilo> plovila =
                    new ArrayList<>(terminal.getPlovila());

            for (Plovilo obalskaStraza : plovila) {
                if (!obalskaStraza.pripadaSluzbi(
                        TipSluzbe.OBALSKA_STRAZA)) {
                    continue;
                }

                Set<Integer> trazeniImoBrojevi =
                        CitacPotjernica.ucitajImoBrojeve(
                                obalskaStraza
                                        .getPutanjaFajlaPotjernica()
                        );

                for (Plovilo kandidat : plovila) {
                    if (kandidat == obalskaStraza
                            || vecObradjeniImoBrojevi.contains(
                                    kandidat.getImoBroj())
                            || !trazeniImoBrojevi.contains(
                                    kandidat.getImoBroj())) {
                        continue;
                    }

                    vecObradjeniImoBrojevi.add(
                            kandidat.getImoBroj()
                    );

                    obalskaStraza.setOdabranoZaIzlazak(false);
                    kandidat.setOdabranoZaIzlazak(false);

                    kandidat.setPodPotjernicom(true);
                    obalskaStraza.setRotacija(true);

                    SlucajPotjernice slucaj =
                            new SlucajPotjernice(
                                    terminal.getBrojTerminala(),
                                    obalskaStraza,
                                    kandidat
                            );

                    pokreniObraduPotjernice(
                            terminal,
                            slucaj
                    );
                }
            }
        }
    }

    private void pokreniObraduPotjernice(
            Terminal terminal,
            SlucajPotjernice slucaj) {

        aktivnePotjernice.incrementAndGet();

        Thread obrada = new Thread(() -> {
            try {
                Platform.runLater(() ->
                        porukaDogadjaja.setText(
                                "Obalska straža je pronašla plovilo "
                                + slucaj.getTrazenoPlovilo()
                                        .getNaziv()
                                + ". Uviđaj traje "
                                + slucaj.getTrajanjeUvidjajaSekunde()
                                + " sekundi. Saobraćaj nije blokiran."
                        )
                );

                Thread.sleep(
                        slucaj.getTrajanjeUvidjajaSekunde()
                        * 1000L
                );

                slucaj.zavrsi();

                evidentirajIzlazakIzLuke(
                        slucaj.getTrazenoPlovilo()
                );

                evidentirajIzlazakIzLuke(
                        slucaj.getObalskaStraza()
                );

                terminal.ukloniPlovilo(
                        slucaj.getTrazenoPlovilo()
                );

                terminal.ukloniPlovilo(
                        slucaj.getObalskaStraza()
                );

                Path putanja =
                        EvidencijaUvidjaja.sacuvaj(slucaj);

                SerijalizacijaLuke.sacuvaj(luka);

                Platform.runLater(() -> {
                    osvjeziPrikazTerminala();

                    porukaDogadjaja.setText(
                            "Traženo plovilo je napustilo luku "
                            + "uz obalsku stražu. Evidencija: "
                            + putanja.getFileName()
                    );
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            } finally {
                aktivnePotjernice.decrementAndGet();

                Platform.runLater(
                        this::provjeriZavrsetakIzlazaka
                );
            }
        });

        obrada.setName(
                "Potjernica-"
                        + slucaj.getIdentifikator()
        );

        obrada.setDaemon(true);
        obrada.start();
    }

}