package com.balatro.core.card;

/** Масть игральной карты. */
public enum Suit {
    HEARTS('♥', "h", true),
    DIAMONDS('♦', "d", true),
    CLUBS('♣', "c", false),
    SPADES('♠', "s", false);

    private final char symbol;
    private final String id;
    private final boolean red;

    Suit(char symbol, String id, boolean red) {
        this.symbol = symbol;
        this.id = id;
        this.red = red;
    }

    public char symbol() {
        return symbol;
    }

    /** Короткий id для сериализации и поиска текстур. */
    public String id() {
        return id;
    }

    /** Красная масть (для выбора цвета карты). */
    public boolean isRed() {
        return red;
    }

    /** Отображаемое имя масти на английском (для описаний джокеров). */
    public String displayName() {
        return switch (this) {
            case HEARTS -> "Hearts";
            case DIAMONDS -> "Diamonds";
            case CLUBS -> "Clubs";
            case SPADES -> "Spades";
        };
    }

    public static Suit byId(String id) {
        for (Suit s : values()) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown suit id: " + id);
    }
}
