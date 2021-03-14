package avdta.network;

import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Location;
import avdta.network.node.Node;
import avdta.util.RunningAvg;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import javax.json.*;
import java.awt.*;
import java.io.*;

public class AvdtaJSONSerializer {

    /* Edit Configuration Variables START */

    public static final String FILE_PATH = "/Users/jeffrey/AVDTA_modified/AVDTA_maps/maps.js";

    public static final double MIN_TT = 0;
    public static final double TT_COLOR2_CUTOFF = 75;
    public static final double MAX_TT = 150;

    public static final double MIN_FLOWIN = 0;
    public static final double FLOWIN_COLOR2_CUTOFF = 40;
    public static final double MAX_FLOWIN = 80;

    public static final double MIN_FFS = 0;
    public static final double FFS_COLOR2_CUTOFF = 30;
    public static final double MAX_FFS = 60;

    public static final double MIN_CAPACITY = 0;
    public static final double CAPACITY_COLOR2_CUTOFF = 900;
    public static final double MAX_CAPACITY = 1800;

    public static final int C1_R = 144;
    public static final int C1_G = 238;
    public static final int C1_B = 144;

    public static final int C2_R = 102;
    public static final int C2_G = 153;
    public static final int C2_B = 255;

    public static final int C3_R = 255;
    public static final int C3_G = 0;
    public static final int C3_B = 0;

    public static final double DISPLACEMENT = 0.0001;

    /* Edit Configuration Variables END */


    private static final Color C1 = new Color(C1_R, C1_G, C1_B);
    private static final Color C2 = new Color(C2_R, C2_G, C2_B);
    private static final Color C3 = new Color(C3_R, C3_G, C3_B);

    private static final JsonBuilderFactory factory = Json.createBuilderFactory(null);


    private static JsonObject serializeLinkCoordinates(Network network, String name) {
        JsonArrayBuilder coordinatesArrayBuilder = factory.createArrayBuilder();

        for (Link link : network.getLinks()) {
            if (link instanceof CentroidConnector) {
                continue;
            }

            Location[] coordinates = calculateDisplacedCoordinates(link);

            coordinatesArrayBuilder.add(factory.createObjectBuilder()
                    .add("source", factory.createObjectBuilder()
                            .add("latitude", coordinates[0].getLat())
                            .add("longitude", coordinates[0].getLon()))
                    .add("dest", factory.createObjectBuilder()
                            .add("latitude", coordinates[1].getLat())
                            .add("longitude", coordinates[1].getLon()))
                    .add("id", link.getId())
                    .add("colorForFFS", getColor(link.getFFSpeed(), MIN_FFS, FFS_COLOR2_CUTOFF, MAX_FFS))
                    .add("colorForCapacity", getColor(link.getCapacity(), MIN_CAPACITY, CAPACITY_COLOR2_CUTOFF, MAX_CAPACITY))
                    .add("colorArrayForTT", createColorArrayForTT(link))
                    .add("colorArrayForFlowIn", createColorArrayForFlowin(link)));
        }

        JsonObject object = factory.createObjectBuilder()
                .add("name", name)
                .add("ASTLabelArray", createASTLabelArray())
                .add("MIN_TT_FOR_LEGEND", MIN_TT)
                .add("C2_TT_CUTOFF_FOR_LEGEND", TT_COLOR2_CUTOFF)
                .add("MAX_TT_FOR_LEGEND", MAX_TT)
                .add("MIN_FLOWIN_FOR_LEGEND", MIN_FLOWIN)
                .add("C2_FLOWIN_CUTOFF_FOR_LEGEND", FLOWIN_COLOR2_CUTOFF)
                .add("MAX_FLOWIN_FOR_LEGEND", MAX_FLOWIN)
                .add("MIN_FFS_FOR_LEGEND", MIN_FFS)
                .add("C2_FFS_CUTOFF_FOR_LEGEND", FFS_COLOR2_CUTOFF)
                .add("MAX_FFS_FOR_LEGEND", MAX_FFS)
                .add("MIN_CAPACITY_FOR_LEGEND", MIN_CAPACITY)
                .add("C2_CAPACITY_CUTOFF_FOR_LEGEND", CAPACITY_COLOR2_CUTOFF)
                .add("MAX_CAPACITY_FOR_LEGEND", MAX_CAPACITY)
                .add("C1_R", C1_R)
                .add("C1_G", C1_G)
                .add("C1_B", C1_B)
                .add("C2_R", C2_R)
                .add("C2_G", C2_G)
                .add("C2_B", C2_B)
                .add("C3_R", C3_R)
                .add("C3_G", C3_G)
                .add("C3_B", C3_B)
                .add("coordinateArray", coordinatesArrayBuilder
                        .build())
                .build();

        return object;
    }

    private static JsonArray createASTLabelArray() {
        JsonArrayBuilder ASTLabelArrayBuilder = factory.createArrayBuilder();

        for (int i = 0; i < Simulator.duration/Simulator.ast_duration; i ++) {
            int start = i * Simulator.ast_duration;
            int end = (i + 1) * Simulator.ast_duration;
            ASTLabelArrayBuilder.add(factory.createObjectBuilder()
                    .add("index", i)
                    .add("label", "Time Interval Shown: " + start/60 + " - " + end/60 + " minutes"));
        }

        return ASTLabelArrayBuilder.build();
    }

    private static JsonArray createColorArrayForFlowin(Link link) {
        int[] flowInArray = link.flowin;

        JsonArrayBuilder colorArrayBuilder = factory.createArrayBuilder();

        for (int i = 0; i < Simulator.duration/Simulator.ast_duration; i++) {
            colorArrayBuilder.add(factory.createObjectBuilder()
                .add("index", i)
                .add("color", getColor(flowInArray[i], MIN_FLOWIN, FLOWIN_COLOR2_CUTOFF, MAX_FLOWIN)));
        }

        return colorArrayBuilder.build();
    }

    private static JsonArray createColorArrayForTT(Link link) {
        RunningAvg[] avgTTArray = link.getAvgTTs();

        JsonArrayBuilder colorArrayBuilder = factory.createArrayBuilder();

        for (int i = 0; i < Simulator.duration/Simulator.ast_duration; i++) {
            colorArrayBuilder.add(factory.createObjectBuilder()
                    .add("index", i)
                    .add("color", getColor(avgTTArray[i].getAverage(), MIN_TT, TT_COLOR2_CUTOFF, MAX_TT)));
        }

        return colorArrayBuilder.build();
    }

    private static String getColor(double value, double min, double c2Cutoff, double max) {
        if (value < c2Cutoff) {
            double proportion = (value - min)/(c2Cutoff - min);

            Double red = (C2.getRed() - C1.getRed()) * proportion + C1.getRed();
            Double green = (C2.getGreen() - C1.getGreen()) * proportion + C1.getGreen();
            Double blue = (C2.getBlue() - C1.getBlue()) * proportion + C1.getBlue();

            return String.format("#%02x%02x%02x", red.intValue(), green.intValue(), blue.intValue());
        } else {
            double proportion = (value - c2Cutoff)/(max - c2Cutoff);

            if (proportion > 1) {
                return String.format("#%02x%02x%02x", C3.getRed(), C3.getGreen(), C3.getBlue());
            }

            Double red = (C3.getRed() - C2.getRed()) * proportion + C2.getRed();
            Double green = (C3.getGreen() - C2.getGreen()) * proportion + C2.getGreen();
            Double blue = (C3.getBlue() - C2.getBlue()) * proportion + C2.getBlue();

            return String.format("#%02x%02x%02x", red.intValue(), green.intValue(), blue.intValue());
        }
    }

    //slightly displaces source and dest coordinates to deliniate between two links on the same road
    private static Location[] calculateDisplacedCoordinates(Link link) {
        Location[] input = link.getCoordinates();

        double sourceLat = link.getSource().getY();
        double sourceLng = link.getSource().getX();
        double destLat = link.getDest().getY();
        double destLng = link.getDest().getX();

        double slope = (destLat - sourceLat)/(destLng - sourceLng);
        double perpendicular_slope = -1.0/slope;

        if (slope == 0) {
            //System.out.println("slope = 0, perp slope = infinity");
            return zeroSlope(sourceLat, sourceLng, destLat, destLng);
        } else if (Double.isInfinite(slope)) {
            //System.out.println("slope = infinity, perp slope = 0");
            perpendicular_slope = 0;
        } else if (destLat - sourceLat == 0 && destLng - sourceLng == 0) {
            System.out.println("source node and dest node are identical for link id: " + link.getId());
            return input;
        }

        int sign;
        if (destLat - sourceLat > 0) {
            sign = 1;
        } else if (destLat - sourceLat < 0) {
            sign = -1;
        } else {
            throw new RuntimeException("Slope = 0. This case should have already been previously handled");
        }

        double normalization = (1.0 + Math.abs(perpendicular_slope))/DISPLACEMENT;

        double newSourceLat = sourceLat + sign*perpendicular_slope/normalization;
        double newSourceLng = sourceLng + sign*1.0/normalization;
        double newDestLat = destLat + sign*perpendicular_slope/normalization;
        double newDestLng = destLng + sign*1.0/normalization;

        Location[] returnArray = {new Location(newSourceLng, newSourceLat), new Location(newDestLng, newDestLat)};
        return returnArray;
    }

    private static Location[] zeroSlope(double sourceLat, double sourceLng, double destLat, double destLng) {
        int sign;
        if (destLng - sourceLng > 0) {
            sign = 1;
        } else if (destLng - sourceLng < 0) {
            sign = -1;
        } else {
            throw new RuntimeException("slope = 0 and (dest & source lat are identical). This is the case where source node and dest node are identical. Should have already been accounted for");
        }

        double newSourceLat = sourceLat + sign*(-1) * DISPLACEMENT;
        double newDestLat = destLat + sign*(-1) * DISPLACEMENT;

        Location[] returnArray = {new Location(sourceLng, newSourceLat), new Location(destLng, newDestLat)};
        return returnArray;
    }

    public static void write(Network network, String name) throws IOException {
        File file = new File(FILE_PATH);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();

        JsonObject serializedNetwork = serializeLinkCoordinates(network, name);

        //read maps.js file - if it is currently empty (i.e. line is null since there should never be a newline in the file) then we only write the current serialized object. Otherwise,
        //we need to append the current serialized object onto the end of the array
        if (line == null) {
            FileOutputStream stream = new FileOutputStream(file);
            PrintStream printStream = new PrintStream(stream);
            JsonWriter jsonWriter = Json.createWriter(stream);

            printStream.print("serializedNetworkArray = [");
            jsonWriter.writeObject(serializedNetwork);
            printStream.print("]");

            stream.close();
            printStream.close();
            jsonWriter.close();
        } else {
            FileOutputStream stream = new FileOutputStream(file);
            PrintStream printStream = new PrintStream(stream);
            JsonWriter jsonWriter = Json.createWriter(stream);

            //find index of the end of the array, and append our current object there
            printStream.print(line.substring(0, line.lastIndexOf("]")));
            printStream.print(",");
            jsonWriter.writeObject(serializedNetwork);
            printStream.println("]");

            stream.close();
            printStream.close();
            jsonWriter.close();
        }
    }
}
