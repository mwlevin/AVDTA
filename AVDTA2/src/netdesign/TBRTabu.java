package netdesign;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import avdta.dta.DTASimulator;
import avdta.network.ReadNetwork;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.network.node.NodeRecord;
import avdta.project.DTAProject;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;

/*
    Rishabh and Karthik
 */
public class TBRTabu extends TabuSearch<TBRIndividual>{

    private int type;

    // intersections is map of <nodeID, control>
    private Map<Integer, Integer> intersections;
    private DTAProject project;

    private int max_tbrs;
    private boolean checkHV;
    private boolean isSO;

    private Map<Integer, Set<Integer>> odpairs;

    public TBRTabu(DTAProject project, int max_tbrs, boolean checkHV, boolean isSO, int population_size,
                   double proportion_kept, double mutate_percent, List<Integer> signals, int max_itr ) {
        super(max_itr, 0);
        this.project = project;
        this.max_tbrs = max_tbrs;
        this.checkHV = checkHV;
        this.isSO = isSO;

        intersections = new HashMap<Integer, Integer>();

        int counter = 0;
        DTASimulator sim = project.getSimulator();

        for (int n : signals) {
            intersections.put(n, counter);
            counter++;
        }

        type = ReadNetwork.RESERVATION + ReadNetwork.FCFS;// ReadNetwork.MCKS +
        // ReadNetwork.PRESSURE;

        if (checkHV) {
            odpairs = new HashMap<Integer, Set<Integer>>();

            for (Vehicle v : sim.getVehicles()) {
                PersonalVehicle veh = (PersonalVehicle) v;
                if (!v.getDriver().isTransit()) {
                    int r = veh.getOrigin().getId();
                    int s = veh.getDest().getId();

                    if (!odpairs.containsKey(r)) {
                        odpairs.put(r, new HashSet<Integer>());
                    }
                    odpairs.get(r).add(s);
                }
            }
        }
    }


    @Override
    public SortedSet<TBRIndividual> generateNeighbor(TBRIndividual currentState) {

        return null;
    }

    @Override
    public TBRIndividual generateRandom() {

        return null;
    }

    @Override
    public boolean isNeighborBetter(TBRIndividual current, TBRIndividual neighbor) {
        return (neighbor.compareTo(current) > 0);
    }
}
