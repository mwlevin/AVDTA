/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.network.AvdtaJSONSerializer;
import avdta.network.Metric;
import avdta.network.Simulator;
import avdta.network.link.*;
import avdta.network.node.Intersection;
import avdta.network.node.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author huxx0254
 */
public class RunSimulation {
    public static boolean testing = false;
    
    public static void main(String[] args) throws Exception{
        // int[] demandSet = {2, 4, 6, 8, 10, 12, 14, 16, 18};
        int[] demandSet = {5};
        int[] cycleLengthSet = {15};

        for (int j : cycleLengthSet) {
            for (int i : demandSet) {
                int cycleLength = j;
          
                System.out.println("Running CycleMP with Demand " + i + " and CycleLength " + cycleLength + "----------------------------");
                Main mainCycle = new Main();
                System.out.println("calling simulate fixed proportions");
                mainCycle.simulateFixedProportions(i, cycleLength);

                PrintStream delayTableOutCycle = new PrintStream(new FileOutputStream(new File("/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/DelayTables/DelayTable_d" + i + "_c" + cycleLength + ".csv")), true);
                delayTableOutCycle.println("delay, count");
                for (int delayIndex = 10; delayIndex <= 300; delayIndex = delayIndex + 10) {
                    delayTableOutCycle.println(delayIndex + "," + Simulator.delayTable.get(delayIndex));
                }
                
                System.out.println("Running RegularMP With Demand " + i + "----------------------------");
                Main main = new Main();
                main.simulateFixedProportions(i);

                Metric average_TT = new Metric(Metric.Type.AVERAGE_TT, 0, 75, 150, "Seconds");
                Metric flow_in = new Metric(Metric.Type.FLOW_IN, 0, 40, 80, "# of Vehicles");
                Metric ffs = new Metric(Metric.Type.FREE_FLOW_SPEED, 0, 30, 60, "Mph");
                Metric capacity = new Metric(Metric.Type.CAPACITY, 0, 900, 1800, "# of Vehicles");
                Metric[] metricArray = { average_TT, flow_in, ffs, capacity };

                AvdtaJSONSerializer.write(main.sim, "MP Demand 18", metricArray);
                
                PrintStream delayTableOutRegular = new PrintStream(new FileOutputStream(new File("/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/DelayTables/DelayTable_d" + i + ".csv")), true);
                delayTableOutRegular.println("delay, count");
                for (int delayIndex = 10; delayIndex <= 300; delayIndex = delayIndex + 10) {
                    delayTableOutRegular.println(delayIndex + "," + Simulator.delayTable.get(delayIndex));
                }

                double sum = 0;
                for (Integer intersectionId : main.highestDelayLinks.keySet()) {
                    int linkId = main.highestDelayLinks.get(intersectionId);
                    Link MPLink = main.sim.getLink(linkId);
                    Link cycleMPLink = mainCycle.sim.getLink(linkId);
                    double difference = MPLink.getCumulativeAvgTT() - cycleMPLink.getCumulativeAvgTT();
                    sum += difference;
                }

                double allLinksSum = 0;
                for (Integer intersectionId: main.highestDelayAllLinks.keySet()) {
                    double difference = main.highestDelayAllLinks.get(intersectionId) - mainCycle.highestDelayAllLinks.get(intersectionId);
                    allLinksSum += difference;
                }

                System.out.println("Average worst case link avg TT difference: " + sum/main.highestDelayLinks.keySet().size());
                System.out.println("Average link avg TT difference: " + allLinksSum/main.highestDelayAllLinks.keySet().size());

                PrintStream highestTurnRedLightOut = new PrintStream(new FileOutputStream(new File("/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/RedLightTimesForWorstTurns/HighestTurnRedLight_d" + i + ".csv")), true);
                highestTurnRedLightOut.println("intersection, Average Red Light Time for Worst Case Turn");
                for (Integer intersectionId : main.highestRedLightTurns.keySet()) {
                    if (!main.highestRedLightTurns.containsKey(intersectionId)) {
                        continue;
                    }
                    highestTurnRedLightOut.println(intersectionId + "," + main.highestRedLightTurns.get(intersectionId));
                }

                PrintStream highestTurnRedLightOutCycle = new PrintStream(new FileOutputStream(new File("/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/RedLightTimesForWorstTurns/HighestTurnRedLight_d" + i + "_c" + cycleLength + ".csv")), true);
                highestTurnRedLightOutCycle.println("intersection, Average Red Light Time for Worst Case Turn");
                for (Integer intersectionId : mainCycle.highestRedLightTurns.keySet()) {
                    if (!mainCycle.highestRedLightTurns.containsKey(intersectionId)) {
                        continue;
                    }
                    highestTurnRedLightOutCycle.println(intersectionId + "," + mainCycle.highestRedLightTurns.get(intersectionId));
                }

                PrintStream highestTurnWaitTimesOut = new PrintStream(new FileOutputStream(new File("/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/WaitTimesForWorstTurns/HighestTurnWaitTimes_d" + i + ".csv")), true);
                highestTurnWaitTimesOut.println("intersection, Average Wait Time for Worst Case Turn");
                for (Integer intersectionId : main.highestWaitTurns.keySet()) {
                    if (!main.highestWaitTurns.containsKey(intersectionId)) {
                        continue;
                    }
                    highestTurnWaitTimesOut.println(intersectionId + "," + main.highestWaitTurns.get(intersectionId));
                }

                PrintStream highestTurnWaitTimesOutCycle = new PrintStream(new FileOutputStream(new File("/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/WaitTimesForWorstTurns/HighestTurnWaitTimes_d" + i + "_c" + cycleLength + ".csv")), true);
                highestTurnWaitTimesOutCycle.println("intersection, Average Wait Time for Worst Case Turn");
                for (Integer intersectionId: mainCycle.highestWaitTurns.keySet()) {
                    if (!mainCycle.highestWaitTurns.containsKey(intersectionId)) {
                        continue;
                    }
                    highestTurnWaitTimesOutCycle.println(intersectionId + "," + mainCycle.highestWaitTurns.get(intersectionId));
                }

                writeTotalQueueLengthPerIntersection(main, mainCycle, i, cycleLength);
            }
        }
    }

    public static void writeTotalQueueLengthPerIntersection(Main main, Main mainCycle, int demand, int cycleLength) throws FileNotFoundException {
        PrintStream totalQueueLengthPerIntersectionOut = new PrintStream(new FileOutputStream(new File(
                "/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/IntersectionQueueLength/intersectionQueue_d" + demand + ".csv")), true);
        totalQueueLengthPerIntersectionOut.println("intersection, total queue length");
        for (Node node : main.sim.getNodes()) {
            if (node instanceof Intersection) {
                Intersection intersection = ((Intersection) node);
                totalQueueLengthPerIntersectionOut.println(intersection.getId() + "," + intersection.getTotalQueueLength());
            }
        }

        PrintStream totalQueueLengthPerIntersectionOutCycle = new PrintStream(new FileOutputStream(new File(
                "/Users/jeffrey/AVDTA_modified/AVDTA2/projects/coacongress2_ttmp/HaiVuDataCollection/IntersectionQueueLength/intersectionQueue_d" + demand + "_c" + cycleLength + ".csv")), true);
        totalQueueLengthPerIntersectionOutCycle.println("intersection, total queue length");
        for (Node node : mainCycle.sim.getNodes()) {
            if (node instanceof Intersection) {
                Intersection intersection = ((Intersection) node);
                totalQueueLengthPerIntersectionOutCycle.println(intersection.getId() + "," + intersection.getTotalQueueLength());
            }
        }
    }
}
