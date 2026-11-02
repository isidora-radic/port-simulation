package gui.admin;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import gui.user.KorisnickiProzor;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.luka.Luka;
import model.luka.Pozicija;
import model.luka.Terminal;
import model.plovila.KontejnerskiBrod;
import model.plovila.Plovilo;
import model.plovila.PutnickiKruzer;
import model.plovila.Tanker;
import util.DnevnikGresaka;
import util.IzvozNaplataCSV;
import util.SerijalizacijaLuke;

public class AdministratorskiProzor {

    private final Stage stage;
    private final Luka luka;
    private ComboBox<Integer> izborTerminala;
    private TableView<Plovilo> tabelaPlovila;
    private Button dugmeDodaj;
    private Button dugmeIzmijeni;
    private Button dugmeObrisi;
    private Button dugmePokreniKorisnickiDio;
    private Label statusTerminala;
    private Label statusLuke;
    private Button dugmeIzveziCSV;

    public AdministratorskiProzor(Stage stage, Luka luka) {
        this.stage = stage;
        this.luka = luka;
    }

    public void prikazi() {
        BorderPane root = new BorderPane();

        root.setTop(kreirajZaglavlje());
        root.setCenter(kreirajSadrzaj());

        Scene scena = new Scene(root, 1000, 650);

        stage.setTitle("Luka – administratorska aplikacija");
        stage.setScene(scena);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    private VBox kreirajZaglavlje() {
        Label naslov = new Label("ADMINISTRACIJA LUKE");
        naslov.setStyle("-fx-font-size: 24px;" + "-fx-font-weight: bold;" + "-fx-text-fill: white;");

        Label opis = new Label("Upravljanje terminalima i plovilima");
        opis.setStyle("-fx-font-size: 14px;" + "-fx-text-fill: white;");

        VBox zaglavlje = new VBox(5, naslov, opis);
        zaglavlje.setAlignment(Pos.CENTER);
        zaglavlje.setPadding(new Insets(20));
        zaglavlje.setStyle("-fx-background-color: #174A6E;");

        return zaglavlje;
    }

    private VBox kreirajSadrzaj() {
        Label labelaTerminala = new Label("Izaberite terminal:");

        labelaTerminala.setStyle("-fx-font-size: 14px;" + "-fx-font-weight: bold;");

        izborTerminala = new ComboBox<>();

        for (int broj = 1; broj <= luka.getBrojTerminala(); broj++) {
            izborTerminala.getItems().add(broj);
        }

        izborTerminala.setPrefWidth(150);

        HBox izbor = new HBox(10, labelaTerminala, izborTerminala);

        izbor.setAlignment(Pos.CENTER_LEFT);

        tabelaPlovila = kreirajTabelu();
        statusTerminala = new Label();
        statusLuke = new Label();

        statusTerminala.setStyle("-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #174A6E;");

        statusLuke.setStyle("-fx-font-size: 14px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #174A6E;");

        HBox status = new HBox(30, statusTerminala, statusLuke);

        status.setAlignment(Pos.CENTER_LEFT);
        HBox akcije = kreirajAkcije();

        izborTerminala.setOnAction(dogadjaj -> osvjeziTabelu());

        izborTerminala.setValue(1);
        osvjeziTabelu();

        VBox sadrzaj = new VBox(15, izbor, status, tabelaPlovila, akcije);

        sadrzaj.setPadding(new Insets(20));
        VBox.setVgrow(tabelaPlovila, javafx.scene.layout.Priority.ALWAYS);

        return sadrzaj;
    }

    private HBox kreirajAkcije() {
        dugmeDodaj = new Button("Dodaj plovilo");
        dugmeIzmijeni = new Button("Izmijeni plovilo");
        dugmeObrisi = new Button("Obriši plovilo");

        dugmeDodaj.setStyle("-fx-background-color: #2E7D32;" + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;");

        dugmeIzmijeni.setStyle("-fx-background-color: #E68A00;" + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;");

        dugmeObrisi.setStyle("-fx-background-color: #C62828;" + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;");

        dugmeDodaj.setPrefWidth(150);
        dugmeIzmijeni.setPrefWidth(150);
        dugmeObrisi.setPrefWidth(150);

        dugmeIzmijeni.setDisable(true);
        dugmeObrisi.setDisable(true);

        tabelaPlovila.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, staro, novo) -> {
                    boolean nijeIzabrano = novo == null;
                    dugmeIzmijeni.setDisable(nijeIzabrano);
                    dugmeObrisi.setDisable(nijeIzabrano);
                });

        dugmeDodaj.setOnAction(dogadjaj -> dodajNovoPlovilo());

        dugmeObrisi.setOnAction(dogadjaj -> obrisiIzabranoPlovilo());

        dugmeIzmijeni.setOnAction(dogadjaj -> izmijeniIzabranoPlovilo());

        dugmePokreniKorisnickiDio = new Button("Pokreni korisnički dio");

        dugmePokreniKorisnickiDio.setStyle("-fx-background-color: #174A6E;" + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;");

        dugmePokreniKorisnickiDio.setPrefWidth(190);

        dugmePokreniKorisnickiDio.setOnAction(dogadjaj -> pokreniKorisnickiDio());

        dugmeIzveziCSV = new Button("Preuzmi CSV");

        dugmeIzveziCSV.setStyle("-fx-background-color: #5E35B1;" + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;");

        dugmeIzveziCSV.setPrefWidth(150);

        dugmeIzveziCSV.setOnAction(dogadjaj -> izveziNaplatuCSV());

        HBox akcije = new HBox(10, dugmePokreniKorisnickiDio, dugmeIzveziCSV, dugmeDodaj, dugmeIzmijeni,
                dugmeObrisi);

        akcije.setAlignment(Pos.CENTER_RIGHT);

        return akcije;
    }

    private void pokreniKorisnickiDio() {
        SerijalizacijaLuke.sacuvaj(luka);

        KorisnickiProzor prozor = new KorisnickiProzor(luka);

        prozor.prikazi();
    }

    private void obrisiIzabranoPlovilo() {
        Plovilo izabranoPlovilo = tabelaPlovila.getSelectionModel()
                        .getSelectedItem();

        if (izabranoPlovilo == null) {
            return;
        }

        Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);

        potvrda.setTitle("Potvrda brisanja");
        potvrda.setHeaderText("Brisanje plovila");
        potvrda.setContentText("Da li želite obrisati plovilo " + izabranoPlovilo.getNaziv() + "?");

        Optional<ButtonType> rezultat = potvrda.showAndWait();

        if (rezultat.isEmpty() || rezultat.get() != ButtonType.OK) {
            return;
        }

        boolean obrisano = luka.ukloniPlovilo(izabranoPlovilo);

        if (obrisano) {
            SerijalizacijaLuke.sacuvaj(luka);
            osvjeziTabelu();

            prikaziInformaciju("Plovilo je uspješno obrisano.");
        } else {
            prikaziGresku("Plovilo nije moguće obrisati.");
        }
    }

    private void prikaziInformaciju(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Informacija");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Greška");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private TableView<Plovilo> kreirajTabelu() {
        TableView<Plovilo> tabela = new TableView<>();

        TableColumn<Plovilo, String> kolonaTip = new TableColumn<>("Tip");

        kolonaTip.setCellValueFactory(
                podatak -> new SimpleStringProperty(odrediTipPlovila(podatak.getValue())));

        TableColumn<Plovilo, String> kolonaNaziv = new TableColumn<>("Naziv");

        kolonaNaziv.setCellValueFactory(podatak -> new SimpleStringProperty(podatak.getValue().getNaziv()));

        TableColumn<Plovilo, Number> kolonaIMO = new TableColumn<>("IMO broj");

        kolonaIMO.setCellValueFactory(
                podatak -> new SimpleIntegerProperty(podatak.getValue().getImoBroj()));

        TableColumn<Plovilo, String> kolonaRegistracija = new TableColumn<>("Registracija");

        kolonaRegistracija.setCellValueFactory(
                podatak -> new SimpleStringProperty(podatak.getValue().getRegistarskiBroj()));

        TableColumn<Plovilo, Number> kolonaMotori = new TableColumn<>("Motori");

        kolonaMotori.setCellValueFactory(
                podatak -> new SimpleIntegerProperty(podatak.getValue().getBrojMotora()));

        TableColumn<Plovilo, Number> kolonaBrzina = new TableColumn<>("Brzina");

        kolonaBrzina.setCellValueFactory(
                podatak -> new SimpleIntegerProperty(podatak.getValue().getBrzina()));

        TableColumn<Plovilo, String> kolonaSluzbe = new TableColumn<>("Službe");

        kolonaSluzbe.setCellValueFactory(
                podatak -> new SimpleStringProperty(podatak.getValue().getSluzbe().toString()));

        TableColumn<Plovilo, String> kolonaRotacija = new TableColumn<>("Rotacija");

        kolonaRotacija.setCellValueFactory(podatak ->
                new SimpleStringProperty(podatak.getValue().isRotacija() ? "DA" : "NE"));

        tabela.getColumns().addAll(kolonaTip, kolonaNaziv, kolonaIMO, kolonaRegistracija, kolonaMotori,
                kolonaBrzina, kolonaSluzbe, kolonaRotacija);

        tabela.setPlaceholder(new Label("Na terminalu nema plovila."));

        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        return tabela;
    }

    private String odrediTipPlovila(Plovilo plovilo) {
        if (plovilo instanceof KontejnerskiBrod) {
            return "Kontejnerski brod";
        }

        if (plovilo instanceof PutnickiKruzer) {
            return "Putnički kruzer";
        }

        if (plovilo instanceof Tanker) {
            return "Tanker";
        }

        return "Nepoznato";
    }

    private void osvjeziTabelu() {
        Integer brojTerminala = izborTerminala.getValue();

        if (brojTerminala == null) {
            tabelaPlovila.getItems().clear();
            return;
        }

        Terminal terminal = luka.getTerminal(brojTerminala);

        tabelaPlovila.setItems(FXCollections.observableArrayList(terminal.getPlovila()));
        osvjeziStatus(terminal);
    }

    private void osvjeziStatus(Terminal terminal) {
        int slobodniDokovi = terminal.getBrojSlobodnihDokova();

        int zauzetiDokovi = Terminal.BROJ_DOKOVA - slobodniDokovi;

        statusTerminala.setText("Terminal " + terminal.getBrojTerminala() + ": " + zauzetiDokovi
                + "/" + Terminal.BROJ_DOKOVA + " zauzetih dokova");

        int ukupanBrojDokova = luka.getBrojTerminala() * Terminal.BROJ_DOKOVA;

        int ukupnoSlobodnih = luka.getUkupanBrojSlobodnihDokova();

        int ukupnoZauzetih = ukupanBrojDokova - ukupnoSlobodnih;

        statusLuke.setText("Cijela luka: " + ukupnoZauzetih + "/" + ukupanBrojDokova + " zauzetih dokova");
    }

    private void dodajNovoPlovilo() {
        Integer brojTerminala = izborTerminala.getValue();

        if (brojTerminala == null) {
            prikaziGresku("Prvo izaberite terminal.");
            return;
        }

        Terminal terminal = luka.getTerminal(brojTerminala);

        if (terminal.jePopunjen()) {
            prikaziGresku("Izabrani terminal nema slobodnih dokova.");
            return;
        }

        FormaPlovila forma = new FormaPlovila(stage);

        Plovilo novoPlovilo = forma.prikazi();

        if (novoPlovilo == null) {
            return;
        }

        Pozicija pozicija = luka.dodajPloviloNaTerminal(novoPlovilo, brojTerminala);

        if (pozicija == null) {
            prikaziGresku("Plovilo nije moguće dodati na terminal.");
            return;
        }

        SerijalizacijaLuke.sacuvaj(luka);
        osvjeziTabelu();

        prikaziInformaciju("Plovilo je dodano na dok " + pozicija + ".");
    }

    private void izmijeniIzabranoPlovilo() {
        Plovilo izabranoPlovilo = tabelaPlovila.getSelectionModel()
                        .getSelectedItem();

        if (izabranoPlovilo == null) {
            return;
        }

        FormaPlovila forma = new FormaPlovila(stage, izabranoPlovilo);

        Plovilo izmijenjenoPlovilo = forma.prikazi();

        if (izmijenjenoPlovilo == null) {
            return;
        }

        SerijalizacijaLuke.sacuvaj(luka);
        osvjeziTabelu();

        prikaziInformaciju("Podaci o plovilu su uspješno izmijenjeni.");
    }

    private void izveziNaplatuCSV() {
        if (luka.getEvidencijaNaplata().isEmpty()) {
            prikaziInformaciju("Još nema evidentiranih naplata.");
            return;
        }

        FileChooser birac = new FileChooser();

        birac.setTitle("Sačuvajte evidenciju naplata");

        birac.setInitialFileName("evidencija-naplata.csv");

        birac.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV fajlovi", "*.csv"));

        File izabraniFajl = birac.showSaveDialog(stage);

        if (izabraniFajl == null) {
            return;
        }

        String putanjaTekst = izabraniFajl.getAbsolutePath();

        if (!putanjaTekst.toLowerCase().endsWith(".csv")) {
            putanjaTekst += ".csv";
        }

        Path putanja = Path.of(putanjaTekst);

        try {
            IzvozNaplataCSV.izvezi(luka.getEvidencijaNaplata(), putanja);

            prikaziInformaciju("CSV evidencija je uspješno sačuvana:\n" + putanja);

        } catch (Exception e) {
            DnevnikGresaka.evidentiraj("Greška pri izvozu CSV evidencije", e);
            prikaziGresku("CSV nije moguće sačuvati:\n" + e.getMessage());
        }
    }
}