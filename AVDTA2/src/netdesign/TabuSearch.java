package netdesign;

import java.util.*;

/**
 *
 * @author ribsthakkar
 */
public abstract class TabuSearch<T extends Individual> {
    private Set<T> tabuList;
    private T bestSolution;
    private T currentSolution;
    private int maxIterations;
    private double convergence;


    private boolean didConverge(int currentIter, T currentBest) {
        return currentIter >= maxIterations;
    }

    public TabuSearch(int max, double conv) {
        tabuList = new HashSet<>();
        maxIterations = max;
        convergence = conv;
        currentSolution = generateRandom();
        bestSolution = generateRandom();
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

    public T getBestNeighbor(SortedSet<T> neighbor) {
        for(T n: neighbor) {
            if(!tabuList.contains(n))
                return n;
        }
        return null;
    }

    public T solve() {
        int currentIter = 0;
        while(didConverge(currentIter++, bestSolution)) {
            SortedSet<T> neighbors = generateNeighbor(currentSolution);
            T bestNeighbor = getBestNeighbor(neighbors);
            if(isNeighborBetter(bestSolution, bestNeighbor))
                bestSolution = bestNeighbor;
            tabuList.add(currentSolution);
            currentSolution = bestNeighbor;
        }

        return bestSolution;
    }
}
