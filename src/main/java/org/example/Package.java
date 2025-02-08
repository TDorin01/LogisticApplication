package org.example;

import java.time.LocalDate;

public class Package {
    private final String targetLocation;
    private final int targetDistance;
    private final int value;
    private final LocalDate deliveryDate;

    public Package(String targetLocation, int targetDistance, int value, LocalDate deliveryDate) {
        this.targetLocation = targetLocation;
        this.targetDistance = targetDistance;
        this.value = value;
        this.deliveryDate = deliveryDate;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public int getTargetDistance() {
        return targetDistance;
    }

    public int getValue() {
        return value;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
}

