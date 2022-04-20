/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.tabusearch;

import avdta.sav.SAVTraveler;
import avdta.sav.Taxi;

/**
 *
 * @author prashanthvenkatraman
 */
public class TabuList {
    private SAVTraveler traveler;
    private Taxi previousTaxi;

    public TabuList(SAVTraveler traveler, Taxi previousTaxi) {
        this.traveler = traveler;
        this.previousTaxi = previousTaxi;
    }

    public SAVTraveler getTraveler() {
        return traveler;
    }

    public void setTraveler(SAVTraveler traveler) {
        this.traveler = traveler;
    }

    public Taxi getPreviousTaxi() {
        return previousTaxi;
    }

    public void setPreviousTaxi(Taxi previousTaxi) {
        this.previousTaxi = previousTaxi;
    }
    
    
}
