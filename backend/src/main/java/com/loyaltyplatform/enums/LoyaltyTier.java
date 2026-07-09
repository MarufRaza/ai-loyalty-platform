package com.loyaltyplatform.enums;

public enum LoyaltyTier {
    SILVER("Silver", 0, 999),
    GOLD("Gold", 1000, 4999),
    PLATINUM("Platinum", 5000, Integer.MAX_VALUE);

    private final String displayName;
    private final int minPoints;
    private final int maxPoints;

    LoyaltyTier(String displayName, int minPoints, int maxPoints) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
    }

    public String getDisplayName() { return displayName; }
    public int getMinPoints() { return minPoints; }
    public int getMaxPoints() { return maxPoints; }

    public static LoyaltyTier fromPoints(int points) {
        if (points >= PLATINUM.minPoints) return PLATINUM;
        if (points >= GOLD.minPoints) return GOLD;
        return SILVER;
    }

    public double getDiscountRate() {
        return switch (this) {
            case SILVER -> 0.05;
            case GOLD -> 0.10;
            case PLATINUM -> 0.15;
        };
    }
}
