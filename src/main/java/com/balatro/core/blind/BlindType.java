package com.balatro.core.blind;

/** Тип текущего слепого на раунде. */
public enum BlindType {

    SMALL(1, "Small Blind"),
    BIG(2, "Big Blind"),
    BOSS(3, "Boss Blind");

    private final int order;
    private final String displayName;

    BlindType(int order, String displayName) {
        this.order = order;
        this.displayName = displayName;
    }

    public int order() {
        return order;
    }

    public String displayName() {
        return displayName;
    }

    /** Базовые множители требования (для а1: 300 / 600 / 800). */
    public double baseRequirement() {
        return switch (this) {
            case SMALL -> 300.0;
            case BIG -> 600.0;
            case BOSS -> 800.0;
        };
    }

    /** Награда за прохождение слепого. */
    public int moneyReward() {
        return switch (this) {
            case SMALL -> 3;
            case BIG -> 4;
            case BOSS -> 5;
        };
    }

    /** Бонус за пропуск раунда (играется вслепую). */
    public int skipBonus() {
        return switch (this) {
            case SMALL -> 2;
            case BIG -> 3;
            case BOSS -> 0;
        };
    }

    public static BlindType byOrder(int order) {
        for (BlindType t : values()) {
            if (t.order == order) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown blind order: " + order);
    }
}
