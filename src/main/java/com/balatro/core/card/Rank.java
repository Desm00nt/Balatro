package com.balatro.core.card;

/**
 * Ранг игральной карты. Значение {@link #chips()} — базовое количество
 * фишек, которое карта даёт при попадании в комбинацию.
 */
public enum Rank {
    TWO('2', 2),
    THREE('3', 3),
    FOUR('4', 4),
    FIVE('5', 5),
    SIX('6', 6),
    SEVEN('7', 7),
    EIGHT('8', 8),
    NINE('9', 9),
    TEN('T', 10),
    JACK('J', 10),
    QUEEN('Q', 10),
    KING('K', 10),
    ACE('A', 11);

    private final char symbol;
    private final int chips;

    Rank(char symbol, int chips) {
        this.symbol = symbol;
        this.chips = chips;
    }

    /** Короткое символьное обозначение ранга (для рендера). */
    public char symbol() {
        return symbol;
    }

    /** Базовая стоимость ранга в фишках. Валты всегда дают 10 фишек. */
    public int chips() {
        return chips;
    }

    public boolean isFace() {
        return this == JACK || this == QUEEN || this == KING;
    }

    /** Порядковый номер ранга для проверки на прямой (2..14, где 14 = Ace). */
    public int order() {
        return ordinal() + 2;
    }
}
