package com.balatro.core.consumable;

import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;

/**
 * Таро-карта: изменяет карты в колоде. Все 22 карты оригинала.
 * Текст эффекта хранится в {@link TarotEffects}.
 */
public enum TarotCard {

    THE_FOOL("The Fool"),
    THE_MAGICIAN("The Magician"),
    THE_HIGH_PRIESTESS("The High Priestess"),
    THE_EMPRESS("The Empress"),
    THE_EMPEROR("The Emperor"),
    THE_HIEROPHANT("The Hierophant"),
    THE_LOVERS("The Lovers"),
    THE_CHARIOT("The Chariot"),
    JUSTICE("Justice"),
    THE_HERMIT("The Hermit"),
    THE_WHEEL_OF_FORTUNE("The Wheel of Fortune"),
    STRENGTH("Strength"),
    THE_HANGED_MAN("The Hanged Man"),
    DEATH("Death"),
    TEMPERANCE("Temperance"),
    THE_DEVIL("The Devil"),
    THE_TOWER("The Tower"),
    THE_STAR("The Star"),
    THE_MOON("The Moon"),
    THE_SUN("The Sun"),
    JUDGEMENT("Judgement"),
    THE_WORLD("The World");

    private final String displayName;

    TarotCard(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** Текст эффекта для интерфейса. */
    public String effect() {
        return TarotEffects.effect(this);
    }

    /** Сколько выбранных карт требуется (0 = выбор не нужен). */
    public int requiredSelection() {
        return switch (this) {
            case THE_MAGICIAN, STRENGTH, DEATH -> 2;
            case THE_EMPRESS, THE_HIEROPHANT, THE_LOVERS, THE_CHARIOT,
                 JUSTICE, THE_DEVIL, THE_TOWER, THE_SUN, JUDGEMENT -> 1;
            default -> 0;
        };
    }

    public int basePrice() {
        return 3;
    }

    /** Масть, в которую превращается карта. */
    public Suit targetSuit() {
        return switch (this) {
            case THE_LOVERS -> Suit.HEARTS;
            case THE_CHARIOT -> Suit.DIAMONDS;
            case JUSTICE -> Suit.CLUBS;
            default -> null;
        };
    }

    public static TarotCard byName(String name) {
        for (TarotCard t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown tarot: " + name);
    }

    /** Пул для случайной выдачи: без «создающих» карт. */
    public static java.util.List<TarotCard> pool() {
        java.util.List<TarotCard> list =
                new java.util.ArrayList<>(java.util.Arrays.asList(values()));
        list.remove(THE_FOOL);
        list.remove(THE_SUN);
        return list;
    }

    /** Низший ранг, к которому можно привести карту. */
    public static Rank lowestRank() {
        return Rank.TWO;
    }
}