/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import avdta.dta.DTASimulator;
import avdta.network.ReadNetwork;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.network.node.NodeRecord;
import avdta.project.DTAProject;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class TBRGA extends GeneticAlgorithm<TBRIndividual> {
	private int type;

	// intersections is map of <nodeID, control>
	private Map<Integer, Integer> intersections;
	private DTAProject project;

	private int max_tbrs;
	private boolean checkHV;
	private boolean isSO;

	private Map<Integer, Set<Integer>> odpairs;

	public TBRGA(DTAProject project, int max_tbrs, boolean checkHV, boolean isSO, int population_size,
			double proportion_kept, double mutate_percent, List<Integer> signals) {
		super(population_size, proportion_kept, mutate_percent);

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

	public TBRIndividual createRandom() throws IOException {
		TBRIndividual org;

		if (!isSO) {

			do {
				int[] controls = new int[intersections.size()];
				List<Integer> tbrs = new ArrayList<>(max_tbrs);

				for (int i = 0; i < controls.length; i++) {
					controls[i] = ReadNetwork.SIGNAL;
				}

				for (int i = 0; i < max_tbrs; i++) {
					int loc = (int) (Math.random() * intersections.size());
					controls[loc] = type;
					tbrs.add(loc);
				}

				org = new TBRIndividual(controls, tbrs, false);
			} while (!isFeasible(org));
		} else {
			int[] controls = new int[intersections.size()];

			for (int i = 0; i < controls.length; i++) {
				controls[i] = Math.random() < 0.5 ? type : ReadNetwork.SIGNAL;
			}
			org = new TBRIndividual(controls);
		}
		return org;
	}

	public TBRIndividual cross(TBRIndividual parent1, TBRIndividual parent2) throws IOException {
		TBRIndividual child;

		if (!isSO) {
			do {
				if (parent1.getObj() < parent2.getObj()) {
					child = parent1.cross(parent2);
				} else {
					child = parent2.cross(parent1);
				}
			} while (!isFeasible(child));
		} else {
			if (parent1.getObj() < parent2.getObj()) {
				child = parent1.cross(parent2);
			} else {
				child = parent2.cross(parent1);
			}
		}
		return child;
	}

	public void mutate(TBRIndividual org) throws IOException {
		int[] newcontrols = org.getControls();
		List<Integer> tbrs = org.getTbrs();
		if (isSO) {
			for (int i = 0; i < newcontrols.length; i++) {
				if (Math.random() <= 0.07) {

					if (newcontrols[i] == ReadNetwork.SIGNAL) {
						newcontrols[i] = type;
					} else
						newcontrols[i] = ReadNetwork.SIGNAL;
				}
			}
		}
		else{
			
			for (int i = 0; i<max_tbrs; i++) {
				if (Math.random() <= 0.07) {
					int number = 0;	
					
					do{
						number = (int) (Math.random()*intersections.size());
					}while(tbrs.contains(number));
					
					newcontrols[tbrs.get(i)] = ReadNetwork.SIGNAL;
					newcontrols[number] = type;
					tbrs.remove(i);
					tbrs.add(number);
				}
			}
		}
	}

	public boolean isFeasible(TBRIndividual org) throws IOException {
		int tbrs = 0;

		for (int i : org.getControls()) {
			if (i == type) {
				tbrs++;
			}
		}

		if (tbrs > max_tbrs) {
			return false;
		}

		if (checkHV) {
			changeNodes(org);

			project.loadSimulator();
			DTASimulator sim = project.getSimulator();

			Map<Integer, Node> nodesmap = sim.createNodeIdsMap();

			for (int r : odpairs.keySet()) {
				sim.dijkstras(nodesmap.get(r), 0, 1.0, DriverType.HV, TravelCost.ffTime);

				for (int s : odpairs.get(r)) {
					Node d = nodesmap.get(s);

					if (d.prev == null) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public void evaluate(TBRIndividual child) throws IOException {
		changeNodes(child);

		project.loadSimulator();
		DTASimulator sim = project.getSimulator();

		// solve DTA
		sim.msa(30, 2.0);

		child.setAssignment(sim.getAssignment());
		child.setObj(sim.getTSTT() / 3600.0);
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

	@Override
	public void print(TBRIndividual best, int iteration, int nummutations) throws FileNotFoundException {

		int counttbr = 0;
		int countsig = 0;
		PrintStream fileout = new PrintStream(new FileOutputStream(new File("GA_RESULTS_35TBRs"), true), true);
		fileout.println("Iteration " + iteration);
		fileout.println("TSTT\t" + best.getObj() + "\tNumber of mutations\t" + nummutations);
		for (int node : intersections.keySet()) {
			if (best.getControl(intersections.get(node)) == 100)
				countsig++;
			else
				counttbr++;
		}
		fileout.println("Num of Signals: " + countsig + "\tNum of Reservations: " + counttbr);
		fileout.println("Prop of Signals: " + (double) countsig / (countsig + counttbr) + "\tProp of Reservations: "
				+ (double) counttbr / (countsig + counttbr));
		fileout.println("Node\tControl");
		for (int node : intersections.keySet()) {
			fileout.println(node + "\t" + best.getControl(intersections.get(node)));
		}
		fileout.println();
		fileout.close();
	}

	public void observeInitial(List<TBRIndividual> population) throws FileNotFoundException {
		PrintStream fileout = new PrintStream(new FileOutputStream(new File("GA_INITIALPOP_RESULTS.txt"), true), true);
		fileout.println("No.\tSignalCount\tTbrCount\tPropOfSig\tPropOfTbr\tTSTT");
		for (int i = 0; i < population.size(); i++) {
			int countSig = 0;
			int countTbr = 0;
			for (int cont : population.get(i).getControls()) {
				if (cont == 100)
					countSig++;
				else
					countTbr++;
			}
			fileout.println(i + "\t" + countSig + "\t" + countTbr + "\t" + (double) (countSig) / (countSig + countTbr)
					+ "\t" + (double) (countTbr) / (countSig + countTbr) + "\t" + population.get(i).getObj());
		}
		fileout.close();
	}
}
