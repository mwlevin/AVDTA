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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

        assignInitialTravelers();
        sim.simulate();
        double best = sim.getTSTT();
        double personBestTt = sim.getTotalPersonTT();
        for(SAVTraveler t:travelers){
        	System.out.println("Traveler enter time:" + t.getEnterTime() + "Travler departure time:" + t.getDepTime() + "Traveler Exit time:s" + t.getExitTime());
        }
        
        double avgWaitTime = sim.getAvgWait()/60;
        double avgIVTT = sim.getAvgIVTT()/60;
        System.out.println("Avg wait:" + avgWaitTime + "Avg IVTT:" + avgIVTT);
        Map<TabuList, Integer> tabuMap = new HashMap<TabuList, Integer>();
        double tstt = getSolution(10, 20, 5, tabuMap);
        System.out.println("initial total person travel time = " + personBestTt + "\t aVerage person travel time " + (personBestTt / 60 / travelers.size()));
        System.out.println("initial total vehicle travel time = " + best);
        
        

//        double tst1 = getSolution(5, 10, 10, tabuMap);
////
//     System.out.println("initial total person travel time = " + personBestTt + "\t aVerage person travel time " + (personBestTt / 60 / travelers.size()));
//       System.out.println("initial total vehicle travel time = " + best);
//        
//        double tst2 = getSolution(5, 10, 15);
//        System.out.println("initial total person travel time = " + personBestTt + "\t aVerage person travel time " + (personBestTt / 60 / travelers.size()));
//        System.out.println("initial total vehicle travel time = " + best);
        return null;
    }


    public int max(int a, int b) {
        return a >= b ? a : b;
    }

    public double getSolution(int pSize, int nmax, int maxMoves, Map<TabuList, Integer> tabuMap) throws IOException {
        double bestTstt = sim.getTSTT();
        double bestPersonTt = sim.getTotalPersonTT();
        
        double avgBestPersonTt = bestPersonTt / 60 / travelers.size();
        double avgIVTT = sim.getAvgIVTT()/60;
        double avgWaitTime = sim.getAvgWait();
        System.out.println("The travel time is " + bestTstt + " expected " + getExpTaxiTT());
        System.out.println("The person travel time is " + bestPersonTt + "\t aVerage person travel time " + (bestPersonTt / 60 / travelers.size()));
        System.out.println("Average Wait Time per traveler " + avgWaitTime + "\nAverage In-Vehicle TravelT " + avgIVTT + "\n");

        int n = 0;
//        Map<TabuList, Integer> tabuMap = new HashMap<TabuList, Integer>();
        for (SAVTraveler t : travelers) {
            List<NearestNeighbour> nNeigbour = getNearestTraveler(t, travelers, pSize);
            Iterator<NearestNeighbour> it = nNeigbour.iterator();
            System.out.println("New traveler " + t);
            n=0;
            while (it.hasNext()) {
                NearestNeighbour neighbour = it.next();

                Taxi tx = t.getAssignedTaxi();
                Taxi old = neighbour.getNeighbour().getAssignedTaxi();

                TabuList tabu = new TabuList(t, neighbour.getAssignedTaxi());
                if (!tabuMap.containsKey(tabu)) {
                    Path p, p1, p2, p3 = null;
                    
                    List<Path> segmentsOld = ((AssignedTaxi) tx).getSegments();
                    int travIndex = segmentsOld.indexOf(t.getPath());
                    p = travIndex>0 ? segmentsOld.get(travIndex-1):null;
                    p2 = travIndex<segmentsOld.size()-1?segmentsOld.get(travIndex+1):null;
                    
                    List<Path> segmentsNew = ((AssignedTaxi) old).getSegments();
                    int neighIndex = segmentsNew.indexOf(neighbour.getNeighbour().getPath());
                    p1 = neighIndex>0 ? segmentsNew.get(neighIndex-1):null;
                    p3 = neighIndex<segmentsNew.size()-1?segmentsNew.get(neighIndex+1):null;
                    
                    genInsert(t, neighbour.getNeighbour(), neighbour.getTravelerNewTaxiPath(), neighbour.getTravelerNewTaxiNextPath(), neighbour.getNeighbourNewTaxiPath(), neighbour.getNeighbourNewTaxiNextPath());
//                System.out.println("Traveler taxi = " + t.getAssignedTaxi() + " Old Taxi = " + tx);
//                System.out.println("Neighbour taxi = " + neighbour.getNeighbour().getAssignedTaxi() + " Old Taxi = " + old);

                    System.out.println("***Simulating***");
                    sim.simulate();
                    double tstt = sim.getTSTT();
                    double personTt = sim.getTotalPersonTT();
                    avgIVTT = sim.getAvgIVTT()/60;
                    avgWaitTime = sim.getAvgWait();
                    double avgPersonTt = personTt / 60 / travelers.size();
                    if (personTt <= bestPersonTt) {
                        if (tstt <= bestTstt) {
                            bestTstt = tstt;
                        }
                        bestPersonTt = personTt;
                        avgBestPersonTt = avgPersonTt;
//                        n = (avgPersonTt >= avgBestPersonTt - 1 && avgPersonTt <= avgBestPersonTt + 1) ? n + 1 : n;
                        n=0;
                        tabu.setPreviousTaxi(t.getAssignedTaxi());
                        tabuMap.put(tabu, 0);
                        System.out.println("The best travel time is " + bestTstt + " expected: " + getExpTaxiTT());
                        System.out.println("Best total person tt " + personTt + "\t Average best person travel time " + (personTt / 60 / travelers.size()));
                        System.out.println("Average Wait Time per traveler " + avgWaitTime + "\nAverage In-Vehicle TravelT " + avgIVTT + "\n");
                        printAssignment();
                        break;
                    } else {
//                        n = avgPersonTt >= avgBestPersonTt - 1 ? n + 1 : n;
//                        System.out.println("n is " + n);
                        n++;
                        System.out.println("The simulated travel time is " + tstt + " expected: " + getExpTaxiTT());
                        System.out.println("Simulated Total person tt " + personTt + " expected = " + getExpTravelerTT() +"\t Average person travel time " + (personTt / 60 / travelers.size()) + "\n");
                        System.out.println("Average Wait Time per traveler " + avgWaitTime + "\n Average In-Vehicle TravelT " + avgIVTT);

                        genInsert(neighbour.getNeighbour(), t, p, p2, p1, p3);
                        if (n == nmax) {
//                            Collections.shuffle(travelers);
//                            System.out.println("***Recursing***");
//                            bestTstt = getSolution(pSize, nmax, maxMoves, tabuMap);
                            break;
                        }

                    }
                }
                for (Map.Entry entry : tabuMap.entrySet()) {
                    int value = (int) entry.getValue();
                    entry.setValue(value++);
                    if (value >= maxMoves) {
                        tabuMap.remove(entry.getKey());
                    }
                }

            }
            if(n == nmax){
                break;
            }

        }
        System.out.println("Simulated Best person travel time " + bestPersonTt + "\t Simulated Average person travel time " + (bestPersonTt / 60 / travelers.size()));
        System.out.println("Simulated Best vehicle travel time " + bestTstt);
        return bestTstt;
    }
    
    public double getExpTravelerTT(){
        double output = 0.0;
        
        for(SAVTraveler t: travelers){
            if(t.isExited()){
                output += t.getTT();
            }
            
        }
        return output;
    }

    public double getExpTaxiTT() {
        double output = 0.0;

        for (Taxi t : taxis) {
            AssignedTaxi taxi = (AssignedTaxi) t;

            for (Path p : taxi.getSegments()) {
                output += p.getFFTime();
            }
        }

        return output;
    }
    
    public void printAssignment() throws IOException
    {
        File file = new File(project.getResultsFolder()+"/SAV_assignment.txt");
        
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        fileout.println("Taxi id\tTravelers (in order)");
        for(Taxi t : sim.getTaxis())
        {
            AssignedTaxi taxi = (AssignedTaxi)t;
            
            fileout.print(taxi.getId());
            
            for(Traveler person : taxi.getTravelers())
            {
                fileout.print("\t"+person.getId());
            }
        }
        
        fileout.close();
    }
    
    public List<NearestNeighbour> getNearestTaxiInsert(SAVTraveler traveler, List<SAVTraveler> travelers, int pSize){
        
        return null;
    }

    /**
     *
     * @param startLocation
     * @param W
     * @return
     */
    public List<NearestNeighbour> getNearestTraveler(SAVTraveler traveler, List<SAVTraveler> travelers, int pSize) {
        double depTime = traveler.getDepTime();

        Taxi travelerTaxi = traveler.getAssignedTaxi();
        List<SAVTraveler> travelerMates = ((AssignedTaxi) travelerTaxi).getTravelers();
        int neighbourPrevPassIndex = travelerMates.indexOf(traveler) - 1;
        int neighbourNextPassIndex = travelerMates.indexOf(traveler) + 1;

        List<NearestNeighbour> nNeighbours = new ArrayList<>();

        for (SAVTraveler t : travelers) {
            if (traveler.getAssignedTaxi() == t.getAssignedTaxi()) {
                continue;
            }

            if (depTime - 450 <= t.getDepTime() && depTime + 450 >= t.getDepTime()) {
                Taxi taxi = t.getAssignedTaxi();
//                System.out.println(taxi);
                List<SAVTraveler> passengers = ((AssignedTaxi) taxi).getTravelers();

                SAVTraveler previousPassenger, nextPassenger, neighPrevPass, neighNextPass = null;
//                System.out.print(travelerTaxi + " to " + taxi + "\n");
//               System.out.print("Traveler " + traveler + " to " + t + "\n");

                int prevPassengerIndex = passengers.indexOf(t) - 1;
                int nextPassengerIndex = passengers.indexOf(t) + 1;
                double travelTimeTravOrig, nextPassDropTime = 0.0;
                double prevPassDropTime = 0.0;
                Path p = null;
                if (prevPassengerIndex >= 0 && prevPassengerIndex < passengers.size() - 1) {
                    previousPassenger = passengers.get(prevPassengerIndex);
                    prevPassDropTime = previousPassenger.getDropTime();
                    p = sim.findPath(previousPassenger.getDest().getLinkedZone(), traveler.getOrigin().getLinkedZone(), (int) previousPassenger.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
                } else {
                    p = sim.findPath(taxi.getStartLocation(), traveler.getOrigin().getLinkedZone(), (int) taxi.getDepTime(), 0, DriverType.AV, TravelCost.ttCost);
                }

                Path p2 = null;
                if (nextPassengerIndex > 0 && nextPassengerIndex <= passengers.size() - 1) {
                    nextPassenger = passengers.get(nextPassengerIndex);
                    nextPassDropTime = nextPassenger.getDropTime();
                    p2= sim.findPath(traveler.getDest().getLinkedZone(), nextPassenger.getOrigin().getLinkedZone(), (int) traveler.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
                } else {
                    p2 = null;
                }
                nextPassDropTime = 0.0 != nextPassDropTime ? nextPassDropTime : traveler.getDropTime();
                prevPassDropTime = 0.0 != prevPassDropTime ? prevPassDropTime : taxi.getDepTime();
                travelTimeTravOrig = nextPassDropTime - prevPassDropTime;

                Path p1 = null;
                double neighTimeTravOrig, neighNextPassDrop = 0.0;
                double neighPrevPassDrop = 0.0;
                if (neighbourPrevPassIndex >= 0 && neighbourPrevPassIndex < travelerMates.size() - 1) {
                    neighPrevPass = travelerMates.get(neighbourPrevPassIndex);
                    neighPrevPassDrop = neighPrevPass.getDropTime();
                    p1 = sim.findPath(neighPrevPass.getDest().getLinkedZone(), t.getOrigin().getLinkedZone(), (int) neighPrevPass.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
                } else {
                    p1 = sim.findPath(travelerTaxi.getStartLocation(), t.getOrigin().getLinkedZone(), (int) travelerTaxi.getDepTime(), 0, DriverType.AV, TravelCost.ttCost);
                }

                Path p3 = null;
                if (neighbourNextPassIndex > 0 && neighbourNextPassIndex <= travelerMates.size() - 1) {
                    neighNextPass = travelerMates.get(neighbourNextPassIndex);
                    neighNextPassDrop = neighNextPass.getDropTime();
                    p3 = sim.findPath(t.getDest().getLinkedZone(), neighNextPass.getOrigin().getLinkedZone(), (int) t.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
                } else {
                    p3 = null;
                }

                neighPrevPassDrop = 0.0 != neighPrevPassDrop ? neighPrevPassDrop : travelerTaxi.getDepTime();
                neighNextPassDrop = 0.0 != neighNextPassDrop ? neighNextPassDrop : t.getDropTime();
                neighTimeTravOrig = neighNextPassDrop - neighPrevPassDrop;

                double dropTimeTraveler = null != p2 ? p2.getCost() : traveler.getDropTime();
                double dropTimeNeighbour = null != p3 ? p3.getCost() : t.getDropTime();
                double travelerTTime = p.getCost() + traveler.getPath().getCost() + dropTimeTraveler;
                double neighTTime = p1.getCost() + t.getPath().getCost() + dropTimeNeighbour;

                if (travelerTTime + neighTTime < travelTimeTravOrig + neighTimeTravOrig) {
                    NearestNeighbour n = new NearestNeighbour(t, taxi, travelerTTime, neighTTime, p, p2, p1, p3);

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

        }

        return nNeighbours;
    }

    public void genInsert(SAVTraveler v, SAVTraveler n, Path p, Path p2, Path p1, Path p3) {

        Taxi nTaxi = n.getAssignedTaxi();
        List<SAVTraveler> travelers = ((AssignedTaxi) nTaxi).getTravelers();
        SAVTraveler previousPassenger, nextPassenger = null;

        int prevPassengerIndex = travelers.indexOf(n) - 1;
        int nextPassengerIndex = travelers.indexOf(n) + 1;

        if (prevPassengerIndex >= 0 && prevPassengerIndex <= travelers.size() - 1) {
            previousPassenger = travelers.get(prevPassengerIndex);
            p = sim.findPath(previousPassenger.getDest().getLinkedZone(), v.getOrigin().getLinkedZone(), (int) previousPassenger.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
        } else {
            p = sim.findPath(nTaxi.getStartLocation(), v.getOrigin().getLinkedZone(), (int) nTaxi.getDepTime(), 0, DriverType.AV, TravelCost.ttCost);
        }

        if (nextPassengerIndex >= travelers.size() - 1) {
            p2 = null;
        } else {
//            System.out.println("The travelers size is " + travelers.size() + " and the index is " + nextPassengerIndex);
            nextPassenger = travelers.get(nextPassengerIndex);
            p2 = sim.findPath(v.getDest().getLinkedZone(), nextPassenger.getOrigin().getLinkedZone(), (int) v.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
        }
        swapPassenger(v, n, n.getAssignedTaxi(), p, p2);

        Taxi travelerTaxi = v.getAssignedTaxi();
        List<SAVTraveler> travelerMates = ((AssignedTaxi) travelerTaxi).getTravelers();
        int neighbourPrevPassIndex = travelerMates.indexOf(v) - 1;
        int neighbourNextPassIndex = travelerMates.indexOf(v) + 1;

        if (neighbourPrevPassIndex >= 0 && neighbourPrevPassIndex <= travelerMates.size()) {
            SAVTraveler prevTravMate = travelerMates.get(neighbourPrevPassIndex);
            p1 = sim.findPath(prevTravMate.getDest().getLinkedZone(), n.getOrigin().getLinkedZone(), (int) prevTravMate.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
        } else {
            p1 = sim.findPath(travelerTaxi.getStartLocation(), n.getOrigin().getLinkedZone(), (int) travelerTaxi.getDepTime(), 0, DriverType.AV, TravelCost.ttCost);
        }

        if (neighbourNextPassIndex >= travelerMates.size() - 1) {
            p3 = null;
        } else {
//            System.out.println("The traveler mates size is " + travelerMates.size() + " and the index is " + neighbourNextPassIndex);
            SAVTraveler nextTravMate = travelerMates.get(neighbourNextPassIndex);
            p3 = sim.findPath(n.getDest().getLinkedZone(), nextTravMate.getOrigin().getLinkedZone(), (int) n.getDropTime(), 0, DriverType.AV, TravelCost.ttCost);
        }

        swapPassenger(n, v, v.getAssignedTaxi(), p1, p3);

        n.setAssignedTaxi(v.getAssignedTaxi());

        v.setAssignedTaxi(nTaxi);

    }

    public void swapPassenger(SAVTraveler t, SAVTraveler neighbour, Taxi swapTaxi, Path prevPath, Path nextPath) {

        List<SAVTraveler> passengers = ((AssignedTaxi) swapTaxi).getTravelers();
        int index = passengers.indexOf(neighbour);
//        System.out.println("Insertion index" + index + " " + neighbour + " " + passengers);

        passengers.add(index, t);
        passengers.remove(index + 1);

//      System.out.println("Traveler " + t + " for " + neighbour + "inserted in Taxi " + swapTaxi + "in" + passengers);
        List<Path> segments = ((AssignedTaxi) swapTaxi).getSegments();
//        System.out.println(segments);

        int segmentIndex = (2 * index) + 1;

//        System.out.println("Segment index " + segmentIndex);
//        System.out.println(neighbour.getPath());
//        System.out.println(segments.get(segmentIndex));
//        System.out.print("Taxi path is : ");
//        for (Path s : segments) {
//            System.out.print(s);
//        }
//        System.out.print("\n");
        segments.add(segmentIndex, t.getPath());

        if (null != prevPath) {
            segments.remove(segmentIndex - 1);
            segments.add(segmentIndex - 1, prevPath);
        }

        segments.remove(segmentIndex + 1);

        if (null != nextPath) {
            segments.remove(segmentIndex + 1);
            segments.add(segmentIndex + 1, nextPath);
        }

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
                best = new Path();
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

        if (null != best) {
            ((AssignedTaxi) minT).addSegment(best);
        }

        int dep_time = (int) Math.max(arrivalTime, traveler.getDepTime());
        Path od = sim.findPath(traveler.getOrigin(), traveler.getDest(), (int) dep_time, 0, DriverType.AV, TravelCost.ttCost);
        minT.setDropTime(dep_time + traveler.getDest().label);
        traveler.setPath(od);
        traveler.setPickupTime(dep_time);
//        traveler.setAssignedTaxi(minT);
        traveler.setDropTime(dep_time + traveler.getDest().label);
        traveler.setTaxiPathToOrigin(best);
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

//        for (SAVTraveler t : travelers) {
//            findClosestTaxi(t, taxis);
        //System.out.print("\n");
//        }
        for (int i = 0, j = 0; i < travelers.size(); i++) {
            Taxi tx = ((AssignedTaxi) taxis.get(j));
            SAVTraveler t = travelers.get(i);
            Path best = new Path();

            double arrivalTime = t.getDropTime();

            if (t.getOrigin() != tx.getCurrentLocation()) {
                best = sim.findPath(tx.getCurrentLocation(), t.getOrigin().getLinkedZone());
                arrivalTime = arrivalTime + best.getCost();
            }
            int dep_time = (int) Math.max(arrivalTime, t.getDepTime());
            Path od = sim.findPath(t.getOrigin(), t.getDest(), (int) dep_time, 0, DriverType.AV, TravelCost.ttCost);
            tx.setDropTime(dep_time + t.getDest().label);
            t.setPath(od);
            t.setPickupTime(dep_time);
            t.setDropTime(dep_time + t.getDest().label);
            t.setTaxiPathToOrigin(best);
            ((AssignedTaxi) tx).addSegment(best);
            ((AssignedTaxi) tx).assignTraveler(t, od);
            t.setAssignedTaxi(tx);
//                System.out.println(t.getAssignedTaxi());
            j++;
            if (j > taxis.size() - 1) {
                j = 0;
            }

        }

    }

}
