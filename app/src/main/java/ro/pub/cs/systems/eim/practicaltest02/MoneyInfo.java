package ro.pub.cs.systems.eim.practicaltest02;

public class MoneyInfo {
    private final String type;
    private final String value;
    private final String time1;

    public MoneyInfo(String type, String value, String time1) {
        this.type = type;
        this.time1 = time1;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getTime1() {
        return time1;
    }
}
