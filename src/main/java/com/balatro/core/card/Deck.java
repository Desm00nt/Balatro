package com.balatro.core.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Колода карт: колода, сброс и перемешивание. */
public final class Deck {

    private final List<Card> drawPile = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();
    private final Random random;

    public Deck(Random random) {
        this.random = random;
    }

    public Deck() {
        this(new Random());
    }

    /** Полная стандартная колода из 52 карт. */
    public static Deck standard(Random random) {
        Deck deck = new Deck(random);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.drawPile.add(new Card(rank, suit));
            }
        }
        deck.shuffle();
        return deck;
    }

    /** Стартовая колода «Abandoned» — без карт с картинками. */
    public static Deck abandoned(Random random) {
        Deck deck = new Deck(random);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                if (!rank.isFace()) {
                    deck.drawPile.add(new Card(rank, suit));
                }
            }
        }
        deck.shuffle();
        return deck;
    }

    public void shuffle() {
        Collections.shuffle(drawPile, random);
    }

    /** Добавляет карту в колоду (например, купленная в боастере). */
    public void addToDeck(Card card) {
        drawPile.add(card);
        shuffle();
    }

    public List<Card> draw(int count) {
        List<Card> drawn = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (drawPile.isEmpty()) {
                reshuffle();
            }
            if (drawPile.isEmpty()) {
                break;
            }
            drawn.add(drawPile.remove(drawPile.size() - 1));
        }
        return drawn;
    }

    public void discard(List<Card> cards) {
        discardPile.addAll(cards);
    }

    /** Возвращает отброшенные карты в колоду и перемешивает. */
    public void reshuffle() {
        if (discardPile.isEmpty()) {
            return;
        }
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    public int remaining() {
        return drawPile.size();
    }

    public List<Card> drawPileView() {
        return Collections.unmodifiableList(drawPile);
    }

    public List<Card> discardPileView() {
        return Collections.unmodifiableList(discardPile);
    }

    public int totalSize() {
        return drawPile.size() + discardPile.size();
    }
}
