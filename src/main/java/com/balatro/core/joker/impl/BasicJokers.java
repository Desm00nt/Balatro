package com.balatro.core.joker.impl;

import com.balatro.core.joker.Joker;
import com.balatro.core.joker.JokerRarity;
import com.balatro.core.joker.ScoringContext;

/** Базовые джокеры с постоянными бонусами. */
public final class BasicJokers {

    private BasicJokers() {
    }

    /** +4 множителя. */
    public static final Joker JOKER = new Joker() {
        @Override
        public String id() {
            return "joker";
        }

        @Override
        public String displayName() {
            return "Joker";
        }

        @Override
        public String description() {
            return "+4 Mult";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.COMMON;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 4;
        }
    };

    /** +23 множителя. */
    public static final Joker MISPRINT = new Joker() {
        @Override
        public String id() {
            return "misprint";
        }

        @Override
        public String displayName() {
            return "Misprint";
        }

        @Override
        public String description() {
            return "+23 Mult";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.COMMON;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 23;
        }
    };

    /** +20 множителя, если в руке 3 или меньше карт. */
    public static final Joker HALF = new Joker() {
        @Override
        public String id() {
            return "half";
        }

        @Override
        public String displayName() {
            return "Half Joker";
        }

        @Override
        public String description() {
            return "+20 Mult with 3 or fewer cards held";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.COMMON;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return ctx.held().size() <= 3 ? 20 : 0;
        }
    };

    /** +3 множителя за каждого джокера (включая себя). */
    public static final Joker ABSTRACT = new Joker() {
        @Override
        public String id() {
            return "abstract";
        }

        @Override
        public String displayName() {
            return "Abstract Joker";
        }

        @Override
        public String description() {
            return "+3 Mult for each Joker";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 3 * ctx.run().jokerCount();
        }
    };

    /** Удваивает множитель, если в руке ровно 5 карт. */
    public static final Joker JOKER_STENCIL = new Joker() {
        @Override
        public String id() {
            return "joker_stencil";
        }

        @Override
        public String displayName() {
            return "Joker Stencil";
        }

        @Override
        public String description() {
            return "x2 Mult with exactly 5 cards held";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return ctx.held().size() == 5 ? 2.0 : 1.0;
        }
    };

    /** +3 множителя за каждый сброс. */
    public static final Joker RED_CARD = new Joker() {
        @Override
        public String id() {
            return "red_card";
        }

        @Override
        public String displayName() {
            return "Red Card";
        }

        @Override
        public String description() {
            return "+3 Mult when discarded";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }
    };
}
