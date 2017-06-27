/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.tabusearch;

import avdta.network.node.Node;
import avdta.sav.AssignedTaxi;
import java.util.List;

/**
 *
 * @author prashanthvenkatraman
 */
public class Search {

    public List<Node> W; //a nonempty subset of V \ { vo } containing vertices that are allowed to be moved from their current route;
    public int q; //number of vertices of W that are candidate for reinsertion into another route;
    public int p1; // the route in which vertex v is reinserted must contain at least one of its p1 nearest neighbors;
    public int p2; //neighborhood size used in GENI;
    public int tabuMin; //Omin, Omax: bounds on the number of iterations for which a move is declared tabu;
    public int tabuMax;
    public double g; // a scaling factor used to define an artificial objective function value;
    public int h; // the frequency at which updates of alpha and beta are considered;
    public int nmax; // maximum number of iterations during which the last step of the procedure is allowed to run without any improvement in the objective function. 
    public AssignedTaxi taxi;
    
    public Search(List<Node> W, int q, int p1, int p2, int tabuMin, int tabuMax, double g, int h, int nmax) {
        this.W = W;
        this.q = q;
        this.p1 = p1;
        this.p2 = p2;
        this.tabuMin = tabuMin;
        this.tabuMax = tabuMax;
        this.g = g;
        this.h = h;
        this.nmax = nmax;
    }

    public List<Node> getW() {
        return W;
    }

    public void setW(List<Node> W) {
        this.W = W;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public int getTabuMin() {
        return tabuMin;
    }

    public void setTabuMin(int tabuMin) {
        this.tabuMin = tabuMin;
    }

    public int getTabuMax() {
        return tabuMax;
    }

    public void setTabuMax(int tabuMax) {
        this.tabuMax = tabuMax;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getNmax() {
        return nmax;
    }

    public void setNmax(int nmax) {
        this.nmax = nmax;
    }

    public AssignedTaxi getTaxi() {
        return taxi;
    }

    public void setTaxi(AssignedTaxi taxi) {
        this.taxi = taxi;
    }
    
    

}
