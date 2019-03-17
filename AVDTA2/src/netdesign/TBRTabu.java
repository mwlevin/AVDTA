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
    public TBRTabu(DTAProject project, boolean isSO, List<Integer> signals, int max_itr ) {
        super(max_itr, 0);
        this.project = project;
        this.isSO = isSO;

        intersections = new HashMap<>();

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
            child.setObj(sim.getTSTT() / 3600.0);
        } catch (IOException e) {
            child.setObj(Double.MAX_VALUE);
        }
//                child.setObj(Math.random() * 6000.0);
    }

    @Override
    public SortedSet<TBRIndividual> generateNeighbor(TBRIndividual currentState) {
        SortedSet<TBRIndividual> output = new TreeSet<>();
        System.out.println("Current Best State: " + bestSolution.getObj());
        System.out.println("Current State: " + currentState.getObj());
        for(int i = 0; i < 5; i ++) {
            TBRIndividual perturbed = currentState.createNeighbor();
//            System.out.println(perturbed.getStreets());
//            System.out.println(perturbed.getTbrs());
            System.out.println("Evaluating Neighbor #" + i);
            evaluate(perturbed);
            System.out.println("Current TSTT:" + currentState.getObj() + ", Neighbor TSTT:" + perturbed.getObj());
            output.add(perturbed);
        }
        System.out.println("Generated and Evaluated Neighbors");
        return output;
    }

    @Override
    public TBRIndividual generateRandom() {
        TBRIndividual org;
        if (!isSO) {
            int[] controls = new int[intersections.size()];
            for (int i = 0; i < controls.length; i++) {
                controls[i] = ReadNetwork.SIGNAL;
            }
            List<Integer> tbrs = new ArrayList<>(max_tbrs);
            for(Street s: streets.values()) {
                if (Math.random() < 0.5) {
                    s.flipAllIntersections();
                }
                for (NodeRecord nr: s.getLights().values()) {
                    controls[intersections.get(nr.getId())] = nr.getType();
                    if(nr.getType() == type)
                        tbrs.add(intersections.get(nr.getId()));
                    else {
                        tbrs.remove(intersections.get(nr.getId()));
                    }
                }
            }
//            System.out.println(streets);
//            System.out.println(tbrs);
            org = new TBRIndividual(controls, tbrs, false, streets, intersections);
            System.out.println("Generated Random");
            System.out.println("Evaluating Random");
		} else {
			int[] controls = new int[intersections.size()];

			for (int i = 0; i < controls.length; i++) {
				controls[i] = Math.random() < 0.5 ? type : ReadNetwork.SIGNAL;
			}
			org = new TBRIndividual(controls, streets, intersections);
		}
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
