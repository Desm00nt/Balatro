package com.balatro.core.joker;

import com.balatro.core.card.Card;
import com.balatro.core.hand.HandType;

import java.util.List;

/**
 * Контекст подсчёта очков, который передаётся джокерам.
 * Позволяет джокерам менять фишки, множитель и выбирать карты.
 */
public final class ScoringContext {

    private final List<Card> played;
    private final List<Card> held;
    private final HandType handType;
    private final int handLevel;
    private final int discardsUsed;
    private final RunView run;

    public ScoringContext(List<Card> played, List<Card> held, HandType handType,
                          int handLevel, int discardsUsed, RunView run) {
        this.played = played;
        this.held = held;
        this.handType = handType;
        this.handLevel = handLevel;
        this.discardsUsed = discardsUsed;
        this.run = run;
    }

    public List<Card> played() {
        return played;
    }

    /** Карты, оставшиеся в руке (в Balatra — «held»). */
    public List<Card> held() {
        return held;
    }

    public HandType handType() {
        return handType;
    }

    public int handLevel() {
        return handLevel;
    }

    public int discardsUsed() {
        return discardsUsed;
    }

    public RunView run() {
        return run;
    }

    /** Количество карт конкретного типа масти среди сыгранных. */
    public long playedSuitCount(com.balatro.core.card.Suit suit) {
        return played.stream().filter(c -> c.suit() == suit).count();
    }

    public long heldSuitCount(com.balatro.core.card.Suit suit) {
        return held.stream().filter(c -> c.suit() == suit).count();
    }

    /**
     * Минимальный ранг среди сыгранных карт (в порядке 2..14).
     * Используется джокером «Mad World»-подобными эффектами.
     */
    public int lowestPlayedOrder() {
        int min = Integer.MAX_VALUE;
        for (Card c : played) {
            min = Math.min(min, c.order());
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }
}
