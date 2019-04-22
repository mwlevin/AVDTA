package netdesign;

import java.util.*;
import java.lang.Math;
/**
 *
 * @author ribsthakkar
 */
public abstract class TabuSearch<T extends Individual> {
    protected Set<T> tabuList;
    protected T bestSolution;
    protected T currentSolution;
    protected int maxIterations;
    private double convergence;


    private boolean didConverge(int currentIter, T currentBest) {
        return currentIter >= maxIterations;
    }

    public TabuSearch(int max, double conv) {
        tabuList = new HashSet<>();
        maxIterations = max;
        convergence = conv;
    }

    public TabuSearch(int max, double conv, T warmStartSolution) {
        tabuList = new HashSet<>();
        maxIterations = max;
        convergence = conv;
        currentSolution = warmStartSolution;
        bestSolution = warmStartSolution;
    }

    public abstract SortedSet<T> generateNeighbor(T currentState);
    public abstract T generateRandom();
    public abstract boolean isNeighborBetter(T current, T neighbor);
    public abstract void evaluate(T child);

    public T getBestNeighbor(SortedSet<T> neighbor) {
        for(T n: neighbor) {
            if(!tabuList.contains(n))
                return n;
        }
        return null;
    }

    public T solve() {
        if(currentSolution == null) {
            currentSolution = generateRandom();
            bestSolution = currentSolution;
        }
        int currentIter = 0;
        while(!didConverge(currentIter++, bestSolution)) {
            SortedSet<T> neighbors = generateNeighbor(currentSolution);
            T bestNeighbor = getBestNeighbor(neighbors);
            if(isNeighborBetter(bestSolution, bestNeighbor))
                bestSolution = bestNeighbor;
            tabuList.add(currentSolution);
            currentSolution = bestNeighbor;
        }

        return bestSolution;
    }

    public T solve(int numIters) {
        if(currentSolution == null) {
            currentSolution = generateRandom();
            bestSolution = currentSolution;
        }
        int currentIter = 0;
        while(currentIter++ < numIters) {
            System.out.println("Iteration #" + currentIter);
            SortedSet<T> neighbors = generateNeighbor(currentSolution);
            T bestNeighbor = getBestNeighbor(neighbors);
            if(isNeighborBetter(bestSolution, bestNeighbor))
                bestSolution = bestNeighbor;
            tabuList.add(currentSolution);
            currentSolution = bestNeighbor;
        }

        return bestSolution;
    }

    public T simulatedAnnealingSolve(int numIters) {
        if(currentSolution == null) {
            currentSolution = generateRandom();
            bestSolution = currentSolution;
        }
        int currentIter = 0;
        double t = 1.0;
        double alpha = 0.95;

        while(currentIter++ < numIters && t > ) {
            SortedSet<T> neighbors = generateNeighbor(currentSolution);
            T bestNeighbor = getBestNeighbor(neighbors);
            double p = acceptanceProb(currentSolution, bestNeighbor, t);
            System.out.println("Acceptance prob is " + p);
            if (p > Math.random()) {
                currentSolution = bestNeighbor;
            } else {
                System.out.println("Chose the worse neighbor");
                currentSolution = getWorseNeighbor(neighbors);
            }
            if(isNeighborBetter(bestSolution, bestNeighbor))
                bestSolution = bestNeighbor;
            tabuList.add(currentSolution);
            t = t * alpha;
        }

        return bestSolution;
    }

    private double acceptanceProb(T currSolution, T neighbor, double t) {
        return Math.exp(-1 * Math.abs((currSolution.getObj() - neighbor.getObj())) * t);
    }

    private T getWorseNeighbor(Set<T> neighbor) {
        for(T n: neighbor) {
            if(!tabuList.contains(n) && n.getObj() > currentSolution.getObj())
                return n;
        }
        return currentSolution;
    }

    public void setCurrentSolution(T t) {
        currentSolution = t;
    }
}
