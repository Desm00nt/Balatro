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

    /**
     * Находит первую карту, равную указанной (по рангу и масти),
     * в колоде или в сбросе, и заменяет её новой.
     *
     * @return true, если замена выполнена
     */
    public boolean replace(Card target, Card replacement) {
        for (int i = 0; i < drawPile.size(); i++) {
            if (drawPile.get(i).equals(target)) {
                drawPile.set(i, replacement);
                return true;
            }
        }
        for (int i = 0; i < discardPile.size(); i++) {
            if (discardPile.get(i).equals(target)) {
                discardPile.set(i, replacement);
                return true;
            }
        }
        return false;
    }

    /** Убирает карту из колоды навсегда (таро Tower/Destroy). */
    public boolean remove(Card target) {
        int i = drawPile.indexOf(target);
        if (i >= 0) {
            drawPile.remove(i);
            return true;
        }
        int j = discardPile.indexOf(target);
        if (j >= 0) {
            discardPile.remove(j);
            return true;
        }
        return false;
    }

    /** Количество копий карты в колоде (издание Crowded). */
    public int count(Card target) {
        int n = 0;
        for (Card c : drawPile) {
            if (c.equals(target)) {
                n++;
            }
        }
        for (Card c : discardPile) {
            if (c.equals(target)) {
                n++;
            }
        }
        return n;
    }

    /** Все карты, кроме указанных (для таро, выбирающих случайную карту). */
    public List<Card> allCards() {
        List<Card> all = new ArrayList<>(drawPile);
        all.addAll(discardPile);
        return all;
    }

    /** Применяет преобразование к каждой карте (таро The Hanged Man). */
    public void transformAll(java.util.function.UnaryOperator<Card> op) {
        for (int i = 0; i < drawPile.size(); i++) {
            drawPile.set(i, op.apply(drawPile.get(i)));
        }
        for (int i = 0; i < discardPile.size(); i++) {
            discardPile.set(i, op.apply(discardPile.get(i)));
        }
    }

    /** Восстанавливает колоду из плоского списка (десериализация). */
    public void setAll(List<Card> cards) {
        drawPile.clear();
        discardPile.clear();
        drawPile.addAll(cards);
    }

    /** Прямой доступ к колоде при восстановлении из снимка. */
    public List<Card> getDrawForRestore() {
        return drawPile;
    }

    /** Прямой доступ к сбросу при восстановлении из снимка. */
    public List<Card> getDiscardForRestore() {
        return discardPile;
    }
}
