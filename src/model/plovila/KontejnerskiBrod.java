package model.plovila;

import model.sluzbe.TipSluzbe;

public class KontejnerskiBrod extends Plovilo {

    private static final long serialVersionUID = 1L;

    private int kapacitetTEU;

    public KontejnerskiBrod(String naziv, int brojMotora, String putanjaFotografije, String registarskiBroj, int kapacitetTEU) {

        super(naziv, brojMotora, putanjaFotografije, registarskiBroj);
        this.kapacitetTEU = kapacitetTEU;
    }

    public int getKapacitetTEU() {
        return kapacitetTEU;
    }

    public void setKapacitetTEU(int kapacitetTEU) {
        this.kapacitetTEU = kapacitetTEU;
    }
    
    @Override
    public boolean mozePripadatiSluzbi(TipSluzbe sluzba) {
        return sluzba == TipSluzbe.OBALSKA_STRAZA;
    }

    @Override
    public String toString() {
        return "Kontejnerski brod: " + super.toString()
                + ", kapacitet=" + kapacitetTEU + " TEU";
    }
}