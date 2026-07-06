package com.allyphone.service;

/** Phone service plans a player can subscribe to. */
public enum ServicePlan {

    BASIC("Basic", 500, 5),
    PREMIUM("Premium", 1200, 0),
    UNLIMITED("Unlimited", 2500, 0);

    private final String displayName;
    private final double monthlyCost;
    private final double smsCost;

    ServicePlan(String displayName, double monthlyCost, double smsCost) {
        this.displayName = displayName;
        this.monthlyCost = monthlyCost;
        this.smsCost = smsCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public double getSmsCost() {
        return smsCost;
    }

    public static ServicePlan fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return BASIC;
        }
    }
}
