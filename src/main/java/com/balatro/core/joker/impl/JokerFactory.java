package com.balatro.core.joker.impl;

import com.balatro.core.card.Card;
import com.balatro.core.card.Suit;
import com.balatro.core.hand.HandType;
import com.balatro.core.joker.Joker;
import com.balatro.core.joker.JokerRarity;
import com.balatro.core.joker.ScoringContext;

/** Фабрика джокеров, зависящих от масти или типа комбинации. */
public final class JokerFactory {

    private JokerFactory() {
    }

    /** +Chips или +Mult за каждую карту заданной масти. */
    public static Joker suitJoker(String id, String name, Suit suit,
                                  int chips, int mult, JokerRarity rarity) {
        return new Joker() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String displayName() {
                return name;
            }

            @Override
            public String description() {
                return (chips > 0 ? "+" + chips + " Chips" : "+" + mult + " Mult")
                        + " per " + suit.displayName() + " played";
            }

            @Override
            public JokerRarity rarity() {
                return rarity;
            }

            @Override
            public int onCard(ScoringContext ctx, Card card) {
                return card.suit() == suit ? chips : 0;
            }

            @Override
            public int onCardMult(ScoringContext ctx, Card card) {
                return card.suit() == suit ? mult : 0;
            }
        };
    }

    /** Фишка/множитель за конкретный тип комбинации. */
    public static Joker comboJoker(String id, String name, HandType type,
                                   int chips, int mult, JokerRarity rarity, int price) {
        return new Joker() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String displayName() {
                return name;
            }

            @Override
            public String description() {
                return (chips > 0 ? "+" + chips + " Chips" : "+" + mult + " Mult")
                        + " if played hand is " + type.displayName();
            }

            @Override
            public JokerRarity rarity() {
                return rarity;
            }

            @Override
            public int basePrice() {
                return price;
            }

            @Override
            public int onHand(ScoringContext ctx) {
                return ctx.handType() == type ? chips : 0;
            }

            @Override
            public int onHandMult(ScoringContext ctx) {
                return ctx.handType() == type ? mult : 0;
            }
        };
    }
}
