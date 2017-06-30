/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.tabusearch;

import avdta.network.Path;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.project.SAVProject;
import avdta.sav.AssignedTaxi;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import avdta.sav.SAVSimulator;
import avdta.sav.SAVTraveler;
import avdta.sav.SAVZone;
import avdta.sav.Taxi;
import avdta.traveler.Traveler;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author prashanthvenkatraman
 */
public class TabuSearch {

    private SAVProject project;
    private SAVSimulator sim;
    private List<SAVTraveler> travelers;
    private List<Taxi> taxis;
    public static int nmax = 100;
    public static int m = 10;

    /**
     *
     */
    public TabuSearch(SAVProject project) throws IOException {
        //this.project = new SAVProject(new File("projects/grid_mel2"));
        this.project = project;
        this.sim = project.getSimulator();
        this.travelers = sim.getTravelers();
        this.taxis = sim.getTaxis();
    }

    public SAVProject getProject() {
        return project;
    }

    public void setProject(SAVProject project) {
        this.project = project;
    }

    public SAVSimulator getSim() {
        return sim;
    }

    public void setSim(SAVSimulator sim) {
        this.sim = sim;
    }

    public List<SAVTraveler> getTravelers() {
        return travelers;
    }

    public void setTravelers(List<SAVTraveler> travelers) {
        this.travelers = travelers;
    }

    public String search(Search p) {

        return null;
    }

    public List<Path> getBestRoute() throws IOException {
        int k = 2;
        List<Node> W = null;
        Search P1 = new Search(W, 5 * m, max(k, 5), 5, 5, 10, 0.01, 10, nmax);
        Search P2 = new Search(W, 5 * m, max(k, 5), 5, 5, 10, 0.01, 10, nmax);
        Search P3 = new Search(W, 5 * m, max(k, 5), 5, 5, 10, 0.01, 10, nmax);

        assignInitialTravelers();
        sim.simulate();
        double tstt = sim.getTSTT();

        return null;
    }

    public SAVTraveler createTravelerCopy(SAVTraveler t) {
        return new SAVTraveler(t.getId(), t.getOrigin(), t.getDest(), t.getDepTime(), t.getVOT(), t.getPickupTime(), t.getDropTime(), t.getPath(), t.getEtd());
    }

    public Taxi createTaxiCopy(Taxi taxi, List<SAVTraveler> travelersCopy) {
        List<SAVTraveler> passengers = taxi.getPassengers();
        Iterator<SAVTraveler> itrav = passengers.iterator();
        List<SAVTraveler> passengersCopy = new ArrayList<>();
        while (itrav.hasNext()) {
            SAVTraveler t = createTravelerCopy(itrav.next());
            passengersCopy.add(t);
            travelersCopy.add(t);
        }
        Taxi tCopy = new Taxi(taxi.getId(), taxi.getStartLocation(), taxi.getLocation(), taxi.getCapacity(), taxi.getDropTime(), taxi.getTT(), passengersCopy, taxi.delay, taxi.eta, taxi.park_time, taxi.total_distance, taxi.empty_distance);
        itrav = passengersCopy.iterator();
        while (itrav.hasNext()) {
            itrav.next().setAssignedTaxi(tCopy);
        }
        return tCopy;
    }

    public int max(int a, int b) {
        return a >= b ? a : b;
    }

    public double getSolution(SAVTraveler traveler, List<SAVTraveler> travelers, int pSize, int nmax) throws IOException {
        double bestTstt = sim.getTSTT();
        int n = 0;
        Map<TabuList, Integer> tabuList = new HashMap<TabuList, Integer>();
        for (SAVTraveler t : travelers) {
            List<NearestNeighbour> nNeigbour = getNearestTraveler(traveler, travelers, pSize);
            Iterator<NearestNeighbour> it = nNeigbour.iterator();
            while (it.hasNext()) {
                List<SAVTraveler> travelerCopy = new ArrayList<>();
                List<Taxi> taxiCopy = new ArrayList<>();
                for (Taxi tx : taxis) {
                    taxiCopy.add(createTaxiCopy(tx, travelerCopy));
                }
                genInsert(t, it.next());
                sim.simulate();
                double tstt = sim.getTSTT();

                //to-do: add item to tabulist and update tabu list count
                if (tstt <= bestTstt) {
                    n = tstt == bestTstt ? n++ : n;
                    bestTstt = tstt;
                    travelerCopy.clear();
                    taxiCopy.clear();

                } else {
                    travelers = travelerCopy;
                    taxis = taxiCopy;
                }
            }
        }
        return bestTstt;
    }

    /**
     *
     * @param startLocation
     * @param W
     * @return
     */
    public List<NearestNeighbour> getNearestTraveler(SAVTraveler traveler, List<SAVTraveler> travelers, int pSize) {
        double depTime = traveler.getDepTime();

        List<NearestNeighbour> nNeighbours = new ArrayList<>();
        for (SAVTraveler t : travelers) {

            if (depTime - 450 <= t.getDepTime() && depTime + 450 >= t.getDepTime()) {
                Taxi taxi = t.getAssignedTaxi();
                List<SAVTraveler> passengers = taxi.getPassengers();
                SAVTraveler previousPassenger = passengers.get(passengers.indexOf(t) - 1);
                Path p = sim.findPath(previousPassenger.getDest().getLinkedZone(), traveler.getOrigin(), (int) t.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);

                NearestNeighbour n = new NearestNeighbour(t, t.getAssignedTaxi(), p.getCost(), p);
                int index = Collections.binarySearch(nNeighbours, n);

                if (index < pSize - 1) {
                    nNeighbours.add(index < 0 ? -index - 1 : index, n);
                    if (nNeighbours.size() > pSize) {
                        nNeighbours.remove(pSize - 1);
                    }
                }
                Collections.sort(nNeighbours);
            }

        }

        return nNeighbours;
    }

    /**
     *
     * @param startLocation
     * @param W
     * @return
     */
//    public List<NearestNeighbour> getPNeighbourhood(SAVTraveler traveler, List<SAVTraveler> W, int pSize) {
//        sim.dijkstras(traveler.getDest(), 0, 1, DriverType.AV, TravelCost.ttCost);
//
//        List<NearestNeighbour> pNeighbours = getNearestTraveler(traveler, travelers);
//        //remove all items that come after pSize
//        for (NearestNeighbour n : pNeighbours) {
//            // node.label
//            Path p = sim.node_trace(traveler.getDest().getLinkedZone(), n.getNeighbour().getOrigin());
//            NearestNeighbour n = new NearestNeighbour(t, p.getCost());
//            int index = Collections.binarySearch(pNeighbours, n);
//            if (index < pSize - 1) {
//                pNeighbours.add(index < 0 ? -index - 1 : index, n);
//                pNeighbours.remove(pSize);
//            }
//            Collections.sort(pNeighbours);
//        }
//
//        return pNeighbours;
//
//    }
    public void genInsert(SAVTraveler v, NearestNeighbour n) {
        Taxi swapTaxi = n.getAssignedTaxi();
        swapPassenger(v, n.getNeighbour(), swapTaxi);
        swapPassenger(n.getNeighbour(), v, v.getAssignedTaxi());

    }

    public void swapPassenger(SAVTraveler t, SAVTraveler neighbour, Taxi swapTaxi) {
        List<SAVTraveler> passengers = swapTaxi.getPassengers();
        int index = passengers.indexOf(neighbour);
        passengers.add(index, t);
        passengers.remove(index + 1);
        List<Path> segments = ((AssignedTaxi) swapTaxi).getSegments();
        int segmentIndex = segments.indexOf(t.getPath());
        segments.add(segmentIndex, neighbour.getPath());
        segments.remove(segmentIndex + 1);

    }

    /**
     *
     * @param traveler
     * @param list of taxis
     * @return
     */
    public void findClosestTaxi(SAVTraveler traveler, List<Taxi> taxis) {

        //minT.setPath(p);
        Taxi minT = null;
        Path best = null;
        SAVOrigin location = null;
        double arrivalTime = Integer.MAX_VALUE;
        //double arrivalTime = minT.getDropTime() + p.getCost();

        for (Node n : sim.getNodes()) {
            if (!(n instanceof SAVOrigin)) {
                continue;
            }
            SAVOrigin node = (SAVOrigin) n;

            Taxi t = null;
            if (node.getFreeTaxis().size() > 0) {
                Taxi best_taxi = null;
                double best_time = Integer.MAX_VALUE;

                // todo: use binary search and insertion sort
                for (Taxi t2 : node.getFreeTaxis()) {
                    if (t2.getDropTime() < best_time) {
                        best_time = t2.getDropTime();
                        best_taxi = t2;
                    }
                }

                t = best_taxi;
            } else {
                continue;
            }

            if (n == traveler.getOrigin()) {
                minT = t;
                arrivalTime = t.getDropTime();
                best = null;
                location = (SAVOrigin) n;
                break;
            } else {
                Path p1 = sim.findPath(n, traveler.getOrigin().getLinkedZone());
                //t.setPath(p1);

                if (t.getDropTime() + p1.getCost() < arrivalTime) {
                    minT = t;
                    arrivalTime = t.getDropTime() + p1.getCost();
                    best = p1;
                    location = (SAVOrigin) n;
                }
            }
        }

        if (best != null) {
            ((AssignedTaxi) minT).addSegment(best);
        }

        int dep_time = (int) Math.max(arrivalTime, traveler.getDepTime());
        Path od = sim.findPath(traveler.getOrigin(), traveler.getDest(), (int) dep_time, 0, DriverType.AV, TravelCost.ttCost);
        minT.setDropTime(dep_time + traveler.getDest().label);
        traveler.setPath(od);
        traveler.setPickupTime(dep_time);
        traveler.setDropTime(dep_time + traveler.getDest().label);
        ((AssignedTaxi) minT).assignTraveler(traveler, od);

        location.removeFreeTaxi(minT);

        ((SAVOrigin) traveler.getDest().getLinkedZone()).addFreeTaxi(minT);

    }

    /**
     *
     * @param traveler origin
     * @param traveler destination
     * @param departure time from traveler origin
     * @return arrival time at traveler destination
     */
    public double findDropTime(SAVOrigin origin, SAVDest dest, double time) {
        Path od = sim.findPath(origin, dest, (int) time, 0, DriverType.AV, TravelCost.ffTime);
        return time + od.getCost();
    }

    /**
     * 1. Sort the travelers by departure time 2. For each traveler, assign the
     * taxi with the earliest arrival time.
     *
     * @param list of travelers
     * @param list of taxis
     * @return find initial solution.
     */
    public void assignInitialTravelers() {

        for (Taxi t : taxis) {
            t.getStartLocation().addFreeTaxi(t);
        }
        Collections.sort(travelers, new Comparator<SAVTraveler>() {
            @Override
            public int compare(SAVTraveler t1, SAVTraveler t2) {
                if (t1.getDepTime() < t2.getDepTime()) {
                    return -1;
                } else if (t1.getDepTime() == t2.getDepTime()) {
                    return 0;
                }
                return 1;
            }

        });

        for (SAVTraveler t : travelers) {
            findClosestTaxi(t, taxis);
        }

    }

}
