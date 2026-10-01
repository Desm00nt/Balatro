package com.balatro.core.hand;

/**
 * Типы покерных комбинаций Balatro с базовыми значениями и
 * приростом за каждый уровень.
 *
 * <p>Итог: {@code chips = (chipsByLevel[level-1] + sum(cardChips)) * multByLevel[level-1]}
 * после применения модификаторов джокеров.
 */
public enum HandType {

    HIGH_CARD("High Card", 5, 1, 10, 1),
    PAIR("Pair", 10, 2, 15, 2),
    TWO_PAIR("Two Pair", 20, 2, 20, 2),
    THREE_OF_A_KIND("Three of a Kind", 30, 3, 20, 3),
    STRAIGHT("Straight", 30, 4, 30, 3),
    FLUSH("Flush", 35, 4, 15, 2),
    FULL_HOUSE("Full House", 40, 4, 25, 2),
    FOUR_OF_A_KIND("Four of a Kind", 60, 7, 30, 3),
    STRAIGHT_FLUSH("Straight Flush", 100, 8, 40, 4),
    FIVE_OF_A_KIND("Five of a Kind", 120, 12, 35, 3),
    FLUSH_HOUSE("Flush House", 140, 14, 40, 4),
    FLUSH_FIVE("Flush Five", 160, 16, 50, 3);

    private final String displayName;
    private final int baseChips;
    private final int baseMult;
    private final int chipsPerLevel;
    private final int multPerLevel;

    HandType(String displayName, int baseChips, int baseMult,
             int chipsPerLevel, int multPerLevel) {
        this.displayName = displayName;
        this.baseChips = baseChips;
        this.baseMult = baseMult;
        this.chipsPerLevel = chipsPerLevel;
        this.multPerLevel = multPerLevel;
    }

    public String displayName() {
        return displayName;
    }

    /** Фишки комбинации на указанном уровне (1-based). */
    public int chips(int level) {
        int lv = Math.max(1, level);
        return baseChips + chipsPerLevel * (lv - 1);
    }

    /** Множитель комбинации на указанном уровне (1-based). */
    public int mult(int level) {
        int lv = Math.max(1, level);
        return baseMult + multPerLevel * (lv - 1);
    }

    /** Планета, повышающая уровень этой комбинации. */
    public String planetName() {
        return switch (this) {
            case HIGH_CARD -> "pluto";
            case PAIR -> "mercury";
            case TWO_PAIR -> "uranus";
            case THREE_OF_A_KIND -> "venus";
            case STRAIGHT -> "saturn";
            case FLUSH -> "jupiter";
            case FULL_HOUSE -> "earth";
            case FOUR_OF_A_KIND -> "mars";
            case STRAIGHT_FLUSH -> "neptune";
            case FIVE_OF_A_KIND -> "planet_x";
            case FLUSH_HOUSE -> "ceres";
            case FLUSH_FIVE -> "eris";
        };
    }
}
