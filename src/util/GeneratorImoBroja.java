package util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class GeneratorImoBroja {

    private static final Random RANDOM = new Random();
    private static final Set<Integer> ZAUZETI_BROJEVI = new HashSet<>();

    private GeneratorImoBroja() {
    }

    public static synchronized int generisi() {
        int imoBroj;

        do {
            imoBroj = 1_000_000 + RANDOM.nextInt(9_000_000);
        } while (ZAUZETI_BROJEVI.contains(imoBroj));

        ZAUZETI_BROJEVI.add(imoBroj);
        return imoBroj;
    }

    public static synchronized boolean registruj(int imoBroj) {
        return ZAUZETI_BROJEVI.add(imoBroj);
    }
}