package model.plovila;
import model.sluzbe.TipSluzbe;

public class PutnickiKruzer extends Plovilo {

    private static final long serialVersionUID = 1L;

    private int brojPutnika;

    public PutnickiKruzer(String naziv, int brojMotora, String putanjaFotografije, String registarskiBroj, int brojPutnika) {

        super(naziv, brojMotora, putanjaFotografije, registarskiBroj);
        this.brojPutnika = brojPutnika;
    }

    public int getBrojPutnika() {
        return brojPutnika;
    }

    public void setBrojPutnika(int brojPutnika) {
        this.brojPutnika = brojPutnika;
    }
    
    @Override
    public boolean mozePripadatiSluzbi(TipSluzbe sluzba) {
        return sluzba == TipSluzbe.OBALSKA_STRAZA
                || sluzba == TipSluzbe.CARINA;
    }

    @Override
    public String toString() {
        return "Putnički kruzer: " + super.toString()
                + ", broj putnika=" + brojPutnika;
    }
}