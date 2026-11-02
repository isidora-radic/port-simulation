package application;

import gui.admin.AdministratorskiProzor;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import model.luka.Luka;
import util.DnevnikGresaka;
import util.Konfiguracija;
import util.SerijalizacijaLuke;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DnevnikGresaka.inicijalizuj();

        Thread.setDefaultUncaughtExceptionHandler((nit, greska) ->
                DnevnikGresaka.evidentiraj("Neobrađena greška u niti " + nit.getName(), greska));

        try {
            int brojTerminala = Konfiguracija.ucitajBrojTerminala();
            Luka luka = SerijalizacijaLuke.ucitajIliKreiraj(brojTerminala);
            AdministratorskiProzor prozor = new AdministratorskiProzor(primaryStage, luka);

            prozor.prikazi();

        } catch (Exception e) {
            DnevnikGresaka.evidentiraj("Greška pri pokretanju aplikacije", e);
            prikaziGresku(e);
        }
    }

    private void prikaziGresku(Exception e) {
        Alert upozorenje = new Alert(AlertType.ERROR);

        upozorenje.setTitle("Greška");
        upozorenje.setHeaderText("Aplikacija se ne može pokrenuti");
        upozorenje.setContentText(e.getMessage());
        upozorenje.showAndWait();

        e.printStackTrace();
    }

    public static void main(String[] args) {
        launch(args);
    }
}