package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.plovila.KontejnerskiBrod;
import model.plovila.Plovilo;
import model.plovila.PutnickiKruzer;
import model.plovila.Tanker;
import model.sluzbe.TipSluzbe;

public final class GeneratorPlovila {

    private static final Random RANDOM = new Random();
    private static int redniBroj = 1;

    private GeneratorPlovila() {
    }

    public static Plovilo generisiPlovilo() {
        int tip = RANDOM.nextInt(3);
        String naziv = "Plovilo-" + redniBroj++;
        int brojMotora = 1 + RANDOM.nextInt(6);
        String registracija = generisiRegistraciju();
        String fotografija = "generisano-bez-fotografije";

        Plovilo plovilo;

        if (tip == 0) {
            int kapacitetTEU = 500 + RANDOM.nextInt(19_501);
            plovilo = new KontejnerskiBrod(naziv, brojMotora, fotografija, registracija, kapacitetTEU);
        } else if (tip == 1) {
            int brojPutnika = 100 + RANDOM.nextInt(4_901);
            plovilo = new PutnickiKruzer(naziv, brojMotora, fotografija, registracija, brojPutnika);
        } else {
            double zapremina = 1_000 + RANDOM.nextInt(199_001);
            plovilo = new Tanker(naziv, brojMotora, fotografija, registracija, zapremina);
        }

        dodijeliSluzbu(plovilo);
        return plovilo;
    }

    private static String generisiRegistraciju() {
        int broj = 10_000 + RANDOM.nextInt(90_000);
        return "REG-" + broj;
    }

    private static void dodijeliSluzbu(Plovilo plovilo) {
        boolean komercijalno = RANDOM.nextInt(100) < 90;

        if (komercijalno) {
            return;
        }

        List<TipSluzbe> dozvoljene = new ArrayList<>();

        for (TipSluzbe sluzba : TipSluzbe.values()) {
            if (plovilo.mozePripadatiSluzbi(sluzba)) {
                dozvoljene.add(sluzba);
            }
        }

        if (dozvoljene.isEmpty()) {
            return;
        }

        TipSluzbe izabranaSluzba = dozvoljene.get(RANDOM.nextInt(dozvoljene.size()));
        plovilo.dodajSluzbu(izabranaSluzba);

        if (izabranaSluzba == TipSluzbe.OBALSKA_STRAZA) {
            plovilo.setPutanjaFajlaPotjernica("potjernice.txt");
        }

        plovilo.setRotacija(RANDOM.nextBoolean());
    }

    public static Plovilo generisiSluzbenoPlovilo(TipSluzbe sluzba) {
        if (sluzba == null) {
            return null;
        }

        String naziv = "Intervencija-" + sluzba + "-" + redniBroj++;
        String registracija = generisiRegistraciju();
        String fotografija = "generisano-bez-fotografije";
        Plovilo plovilo;

        if (sluzba == TipSluzbe.VATROGASCI) {
            plovilo = new Tanker(naziv, 4, fotografija, registracija, 100_000);
        } else if (sluzba == TipSluzbe.CARINA) {
            plovilo = new PutnickiKruzer(naziv, 3, fotografija, registracija, 500);
        } else {
            plovilo = new KontejnerskiBrod(naziv, 4, fotografija, registracija, 5_000);
        }

        plovilo.dodajSluzbu(sluzba);
        plovilo.setRotacija(true);

        if (sluzba == TipSluzbe.OBALSKA_STRAZA) {
            plovilo.setPutanjaFajlaPotjernica("potjernice.txt");
        }

        return plovilo;
    }
}