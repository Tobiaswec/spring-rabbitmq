package fhhgb.producer.producerservice.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class Measurement {


    private int value;
    private long timeStamp;


    public Measurement() {
    }

    public Measurement(Integer value) {
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
}
