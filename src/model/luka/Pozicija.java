package model.luka;

import java.io.Serializable;
import java.util.Objects;

public class Pozicija implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int red;
    private final int kolona;

    public Pozicija(int red, int kolona) {
        this.red = red;
        this.kolona = kolona;
    }

    public int getRed() {
        return red;
    }

    public int getKolona() {
        return kolona;
    }

    @Override
    public String toString() {
        return "(" + red + ", " + kolona + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, kolona);
    }

    @Override
    public boolean equals(Object objekat) {
        if (this == objekat) {
            return true;
        }

        if (objekat == null || getClass() != objekat.getClass()) {
            return false;
        }

        Pozicija druga = (Pozicija) objekat;
        return red == druga.red && kolona == druga.kolona;
    }
}