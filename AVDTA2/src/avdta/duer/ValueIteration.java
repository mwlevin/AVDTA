/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import avdta.network.link.Link;
import avdta.network.node.Node;
import java.util.HashMap;

/**
 *
 * @author hdx
 */
public class ValueIteration{

//    private DUERSimulator coacongress2;
//    private State State;
//    private ArrayList<State> StateSpace;
//    private Map<Incident, Map<Link, Double>> avgTT = new HashMap<>(); // store average travel times per incident state
//    private Map<State, Link> bestAction = new HashMap<>();
//    private double p; /*Incident happening probability*/
//    private double q; /*Perception probability of CAV*/
//    private int dest; /*ID of destination node*/
//    
//    //private Set<Node> nodes = freeway.getNodes();
//    //private Set<Incident> incidents = freeway.getIncidents();
//    
//    public ValueIteration(DUERSimulator coacongress2, double p, double q, int dest/*freeway*/)
//    {
//        //this.freeway = freeway;
//        this.coacongress2 = coacongress2;
//        StateSpace = getStateSpace();
//        this.p = p;
//        this.q = q;
//        this.dest = dest;
//        for (Incident i : coacongress2.getIncidents())
//        {
//            Map<Link, Double> innerMap = new HashMap<>();
//            //innerMap = new HashMap<>();
//            avgTT.put(i, innerMap);
//        }
//    }
//    
//    public void hardcode()
//    {
//        Incident i = coacongress2.createIncidentIdsMap().get(1);
//        //Map<Integer, Link> links = coacongress2.createLinkIdsMap();
//        for (Link l : coacongress2.getLinks()) {
//            
//            avgTT.get(Incident.NULL).put(l, l.getFFTime());
//            avgTT.get(i).put(l, l.getFFTime());
//            switch (l.getId()){
//
//                case 5166: avgTT.get(i).put(l, l.getFFTime() + 4.0);
//                break;
//                case 5190: avgTT.get(i).put(l, l.getFFTime() + 8.0);
//                break;
//                case 5192: avgTT.get(i).put(l, l.getFFTime() + 6.0);
//                break;
//                case 5173: avgTT.get(i).put(l, l.getFFTime() + 6.0);
//                break;
//                case 14516: avgTT.get(i).put(l, l.getFFTime() + 8.0);
//                break;
//                case 5180: avgTT.get(i).put(l, l.getFFTime() + 8.0);
//                break; 
//                
//                case 105181: avgTT.get(i).put(l, l.getFFTime() + 2.0);
//                break;
//                case 114456: avgTT.get(i).put(l, l.getFFTime() + 4.0);
//                break;
//                case 105191: avgTT.get(i).put(l, l.getFFTime() + 4.0);
//                break;
//                case 14518: avgTT.get(i).put(l, l.getFFTime() + 6.0);
//                break;
//                
//            }
//        }
////        avgTT.get(i).put(links.get(1), 1.0);
////        avgTT.get(Incident.NULL).put(links.get(1), 1.0);
////        avgTT.get(i).put(links.get(2), 2.0);
////        avgTT.get(Incident.NULL).put(links.get(2), 1.0);
////        avgTT.get(i).put(links.get(3), 3.0);
////        avgTT.get(Incident.NULL).put(links.get(3), 1.0);
////        avgTT.get(i).put(links.get(4), 16.0);
////        avgTT.get(Incident.NULL).put(links.get(4), 4.0);
////        avgTT.get(i).put(links.get(5), 11.5);
////        avgTT.get(Incident.NULL).put(links.get(5), 11.5);
////        avgTT.get(i).put(links.get(6), 10.0);
////        avgTT.get(Incident.NULL).put(links.get(6), 10.0);
////        avgTT.get(i).put(links.get(7), 8.0);
////        avgTT.get(Incident.NULL).put(links.get(7), 8.0);
//    }
//    
//    double epsilon = 1e-4;   /* The error threshold to stop the iteration */
//
//    public int solve() {
//	
//	double threshold = epsilon;
//	boolean finished = false;
//	int numIterations = 0;
//        
//	while(!finished) {
//            double maxError = -1.;
//            
//            for(State State : StateSpace) {
//                
//                if (State.first.getId() == dest) continue;
//		//System.out.println(State.first);
//                //System.out.println(State.second);
//                //System.out.println(State.util);
//                
//                double utility = State.util;
//                double maxCurrentUtil = -1e30;
//		Link maxAction = null;
//		
//		// The following while loop computes \max_¦Ì\sum P(s'|s,¦Ì)*[-C(s'|s,¦Ì)+V(s')]
//		for(Link action : State.first.getOutgoing()){
//                    
//                    //System.out.println(action);
//                    double nextUtil = 0;
//
//                    for (State s_Prime: getNextState(State, action)){
//
//                        double prob = getProb(State, action, s_Prime);
//                        double cost = getCost(State, action);
//                        //System.out.println(s_Prime.util);
//                        nextUtil += prob * (s_Prime.util - cost);
//                        
//                    }
//
//                    if(nextUtil > maxCurrentUtil){
//                        maxCurrentUtil = nextUtil;
//                        maxAction = action;
//                    }
//		}
//		
//                //System.out.println(maxCurrentUtil);
//                //System.out.println(maxAction +"- - - - - -");
//                State.util = maxCurrentUtil;
//                bestAction.put(State, maxAction);
//		//setUtility(State, minCurrentUtil);
//		//setAction(State, maxAction);
//	
//		double currentError = Math.abs(maxCurrentUtil-utility);
//		if(currentError > maxError)
//			maxError = currentError;
//            }
//            
//            numIterations ++;
//            if(maxError < threshold)
//                {
//                    finished = true;
//                }
//            
//            System.out.println("Iteration: " + numIterations + " error:" + String.format("%.1f", maxError));
//        }
//
//            System.out.println("Incident Probability:"+ p +", Perception Probability:" + q);
//            for (State State : StateSpace) {
//                System.out.println("Node:" + State.first.getId() + ", Incident:" + State.second.getId() + ", Cost:" + String.format("%.4f", State.util) + ", BestAction:" + bestAction.get(State));
//            }
//
//            return numIterations;
//    }
//
//    private ArrayList<State> getStateSpace()
//    {
//        StateSpace = new ArrayList();
//        
//        for(Node node : coacongress2.getNodes()){
////            if (node.getId() == 1) {
////                State State = new State(node, Incident.NULL);
////                StateSpace.add(State);
////            }
//            for(Incident incident : coacongress2.getIncidents()){
//                State State = new State(node, incident);
//                StateSpace.add(State);
//            }
//        }
//        return StateSpace;
//    }
//
//    public Set<Link> getActionSpace(State State, Link action){
//        //Node node = State.first;
//        //Set<Link> action = node.getOutgoing();
//        //return action;
//        return State.first.getOutgoing();
//    }
//        
//    public ArrayList<State> getNextState(State State, Link action){
//        ArrayList<State> nextState = new ArrayList();
//        
//        if (State.second.getId()!=0){   //If the vehicle gets the incident information, then it will get this information from then on.
//            nextState.add(FindState(action.getDest(),State.second));
//        }
//        else {
//            if (action.getDest().getId() == 5469){  //If the next node is the destination.
//                nextState.add(FindState(action.getDest(),State.second));
//            }
//            else {
//                for (Incident incident : coacongress2.getIncidents()){
//                    nextState.add(FindState(action.getDest(),incident));
//                }
//            }
//        }
//        return nextState;
//    }
//    
//    public State FindState (Node node, Incident incident){
//        for (int i = 0; i < StateSpace.size(); i++) {
//            if (StateSpace.get(i).first == node && StateSpace.get(i).second == incident) {
//                return StateSpace.get(i);
//            }
//        }
//        return null;
//    }
//    
//    public double getProb(State State, Link link, State nextstate){
//        
//        if (State.second.getId() == 0){
//            if (nextstate.first.getId() == dest){ //If the next node is the destination.
//                return 1.0;
//            }
//            else if(nextstate.second.getId() != 0){
//                return p*q;
//            }
//            else{
//                return 1.0 - p*q;
//            }
//        }
//        else{
//            return 1.0;
//        }
//        
//    }
//        
//    public double getCost(State State, Link link){
//        Incident i = coacongress2.createIncidentIdsMap().get(1);
//        if (State.second.getId()!=0) {
//            return avgTT.get(State.second).get(link);
//        }
//        else {
//            return avgTT.get(State.second).get(link) * (1-p) + avgTT.get(i).get(link) * p;
//        }
//    }
    
    //private DUERSimulator freeway;
    private final DUERSimulator SiouxFalls;
    private State State;
    private ArrayList<State> StateSpace;
    private Map<Incident, Map<Link, Double>> avgTT = new HashMap<>(); // store average travel times per incident state
    private Map<State, Link> bestAction = new HashMap<>();
    private double p; /*Incident happening probability*/
    private double q; /*Perception probability of CAV*/
    private int dest; /*ID of destination node*/
    
    //private Set<Node> nodes = freeway.getNodes();
    //private Set<Incident> incidents = freeway.getIncidents();
    
    public ValueIteration(DUERSimulator SiouxFalls, double p, double q, int dest/*freeway*/)
    {
        //this.freeway = freeway;
        this.SiouxFalls = SiouxFalls;
        StateSpace = getStateSpace();
        this.p = p;
        this.q = q;
        this.dest = dest;
        for (Incident i : SiouxFalls.getIncidents())
        {
            Map<Link, Double> innerMap = new HashMap<>();
            //innerMap = new HashMap<>();
            avgTT.put(i, innerMap);
        }
    }
    
    public void hardcode()
    {
        //Incident i = freeway.createIncidentIdsMap().get(1);
        //Map<Integer, Link> links = freeway.createLinkIdsMap();
        Incident i = SiouxFalls.createIncidentIdsMap().get(1);
        //Map<Integer, Link> links = SiouxFalls.createLinkIdsMap();
        for (Link l : SiouxFalls.getLinks()) {
            avgTT.get(i).put(l, l.getLength());
            avgTT.get(Incident.UNKNOWN).put(l, l.getLength());
            switch (l.getId()){
                case 13: avgTT.get(i).put(l, l.getLength() + 3.0);
                break;
                case 21: avgTT.get(i).put(l, l.getLength() + 3.0);
                break;
                case 25: avgTT.get(i).put(l, l.getLength() + 6.0);
                break;
                case 32: avgTT.get(i).put(l, l.getLength() + 6.0);
                break;
                case 48: avgTT.get(i).put(l, l.getLength() + 6.0);
                break;
                case 28: avgTT.get(i).put(l, l.getLength() + 9.0);
                break;
                case 41: avgTT.get(i).put(l, l.getLength() + 9.0);
                break;
                case 57: avgTT.get(i).put(l, l.getLength() + 9.0);
                break;
                case 46: avgTT.get(i).put(l, l.getLength() + 12.0);
                break; 
                
            }
        }
//        avgTT.get(i).put(links.get(1), 1.0);
//        avgTT.get(Incident.NULL).put(links.get(1), 1.0);
//        avgTT.get(i).put(links.get(2), 2.0);
//        avgTT.get(Incident.NULL).put(links.get(2), 1.0);
//        avgTT.get(i).put(links.get(3), 3.0);
//        avgTT.get(Incident.NULL).put(links.get(3), 1.0);
//        avgTT.get(i).put(links.get(4), 16.0);
//        avgTT.get(Incident.NULL).put(links.get(4), 4.0);
//        avgTT.get(i).put(links.get(5), 11.5);
//        avgTT.get(Incident.NULL).put(links.get(5), 11.5);
//        avgTT.get(i).put(links.get(6), 10.0);
//        avgTT.get(Incident.NULL).put(links.get(6), 10.0);
//        avgTT.get(i).put(links.get(7), 8.0);
//        avgTT.get(Incident.NULL).put(links.get(7), 8.0);
    }
    
    
    double epsilon = 1e-4;   /* The error threshold to stop the iteration */
    //double p = 0.1*i;  /*The probability that the incident will happen */ 
    //double q = 0.1*j;  /*The probability that the vehicle will get incident information from the system */
    //int dest = 22;
    
    public int solve() {
              
        double threshold = epsilon;		
        boolean finished = false;
        int numIterations = 0;

        while(!finished) {
            double maxError = -1.;

            for(State State : StateSpace) {

                if (State.first.getId() == dest) continue;

                double utility = State.util;
                double maxCurrentUtil = -1e30;
                Link maxAction = null;

                // The following while loop computes \max_¦Ì\sum P(s'|s,¦Ì)*[-C(s'|s,¦Ì)+V(s')]
                for(Link action : State.first.getOutgoing()){

                    //System.out.println(action);
                    double nextUtil = 0;

                    for (State s_Prime: getNextState(State, action)){

                        double prob = getProb(State, s_Prime);
                        double cost = getCost(State, action);
                        //System.out.println(s_Prime.util);
                        nextUtil += prob * (s_Prime.util - cost);

                    }

                    if(nextUtil > maxCurrentUtil){
                        maxCurrentUtil = nextUtil;
                        maxAction = action;
                    }
                }

                //System.out.println(maxCurrentUtil);
                //System.out.println(maxAction +"- - - - - -");
                State.util = maxCurrentUtil;
                //setUtility(State, minCurrentUtil);
                bestAction.put(State, maxAction);
                //setAction(State, maxAction);

                double currentError = Math.abs(maxCurrentUtil-utility);
                if(currentError > maxError)
                        maxError = currentError;
            }

            numIterations ++;
            if(maxError < threshold)
            {
                finished = true;
            }
            //System.out.println("Iteration: " + numIterations + " error:" + String.format("%.1f", maxError));
        }

        //System.out.println("Incident Probability:"+ p +", Perception Probability:" + q + ",Destination:"+ dest);
        double a = 0;
        for (State State : StateSpace) {
            a += State.util;
            //System.out.println("Node:" + State.first.getId() + ", Incident:" + State.second.getId() + ", Cost:" + String.format("%.4f", State.util) + ", BestAction:" + bestAction.get(State));
            
        }
        System.out.println(a/46); 

        return numIterations;
        
    }
    
    
    private ArrayList<State> getNextState(State State, Link action){
        ArrayList<State> nextState = new ArrayList();
        
        if (State.second.getId()!=0){   //If the vehicle gets the incident information, then it will get this information from then on.
            nextState.add(FindState(action.getDest(),State.second));
        }
        else {
            if (action.getDest().getId() == dest){  //If the next node is the destination.
                nextState.add(FindState(action.getDest(),State.second));
            }
            else {
                for (Incident incident : SiouxFalls.getIncidents()){
                    nextState.add(FindState(action.getDest(),incident));
                }
            }
        }
        return nextState;
    }
    
    private State FindState (Node node, Incident incident){
        for (int i = 0; i < StateSpace.size(); i++) {
            if (StateSpace.get(i).first == node && StateSpace.get(i).second == incident) {
                return StateSpace.get(i);
            }
        }
        return null;
    }
    
    private double getProb(State State, State nextstate){
        
        if (State.second.getId() == 0){
            if (nextstate.first.getId() == dest){ //If the next node is the destination.
                return 1.0;
            }
            else if(nextstate.second.getId() != 0){
                return p*q;
            }
            else{
                return 1.0 - p*q;
            }
        }
        else{
            return 1.0;
        }
        
    }
        
    private double getCost(State State, Link link){
        Incident i = SiouxFalls.createIncidentIdsMap().get(1);
        if (State.second.getId()!=0) {
            return avgTT.get(State.second).get(link);
        }
        else {
            return avgTT.get(State.second).get(link)*(1-p) + avgTT.get(i).get(link)*p;
        }
    }
        
    private ArrayList<State> getStateSpace(){
            
        StateSpace = new ArrayList();

        for(Node node : SiouxFalls.getNodes()){

            for(Incident incident : SiouxFalls.getIncidents()){
                State State = new State(node, incident);
                StateSpace.add(State);
            }
        }
        return StateSpace;
    }
        
    private class State {

        private Node first;
        private Incident second;
        public double util;

        private State(Node first, Incident second) {
            this.first = first;
            this.second = second;
        }
    }  

}