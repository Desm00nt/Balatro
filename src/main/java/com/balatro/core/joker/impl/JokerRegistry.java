package com.balatro.core.joker.impl;

import com.balatro.core.card.Suit;
import com.balatro.core.hand.HandType;
import com.balatro.core.joker.Joker;
import com.balatro.core.joker.JokerRarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Реестр всех джокеров мода. */
public final class JokerRegistry {

    private static final Map<String, Joker> JOKERS = new LinkedHashMap<>();

    static {
        // Базовые
        register(BasicJokers.JOKER);
        register(BasicJokers.MISPRINT);
        register(BasicJokers.HALF);
        register(BasicJokers.ABSTRACT);
        register(BasicJokers.JOKER_STENCIL);
        register(BasicJokers.RED_CARD);
        // По масти
        register(JokerFactory.suitJoker("greedy", "Greedy Joker", Suit.DIAMONDS, 3, 0,
                JokerRarity.UNCOMMON));
        register(JokerFactory.suitJoker("lusty", "Lusty Joker", Suit.HEARTS, 0, 2,
                JokerRarity.UNCOMMON));
        register(JokerFactory.suitJoker("wrathful", "Wrathful Joker", Suit.CLUBS, 3, 0,
                JokerRarity.UNCOMMON));
        register(JokerFactory.suitJoker("gluttonous", "Gluttonous Joker", Suit.SPADES, 0, 2,
                JokerRarity.UNCOMMON));
        // По комбинации: +Mult
        register(JokerFactory.comboJoker("jolly", "Jolly Joker", HandType.PAIR, 0, 8,
                JokerRarity.COMMON, 3));
        register(JokerFactory.comboJoker("mad", "Mad Joker", HandType.TWO_PAIR, 0, 10,
                JokerRarity.UNCOMMON, 4));
        register(JokerFactory.comboJoker("zany", "Zany Joker", HandType.THREE_OF_A_KIND, 0, 12,
                JokerRarity.UNCOMMON, 4));
        register(JokerFactory.comboJoker("crazy", "Crazy Joker", HandType.STRAIGHT, 0, 12,
                JokerRarity.RARE, 4));
        register(JokerFactory.comboJoker("droll", "Droll Joker", HandType.FLUSH, 0, 10,
                JokerRarity.RARE, 4));
        // По комбинации: +Chips
        register(JokerFactory.comboJoker("sly", "Sly Joker", HandType.PAIR, 50, 0,
                JokerRarity.UNCOMMON, 3));
        register(JokerFactory.comboJoker("clever", "Clever Joker", HandType.TWO_PAIR, 80, 0,
                JokerRarity.RARE, 4));
        register(JokerFactory.comboJoker("wily", "Wily Joker", HandType.THREE_OF_A_KIND, 100, 0,
                JokerRarity.RARE, 4));
        register(JokerFactory.comboJoker("devious", "Devious Joker", HandType.STRAIGHT, 100, 0,
                JokerRarity.RARE, 4));
        register(JokerFactory.comboJoker("crafty", "Crafty Joker", HandType.FLUSH, 80, 0,
                JokerRarity.RARE, 4));
    }

    private JokerRegistry() {
    }

    public static void register(Joker joker) {
        JOKERS.put(joker.id(), joker);
    }

    public static Collection<Joker> all() {
        return JOKERS.values();
    }

    public static Joker byId(String id) {
        return JOKERS.get(id);
    }

    /** Случайный джокер с учётом веса редкости. */
    public static Joker randomJoker(Random random) {
        return randomJokerUpTo(random, JokerRarity.LEGENDARY);
    }

    /** Случайный джокер не выше указанной редкости. */
    public static Joker randomJokerUpTo(Random random, JokerRarity maxRarity) {
        List<Joker> pool = new ArrayList<>();
        int total = 0;
        for (Joker j : JOKERS.values()) {
            if (j.rarity().tier() <= maxRarity.tier()) {
                pool.add(j);
                total += j.rarity().shopWeight();
            }
        }
        if (pool.isEmpty()) {
            return BasicJokers.JOKER;
        }
        int roll = random.nextInt(Math.max(1, total));
        for (Joker j : pool) {
            roll -= j.rarity().shopWeight();
            if (roll < 0) {
                return j;
            }
        }
        return pool.get(pool.size() - 1);
    }
}
