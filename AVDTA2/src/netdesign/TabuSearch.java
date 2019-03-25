package netdesign;

import java.util.*;

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
        currentSolution = generateRandom();
        bestSolution = currentSolution;
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
        currentSolution = generateRandom();
        bestSolution = currentSolution;
        int currentIter = 0;
        while(currentIter++ < numIters) {
            SortedSet<T> neighbors = generateNeighbor(currentSolution);
            T bestNeighbor = getBestNeighbor(neighbors);
            if(isNeighborBetter(bestSolution, bestNeighbor))
                bestSolution = bestNeighbor;
            tabuList.add(currentSolution);
            currentSolution = bestNeighbor;
        }

        return bestSolution;
    }
    public void setCurrentSolution(T t) {
        currentSolution = t;
    }
}
