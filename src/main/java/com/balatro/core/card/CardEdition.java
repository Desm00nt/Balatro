package com.balatro.core.card;

/**
 * Издание карты — постоянный модификатор, получаемый из таро
 * или бонусных паков. Применяется при подсчёте очков.
 */
public enum CardEdition {

    NONE("None", 0, 0, 1.0, 0),
    /** +50 фишек. */
    FOIL("Foil", 50, 0, 1.0, 0),
    /** +10 множителя. */
    HOLOGRAPHIC("Holographic", 0, 10, 1.0, 0),
    /** x1.5 к итоговому множителю. */
    POLYCHROME("Polychrome", 0, 0, 1.5, 0),
    /** +1 рука и −1 сброс за раунд. */
    NEGATIVE("Negative", 0, 0, 1.0, 0),
    /** x2 множителя, если карта осталась в руке, а не сыграна. */
    GLASS("Glass", 0, 0, 2.0, 0),
    /** +20 множителя, если сумма фишек и множителя карт равна 7. */
    LUCKY("Lucky", 0, 20, 1.0, 7),
    /** +1 множителя за каждую копию этой карты в колоде. */
    CROWDED("Crowded", 0, 1, 1.0, -1);

    private final String displayName;
    private final int chips;
    private final int mult;
    private final double multiplier;
    /** 7 — триггер LUCKY; −1 — триггер CROWDED; 0 — без условия. */
    private final int trigger;

    CardEdition(String displayName, int chips, int mult, double multiplier, int trigger) {
        this.displayName = displayName;
        this.chips = chips;
        this.mult = mult;
        this.multiplier = multiplier;
        this.trigger = trigger;
    }

    public String displayName() {
        return displayName;
    }

    public int chips() {
        return chips;
    }

    public int mult() {
        return mult;
    }

    /** Множитель, действующий только при выполнении условия триггера. */
    public double multiplier() {
        return multiplier;
    }

    public int trigger() {
        return trigger;
    }

    /** Даёт ли карту дополнительную руку в начале раунда. */
    public boolean grantsExtraHand() {
        return this == NEGATIVE;
    }

    public static CardEdition byName(String name) {
        for (CardEdition e : values()) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return NONE;
    }
}