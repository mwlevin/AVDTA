/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.dta.Assignment;
import avdta.network.ReadNetwork;
import avdta.network.node.Intersection;
import avdta.network.node.NodeRecord;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author micha
 */
public class TBRIndividual extends Individual<TBRIndividual> {
	private int[] controls;
	private int hash;
	private List<Integer> tbrs;
	private boolean isSO;
	private Map<String, Street> streets;
	private Map<Integer, Integer> intersections;

	public TBRIndividual(int num_inter, int fillType) {
		controls = new int[num_inter];

		for (int i = 0; i < controls.length; i++) {
			controls[i] = fillType;
		}
		computeHash();
	}

	public float tbrRatio() {
		return (float) tbrs.size() / controls.length;
	}
	public TBRIndividual(int[] controls) {
		this.controls = controls;
		computeHash();
		streets = new HashMap<>();
	}

	public TBRIndividual(int[] controls, Map<String, Street> str , Map<Integer, Integer> ints) {
		this.controls = controls;
		computeHash();
		streets = str;
		intersections = ints;
	}

	public TBRIndividual(int[] controls, List<Integer> tbrs, boolean isSO) {
		// TODO Auto-generated constructor stub
		this.controls = controls;
		computeHash();
		this.tbrs = tbrs;
		this.isSO = isSO;
	}

	public TBRIndividual(int[] controls, List<Integer> tbrs, boolean isSO, Map<String, Street> st, Map<Integer, Integer> ints) {
		// TODO Auto-generated constructor stub
		this.controls = controls;
		computeHash();
		this.tbrs = tbrs;
		this.isSO = isSO;
		streets = st;
		intersections = ints;
	}

	public void setAssignment(Assignment assign) {
		super.setAssignment(assign);
	}

	protected void computeHash() {
		hash = 0;

		for (int i = 0; i < controls.length; i++) {
			hash += (100 * controls[i]) / (i + 1);
		}
	}

	public TBRIndividual cross(TBRIndividual rhs) {
		int[] newControls = new int[controls.length];
		List<Integer> childTbrs = new ArrayList<Integer>();
		double diff = rhs.getObj() - this.getObj();
		double prop = diff / 2500.0;

		if (isSO) {
			for (int i = 0; i < newControls.length; i++) {
				if (Math.random() < 0.5 * prop + 0.5) {
					newControls[i] = controls[i];
				} else {
					newControls[i] = rhs.controls[i];
				}
			}
			return new TBRIndividual(newControls);
		} else {
			List<Integer> parent1Tbr = new ArrayList<>();
			List<Integer> parent2Tbr = new ArrayList<>();
			
			for(int i :tbrs){
				if(rhs.tbrs.contains(i)){
					
					continue;
				}
				parent1Tbr.add(i);
			}
			
			for(int i : rhs.tbrs){
				if(tbrs.contains(i)) continue;
				parent2Tbr.add(i);
			}
			
			for(int i = 0; i < newControls.length; i++){
				if(controls[i] == rhs.controls[i]){
					newControls[i] = controls[i];
					if(controls[i] == ReadNetwork.RESERVATION + ReadNetwork.FCFS){
						childTbrs.add(i);
					}
				}
				else{
					newControls[i] = ReadNetwork.SIGNAL;
				}
			}
			int toChange = parent1Tbr.size();
			List<Integer> addedControls = new ArrayList<>();
			while(addedControls.size() < toChange){
				int p1 = (int) ((parent1Tbr.size() - 1)*Math.random());
				int p2 = (int) ((parent2Tbr.size() - 1)*Math.random());
				int checkP1 = parent1Tbr.get(p1);
				int checkP2 = parent2Tbr.get(p2);
				
				if(addedControls.contains(checkP1) || addedControls.contains(checkP2)){
					continue;
				}
				
				if (Math.random() < 0.5 * prop + 0.5) {
					newControls[checkP1] = this.controls[checkP1];
					addedControls.add(checkP1);
                                        parent1Tbr.remove(p1);
				} 
				else {
					newControls[checkP2] = rhs.controls[checkP2];
					addedControls.add(checkP2);
                                        parent2Tbr.remove(p2);
				}
			}
			for(int c : addedControls){
				childTbrs.add(c);
			}
			return new TBRIndividual(newControls, childTbrs, false);
		}
	}

	public boolean equals(TBRIndividual rhs) {
		if (rhs.controls.length != controls.length) {
			return false;
		}

		for (int i = 0; i < controls.length; i++) {
			if (controls[i] != rhs.controls[i]) {
				return false;
			}
		}

		return true;
	}

	public TBRIndividual createNeighbor(int radius) {
		// Generate neighbor from current state
		int[] newControls = new int[controls.length];
		System.arraycopy(controls, 0, newControls, 0, controls.length);
		List<Integer> neighborTBRs = new ArrayList<>(tbrs);
		Map<String, Street> neighborStreets = new HashMap<>();
		for (String s: streets.keySet()) {
			Street str = streets.get(s);
			Street strCopy = new Street(s, str.getControl());
			if (!str.isContiguous())
				str.allowInterUpdates();
			for(NodeRecord nr: str.getLights().values()) {
				strCopy.addNode(new NodeRecord(nr.getId(), nr.getType(), nr.getLongitude(), nr.getLatitude(), 0.0));
			}
			neighborStreets.put(s, strCopy);
		}
		if (!isSO) {
			for (int i = 0; i < radius; i ++){
				String randomStreet = "";
				int size = streets.size();
				int item = new Random().nextInt(size);
				int idx = 0;
				for(String obj : streets.keySet()) {
					if (idx == item) {
						randomStreet = obj;
						break;
					}
					idx++;
				}
				Street rand = neighborStreets.get(randomStreet);
				rand.flipIntersections();
				for (NodeRecord nr: rand.getLights().values()) {
					newControls[intersections.get(nr.getId())] = nr.getType();
					if(nr.getType() == ReadNetwork.RESERVATION + ReadNetwork.FCFS)
						neighborTBRs.add(intersections.get(nr.getId()));
					else {
						neighborTBRs.remove(intersections.get(nr.getId()));
					}
				}
			}
//			System.out.println(streets.equals(neighborStreets));
			return new TBRIndividual(newControls, neighborTBRs, false, neighborStreets, intersections);
		}
		return null;
	}

	public int hashCode() {
		return hash;
	}

	public int getControl(int idx) {
		return controls[idx];
	}

	public int[] getControls() {
		return controls;
	}
        
	public void setControls(int[] controls) {
            this.controls = controls;
        }

	protected void writeToFile(PrintStream out) {
		super.writeToFile(out);

		out.println(streets);
		out.println(tbrs);
		for (int i : intersections.keySet()) {
			out.println(i + " " + controls[intersections.get(i)]);
		}
	}

	protected void readFromFile(Scanner in) throws IOException {
		super.readFromFile(in);

		for (int i = 0; i < controls.length; i++) {
			controls[i] = in.nextInt();
		}
		computeHash();
	}

	public List<Integer> getTbrs() {
		return tbrs;
	}

	public void setTbrs(List<Integer> tbrs) {
		this.tbrs = tbrs;
	}
	public Map<String, Street> getStreets() {
		return streets;
	}
}
