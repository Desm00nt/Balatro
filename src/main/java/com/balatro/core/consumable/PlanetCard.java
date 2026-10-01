package com.balatro.core.consumable;

import com.balatro.core.hand.HandType;

/**
 * Карта-планета: однократно повышает уровень своей комбинации на 1.
 *
 * <p>В Balatro секретные планеты (Eris, Ceres, Planet X) появляются
 * в магазине только после того, как комбинация сыграна хотя бы раз.
 */
public enum PlanetCard {

    PLUTO("Pluto", HandType.HIGH_CARD, false),
    MERCURY("Mercury", HandType.PAIR, false),
    URANUS("Uranus", HandType.TWO_PAIR, false),
    VENUS("Venus", HandType.THREE_OF_A_KIND, false),
    SATURN("Saturn", HandType.STRAIGHT, false),
    JUPITER("Jupiter", HandType.FLUSH, false),
    EARTH("Earth", HandType.FULL_HOUSE, false),
    MARS("Mars", HandType.FOUR_OF_A_KIND, false),
    NEPTUNE("Neptune", HandType.STRAIGHT_FLUSH, false),
    PLANET_X("Planet X", HandType.FIVE_OF_A_KIND, true),
    CERES("Ceres", HandType.FLUSH_HOUSE, true),
    ERIS("Eris", HandType.FLUSH_FIVE, true);

    private final String displayName;
    private final HandType handType;
    private final boolean secret;

    PlanetCard(String displayName, HandType handType, boolean secret) {
        this.displayName = displayName;
        this.handType = handType;
        this.secret = secret;
    }

    public String displayName() {
        return displayName;
    }

    public HandType handType() {
        return handType;
    }

    /** Секретная планета: доступна только после первого розыгрыша комбинации. */
    public boolean isSecret() {
        return secret;
    }

    public int price() {
        return 3;
    }

    public String description() {
        return "Level up " + handType.displayName();
    }

    /** Ищет планету по типу комбинации. */
    public static PlanetCard forHand(HandType type) {
        for (PlanetCard p : values()) {
            if (p.handType == type) {
                return p;
            }
        }
        throw new IllegalArgumentException("No planet for hand: " + type);
    }
}
