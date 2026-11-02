package model.sluzbe;

public enum TipSluzbe {

    CARINA("C", 1),
    OBALSKA_STRAZA("O", 2),
    VATROGASCI("V", 3);

    private final String oznaka;
    private final int prioritet;

    TipSluzbe(String oznaka, int prioritet) {
        this.oznaka = oznaka;
        this.prioritet = prioritet;
    }

    public String getOznaka() {
        return oznaka;
    }

    public int getPrioritet() {
        return prioritet;
    }
}