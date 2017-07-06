/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.tabusearch;

import avdta.network.Path;
import avdta.sav.SAVTraveler;
import avdta.sav.Taxi;

/**
 *
 * @author prashanthvenkatraman
 */
public class NearestNeighbour implements Comparable<NearestNeighbour> {

    private SAVTraveler neighbour;
    private SAVTraveler previousPassenger;
    private Taxi assignedTaxi;
    private double travelTime;
    private Path travelerTaxiPath;
    private Path neighbourTaxiPath;

    public NearestNeighbour(SAVTraveler neighbour, SAVTraveler previousPassenger, Taxi assignedTaxi, double travelTime, Path travelerTaxiPath, Path neighbourTaxiPath) {
        this.neighbour = neighbour;
        this.previousPassenger = previousPassenger;
        this.assignedTaxi = assignedTaxi;
        this.travelTime = travelTime;
        this.travelerTaxiPath = travelerTaxiPath;
        this.neighbourTaxiPath = neighbourTaxiPath;
    }

    public SAVTraveler getNeighbour() {
        return neighbour;
    }

    public void setNeighbour(SAVTraveler neighbour) {
        this.neighbour = neighbour;
    }

    public Taxi getAssignedTaxi() {
        return assignedTaxi;
    }

    public void setAssignedTaxi(Taxi assignedTaxi) {
        this.assignedTaxi = assignedTaxi;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }

    public SAVTraveler getPreviousPassenger() {
        return previousPassenger;
    }

    public void setPreviousPassenger(SAVTraveler previousPassenger) {
        this.previousPassenger = previousPassenger;
    }

    public Path getTravelerTaxiPath() {
        return travelerTaxiPath;
    }

    public void setTravelerTaxiPath(Path travelerTaxiPath) {
        this.travelerTaxiPath = travelerTaxiPath;
    }

    public Path getNeighbourTaxiPath() {
        return neighbourTaxiPath;
    }

    public void setNeighbourTaxiPath(Path neighbourTaxiPath) {
        this.neighbourTaxiPath = neighbourTaxiPath;
    }

    @Override
    public int compareTo(NearestNeighbour neighbour) {
        if (this.getTravelTime() > neighbour.getTravelTime()) {
            return 1;
        } else if (this.getTravelTime() < neighbour.getTravelTime()) {
            return -1;
        }
        return 0;
    }

}
