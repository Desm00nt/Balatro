package com.balatro.core.joker;

/** Редкость джокера — влияет на диапазон цены в магазине. */
public enum JokerRarity {
    COMMON(1, 3, 5),
    UNCOMMON(2, 4, 8),
    RARE(3, 6, 10),
    LEGENDARY(4, 8, 12);

    private final int tier;
    private final int minPrice;
    private final int maxPrice;

    JokerRarity(int tier, int minPrice, int maxPrice) {
        this.tier = tier;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public int tier() {
        return tier;
    }

    public int minPrice() {
        return minPrice;
    }

    public int maxPrice() {
        return maxPrice;
    }

    /** Шанс появиться в магазине: чем реже, тем меньше. */
    public int shopWeight() {
        return switch (this) {
            case COMMON -> 70;
            case UNCOMMON -> 25;
            case RARE -> 12;
            case LEGENDARY -> 4;
        };
    }
}
