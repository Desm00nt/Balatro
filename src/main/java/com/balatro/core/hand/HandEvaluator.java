package com.balatro.core.hand;

import com.balatro.core.card.Card;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Определяет тип покерной комбинации по выбранным картам.
 *
 * <p>Поддерживает все комбинации Balatro, включая «покерные» расширения
 * (Five of a Kind, Flush House, Flush Five), которые появляются
 * благодаря джокерам и таро, меняющим масть или ранг.
 */
public final class HandEvaluator {

    private HandEvaluator() {
    }

    public static HandType evaluate(List<Card> selected) {
        if (selected == null || selected.isEmpty()) {
            return null;
        }
        int[] counts = countByOrder(selected);
        int distinct = distinctCount(counts);
        boolean flush = isFlush(selected);
        boolean straight = isStraight(counts, distinct);

        // 1. Пять одинаковых рангов — сильнее любого флеша.
        if (distinct == 1) {
            return HandType.FIVE_OF_A_KIND;
        }
        // 2. Прямая одной мастью.
        if (flush && straight) {
            return HandType.STRAIGHT_FLUSH;
        }
        // 3. Флеш-дом: флеш с раскладом 3+2.
        if (flush && hasThreeOfKind(counts) && hasPair(counts)) {
            return HandType.FLUSH_HOUSE;
        }
        // 4. Flush Five: пять и более карт одной масти без прямой и дома.
        //    Пять карт одной масти — это Flush Five, как в оригинале;
        //    Flush получается только при меньшем наборе (например, 3-4 карты).
        if (flush && selected.size() >= 5) {
            return HandType.FLUSH_FIVE;
        }
        if (flush) {
            return HandType.FLUSH;
        }
        if (hasFourOfKind(counts)) {
            return HandType.FOUR_OF_A_KIND;
        }
        if (hasThreeOfKind(counts) && hasPair(counts)) {
            return HandType.FULL_HOUSE;
        }
        if (straight) {
            return HandType.STRAIGHT;
        }
        if (hasThreeOfKind(counts)) {
            return HandType.THREE_OF_A_KIND;
        }
        if (countPairs(counts) == 2) {
            return HandType.TWO_PAIR;
        }
        if (hasPair(counts)) {
            return HandType.PAIR;
        }
        return HandType.HIGH_CARD;
    }

    /** Счётчик карт по рангу, индексированный от 2 до 14 (14 = Ace). */
    private static int[] countByOrder(List<Card> cards) {
        int[] counts = new int[15];
        for (Card c : cards) {
            counts[clampOrder(c.order())]++;
        }
        return counts;
    }

    private static int clampOrder(int order) {
        return Math.max(2, Math.min(14, order));
    }

    private static int distinctCount(int[] counts) {
        int n = 0;
        for (int i = 2; i <= 14; i++) {
            if (counts[i] > 0) {
                n++;
            }
        }
        return n;
    }

    /** Ровно две карты одного ранга (не тройка и не четвёрка). */
    private static int countPairs(int[] counts) {
        int pairs = 0;
        for (int i = 2; i <= 14; i++) {
            if (counts[i] == 2) {
                pairs++;
            }
        }
        return pairs;
    }

    private static boolean hasPair(int[] counts) {
        for (int i = 2; i <= 14; i++) {
            if (counts[i] == 2) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasThreeOfKind(int[] counts) {
        for (int i = 2; i <= 14; i++) {
            if (counts[i] == 3) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFourOfKind(int[] counts) {
        for (int i = 2; i <= 14; i++) {
            if (counts[i] >= 4) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFlush(List<Card> cards) {
        if (cards.size() < 3) {
            return false;
        }
        Suit first = cards.get(0).suit();
        for (Card c : cards) {
            if (c.suit() != first) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет наличие последовательности из пяти рангов.
     * Прямая может начинаться с любого ранга от 2 до 10, а также
     * «колесо» A-2-3-4-5 считается прямым.
     */
    private static boolean isStraight(int[] counts, int distinct) {
        if (distinct < 5) {
            return false;
        }
        for (int low = 2; low <= 10; low++) {
            if (isRun(counts, low)) {
                return true;
            }
        }
        // «Колесо»: A-2-3-4-5 считается прямым. Для него нужны ранги
        // 2,3,4,5 (четыре карты) плюс Ace, поэтому проверяем их отдельно.
        if (counts[14] > 0 && counts[2] > 0 && counts[3] > 0
                && counts[4] > 0 && counts[5] > 0) {
            return true;
        }
        return false;
    }

    /** Пять последовательных рангов начиная с {@code low} (2..10). */
    private static boolean isRun(int[] counts, int low) {
        for (int i = low; i < low + 5; i++) {
            if (counts[i] == 0) {
                return false;
            }
        }
        return true;
    }

    /** Группировка карт по рангу — используется джокерами (например DNA). */
    public static Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> map = new HashMap<>();
        for (Card c : cards) {
            map.computeIfAbsent(c.rank(), k -> new ArrayList<>()).add(c);
        }
        return map;
    }
}
