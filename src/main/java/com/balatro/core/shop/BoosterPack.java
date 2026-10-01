package com.balatro.core.shop;

/** Тип боастер-пака: сколько карт внутри и сколько можно выбрать. */
public enum BoosterPack {

    /** 3 карты, выбрать 1. Содержит джокеров. */
    JOKER_PACK("Joker Pack", 3, 1),
    /** 3 карты, выбрать 1. Содержит обычные карты. */
    MEGA_PACK("Mega Pack", 3, 1),
    /** 3 карты, выбрать 1. Содержит таро/планеты. */
    TAROT_PACK("Tarot Pack", 3, 1),
    /** 4 карты, выбрать 2. Содержит спектральные. */
    SPECTRAL_PACK("Spectral Pack", 4, 2),
    /** 2 карты, выбрать 1. Содержит обычные карты. */
    STANDARD_PACK("Standard Pack", 2, 1);

    private final String displayName;
    private final int cardCount;
    private final int picks;

    BoosterPack(String displayName, int cardCount, int picks) {
        this.displayName = displayName;
        this.cardCount = cardCount;
        this.picks = picks;
    }

    public String displayName() {
        return displayName;
    }

    public int cardCount() {
        return cardCount;
    }

    /** Сколько карт игрок забирает из пака. */
    public int picks() {
        return picks;
    }

    public int basePrice() {
        return switch (this) {
            case JOKER_PACK, MEGA_PACK, TAROT_PACK -> 4;
            case SPECTRAL_PACK -> 4;
            case STANDARD_PACK -> 3;
        };
    }

    /** Содержит ли пак джокеров. */
    public boolean containsJokers() {
        return this == JOKER_PACK;
    }

    /** Содержит ли пак таро или планеты. */
    public boolean containsConsumables() {
        return this == TAROT_PACK;
    }

    /** Содержит ли пак спектральные карты. */
    public boolean containsSpectrals() {
        return this == SPECTRAL_PACK;
    }
}