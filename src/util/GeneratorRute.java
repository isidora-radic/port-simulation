package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.luka.Pozicija;
import model.luka.Terminal;
import model.luka.TipPolja;
import model.plovila.Plovilo;

public final class GeneratorRute {

    private GeneratorRute() {
    }

    public static List<Pozicija> kreirajRutuZaIzlazak(Terminal terminal, Plovilo plovilo) {
        if (terminal == null || plovilo == null) {
            return Collections.emptyList();
        }

        Pozicija pocetnaPozicija = terminal.pronadjiPozicijuPlovila(plovilo);

        if (pocetnaPozicija == null) {
            return Collections.emptyList();
        }

        if (terminal.getPolje(pocetnaPozicija.getRed(), pocetnaPozicija.getKolona()).getTip() != TipPolja.DOK) {
            return Collections.emptyList();
        }

        List<Pozicija> ruta = new ArrayList<>();

        if (pocetnaPozicija.getRed() == 0) {
            dodajRutuSaGornjegDoka(ruta, pocetnaPozicija.getKolona());
        } else if (pocetnaPozicija.getRed() == 3) {
            dodajRutuSaDonjegDoka(ruta, pocetnaPozicija.getKolona());
        }

        return ruta;
    }

    public static List<Pozicija> kreirajRutuPravoKrozTerminal() {
        List<Pozicija> ruta = new ArrayList<>();
        ruta.add(new Pozicija(1, 0));
        ruta.add(new Pozicija(2, 0));
        ruta.add(new Pozicija(3, 0));
        return ruta;
    }

    public static List<Pozicija> kreirajKruznuRutuKrozTerminal() {
        List<Pozicija> ruta = new ArrayList<>();

        ruta.add(new Pozicija(1, 0));
        ruta.add(new Pozicija(2, 0));
        ruta.add(new Pozicija(2, 1));

        for (int kolona = 2; kolona < Terminal.BROJ_KOLONA; kolona++) {
            ruta.add(new Pozicija(2, kolona));
        }

        ruta.add(new Pozicija(1, 16));

        for (int kolona = 15; kolona >= 1; kolona--) {
            ruta.add(new Pozicija(1, kolona));
        }

        ruta.add(new Pozicija(0, 1));
        return ruta;
    }

    public static List<Pozicija> kreirajRutuDoDoka(Terminal terminal, Pozicija ciljniDok) {
        if (terminal == null || ciljniDok == null
                || !terminal.ispravnaPozicija(ciljniDok.getRed(), ciljniDok.getKolona())) {
            return Collections.emptyList();
        }

        if (terminal.getPolje(ciljniDok.getRed(), ciljniDok.getKolona()).getTip() != TipPolja.DOK
                || !terminal.getPolje(ciljniDok.getRed(), ciljniDok.getKolona()).jeSlobodno()) {
            return Collections.emptyList();
        }

        List<Pozicija> ruta = new ArrayList<>();
        ruta.add(new Pozicija(1, 0));
        ruta.add(new Pozicija(2, 0));
        ruta.add(new Pozicija(2, 1));

        if (ciljniDok.getRed() == 3) {
            dodajRutuDoDonjegDoka(ruta, ciljniDok.getKolona());
        } else if (ciljniDok.getRed() == 0) {
            dodajRutuDoGornjegDoka(ruta, ciljniDok.getKolona());
        }

        return ruta;
    }

    private static void dodajRutuSaGornjegDoka(List<Pozicija> ruta, int kolonaDoka) {
        ruta.add(new Pozicija(1, kolonaDoka));

        for (int kolona = kolonaDoka - 1; kolona >= 1; kolona--) {
            ruta.add(new Pozicija(1, kolona));
        }

        ruta.add(new Pozicija(0, 1));
    }

    private static void dodajRutuSaDonjegDoka(List<Pozicija> ruta, int kolonaDoka) {
        ruta.add(new Pozicija(2, kolonaDoka));

        for (int kolona = kolonaDoka + 1; kolona < Terminal.BROJ_KOLONA; kolona++) {
            ruta.add(new Pozicija(2, kolona));
        }

        ruta.add(new Pozicija(1, 16));

        for (int kolona = 15; kolona >= 1; kolona--) {
            ruta.add(new Pozicija(1, kolona));
        }

        ruta.add(new Pozicija(0, 1));
    }

    private static void dodajRutuDoDonjegDoka(List<Pozicija> ruta, int kolonaDoka) {
        for (int kolona = 2; kolona <= kolonaDoka; kolona++) {
            ruta.add(new Pozicija(2, kolona));
        }

        ruta.add(new Pozicija(3, kolonaDoka));
    }

    private static void dodajRutuDoGornjegDoka(List<Pozicija> ruta, int kolonaDoka) {
        for (int kolona = 2; kolona < Terminal.BROJ_KOLONA; kolona++) {
            ruta.add(new Pozicija(2, kolona));
        }

        ruta.add(new Pozicija(1, 16));

        for (int kolona = 15; kolona >= kolonaDoka; kolona--) {
            ruta.add(new Pozicija(1, kolona));
        }

        ruta.add(new Pozicija(0, kolonaDoka));
    }
}
