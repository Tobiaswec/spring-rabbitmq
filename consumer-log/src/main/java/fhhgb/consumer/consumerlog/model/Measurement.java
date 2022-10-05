package fhhgb.consumer.consumerlog.model;


import java.time.OffsetDateTime;

public class Measurement {


    private int value;
    private long timeStamp;
    private int id;


    public Measurement() {
    }

    public Measurement(int value) {
        this.value = value;
        this.timeStamp = OffsetDateTime.now().toEpochSecond();
    }


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
