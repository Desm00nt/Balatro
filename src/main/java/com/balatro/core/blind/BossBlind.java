package com.balatro.core.blind;

import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;

/**
 * Босс-слепой: повышенное требование и уникальное правило,
 * меняющее игру в раунде.
 */
public enum BossBlind {

    /** Обычный босс без особого правила. */
    NONE("No Boss", 1.0),
    THE_WALL("The Wall", 2.0),
    THE_PSYCHIC("The Psychic", 1.0),
    THE_GOAD("The Goad", 1.0),
    THE_WATER("The Water", 1.0),
    THE_WINDOW("The Window", 1.0),
    THE_MANACLE("The Manacle", 1.0),
    THE_EYE("The Eye", 1.0),
    THE_MOUTH("The Mouth", 1.0),
    THE_FISH("The Fish", 1.0),
    THE_SERPENT("The Serpent", 1.0),
    THE_PALLADIUM("The Palladium", 1.0),
    THE_SHRINE("The Shrine", 1.0),
    THE_TOWER("The Tower", 1.0),
    THE_CLOUD("The Cloud", 1.0),
    THE_HOUSE("The House", 1.0),
    THE_SKILL("The Skill", 1.0),
    THE_STONE("The Stone", 1.0);

    private final String displayName;
    private final double requirementScale;

    BossBlind(String displayName, double requirementScale) {
        this.displayName = displayName;
        this.requirementScale = requirementScale;
    }

    public String displayName() {
        return displayName;
    }

    /** Множитель требования к очкам. */
    public double requirementScale() {
        return requirementScale;
    }

    /** Текст особого правила босса. */
    public String description() {
        return "";
    }

    /** Допустимое число сыгранных карт. */
    public boolean isValidPlay(int selectedCount) {
        return true;
    }

    public boolean restrictsHandType() {
        return false;
    }

    public boolean debuffsAllCards() {
        return false;
    }

    public boolean debuffsOverTime() {
        return false;
    }

    public boolean debuffsFaceCards() {
        return false;
    }

    public boolean requiresHandToUndebuff() {
        return false;
    }

    public boolean generatesTarotOverTime() {
        return false;
    }

    public Rank debuffedRank() {
        return null;
    }

    public Suit debuffedSuit() {
        return null;
    }

    public int handSizeModifier() {
        return 0;
    }

    public int handsModifier() {
        return 0;
    }

    public int discardsModifier() {
        return 0;
    }

    public int maxHandSize() {
        return Integer.MAX_VALUE;
    }

    /** Сколько карт босс добавляет при розыгрыше руки. */
    public int extraCardsOnPlay() {
        return 0;
    }
}