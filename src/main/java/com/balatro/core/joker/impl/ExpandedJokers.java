package com.balatro.core.joker.impl;

import com.balatro.core.card.Card;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;
import com.balatro.core.hand.HandType;
import com.balatro.core.joker.Joker;
import com.balatro.core.joker.JokerRarity;
import com.balatro.core.joker.RunView;
import com.balatro.core.joker.ScoringContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Расширенный набор джокеров Balatro: 30 карт, воспроизводящих поведение
 * оригинальных джокеров в рамках доступного {@link Joker} API.
 *
 * <p>Ключи счётчиков передаются в {@code RunView.triggerCount} строковыми
 * литералами — движок забега сам инкрементирует их (за сбросы, таро или планеты):
 * {@code faceless_discards}, {@code diamond_discards}, {@code stone_discards},
 * {@code planets_used}, {@code tarots_used}.
 *
 * <p>Джокеры, накапливающие состояние между раундами ({@code green_rounds},
 * {@code cloud9_hands}, {@code rocket_discards}, {@code moon_income},
 * {@code popcorn_decay}), ведут собственный счётчик в экземпляре: {@link RunView}
 * доступен только для чтения и не содержит мутаторов.
 */
public final class ExpandedJokers {

    private ExpandedJokers() {
    }

    /** Базовая величина сбросов за раунд — используется для «оставшихся сбросов». */
    private static final int DISCARDS_PER_ROUND = 4;

    /** Нижняя граница множителя: ни один джокер не уходит в ноль или отрицательные значения. */
    private static final double MIN_MULTIPLIER = 0.05;

    private static final List<Joker> ALL = new ArrayList<>();

    private static void add(Joker j) {
        ALL.add(j);
    }

    // --- Вспомогательные функции (безопасная работа с нулевым RunView) ---

    /** Число срабатываний счётчика забега; 0, если вида нет. */
    private static int trigger(ScoringContext ctx, String key) {
        RunView run = ctx.run();
        return run == null ? 0 : Math.max(0, run.triggerCount(key));
    }

    /** Номер текущего раунда (слепой). */
    private static int roundNumber(ScoringContext ctx) {
        RunView run = ctx.run();
        return run == null ? 0 : Math.max(0, run.round());
    }

    /** Текущий анте. */
    private static int anteNumber(ScoringContext ctx) {
        RunView run = ctx.run();
        return run == null ? 0 : Math.max(0, run.ante());
    }

    /** Сколько сбросов ещё осталось в раунде (не уходит в отрицательные значения). */
    private static int discardsLeft(ScoringContext ctx) {
        return Math.max(0, DISCARDS_PER_ROUND - Math.max(0, ctx.discardsUsed()));
    }

    /** Множитель не ниже {@link #MIN_MULTIPLIER}. */
    private static double mult(double value) {
        return Math.max(MIN_MULTIPLIER, value);
    }

    /** Есть ли среди сыгранных карт пять последовательных рангов (включая A-2-3-4-5). */
    private static boolean hasStraight(List<Card> played) {
        if (played.size() < 5) {
            return false;
        }
        boolean[] seen = new boolean[15];
        for (Card c : played) {
            int order = c.order();
            if (order >= 2 && order <= 14) {
                seen[order] = true;
            }
        }
        int consecutive = 0;
        for (int order = 2; order <= 14; order++) {
            consecutive = seen[order] ? consecutive + 1 : 0;
            if (consecutive >= 5) {
                return true;
            }
        }
        return seen[14] && seen[2] && seen[3] && seen[4] && seen[5];
    }

    /** Есть ли среди сыгранных карт карта-карта (J/Q/K). */
    private static boolean hasFaceCard(List<Card> played) {
        for (Card c : played) {
            if (c.rank().isFace()) {
                return true;
            }
        }
        return false;
    }

    // --- Джокеры ---

    /** Blue Joker: +2 Chips за каждый оставшийся сброс. */
    private static final Joker BLUE_JOKER = new Joker() {
        @Override
        public String id() {
            return "blue_joker";
        }

        @Override
        public String displayName() {
            return "Blue Joker";
        }

        @Override
        public String description() {
            return "+2 Chips per discard remaining";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onHand(ScoringContext ctx) {
            return 2 * discardsLeft(ctx);
        }
    };

    /** Sixth Sense: x6 Mult, если сыграно ровно 3 карты. */
    private static final Joker SIXTH_SENSE = new Joker() {
        @Override
        public String id() {
            return "sixth_sense";
        }

        @Override
        public String displayName() {
            return "Sixth Sense";
        }

        @Override
        public String description() {
            return "x6 Mult if played hand is exactly 3 cards";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return ctx.played().size() == 3 ? 6.0 : 1.0;
        }
    };

    /** Spade Traps: +10 Mult за каждую оставленную в руке пику. */
    private static final Joker SPADE_TRAPS = new Joker() {
        @Override
        public String id() {
            return "spade_traps";
        }

        @Override
        public String displayName() {
            return "Spade Traps";
        }

        @Override
        public String description() {
            return "+10 Mult per Spade held";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 10 * (int) ctx.heldSuitCount(Suit.SPADES);
        }
    };

    /** Faceless Joker: +5 Mult за каждую сброшенную карту-карту за забег. */
    private static final Joker FACELESS = new Joker() {
        @Override
        public String id() {
            return "faceless";
        }

        @Override
        public String displayName() {
            return "Faceless Joker";
        }

        @Override
        public String description() {
            return "+5 Mult per discarded face card this run";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 5 * trigger(ctx, "faceless_discards");
        }
    };

    /** Cardcaptor: x2 Mult, если ранги содержат стрит, иначе +2 Mult за сброс. */
    private static final Joker CARDCAPTOR = new Joker() {
        @Override
        public String id() {
            return "cardcaptor";
        }

        @Override
        public String displayName() {
            return "Cardcaptor";
        }

        @Override
        public String description() {
            return "x2 Mult if played ranks hold a straight, else +2 Mult per discard";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return hasStraight(ctx.played()) ? 0 : 2 * Math.max(0, ctx.discardsUsed());
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return hasStraight(ctx.played()) ? 2.0 : 1.0;
        }
    };
    /** Blackboard: x3 Mult, если все 3+ сыгранные карты — пики или трефы. */
    private static final Joker BLACKBOARD = new Joker() {
        @Override
        public String id() {
            return "blackboard";
        }

        @Override
        public String displayName() {
            return "Blackboard";
        }

        @Override
        public String description() {
            return "x3 Mult if all played cards are Spades or Clubs";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            List<Card> played = ctx.played();
            if (played.size() < 3) {
                return 1.0;
            }
            for (Card c : played) {
                if (c.suit() != Suit.SPADES && c.suit() != Suit.CLUBS) {
                    return 1.0;
                }
            }
            return 3.0;
        }
    };

    /** Constellation: x0.1 Mult за каждую использованную карту-планету. */
    private static final Joker CONSTELLATION = new Joker() {
        @Override
        public String id() {
            return "constellation";
        }

        @Override
        public String displayName() {
            return "Constellation";
        }

        @Override
        public String description() {
            return "x0.1 Mult for every Planet card used";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return mult(1.0 + 0.1 * trigger(ctx, "planets_used"));
        }
    };

    /** Green Joker: +1 Hand, -1 Discard, +25 Mult за каждый сыгранный раунд. */
    private static final Joker GREEN_JOKER = new Joker() {
        /** Завершённые раунды: RunView только для чтения, поэтому счётчик свой. */
        private int roundsPlayed;

        @Override
        public String id() {
            return "green_joker";
        }

        @Override
        public String displayName() {
            return "Green Joker";
        }

        @Override
        public String description() {
            return "+25 Mult per round played, +1 Hand, -1 Discard";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 8;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 25 * Math.max(roundNumber(ctx), roundsPlayed);
        }

        @Override
        public void onRoundEnd(RunView run) {
            roundsPlayed++;
        }
    };

    /** Superposition: x1.5 Mult на Pair, иначе +4 Mult. */
    private static final Joker SUPERPOSITION = new Joker() {
        @Override
        public String id() {
            return "superposition";
        }

        @Override
        public String displayName() {
            return "Superposition";
        }

        @Override
        public String description() {
            return "x1.5 Mult if played hand is Pair, else +4 Mult";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return ctx.handType() == HandType.PAIR ? 0 : 4;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return ctx.handType() == HandType.PAIR ? 1.5 : 1.0;
        }
    };

    /** Lucky Mul: x3 Mult, если фишки + множитель сыгранных карт дают ровно 7. */
    private static final Joker LUCKY_MUL = new Joker() {
        @Override
        public String id() {
            return "lucky_mul";
        }

        @Override
        public String displayName() {
            return "Lucky Mul";
        }

        @Override
        public String description() {
            return "x3 Mult if total chips + mult of played cards is exactly 7";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            int total = 0;
            for (Card c : ctx.played()) {
                total += c.chips() + c.mult();
            }
            return total == 7 ? 3.0 : 1.0;
        }
    };
/** Cavendish: x3 Mult, если среди сыгранных карт есть карта-карта. */
    private static final Joker CAVENDISH = new Joker() {
        @Override
        public String id() {
            return "cavendish";
        }

        @Override
        public String displayName() {
            return "Cavendish";
        }

        @Override
        public String description() {
            return "x3 Mult if played hand contains a face card";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return hasFaceCard(ctx.played()) ? 3.0 : 1.0;
        }
    };

    /** Pietra: x1.5/x2/x3 Mult за 5/6/7 сыгранных карт. */
    private static final Joker PIETRA = new Joker() {
        @Override
        public String id() {
            return "pietra";
        }

        @Override
        public String displayName() {
            return "Pietra";
        }

        @Override
        public String description() {
            return "x1.5 Mult with 5 cards, x2 with 6, x3 with 7";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return mult(switch (ctx.played().size()) {
                case 5 -> 1.5;
                case 6 -> 2.0;
                case 7 -> 3.0;
                default -> 1.0;
            });
        }
    };

    /** To the Moon: дополнительный доход (x0.2) и +5 Mult за комбинацию. */
    private static final Joker TO_THE_MOON = new Joker() {
        /** Сколько раз сработал бонус «+1 $ за каждые 5 $». */
        private int incomeEvents;

        @Override
        public String id() {
            return "to_the_moon";
        }

        @Override
        public String displayName() {
            return "To the Moon";
        }

        @Override
        public String description() {
            return "Extra $1 interest per $5, +5 Mult per played hand";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 5;
        }

        @Override
        public void onRoundEnd(RunView run) {
            if (run != null && run.money() > 0) {
                // По одному «событию» дохода на каждые полные 5 долларов.
                incomeEvents += Math.max(1, run.money() / 5);
            }
        }
    };

    /** Batman: x2 Mult, растущий на +0.25 за каждый сброс. */
    private static final Joker BATMAN = new Joker() {
        private static final double BASE_MULTIPLIER = 2.0;
        private static final double PER_DISCARD = 0.25;

        @Override
        public String id() {
            return "batman";
        }

        @Override
        public String displayName() {
            return "Batman";
        }

        @Override
        public String description() {
            return "x2 Mult, +0.25 Mult more for each discard used";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 8;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return mult(BASE_MULTIPLIER + PER_DISCARD * Math.max(0, ctx.discardsUsed()));
        }
    };

    /** Hologram: x0.25 Mult за сброс и +10 Mult за тот же сброс. */
    private static final Joker HOLOGRAM = new Joker() {
        private static final double MULT_PER_DISCARD = 0.25;
        private static final int BONUS_MULT_PER_DISCARD = 10;

        @Override
        public String id() {
            return "hologram";
        }

        @Override
        public String displayName() {
            return "Hologram";
        }

        @Override
        public String description() {
            return "x0.25 Mult per discard, +10 Mult per discard";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 7;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return BONUS_MULT_PER_DISCARD * Math.max(0, ctx.discardsUsed());
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            return mult(1.0 + MULT_PER_DISCARD * Math.max(0, ctx.discardsUsed()));
        }
    };

    /** Vampire: +3 Mult за каждый сделанный сброс. */
    private static final Joker VAMPIRE = new Joker() {
        private static final int MULT_PER_DISCARD = 3;

        @Override
        public String id() {
            return "vampire";
        }

        @Override
        public String displayName() {
            return "Vampire";
        }

        @Override
        public String description() {
            return "+3 Mult per discard used";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return MULT_PER_DISCARD * Math.max(0, ctx.discardsUsed());
        }
    };
/** Shortcut: +3 Mult за каждую карту минимального ранга в комбинации. */
    private static final Joker SHORTCUT = new Joker() {
        private static final int MULT_PER_LOWEST_CARD = 3;

        @Override
        public String id() {
            return "shortcut";
        }

        @Override
        public String displayName() {
            return "Shortcut";
        }

        @Override
        public String description() {
            return "+3 Mult per card of the lowest rank in the played hand";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onCardMult(ScoringContext ctx, Card card) {
            int lowest = ctx.lowestPlayedOrder();
            return lowest > 0 && card.order() == lowest ? MULT_PER_LOWEST_CARD : 0;
        }
    };

    /** Cloud 9: +1 Hand в начале каждого раунда, пока бонус не потрачен. */
    private static final Joker CLOUD9 = new Joker() {
        /** Неиспользованные дополнительные руки (ключ "cloud9_hands"). */
        private int bonusHands;

        @Override
        public String id() {
            return "cloud9";
        }

        @Override
        public String displayName() {
            return "Cloud 9";
        }

        @Override
        public String description() {
            return "+1 Hand at the start of each round, +3 Mult while held";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return bonusHands > 0 ? 3 : 0;
        }

        @Override
        public void onRoundEnd(RunView run) {
            if (bonusHands > 0) {
                bonusHands--;
            }
            bonusHands++;
        }
    };

    /** Rocket Man: +1 Discard в начале каждого раунда, пока бонус не потрачен. */
    private static final Joker ROCKET_MAN = new Joker() {
        /** Неиспользованные дополнительные сбросы (ключ "rocket_discards"). */
        private int bonusDiscards;

        @Override
        public String id() {
            return "rocket_man";
        }

        @Override
        public String displayName() {
            return "Rocket Man";
        }

        @Override
        public String description() {
            return "+1 Discard at the start of each round, +3 Mult while held";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return bonusDiscards > 0 ? 3 : 0;
        }

        @Override
        public void onRoundEnd(RunView run) {
            if (bonusDiscards > 0) {
                bonusDiscards--;
            }
            bonusDiscards++;
        }
    };

    /** Sock and Buskin: +2 Chips за каждый оставшийся сброс. */
    private static final Joker SOCK_AND_BUSKIN = new Joker() {
        @Override
        public String id() {
            return "sock_and_buskin";
        }

        @Override
        public String displayName() {
            return "Sock and Buskin";
        }

        @Override
        public String description() {
            return "+2 Chips per discard remaining";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.COMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onHand(ScoringContext ctx) {
            return 2 * discardsLeft(ctx);
        }
    };
/** Spaceship: +4 Mult за каждую сыгранную карту ранга 5 или ниже. */
    private static final Joker SPACESHIP = new Joker() {
        private static final int MAX_RANK_ORDER = 5;
        private static final int MULT_PER_CARD = 4;

        @Override
        public String id() {
            return "spaceship";
        }

        @Override
        public String displayName() {
            return "Spaceship";
        }

        @Override
        public String description() {
            return "+4 Mult for each played card of rank 5 or lower";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onCardMult(ScoringContext ctx, Card card) {
            int order = card.order();
            return order > 0 && order <= MAX_RANK_ORDER ? MULT_PER_CARD : 0;
        }
    };

    /** Banana: +2 Chips, но отрицательные, пока не набрано 10 сбросов. */
    private static final Joker BANANA = new Joker() {
        private static final int GOOD_CHIPS = 2;
        private static final int BREAK_EVEN_DISCARDS = 10;

        @Override
        public String id() {
            return "banana";
        }

        @Override
        public String displayName() {
            return "Banana";
        }

        @Override
        public String description() {
            return "+2 Chips, negative until 10 discards total";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.COMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onHand(ScoringContext ctx) {
            int used = Math.max(Math.max(0, ctx.discardsUsed()),
                    trigger(ctx, "banana_discards"));
            if (used >= BREAK_EVEN_DISCARDS) {
                return GOOD_CHIPS;
            }
            return GOOD_CHIPS - (BREAK_EVEN_DISCARDS - used);
        }
    };

    /** Popcorn: x1.5 Mult, слабеет на 0.1 за каждый завершённый раунд. */
    private static final Joker POPCORN = new Joker() {
        private static final double BASE_MULTIPLIER = 1.5;
        private static final double DECAY_PER_ROUND = 0.1;

        /** Количество завершённых раундов (ключ "popcorn_decay"). */
        private int decay;

        @Override
        public String id() {
            return "popcorn";
        }

        @Override
        public String displayName() {
            return "Popcorn";
        }

        @Override
        public String description() {
            return "x1.5 Mult, decays 0.1 each round (never below x1)";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            int rounds = Math.max(decay, trigger(ctx, "popcorn_decay"));
            return mult(Math.max(1.0, BASE_MULTIPLIER - DECAY_PER_ROUND * rounds));
        }

        @Override
        public void onRoundEnd(RunView run) {
            decay++;
        }
    };

    /** Turtle Bean: +5 Chips за каждый оставшийся сброс. */
    private static final Joker TURTLE_BEAN = new Joker() {
        private static final int CHIPS_PER_DISCARD_LEFT = 5;

        @Override
        public String id() {
            return "turtle_bean";
        }

        @Override
        public String displayName() {
            return "Turtle Bean";
        }

        @Override
        public String description() {
            return "+5 Chips per discard remaining";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.COMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHand(ScoringContext ctx) {
            return CHIPS_PER_DISCARD_LEFT * discardsLeft(ctx);
        }
    };

    /** Trance: +10 Mult за каждую использованную карту-таро. */
    private static final Joker TRANCE = new Joker() {
        private static final int MULT_PER_TAROT = 10;

        @Override
        public String id() {
            return "trance";
        }

        @Override
        public String displayName() {
            return "Trance";
        }

        @Override
        public String description() {
            return "+10 Mult per Tarot card used this run";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return MULT_PER_TAROT * trigger(ctx, "tarots_used");
        }
    };

/** Horus: +6 Chips за каждый сыгранный бубен, x4 при 5+ бубнах. */
    private static final Joker HORUS = new Joker() {
        private static final int CHIPS_PER_DIAMOND = 6;
        private static final int DIAMONDS_FOR_BOOST = 5;
        private static final int BOOST = 4;

        @Override
        public String id() {
            return "horus";
        }

        @Override
        public String displayName() {
            return "Horus";
        }

        @Override
        public String description() {
            return "+6 Chips per Diamond played, x4 with 5 or more";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onCard(ScoringContext ctx, Card card) {
            if (card.suit() != Suit.DIAMONDS) {
                return 0;
            }
            return ctx.playedSuitCount(Suit.DIAMONDS) >= DIAMONDS_FOR_BOOST
                    ? CHIPS_PER_DIAMOND * BOOST
                    : CHIPS_PER_DIAMOND;
        }
    };

    /** Diamond Joker: +3 Mult за каждый сброшенный бубен за забег. */
    private static final Joker DIAMOND_JOKER = new Joker() {
        @Override
        public String id() {
            return "diamond_joker";
        }

        @Override
        public String displayName() {
            return "Diamond Joker";
        }

        @Override
        public String description() {
            return "+3 Mult per Diamond discarded this run";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return 3 * trigger(ctx, "diamond_discards");
        }
    };

    /** Stone Joker: +25 Chips за первый сброс, затем по +5 за каждый следующий. */
    private static final Joker STONE_JOKER = new Joker() {
        private static final int FIRST_DISCARD_CHIPS = 25;
        private static final int EXTRA_CHIPS_PER_DISCARD = 5;

        @Override
        public String id() {
            return "stone_joker";
        }

        @Override
        public String displayName() {
            return "Stone Joker";
        }

        @Override
        public String description() {
            return "+25 Chips for the 1st discard, +5 Chips for each next one";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 5;
        }

        @Override
        public int onHand(ScoringContext ctx) {
            int discards = Math.max(trigger(ctx, "stone_discards"), ctx.discardsUsed());
            if (discards <= 0) {
                return 0;
            }
            return FIRST_DISCARD_CHIPS + EXTRA_CHIPS_PER_DISCARD * (discards - 1);
        }
    };

    /** Wild Joker: +5 Mult за каждого сыгранного туза (туз считается «дикой» картой). */
    private static final Joker WILD_JOKER = new Joker() {
        @Override
        public String id() {
            return "wild_joker";
        }

        @Override
        public String displayName() {
            return "Wild Joker";
        }

        @Override
        public String description() {
            return "+5 Mult per wild card (Ace) played";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.UNCOMMON;
        }

        @Override
        public int basePrice() {
            return 4;
        }

        @Override
        public int onCardMult(ScoringContext ctx, Card card) {
            return card.rank() == Rank.ACE ? 5 : 0;
        }
    };

    /** Gros Michel: +15 Mult, x1.5 на 2-4 анте. */
    private static final Joker GROS_MICHEL = new Joker() {
        private static final int BASE_MULT = 15;
        private static final int ANTE_BONUS = 4;

        @Override
        public String id() {
            return "gros_michel";
        }

        @Override
        public String displayName() {
            return "Gros Michel";
        }

        @Override
        public String description() {
            return "+15 Mult, x1.5 Mult at Ante 2, 3 or 4";
        }

        @Override
        public JokerRarity rarity() {
            return JokerRarity.RARE;
        }

        @Override
        public int basePrice() {
            return 6;
        }

        @Override
        public int onHandMult(ScoringContext ctx) {
            return BASE_MULT;
        }

        @Override
        public double onHandMultMultiplier(ScoringContext ctx) {
            int ante = anteNumber(ctx);
            return ante >= 2 && ante <= ANTE_BONUS ? 1.5 : 1.0;
        }
    };
    static {
        add(BLUE_JOKER);
        add(SIXTH_SENSE);
        add(SPADE_TRAPS);
        add(FACELESS);
        add(CARDCAPTOR);
        add(BLACKBOARD);
        add(CONSTELLATION);
        add(GREEN_JOKER);
        add(SUPERPOSITION);
        add(LUCKY_MUL);
        add(HORUS);
        add(DIAMOND_JOKER);
        add(STONE_JOKER);
        add(WILD_JOKER);
        add(GROS_MICHEL);
        add(CAVENDISH);
        add(PIETRA);
        add(TO_THE_MOON);
        add(BATMAN);
        add(HOLOGRAM);
        add(VAMPIRE);
        add(SHORTCUT);
        add(CLOUD9);
        add(ROCKET_MAN);
        add(SOCK_AND_BUSKIN);
        add(SPACESHIP);
        add(BANANA);
        add(POPCORN);
        add(TURTLE_BEAN);
        add(TRANCE);
    }

    /** Все расширенные джокеры в порядке регистрации. */
    public static List<Joker> all() {
        return Collections.unmodifiableList(ALL);
    }
}
