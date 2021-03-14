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
    private double neighbourTravelTime;

    private Path travelerNewTaxiPath;
    private Path travelerNewTaxiNextPath;
    private Path neighbourNewTaxiPath;
    private Path neighbourNewTaxiNextPath;

    public NearestNeighbour(SAVTraveler neighbour, Taxi assignedTaxi, double travelTime, double neighbourTravelTime, Path travelerNewTaxiPath, Path travelerNewTaxiNextPath, Path neighbourNewTaxiPath, Path neighbourNewTaxiNextPath) {
        this.neighbour = neighbour;
        this.assignedTaxi = assignedTaxi;
        this.travelTime = travelTime;
        this.neighbourTravelTime = neighbourTravelTime;
        this.travelerNewTaxiPath = travelerNewTaxiPath;
        this.travelerNewTaxiNextPath = travelerNewTaxiNextPath;
        this.neighbourNewTaxiPath = neighbourNewTaxiPath;
        this.neighbourNewTaxiNextPath = neighbourNewTaxiNextPath;
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

    public Path getTravelerNewTaxiPath() {
        return travelerNewTaxiPath;
    }

    public void setTravelerNewTaxiPath(Path travelerNewTaxiPath) {
        this.travelerNewTaxiPath = travelerNewTaxiPath;
    }

    public double getNeighbourTravelTime() {
        return neighbourTravelTime;
    }

    public void setNeighbourTravelTime(double neighbourTravelTime) {
        this.neighbourTravelTime = neighbourTravelTime;
    }

    public Path getTravelerNewTaxiNextPath() {
        return travelerNewTaxiNextPath;
    }

    public void setTravelerNewTaxiNextPath(Path travelerNewTaxiNextPath) {
        this.travelerNewTaxiNextPath = travelerNewTaxiNextPath;
    }

    public Path getNeighbourNewTaxiPath() {
        return neighbourNewTaxiPath;
    }

    public void setNeighbourNewTaxiPath(Path neighbourNewTaxiPath) {
        this.neighbourNewTaxiPath = neighbourNewTaxiPath;
    }

    public Path getNeighbourNewTaxiNextPath() {
        return neighbourNewTaxiNextPath;
    }

    public void setNeighbourNewTaxiNextPath(Path neighbourNewTaxiNextPath) {
        this.neighbourNewTaxiNextPath = neighbourNewTaxiNextPath;
    }

    @Override
    public int compareTo(NearestNeighbour neighbour) {
        if (this.getTravelTime() + this.getNeighbourTravelTime() > neighbour.getTravelTime() + neighbour.getNeighbourTravelTime()) {
            return 1;
        } else if (this.getTravelTime() + this.getNeighbourTravelTime() < neighbour.getTravelTime() + neighbour.getNeighbourTravelTime()) {
            return -1;
        }
        return 0;
    }

}
