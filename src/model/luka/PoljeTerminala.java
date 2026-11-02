package model.luka;

import java.io.Serializable;

import model.plovila.Plovilo;

public class PoljeTerminala implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Pozicija pozicija;
    private final TipPolja tip;
    private Plovilo plovilo;

    public PoljeTerminala(Pozicija pozicija, TipPolja tip) {
        this.pozicija = pozicija;
        this.tip = tip;
        plovilo = null;
    }

    public Pozicija getPozicija() {
        return pozicija;
    }

    public TipPolja getTip() {
        return tip;
    }

    public synchronized Plovilo getPlovilo() {
        return plovilo;
    }

    public synchronized boolean jeSlobodno() {
        return plovilo == null;
    }

    public synchronized boolean zauzmi(Plovilo novoPlovilo) {
        if (novoPlovilo == null || plovilo != null) {
            return false;
        }

        plovilo = novoPlovilo;
        return true;
    }

    public synchronized boolean oslobodi(Plovilo postojecePlovilo) {
        if (plovilo != postojecePlovilo) {
            return false;
        }

        plovilo = null;
        return true;
    }

    @Override
    public synchronized String toString() {
        if (plovilo == null) {
            return tip == TipPolja.DOK ? "*" : ".";
        }

        String oznaka = plovilo.getOznakaSluzbe();
        return oznaka.isEmpty() ? "B" : oznaka;
    }
}