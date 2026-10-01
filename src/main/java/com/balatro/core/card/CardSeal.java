package com.balatro.core.card;

/**
 * Печать на карте. Даёт эффект, срабатывающий в особых ситуациях:
 * повторный розыгрыш комбинации, доход, генерация карт после слепого.
 */
public enum CardSeal {

    NONE(""),
    /** Повторно срабатывает, когда комбинация карты сыграна второй раз. */
    RED("Red Seal"),
    /** Даёт $1 при сбросе карты. */
    GOLD("Gold Seal"),
    /** Создаёт карту-планету после победы над слепым. */
    BLUE("Blue Seal"),
    /** Создаёт таро-карту после победы над боссом. */
    PURPLE("Purple Seal");

    private final String displayName;

    CardSeal(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isEmpty() {
        return this == NONE;
    }

    public static CardSeal byName(String name) {
        for (CardSeal s : values()) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return NONE;
    }
}