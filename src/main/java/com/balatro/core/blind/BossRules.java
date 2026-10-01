package com.balatro.core.blind;

import com.balatro.core.card.Card;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;

/**
 * Правила боссов-слепых: описание, проверки ходов и модификаторы.
 * Вынесено отдельно, чтобы перечисление оставалось компактным.
 */
public final class BossRules {

    private BossRules() {
    }

    /** Текст особого правила. */
    public static String description(BossBlind boss) {
        return switch (boss) {
            case THE_WALL -> "Extra Large Blind: 2x score requirement";
            case THE_PSYCHIC -> "Must play 5 cards";
            case THE_GOAD -> "Only one hand type may be played";
            case THE_WATER -> "Start the round with every card debuffed";
            case THE_WINDOW -> "The Ace of Diamonds is debuffed";
            case THE_MANACLE -> "-1 hand size";
            case THE_EYE -> "Every 2 hands, a random card is debuffed";
            case THE_MOUTH -> "Only 1 hand may be played";
            case THE_FISH -> "Cards stay debuffed until a Flush is played";
            case THE_SERPENT -> "-3 hand size, +3 hands, -2 discards";
            case THE_PALLADIUM -> "Must play exactly 5 cards";
            case THE_SHRINE -> "Every 2 hands, a Tarot card is generated";
            case THE_TOWER -> "Card slots are limited to 3";
            case THE_CLOUD -> "+2 extra cards drawn each hand";
            case THE_HOUSE -> "+1 extra card drawn each hand";
            case THE_SKILL -> "All Aces are debuffed";
            case THE_STONE -> "Face cards in hand are debuffed";
            case NONE -> "";
        };
    }

    /** Проверка допустимости числа сыгранных карт. */
    public static boolean isValidPlay(BossBlind boss, int selectedCount) {
        return switch (boss) {
            case THE_PSYCHIC, THE_PALLADIUM -> selectedCount == 5;
            default -> true;
        };
    }

    public static boolean restrictsHandType(BossBlind boss) {
        return boss == BossBlind.THE_GOAD;
    }

    public static boolean debuffsAllCards(BossBlind boss) {
        return boss == BossBlind.THE_WATER;
    }

    public static boolean debuffsOverTime(BossBlind boss) {
        return boss == BossBlind.THE_EYE;
    }

    public static boolean debuffsFaceCards(BossBlind boss) {
        return boss == BossBlind.THE_STONE;
    }

    public static boolean requiresHandToUndebuff(BossBlind boss) {
        return boss == BossBlind.THE_FISH;
    }

    public static boolean generatesTarotOverTime(BossBlind boss) {
        return boss == BossBlind.THE_SHRINE;
    }

    public static Rank debuffedRank(BossBlind boss) {
        return switch (boss) {
            case THE_WINDOW, THE_SKILL -> Rank.ACE;
            default -> null;
        };
    }

    public static Suit debuffedSuit(BossBlind boss) {
        return switch (boss) {
            case THE_WINDOW -> Suit.DIAMONDS;
            case THE_SKILL -> null;
            default -> null;
        };
    }

    public static int handSizeModifier(BossBlind boss) {
        return switch (boss) {
            case THE_MANACLE -> -1;
            case THE_SERPENT -> -3;
            default -> 0;
        };
    }

    public static int handsModifier(BossBlind boss) {
        return switch (boss) {
            case THE_MOUTH -> -99;
            case THE_SERPENT -> 3;
            default -> 0;
        };
    }

    public static int discardsModifier(BossBlind boss) {
        return switch (boss) {
            case THE_SERPENT -> -2;
            default -> 0;
        };
    }

    public static int maxHandSize(BossBlind boss) {
        return boss == BossBlind.THE_TOWER ? 3 : Integer.MAX_VALUE;
    }

    public static int extraCardsOnPlay(BossBlind boss) {
        return switch (boss) {
            case THE_CLOUD -> 2;
            case THE_HOUSE -> 1;
            default -> 0;
        };
    }

    /**
     * Применяет постоянный debuff карты для босса.
     * Возвращает ту же карту, если правило на неё не действует.
     */
    public static Card applyPermanentDebuff(BossBlind boss, Card card) {
        if (boss == BossBlind.THE_WATER) {
            return card.debuffed(true);
        }
        if (boss == BossBlind.THE_SKILL && card.rank() == Rank.ACE) {
            return card.debuffed(true);
        }
        if (boss == BossBlind.THE_WINDOW
                && card.rank() == Rank.ACE && card.suit() == Suit.DIAMONDS) {
            return card.debuffed(true);
        }
        return card;
    }

    /** Debuff, действующий на карты в руке в начале раунда. */
    public static boolean isDebuffedInHand(BossBlind boss, Card card) {
        if (boss == BossBlind.THE_STONE && card.rank().isFace()) {
            return true;
        }
        return false;
    }
}