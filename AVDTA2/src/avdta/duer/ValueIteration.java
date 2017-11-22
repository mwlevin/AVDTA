/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import java.util.Map;
import java.util.Vector;

/**
 *
 * @author hdx
 */
public class ValueIteration {
    
    private MarkovDecisionProcess mdp;
    //private Action action;
    private Map<Node, Incident> state;
    private Map<Incident, Map<Link, Double>> LinkCost;
    
    double epsilon = 1e-4;   /* The error threshold to stop the iteration */
	
    int numIterations;

    private ValueIteration(MarkovDecisionProcess mdp){
	this.mdp = mdp;
    }
	
    public void setError(double epsilon){
	this.epsilon = epsilon;
    }
	
    public int getNumberIterations() {
	return numIterations;
    }
	
    public int solve() {
		
	double threshold = epsilon;
		
	boolean finished = false;
		
	numIterations = 0;

	while(!finished) {
            double maxError = -1.;
            
            for(State state=mdp.getStartState();state!=null;state=mdp.getNextState()) {
		
		double utility = mdp.getUtility(state);
		double maxCurrentUtil = -1e30;
		Action maxAction = null;
		
		// The following while loop computes \max_a\sum P(s'|s,μ)*[-C(s'|s,μ)+V(s')]
		for(Action action=mdp.getStartAction(); action!=null; action=mdp.getNextAction()){
			Vector T = mdp.getTransition(state, action);
			int k = T.size();
			double nextUtil = 0;
			for(int i=0; i<k; ++i) {
				Transition t=(Transition)T.get(i);
				double prob=t.probability;
				State s_Prime=t.nextState;
                                double cost=t.getCost(s_Prime,state,action); 
				nextUtil += prob * (mdp.getUtility(s_Prime) + cost);
			}
			if(nextUtil > maxCurrentUtil){
				maxCurrentUtil = nextUtil;
				maxAction = action;
			}
		}
				
		mdp.setUtility(state, maxCurrentUtil);
		mdp.setAction(state, maxAction);
				
		double currentError=Math.abs(maxCurrentUtil-utility);
		if(currentError>maxError)
			maxError = currentError;
            }
            
            numIterations ++;		
            if(maxError < threshold)	
                finished = true;
		//System.out.println("Iteration: "+numIterations + " error:"+maxError);
	}
		
	return numIterations;
    }

    private static class MarkovDecisionProcess {

        public MarkovDecisionProcess() {
        }

        private State getStartState() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private State getNextState() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private double getUtility(State state) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private double getReward(State state) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void setUtility(State state, double maxCurrentUtil) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void setAction(State state, Action maxAction) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private Action getStartAction() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private Action getNextAction() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private Vector getTransition(State state, Action action) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private double getCost(State state) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static class State {

        public State() {
        }
    }

    private static class Node {

        public Node() {
        }
    }

    private static class Link {

        public Link() {
        }
    }

    private static class Action {

        public Action() {
        }
    }

    private static class Transition {

        private double probability;
        private State nextState;

        public Transition() {
        }

        private double getCost(State sPrime, State state, Action action) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
