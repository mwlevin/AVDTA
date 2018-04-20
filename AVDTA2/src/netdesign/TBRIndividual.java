/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.dta.Assignment;
import avdta.network.ReadNetwork;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class TBRIndividual extends Individual<TBRIndividual> {
	private int[] controls;
	private int hash;
	private List<Integer> tbrs;
	private boolean isSO;

	public TBRIndividual(int num_inter, int fillType) {
		controls = new int[num_inter];

		for (int i = 0; i < controls.length; i++) {
			controls[i] = fillType;
		}
		computeHash();
	}

	public TBRIndividual(int[] controls) {
		this.controls = controls;
		computeHash();
	}

	public TBRIndividual(int[] controls, List<Integer> tbrs, boolean isSO) {
		// TODO Auto-generated constructor stub
		this.controls = controls;
		computeHash();
		this.tbrs = tbrs;
		this.isSO = isSO;
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
			List<Integer> parent1Tbr = new ArrayList<Integer>();
			List<Integer> parent2Tbr = new ArrayList<Integer>();
			
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
			List<Integer> addedControls = new ArrayList<Integer>();
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

		for (int i : controls) {
			out.println(i);
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
}
