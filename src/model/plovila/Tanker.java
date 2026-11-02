package model.plovila;
import model.sluzbe.TipSluzbe;

public class Tanker extends Plovilo {

    private static final long serialVersionUID = 1L;

    private double zapreminaBareli;

    public Tanker(String naziv, int brojMotora, String putanjaFotografije, String registarskiBroj, double zapreminaBareli) {

        super(naziv, brojMotora, putanjaFotografije, registarskiBroj);
        this.zapreminaBareli = zapreminaBareli;
    }

    public double getZapreminaBareli() {
        return zapreminaBareli;
    }

    public void setZapreminaBareli(double zapreminaBareli) {
        this.zapreminaBareli = zapreminaBareli;
    }
    
    @Override
    public boolean mozePripadatiSluzbi(TipSluzbe sluzba) {
        return sluzba == TipSluzbe.OBALSKA_STRAZA
                || sluzba == TipSluzbe.CARINA
                || sluzba == TipSluzbe.VATROGASCI;
    }

    @Override
    public String toString() {
        return "Tanker: " + super.toString()
                + ", zapremina=" + zapreminaBareli + " barela";
    }
}