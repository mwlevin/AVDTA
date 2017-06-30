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
    private Taxi assignedTaxi;
    private double travelTime;
    private Path path;

    public NearestNeighbour(SAVTraveler neighbour, Taxi taxi, double travelTime, Path path) {
        this.neighbour = neighbour;
        this.assignedTaxi = taxi;
        this.travelTime = travelTime;
        this.path = path;
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

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
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
