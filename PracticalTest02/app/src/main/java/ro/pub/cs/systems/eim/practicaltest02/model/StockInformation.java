package ro.pub.cs.systems.eim.practicaltest02.model;

public class StockInformation {

    private String time;
    private String value;
    private long timestamp;

    public StockInformation() {
        this.time = null;
        this.value = null;
        timestamp = System.currentTimeMillis();
    }

    public StockInformation(String time, String value) {
        this.time = time;
        this.value = value;
        timestamp = System.currentTimeMillis();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String windSpeed) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Time: " + time + "\n" + "Value: " + value;
    }

}
