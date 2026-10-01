package com.balatro.core;

import com.balatro.core.card.Card;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;
import com.balatro.core.joker.Joker;
import com.balatro.core.run.RunState;
import com.balatro.core.run.ScoreResult;
import com.balatro.core.run.ScoringEngine;

import java.util.ArrayList;
import java.util.List;

/** Утилиты для тестов ядра. */
public final class ScoringHelper {

    private ScoringHelper() {
    }

    /** Подсчёт очков с пустым забегом (без джокеров в колоде). */
    public static ScoreResult score(List<Card> played, com.balatro.core.hand.HandType type,
                                   int level, List<Joker> jokers) {
        RunState run = new RunState(com.balatro.core.run.StartingDeck.RED, 42L);
        List<Card> held = new ArrayList<>();
        return ScoringEngine.score(played, held, type, level, jokers, run, 0);
    }

    public static Card c(Rank rank, Suit suit) {
        return new Card(rank, suit);
    }

    public static List<Card> cards(Card... cs) {
        return new ArrayList<>(List.of(cs));
    }
}
