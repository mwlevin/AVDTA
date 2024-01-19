package avdta.network;

import avdta.network.link.Link;

public class Metric {

    private Type metricType;
    private double minValue;
    private double c2Value;
    private double maxValue;
    private String unitOfMeasurement;

    public enum Type {
        CAPACITY,
        FLOW_IN,
        AVERAGE_TT,
        FREE_FLOW_SPEED
    }

    public Metric(Type metricType, double minValue, double c2Value, double maxValue, String unitOfMeasurement) {
        this.metricType = metricType;
        this.minValue = minValue;
        this.c2Value = c2Value;
        this.maxValue = maxValue;
        this.unitOfMeasurement = unitOfMeasurement;
    }

    //the range of ast values should be from [0, Simulator.duration/Simulator.ast_duration).
    public double getValue(Link link, int ast) {
        switch (metricType) {
            case CAPACITY:
                return link.getCapacity();
            case FLOW_IN:
                return link.flowin[ast];
            case AVERAGE_TT:
                if (link.getId() == 18178) {
                    System.out.println("ast: " + ast + ". averageTT: " + link.getAvgTTs()[ast].getAverage());
                }
                return link.getAvgTTs()[ast].getAverage();
            case FREE_FLOW_SPEED:
                return link.getFFSpeed();
            default:
                throw new RuntimeException("metric type of name: " + metricType + " is not supported;");
        }
    }

    public Type getMetricType() {
        return metricType;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getC2Value() {
        return c2Value;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }
}

