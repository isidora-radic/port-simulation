package gui.admin;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import model.plovila.KontejnerskiBrod;
import model.plovila.Plovilo;
import model.plovila.PutnickiKruzer;
import model.plovila.Tanker;
import javafx.scene.control.CheckBox;
import model.sluzbe.TipSluzbe;

public class FormaPlovila {

    private final Stage prozor;
    private Plovilo kreiranoPlovilo;
    private final Plovilo ploviloZaIzmjenu;

    private ComboBox<String> izborTipa;
    private TextField poljeNaziv;
    private TextField poljeBrojMotora;
    private TextField poljeRegistracija;
    private TextField poljeFotografija;

    private Label labelaPosebnogPodatka;
    private TextField poljePosebnogPodatka;
    private CheckBox poljeObalskaStraza;
    private CheckBox poljeCarina;
    private CheckBox poljeVatrogasci;
    private CheckBox poljeRotacija;

    private TextField poljePotjernice;
    private Button dugmePotjernice;

    public FormaPlovila(Window vlasnik) {
        this(vlasnik, null);
    }

    public FormaPlovila(
            Window vlasnik,
            Plovilo ploviloZaIzmjenu) {

        this.ploviloZaIzmjenu = ploviloZaIzmjenu;
        this.kreiranoPlovilo = null;

        prozor = new Stage();

        prozor.initOwner(vlasnik);
        prozor.initModality(Modality.APPLICATION_MODAL);

        prozor.setTitle(
                ploviloZaIzmjenu == null
                        ? "Dodavanje plovila"
                        : "Izmjena plovila"
        );

        prozor.setScene(
                new Scene(kreirajSadrzaj(), 550, 600)
        );

        prozor.setResizable(false);

        if (ploviloZaIzmjenu != null) {
            popuniFormu(ploviloZaIzmjenu);
        }
    }
    
    private void popuniFormu(Plovilo plovilo) {
        if (plovilo instanceof KontejnerskiBrod brod) {
            izborTipa.setValue("Kontejnerski brod");
            poljePosebnogPodatka.setText(
                    String.valueOf(brod.getKapacitetTEU())
            );

        } else if (plovilo instanceof PutnickiKruzer kruzer) {
            izborTipa.setValue("Putnički kruzer");
            poljePosebnogPodatka.setText(
                    String.valueOf(kruzer.getBrojPutnika())
            );

        } else if (plovilo instanceof Tanker tanker) {
            izborTipa.setValue("Tanker");
            poljePosebnogPodatka.setText(
                    String.valueOf(tanker.getZapreminaBareli())
            );
        }

        izborTipa.setDisable(true);

        poljeNaziv.setText(plovilo.getNaziv());

        poljeBrojMotora.setText(
                String.valueOf(plovilo.getBrojMotora())
        );

        poljeRegistracija.setText(
                plovilo.getRegistarskiBroj()
        );

        poljeFotografija.setText(
                plovilo.getPutanjaFotografije()
        );

        poljeObalskaStraza.setSelected(
                plovilo.pripadaSluzbi(
                        TipSluzbe.OBALSKA_STRAZA
                )
        );

        poljeCarina.setSelected(
                plovilo.pripadaSluzbi(TipSluzbe.CARINA)
        );

        poljeVatrogasci.setSelected(
                plovilo.pripadaSluzbi(
                        TipSluzbe.VATROGASCI
                )
        );

        prilagodiPotjernice();
        prilagodiRotaciju();

        if (plovilo.getPutanjaFajlaPotjernica() != null) {
            poljePotjernice.setText(
                    plovilo.getPutanjaFajlaPotjernica()
            );
        }

        poljeRotacija.setSelected(
                plovilo.isRotacija()
        );
    }

    public Plovilo prikazi() {
        prozor.showAndWait();
        return kreiranoPlovilo;
    }

    private GridPane kreirajSadrzaj() {
        GridPane forma = new GridPane();

        forma.setPadding(new Insets(25));
        forma.setHgap(15);
        forma.setVgap(15);
        forma.setAlignment(Pos.CENTER);

        izborTipa = new ComboBox<>();
        izborTipa.getItems().addAll(
                "Kontejnerski brod",
                "Putnički kruzer",
                "Tanker"
        );
        izborTipa.setPrefWidth(250);

        poljeNaziv = new TextField();
        poljeBrojMotora = new TextField();
        poljeRegistracija = new TextField();

        poljeFotografija = new TextField();
        poljeFotografija.setEditable(false);

        Button dugmeFotografija =
                new Button("Izaberi...");

        HBox izborFotografije = new HBox(
                8,
                poljeFotografija,
                dugmeFotografija
        );

        poljeFotografija.setPrefWidth(165);

        labelaPosebnogPodatka =
                new Label("Poseban podatak:");

        poljePosebnogPodatka = new TextField();
        
        poljeObalskaStraza =
                new CheckBox("Obalska straža");

        poljeCarina = new CheckBox("Carina");
        poljeVatrogasci = new CheckBox("Vatrogasci");
        poljeRotacija = new CheckBox("Uključena rotacija");

        HBox izborSluzbi = new HBox(
                10,
                poljeObalskaStraza,
                poljeCarina,
                poljeVatrogasci
        );

        poljePotjernice = new TextField();
        poljePotjernice.setEditable(false);
        poljePotjernice.setPrefWidth(165);

        dugmePotjernice = new Button("Izaberi...");

        HBox izborPotjernica = new HBox(
                8,
                poljePotjernice,
                dugmePotjernice
        );

        Button dugmeSacuvaj = new Button("Sačuvaj");
        Button dugmeOdustani = new Button("Odustani");

        dugmeSacuvaj.setStyle(
                "-fx-background-color: #2E7D32;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );

        dugmeOdustani.setStyle(
                "-fx-background-color: #757575;"
                + "-fx-text-fill: white;"
        );

        HBox dugmad = new HBox(
                10,
                dugmeSacuvaj,
                dugmeOdustani
        );
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        forma.add(new Label("Tip plovila:"), 0, 0);
        forma.add(izborTipa, 1, 0);

        forma.add(new Label("Naziv:"), 0, 1);
        forma.add(poljeNaziv, 1, 1);

        forma.add(new Label("Broj motora:"), 0, 2);
        forma.add(poljeBrojMotora, 1, 2);

        forma.add(new Label("Registracija:"), 0, 3);
        forma.add(poljeRegistracija, 1, 3);

        forma.add(new Label("Fotografija:"), 0, 4);
        forma.add(izborFotografije, 1, 4);

        forma.add(labelaPosebnogPodatka, 0, 5);
        forma.add(poljePosebnogPodatka, 1, 5);

        forma.add(new Label("Državne službe:"), 0, 6);
        forma.add(izborSluzbi, 1, 6);

        forma.add(new Label("Rotacija:"), 0, 7);
        forma.add(poljeRotacija, 1, 7);

        forma.add(new Label("Fajl potjernica:"), 0, 8);
        forma.add(izborPotjernica, 1, 8);

        forma.add(dugmad, 1, 9);

        izborTipa.setOnAction(
                dogadjaj -> prilagodiPosebnoPolje()
        );

        dugmeFotografija.setOnAction(
                dogadjaj -> izaberiFotografiju()
        );

        dugmeOdustani.setOnAction(
                dogadjaj -> prozor.close()
        );
        
        dugmeSacuvaj.setOnAction(
                dogadjaj -> sacuvajPlovilo()
        );
        
        poljeObalskaStraza.setOnAction(
                dogadjaj -> prilagodiPotjernice()
        );

        poljeCarina.setOnAction(
                dogadjaj -> prilagodiRotaciju()
        );

        poljeVatrogasci.setOnAction(
                dogadjaj -> prilagodiRotaciju()
        );

        dugmePotjernice.setOnAction(
                dogadjaj -> izaberiFajlPotjernica()
        );

        izborTipa.setValue("Kontejnerski brod");

        return forma;
    }
    
    private void prilagodiPosebnoPolje() {
        String tip = izborTipa.getValue();

        poljePosebnogPodatka.clear();

        if ("Kontejnerski brod".equals(tip)) {
            labelaPosebnogPodatka.setText(
                    "Kapacitet u TEU:"
            );
        } else if ("Putnički kruzer".equals(tip)) {
            labelaPosebnogPodatka.setText(
                    "Broj putnika:"
            );
        } else if ("Tanker".equals(tip)) {
            labelaPosebnogPodatka.setText(
                    "Zapremina u barelima:"
            );
        }
        poljeObalskaStraza.setSelected(false);
        poljeCarina.setSelected(false);
        poljeVatrogasci.setSelected(false);
        poljeRotacija.setSelected(false);
        poljePotjernice.clear();

        poljeObalskaStraza.setDisable(false);

        poljeCarina.setDisable(
                "Kontejnerski brod".equals(tip)
        );

        poljeVatrogasci.setDisable(
                !"Tanker".equals(tip)
        );

        prilagodiPotjernice();
        prilagodiRotaciju();
    }
    
    private void prilagodiPotjernice() {
        boolean obalskaStraza =
                poljeObalskaStraza.isSelected();

        poljePotjernice.setDisable(!obalskaStraza);
        dugmePotjernice.setDisable(!obalskaStraza);

        if (!obalskaStraza) {
            poljePotjernice.clear();
        }

        prilagodiRotaciju();
    }
    
    private void prilagodiRotaciju() {
        boolean izabranaSluzba =
                poljeObalskaStraza.isSelected()
                || poljeCarina.isSelected()
                || poljeVatrogasci.isSelected();

        poljeRotacija.setDisable(!izabranaSluzba);

        if (!izabranaSluzba) {
            poljeRotacija.setSelected(false);
        }
    }
    
    private void izaberiFajlPotjernica() {
        FileChooser birac = new FileChooser();

        birac.setTitle(
                "Izaberite fajl sa IMO brojevima"
        );

        birac.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Tekstualni fajlovi",
                        "*.txt",
                        "*.csv"
                )
        );

        File fajl = birac.showOpenDialog(prozor);

        if (fajl != null) {
            poljePotjernice.setText(
                    fajl.getAbsolutePath()
            );
        }
    }
    
    private void izaberiFotografiju() {
        FileChooser birac = new FileChooser();

        birac.setTitle("Izaberite fotografiju plovila");

        birac.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Slike",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif"
                )
        );

        File fajl = birac.showOpenDialog(prozor);

        if (fajl != null) {
            poljeFotografija.setText(
                    fajl.getAbsolutePath()
            );
        }
    }
    
    private void sacuvajPlovilo() {
        String tip = izborTipa.getValue();
        String naziv = poljeNaziv.getText().trim();
        String registracija =
                poljeRegistracija.getText().trim();
        String fotografija =
                poljeFotografija.getText().trim();

        if (tip == null
                || naziv.isBlank()
                || registracija.isBlank()
                || fotografija.isBlank()
                || poljeBrojMotora.getText().isBlank()
                || poljePosebnogPodatka.getText().isBlank()) {

            prikaziGresku(
                    "Potrebno je popuniti sva polja."
            );
            return;
        }
        
        if (poljeObalskaStraza.isSelected()
                && poljePotjernice.getText().isBlank()) {

            prikaziGresku(
                    "Za plovilo obalske straže potrebno je "
                    + "izabrati fajl potjernica."
            );
            return;
        }

        try {
            int brojMotora = Integer.parseInt(
                    poljeBrojMotora.getText().trim()
            );

            if (brojMotora <= 0) {
                prikaziGresku(
                        "Broj motora mora biti veći od nule."
                );
                return;
            }
            
            if (ploviloZaIzmjenu != null) {
                izmijeniPostojecePlovilo(
                        naziv,
                        brojMotora,
                        fotografija,
                        registracija
                );

                prozor.close();
                return;
            }

            if ("Kontejnerski brod".equals(tip)) {
                int kapacitetTEU = Integer.parseInt(
                        poljePosebnogPodatka
                                .getText()
                                .trim()
                );

                if (kapacitetTEU <= 0) {
                    prikaziGresku(
                            "Kapacitet mora biti veći od nule."
                    );
                    return;
                }

                kreiranoPlovilo = new KontejnerskiBrod(
                        naziv,
                        brojMotora,
                        fotografija,
                        registracija,
                        kapacitetTEU
                );

            } else if ("Putnički kruzer".equals(tip)) {
                int brojPutnika = Integer.parseInt(
                        poljePosebnogPodatka
                                .getText()
                                .trim()
                );

                if (brojPutnika <= 0) {
                    prikaziGresku(
                            "Broj putnika mora biti veći od nule."
                    );
                    return;
                }

                kreiranoPlovilo = new PutnickiKruzer(
                        naziv,
                        brojMotora,
                        fotografija,
                        registracija,
                        brojPutnika
                );

            } else if ("Tanker".equals(tip)) {
                double zapremina = Double.parseDouble(
                        poljePosebnogPodatka
                                .getText()
                                .trim()
                );

                if (zapremina <= 0) {
                    prikaziGresku(
                            "Zapremina mora biti veća od nule."
                    );
                    return;
                }

                kreiranoPlovilo = new Tanker(
                        naziv,
                        brojMotora,
                        fotografija,
                        registracija,
                        zapremina
                );
            }

            primijeniSluzbe();
            prozor.close();

        } catch (NumberFormatException e) {
            prikaziGresku(
                    "Broj motora i poseban podatak "
                    + "moraju biti ispravni brojevi."
            );
        }
    }
    
    private void primijeniSluzbe() {
        if (kreiranoPlovilo == null) {
            return;
        }

        if (poljeObalskaStraza.isSelected()) {
            kreiranoPlovilo.dodajSluzbu(
                    TipSluzbe.OBALSKA_STRAZA
            );

            kreiranoPlovilo.setPutanjaFajlaPotjernica(
                    poljePotjernice.getText().trim()
            );
        }

        if (poljeCarina.isSelected()) {
            kreiranoPlovilo.dodajSluzbu(
                    TipSluzbe.CARINA
            );
        }

        if (poljeVatrogasci.isSelected()) {
            kreiranoPlovilo.dodajSluzbu(
                    TipSluzbe.VATROGASCI
            );
        }

        kreiranoPlovilo.setRotacija(
                poljeRotacija.isSelected()
        );
    }
    
    private void izmijeniPostojecePlovilo(
            String naziv,
            int brojMotora,
            String fotografija,
            String registracija) {

        double posebanPodatak = Double.parseDouble(
                poljePosebnogPodatka.getText().trim()
        );

        if (posebanPodatak <= 0) {
            throw new NumberFormatException();
        }

        ploviloZaIzmjenu.setNaziv(naziv);
        ploviloZaIzmjenu.setBrojMotora(brojMotora);
        ploviloZaIzmjenu.setPutanjaFotografije(
                fotografija
        );
        ploviloZaIzmjenu.setRegistarskiBroj(
                registracija
        );

        if (ploviloZaIzmjenu
                instanceof KontejnerskiBrod brod) {

            if (posebanPodatak % 1 != 0) {
                throw new NumberFormatException();
            }

            brod.setKapacitetTEU((int) posebanPodatak);

        } else if (ploviloZaIzmjenu
                instanceof PutnickiKruzer kruzer) {

            if (posebanPodatak % 1 != 0) {
                throw new NumberFormatException();
            }

            kruzer.setBrojPutnika((int) posebanPodatak);

        } else if (ploviloZaIzmjenu instanceof Tanker tanker) {
            tanker.setZapreminaBareli(posebanPodatak);
        }

        ukloniPostojeceSluzbe();

        kreiranoPlovilo = ploviloZaIzmjenu;
        primijeniSluzbe();
    }
    
    private void ukloniPostojeceSluzbe() {
        ploviloZaIzmjenu.ukloniSluzbu(
                TipSluzbe.CARINA
        );

        ploviloZaIzmjenu.ukloniSluzbu(
                TipSluzbe.OBALSKA_STRAZA
        );

        ploviloZaIzmjenu.ukloniSluzbu(
                TipSluzbe.VATROGASCI
        );
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.initOwner(prozor);
        alert.setTitle("Neispravan unos");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
    
}