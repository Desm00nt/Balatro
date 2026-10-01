package com.balatro.core.consumable;

/** Тексты эффектов таро-карт для интерфейса. */
public final class TarotEffects {

    private TarotEffects() {
    }

    public static String effect(TarotCard card) {
        return switch (card) {
            case THE_FOOL -> "Creates the last Tarot or Planet card used";
            case THE_MAGICIAN -> "Enhances 2 selected cards: +10 Chips, +20 Mult";
            case THE_HIGH_PRIESTESS -> "Creates 2 random Planet cards";
            case THE_EMPRESS -> "Enhances up to 2 selected cards with +3 Mult each";
            case THE_EMPEROR -> "Creates 2 Vouchers";
            case THE_HIEROPHANT -> "+20 Mult to a card, then adds 2 copies to the deck";
            case THE_LOVERS -> "1 selected card is converted to Hearts";
            case THE_CHARIOT -> "1 selected card is converted to Diamonds";
            case JUSTICE -> "1 selected card is converted to Clubs";
            case THE_HERMIT -> "Doubles money (max $20)";
            case THE_WHEEL_OF_FORTUNE -> "1 of 4: +50 Chips, +$1, +5 hands, +1 discard";
            case STRENGTH -> "Enhances up to 2 selected cards with +25 Chips each";
            case THE_HANGED_MAN -> "Reverses the suits of all cards in the deck";
            case DEATH -> "Select 2 cards: the left takes the rank of the right";
            case TEMPERANCE -> "Gives the total sell value of all Jokers";
            case THE_DEVIL -> "1 selected card becomes Gold Seal";
            case THE_TOWER -> "Destroys up to 3 selected cards";
            case THE_STAR -> "Adds 3 random Tarot cards to the deck";
            case THE_MOON -> "Adds 3 random Planet cards to the deck";
            case THE_SUN -> "Converts 1 selected card into a Tarot card";
            case JUDGEMENT -> "1 selected card becomes Holographic";
            case THE_WORLD -> "Adds a Legendary Joker to the shop";
        };
    }
}