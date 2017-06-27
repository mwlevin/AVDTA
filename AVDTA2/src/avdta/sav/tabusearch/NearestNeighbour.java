/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.tabusearch;

import avdta.traveler.Traveler;

/**
 *
 * @author prashanthvenkatraman
 */
public class NearestNeighbour implements Comparable<NearestNeighbour> {
    private Traveler neighbour;
    private double travelTime;

    public NearestNeighbour(Traveler neighbour, double travelTime) {
        this.neighbour = neighbour;
        this.travelTime = travelTime;
    }

    public Traveler getNeighbour() {
        return neighbour;
    }

    public void setNeighbour(Traveler neighbour) {
        this.neighbour = neighbour;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }

    @Override
    public int compareTo(NearestNeighbour neighbour) {
        if(this.getTravelTime() > neighbour.getTravelTime()){
            return 1;
        }
        else if(this.getTravelTime() < neighbour.getTravelTime()){
            return -1;
        }
        return 0;
    }
    
    
}
