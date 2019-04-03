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
import avdta.network.node.TBR;
import avdta.project.DTAProject;
import avdta.util.Pair;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;

/*
    Rishabh and Karthik
 */
public class TBRTabu extends TabuSearch<TBRIndividual>{

    private int type;

    // intersections is map of <nodeID, index into controls>
    private Map<Integer, Integer> intersections;
    private DTAProject project;

    private int max_tbrs;
    private boolean isSO;

    private Map<String, Street> streets;
    private Map<Integer, Pair<String, String>> base;
    private int radius;
    private int numNeighbors;
    private List<Integer> signals;

    public TBRTabu(DTAProject project, boolean isSO, List<Integer> signals, int max_itr, int rad, int neigh) {
        super(max_itr, 0);
        this.project = project;
        this.isSO = isSO;
        radius = rad;
        numNeighbors = neigh;
        intersections = new HashMap<>();
        this.signals = signals;
        int counter = 0;

        for (int n : signals) {
            intersections.put(n, counter);
            counter++;
        }

        type = ReadNetwork.RESERVATION + ReadNetwork.FCFS;
        base = new HashMap<>();
        streets = new HashMap<>();
        try {
            Scanner intersections = new Scanner(new File("AVDTA2/src/netdesign/intersections.txt"));
            intersections.nextLine();
            while(intersections.hasNextLine()) {
                String[] details = intersections.nextLine().split("\\s+");
//                System.out.println(details[0] + " " + details[1] + " " +  details[2]);
                int id = Integer.parseInt(details[0]);
                String NS = details[1];
                String EW = details[2];
                base.put(id, new Pair<>(NS, EW));
            }

            Scanner filein = new Scanner(project.getNodesFile());
            filein.nextLine();
            while(filein.hasNextLine()) {
                NodeRecord n = new NodeRecord(filein.nextLine());
                if(!n.isZone() && base.containsKey(n.getId())) {
                    Pair<String, String> s = base.get(n.getId());
                    if(streets.containsKey(s.first())) {
                        streets.get(s.first()).addNode(n);
                    } else {
                        Street str = new Street(s.first(), n);
                        streets.put(s.first(), str);
                    }
                    if(streets.containsKey(s.second())) {
                        streets.get(s.second()).addNode(n);
                    } else {
                        Street str = new Street(s.second(), n);
                        streets.put(s.second(), str);
                    }
                }
            }
        } catch (FileNotFoundException ignored){
            System.out.println("file not found");
            System.out.println(new File(".").getAbsolutePath());
            System.exit(1);
        }
        System.out.println("Constructed TBRTabu");
//        System.out.println(intersections);
//        System.exit(0);
    }

    public TBRTabu(DTAProject project, boolean isSO, List<Integer> signals, int max_itr, int rad, int neigh, String warmNodes) {
        super(max_itr, 0);
        this.project = project;
        this.isSO = isSO;
        radius = rad;
        numNeighbors = neigh;
        intersections = new HashMap<>();
        TBRIndividual warm;
        int counter = 0;

        for (int n : signals) {
            intersections.put(n, counter);
            counter++;
        }

        type = ReadNetwork.RESERVATION + ReadNetwork.FCFS;
        base = new HashMap<>();
        streets = new HashMap<>();
        HashMap<Integer, Integer> temp = new HashMap<>();
        try {
            Scanner intersections = new Scanner(new File("AVDTA2/src/netdesign/intersections.txt"));
            intersections.nextLine();
            while(intersections.hasNextLine()) {
                String[] details = intersections.nextLine().split("\\s+");
//                System.out.println(details[0] + " " + details[1] + " " +  details[2]);
                int id = Integer.parseInt(details[0]);
                String NS = details[1];
                String EW = details[2];
                base.put(id, new Pair<>(NS, EW));
            }

            Scanner filein = new Scanner(project.getNodesFile());
            Scanner warmStart = new Scanner(new File(warmNodes));
            warmStart.nextLine();
            while(warmStart.hasNextLine()) {
                String[] details = warmStart.nextLine().split("\\s+");
                int id = Integer.parseInt(details[0]);
                int setting = Integer.parseInt(details[1]);
                temp.put(id, setting);
            }
            System.out.println(temp);
            filein.nextLine();
            while(filein.hasNextLine()) {
                NodeRecord n = new NodeRecord(filein.nextLine());
                if(!n.isZone() && base.containsKey(n.getId())) {
                    Pair<String, String> s = base.get(n.getId());
                    if(streets.containsKey(s.first())) {
                        streets.get(s.first()).addNode(n, temp.get(n.getId()));
                    } else {
                        Street str = new Street(s.first(), n, temp.get(n.getId()));
                        str.allowInterUpdates();;
                        streets.put(s.first(), str);
                    }
                    if(streets.containsKey(s.second())) {
                        streets.get(s.second()).addNode(n);
                        n.setType(temp.get(n.getId()));
                    } else {
                        Street str = new Street(s.second(), n, temp.get(n.getId()));
                        str.allowInterUpdates();
                        streets.put(s.second(), str);
                    }
                }
            }
        } catch (FileNotFoundException ignored){
            System.out.println("file not found");
            System.out.println(new File(".").getAbsolutePath());
            System.exit(1);
        }
        System.out.println("Constructed TBRTabu from Warm Start");
        currentSolution = generateWarm();
        bestSolution = currentSolution;
    }

    public void changeNodes(TBRIndividual org) throws IOException {
        Scanner filein = new Scanner(project.getNodesFile());
        File newFile = new File(project.getProjectDirectory() + "/new_nodes.txt");
        PrintStream fileout = new PrintStream(new FileOutputStream(newFile), true);

        fileout.println(filein.nextLine());

        while (filein.hasNextLine()) {
            NodeRecord node = new NodeRecord(filein.nextLine());
            if (!node.isZone()) {
                if (intersections.containsKey(node.getId())) {
                    node.setType(org.getControl(intersections.get(node.getId())));
                } else {
                    node.setType(ReadNetwork.SIGNAL);
                }
            }
            fileout.println(node);
        }
        filein.close();
        fileout.close();

        project.getNodesFile().delete();
        newFile.renameTo(project.getNodesFile());
    }

    public void evaluate(TBRIndividual child){
        try {
            changeNodes(child);
            project.loadSimulator();
            DTASimulator sim = project.getSimulator();
            // solve DTA
            sim.msa(30, 2.0);
            child.setAssignment(sim.getAssignment());
            PrintStream out = new PrintStream(child.getAssignment().getResultsDirectory() + "/assignNodes.txt");
            child.setObj(sim.getTSTT() / 3600.0);
            child.writeToFile(out);
        } catch (IOException e) {
            child.setObj(Double.MAX_VALUE);
        }
//                child.setObj(Math.random() * 6000.0);
    }

    @Override
    public TBRIndividual solve() {
        TBRIndividual out =  super.solve();
        try {
            changeNodes(out);
            System.out.println("Saved best configuration in the network file");
        } catch (IOException ignored){
            System.out.println("Couldn't save best configuration in network file");
        }

        return out;
    }

    public TBRIndividual solve(int baseIterations, int microIterations, int microRadius, int microNeighbors) {
        currentSolution = solve(baseIterations);
        System.out.println("Finished execution of big Tabu steps and moving to small steps");
        currentSolution.getStreets().values().forEach(Street::allowInterUpdates);
        maxIterations = microIterations;
        radius = microRadius;
        numNeighbors = microNeighbors;
        return this.solve();

    }

    @Override
    public SortedSet<TBRIndividual> generateNeighbor(TBRIndividual currentState) {
        SortedSet<TBRIndividual> output = new TreeSet<>();
        System.out.println("Current Best State: " + bestSolution.getObj() + " Proportion of Reservations: " + bestSolution.tbrRatio());
        System.out.println("Current State: " + currentState.getObj() + " Proportion of Reservations: " + currentState.tbrRatio());
        for(int i = 0; i < numNeighbors; i ++) {
            TBRIndividual perturbed = currentState.createNeighbor(radius);
//            System.out.println(perturbed.getStreets());
//            System.out.println(perturbed.getTbrs());
            if(tabuList.contains(perturbed)) {
                System.out.println("Generated configuration that exists in Tabu List");
                i--;
                continue;
            }
            System.out.println("Evaluating Neighbor #" + i);
            evaluate(perturbed);
            System.out.println("Current TSTT: " + currentState.getObj() + ", Neighbor TSTT:" + perturbed.getObj());
            System.out.println("Current Proportion: " + currentState.tbrRatio() +", Neighbor Proportion: " + perturbed.tbrRatio());
            output.add(perturbed);
        }
        System.out.println("Generated and Evaluated Neighbors");
        return output;
    }

    @Override
    public TBRIndividual generateRandom() {
        TBRIndividual org;
        int[] controls = new int[intersections.size()];
        for (int i = 0; i < controls.length; i++) {
            controls[i] = ReadNetwork.SIGNAL;
        }
        List<Integer> tbrs = new ArrayList<>(max_tbrs);
        for(Street s: streets.values()) {
            if (Math.random() < 0.5) {
                s.flipIntersections();
            }
            for (NodeRecord nr: s.getLights().values()) {
                controls[intersections.get(nr.getId())] = nr.getType();
                if(nr.getType() == type && !tbrs.contains(intersections.get(nr.getId()))) {
                    tbrs.add(intersections.get(nr.getId()));
                }
                if(nr.getType() != type) {
                    tbrs.remove(intersections.get(nr.getId()));
                }
            }
        }

//            System.out.println(streets);
//            System.out.println(tbrs);
        org = new TBRIndividual(controls, tbrs, false, streets, intersections);
        System.out.println("Generated Random");
        System.out.println("Random Proportion of Reservations: " + org.tbrRatio());
        System.out.println("Evaluating Random");
		evaluate(org);
		return org;
    }

    public TBRIndividual generateWarm() {
        TBRIndividual org;
        int[] controls = new int[intersections.size()];
        for (int i = 0; i < controls.length; i++) {
            controls[i] = ReadNetwork.SIGNAL;
        }
        List<Integer> tbrs = new ArrayList<>(max_tbrs);
        for(Street s: streets.values()) {
            for (NodeRecord nr: s.getLights().values()) {
                controls[intersections.get(nr.getId())] = nr.getType();
                if(nr.getType() == type && !tbrs.contains(intersections.get(nr.getId()))) {
                    tbrs.add(intersections.get(nr.getId()));
                }
                if(nr.getType() != type) {
                    tbrs.remove(intersections.get(nr.getId()));
                }
            }
        }

//        System.out.println(Arrays.toString(controls));
//        System.out.println(tbrs);
        org = new TBRIndividual(controls, tbrs, false, streets, intersections);
        System.out.println("Using Warm Start");
        System.out.println("Warm Proportion of Reservations: " + org.tbrRatio());
        System.out.println("Evaluating Warm");
        evaluate(org);
        return org;
    }
//    public boolean isFeasible(TBRIndividual org) {
//        int tbrs = 0;
//
//        for (int i : org.getControls()) {
//            if (i == type) {
//                tbrs++;
//            }
//        }
//        return tbrs == max_tbrs;
//    }
    @Override
    public boolean isNeighborBetter(TBRIndividual current, TBRIndividual neighbor) {
        return (neighbor.compareTo(current) < 0);
    }
}
