/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import java.util.Map;
import java.util.ArrayList;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.vehicle.route.Hyperpath;
import java.util.HashMap;

/**
 *
 * @author hdx
 */
public class ValueIteration{

//    private DUERSimulator coacongress2;
//    private NodeState State;
//    private ArrayList<NodeState> StateSpace;
//    private Map<Incident, Map<Link, Double>> avgTT = new HashMap<>(); // store average travel times per incident state
//    private Map<NodeState, Link> bestAction = new HashMap<>();
//    private double p; /*Incident happening probability*/
//    private double q; /*Perception probability of CAV*/
//    private int dest /*= 5551*/; /*ID of destination node*/
//   
//    public ValueIteration(DUERSimulator coacongress2, double p, double q, int dest/*coacongress2*/)
//    {
//        //this.coacongress2 = coacongress2;
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
//        for (Link l : coacongress2.getLinks()) {
//            avgTT.get(i).put(l, l.getLength());
//            avgTT.get(Incident.NULL).put(l, l.getLength());
//            switch (l.getId()){
//                case 13: avgTT.get(i).put(l, l.getLength() + 3.0);
//                break;
//                case 21: avgTT.get(i).put(l, l.getLength() + 3.0);
//                break;
//                case 25: avgTT.get(i).put(l, l.getLength() + 6.0);
//                break;
//                case 32: avgTT.get(i).put(l, l.getLength() + 6.0);
//                break;
//                case 48: avgTT.get(i).put(l, l.getLength() + 6.0);
//                break;
//                case 28: avgTT.get(i).put(l, l.getLength() + 9.0);
//                break;
//                case 41: avgTT.get(i).put(l, l.getLength() + 9.0);
//                break;
//                case 57: avgTT.get(i).put(l, l.getLength() + 9.0);
//                break;
//                case 46: avgTT.get(i).put(l, l.getLength() + 12.0);
//                break; 
//                
//            }
//        }
//        
////        Map<Integer, Link> links = coacongress2.createLinkIdsMap();
////        for (Link l : coacongress2.getLinks()) {
////            
////            avgTT.get(Incident.NULL).put(l, l.getFFTime());
////            avgTT.get(i).put(l, l.getFFTime());
////            switch (l.getId()){
////
////                case 5166: avgTT.get(i).put(l, l.getFFTime() + 4.0);
////                break;
////                case 5190: avgTT.get(i).put(l, l.getFFTime() + 8.0);
////                break;
////                case 5192: avgTT.get(i).put(l, l.getFFTime() + 6.0);
////                break;
////                case 5173: avgTT.get(i).put(l, l.getFFTime() + 6.0);
////                break;
////                case 14516: avgTT.get(i).put(l, l.getFFTime() + 8.0);
////                break;
////                case 5180: avgTT.get(i).put(l, l.getFFTime() + 8.0);
////                break; 
////                
////                case 105181: avgTT.get(i).put(l, l.getFFTime() + 2.0);
////                break;
////                case 114456: avgTT.get(i).put(l, l.getFFTime() + 4.0);
////                break;
////                case 105191: avgTT.get(i).put(l, l.getFFTime() + 4.0);
////                break;
////                case 14518: avgTT.get(i).put(l, l.getFFTime() + 6.0);
////                break;
////                
////            }
////        }
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
//            for(NodeState State : StateSpace) {
//                
//                if (State.node.getId() == dest) continue;
//		//System.out.println(State.node);
//                //System.out.println(State.inci);
//                //System.out.println(State.util);
//                
//                double utility = State.util;
//                double maxCurrentUtil = -1e30;
//		Link maxAction = null;
//		
//		// The following while loop computes \max_μ\sum P(s'|s,μ)*[-C(s'|s,μ)+V(s')]
//		for(Link action : State.node.getOutgoing()){
//                    
//                    //System.out.println(action);
//                    double nextUtil = 0;
//
//                    for (NodeState s_Prime: getNextState(State, action)){
//
//                        double prob = getProb(State, s_Prime);
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
//            //System.out.println("Iteration: " + numIterations + " error:" + String.format("%.1f", maxError));
//        }
//
//        //System.out.println("Incident Probability:"+ p +", Perception Probability:" + q + ",Destination:"+ dest);
//            double a = 0;
//            for (NodeState State : StateSpace) {
//                a += State.util;
//                //System.out.println("Node:" + State.node.getId() + ", Incident:" + State.inci.getId() + ", Cost:" + String.format("%.4f", State.util) + ", BestAction:" + bestAction.get(State));
//
//            }
//            System.out.println(a/46);
//        
//            return numIterations;
//    }
//
//    private ArrayList<NodeState> getStateSpace()
//    {
//        StateSpace = new ArrayList();
//        
//        for(Node node : coacongress2.getNodes()){
//
//            for(Incident incident : coacongress2.getIncidents()){
//                NodeState State = new NodeState(node, incident);
//                StateSpace.add(State);
//            }
//        }
//        return StateSpace;
//    }
//        
//    public ArrayList<NodeState> getNextState(NodeState State, Link action){
//        ArrayList<NodeState> nextState = new ArrayList();
//        
//        if (State.inci.getId()!=0){   //If the vehicle gets the incident information, then it will get this information from then on.
//            nextState.add(FindState(action.getDest(),State.inci));
//        }
//        else {
//            if (action.getDest().getId() == dest){  //If the next node is the destination.
//                nextState.add(FindState(action.getDest(),State.inci));
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
//    public NodeState FindState (Node node, Incident incident){
//        for (int i = 0; i < StateSpace.size(); i++) {
//            if (StateSpace.get(i).node == node && StateSpace.get(i).inci == incident) {
//                return StateSpace.get(i);
//            }
//        }
//        return null;
//    }
//    
//    public double getProb(NodeState State, NodeState nextstate){
//        
//        if (State.inci.getId() == 0){
//            if (nextstate.node.getId() == dest){ //If the next node is the destination.
//                return 1.0;
//            }
//            else if(nextstate.inci.getId() != 0){
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
//    public double getCost(NodeState State, Link link){
//        Incident i = coacongress2.createIncidentIdsMap().get(1);
//        if (State.inci.getId()!=0) {
//            return avgTT.get(State.inci).get(link);
//        }
//        else {
//            return avgTT.get(State.inci).get(link) * (1-p) + avgTT.get(i).get(link) * p;
//        }
//    }
//}
    

    private final DUERSimulator coacongress2;
    private NodeState State;
    private ArrayList<NodeState> StateSpace;
    private Map<Incident, Map<Link, Double>> avgTT = new HashMap<>(); // To store link travel times with or without incident
    //private Map<NodeState, Link> bestAction = new HashMap<>();
    private double p; /*Incident happening probability*/
    private double q; /*Perception probability of CAV*/
    private int dest = 5469; /*ID of destination node*/
    Hyperpath output = new Hyperpath();
        
    public ValueIteration(DUERSimulator coacongress2, double p, double q/*, int dest/*coacongress2*/)
    {
        //this.coacongress2 = coacongress2;
        this.coacongress2 = coacongress2;
        StateSpace = getStateSpace();
        this.p = p;
        this.q = q;
        //this.dest = dest;
        for (Incident i : coacongress2.getIncidents())
        {
            Map<Link, Double> innerMap = new HashMap<>();
            avgTT.put(i, innerMap);
        }
    }
    
    public void hardcode()
    {
        
        Incident i = coacongress2.createIncidentIdsMap().get(1);
        Map<Integer, Link> links = coacongress2.createLinkIdsMap();
        for (Link l : coacongress2.getLinks()) {
            
            avgTT.get(Incident.NULL).put(l, l.getFFTime());
            avgTT.get(i).put(l, l.getFFTime());
            switch (l.getId()){

                case 5166: avgTT.get(i).put(l, l.getFFTime() + 4.0);
                break;
                case 5190: avgTT.get(i).put(l, l.getFFTime() + 8.0);
                break;
                case 5192: avgTT.get(i).put(l, l.getFFTime() + 6.0);
                break;
                case 5173: avgTT.get(i).put(l, l.getFFTime() + 6.0);
                break;
                case 14516: avgTT.get(i).put(l, l.getFFTime() + 8.0);
                break;
                case 5180: avgTT.get(i).put(l, l.getFFTime() + 8.0);
                break; 
                
                case 105181: avgTT.get(i).put(l, l.getFFTime() + 2.0);
                break;
                case 114456: avgTT.get(i).put(l, l.getFFTime() + 4.0);
                break;
                case 105191: avgTT.get(i).put(l, l.getFFTime() + 4.0);
                break;
                case 14518: avgTT.get(i).put(l, l.getFFTime() + 6.0);
                break;
                
            }
        }
        
//        Incident i = coacongress2.createIncidentIdsMap().get(1);
//        Map<Integer, Link> links = coacongress2.createLinkIdsMap();
//
//        for (Link l : coacongress2.getLinks()) {
//            avgTT.get(i).put(l, l.getLength());
//            avgTT.get(Incident.NULL).put(l, l.getLength());
//            switch (l.getId()){
//                case 13: avgTT.get(i).put(l, l.getLength() + 3.0);
//                break;
//                case 21: avgTT.get(i).put(l, l.getLength() + 3.0);
//                break;
//                case 25: avgTT.get(i).put(l, l.getLength() + 6.0);
//                break;
//                case 32: avgTT.get(i).put(l, l.getLength() + 6.0);
//                break;
//                case 48: avgTT.get(i).put(l, l.getLength() + 6.0);
//                break;
//                case 28: avgTT.get(i).put(l, l.getLength() + 9.0);
//                break;
//                case 41: avgTT.get(i).put(l, l.getLength() + 9.0);
//                break;
//                case 57: avgTT.get(i).put(l, l.getLength() + 9.0);
//                break;
//                case 46: avgTT.get(i).put(l, l.getLength() + 12.0);
//                break;
//                
//            }
//        }

//        avgTT.get(i).put(links.get(1), 2.0);
//        avgTT.get(Incident.NULL).put(links.get(1), 1.0);
//        avgTT.get(i).put(links.get(2), 3.0);
//        avgTT.get(Incident.NULL).put(links.get(2), 1.0);
//        avgTT.get(i).put(links.get(3), 16.0);
//        avgTT.get(Incident.NULL).put(links.get(3), 4.0);
//        avgTT.get(i).put(links.get(4), 11.5);
//        avgTT.get(Incident.NULL).put(links.get(4), 11.5);
//        avgTT.get(i).put(links.get(5), 10.0);
//        avgTT.get(Incident.NULL).put(links.get(5), 10.0);
//        avgTT.get(i).put(links.get(6), 8.0);
//        avgTT.get(Incident.NULL).put(links.get(6), 8.0);
    }
   
    double epsilon = 1e-4;   /* The error threshold to stop the iteration */
    
    public int solve() {
              
        double threshold = epsilon;		
        boolean finished = false;
        int numIterations = 0;
        
        while(!finished) {
            double maxError = -1.;
            
            for(NodeState State : StateSpace) {

                if (State.node.getId() == dest) continue;
                                    
                double utility = State.util;
                double maxCurrentUtil = -1e30;
                Link maxAction = null;
                
                if (State.inci.getId() == 1 && State.info == false){
                    
                    Link action = FindState(State.node, coacongress2.createIncidentIdsMap().get(0), State.info).bestAction;
                    
                    double nextUtil = 0;
                    
                    for (NodeState s_prime: getNextState(State, action)){
                        double prob = getProb(State, s_prime);
                        double cost = getCost(State, action, s_prime);
                        //System.out.println(cost);
                        nextUtil += prob * (s_prime.util - cost);	
                    }
                        
                        maxCurrentUtil = nextUtil;
                        maxAction = action;
                }
                
                else {
                    // The following while loop computes \max_μ\sum P(s'|s,μ)*[-C(s'|s,μ)+V(s')]
                    for(Link action : State.node.getOutgoing()){

                        //System.out.println(action);
                        double nextUtil = 0;
                    
                        for (NodeState s_prime: getNextState(State, action)){

                            //System.out.println("Node:" + s_prime.node.getId() + ", Incident:" + s_prime.inci.getId() + ", Info:"+ s_prime.info +", Cost:" + String.format("%.4f", s_prime.util));
                            double prob = getProb(State, s_prime);
                            double cost = getCost(State, action, s_prime);
                            //System.out.println(cost);
                            nextUtil += prob * (s_prime.util - cost);

                        }

                        //System.out.println(nextUtil);
                        if(nextUtil >= maxCurrentUtil){
                            maxCurrentUtil = nextUtil;
                            maxAction = action;
                        }
                    }
                }
                
                //System.out.println(maxCurrentUtil);
                //System.out.println(maxAction +"- - - - - -");
                State.util = maxCurrentUtil;
                State.bestAction = maxAction;

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
        for (NodeState State : StateSpace) {
            if (State.inci.getId() !=0 & State.info == false) continue;
            else
            {
                a += State.util;
                //System.out.println("Node:" + State.node.getId() + ", Incident:" + State.inci.getId() + ", Info:"+ State.info +", Cost:" + String.format("%.4f", State.util) + ", BestAction:" + State.bestAction);            
            }
        }
        System.out.println(3*a/(2*StateSpace.size()-2*3));

        return numIterations;
        
    }
    
    
    public ArrayList<NodeState> getNextState(NodeState State, Link action){
        
        ArrayList<NodeState> nextStateSpace = new ArrayList<>();
        
        //if (State.node.getId() != dest){

            if (State.inci.getId()!=0){   //If there happens one type of incident in the network.
                
                if(State.info == true){
                    nextStateSpace.add(FindState(action.getDest(), State.inci, State.info));
                }
                else {
                    //System.out.println(State.node.getId() + ", Incident:" + State.inci.getId());
                    //System.out.println(action);
                    nextStateSpace.add(FindState(action.getDest(), State.inci, State.info));                         
                    nextStateSpace.add(FindState(action.getDest(), State.inci, true));
                }
            }
            else {  //If there is no incident in the network.
                for (Incident incident : coacongress2.getIncidents()){
                    if(incident.getId() == 0){
                        nextStateSpace.add(FindState(action.getDest(),incident, State.info));
                    }
                    else {
                        nextStateSpace.add(FindState(action.getDest(), incident, State.info));                    
                        nextStateSpace.add(FindState(action.getDest(), incident, true));
                    }
                }
            }
        //}
        return nextStateSpace;
    }
    
    public NodeState FindState(Node node, Incident incident, boolean info){
        for (int i = 0; i < StateSpace.size(); i++) {
            if (StateSpace.get(i).node == node && StateSpace.get(i).inci == incident && StateSpace.get(i).info == info) {
                return StateSpace.get(i);
            }
        }
        return null;
    }
    
    public double getProb(NodeState State, NodeState nextState){
        
        if (State.inci.getId() != 0){  //If there indeed happens one type of incident in the network
            if (State.info == true){ //If the vehicle perceives the information.
                return 1.0;
            }
            else if(nextState.info == State.info) //If the vehicle does not perceive the information at both current state and next state.
                return 1-q;
            else  //If the vehicle does not perceive the information at current state, but perceives information at next state.
                return q;
        }
        else { //If there is no incident in the network.
            if(nextState.inci.getId() != 0){ //No incident happens when the vehicle gets to the next state.
                if(nextState.info == true) return p*q;
                else return p*(1-q);
            }
            else return 1-p;
        }
    }
        
    public double getCost(NodeState State, Link action, NodeState nextState){
        
        if (State.inci.getId()!=0) {
            return avgTT.get(State.inci).get(action);
        }
        else {
            return avgTT.get(nextState.inci).get(action);
        }
    }
        
    public ArrayList<NodeState> getStateSpace(){
        
        ArrayList<NodeState> StateSpace = new ArrayList();
        
        for(Node node : coacongress2.getNodes()){
            
            for(Incident incident : coacongress2.getIncidents()){
                
                if(incident.getId()==0){
                    //Information info = new Information(false);
                    NodeState State = new NodeState(node, incident, false);
                    StateSpace.add(State);
                }
                else {
                    //Information info1 = new Information(false);
                    NodeState State1 = new NodeState(node, incident, false);
                    StateSpace.add(State1);
                    //Information info2 = new Information(true);
                    NodeState State2 = new NodeState(node, incident, true);
                    StateSpace.add(State2);
                }
            }

        }
        return StateSpace;
    }
    
//    public Hyperpath getHyperpath(){
//        Hyperpath hyperpath = new Hyperpath();
//        List<NodeState> stateSpace = getStateSpace();
//        
//        for(NodeState state:stateSpace){
//            hyperpath.setNextLink(state.node, new InfoClass(state.inci, state.info), state.bestAction);
//        }
//        return hyperpath;
//    }
}

