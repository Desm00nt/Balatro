package com.balatro.core.joker;

/**
 * Представление состояния забега, доступное джокерам.
 * Чтение состояния и запись счётчиков/денег.
 */
public interface RunView {

    int money();

    int handsLeft();

    int discardsLeft();

    int round();

    int ante();

    int jokerCount();

    /** Сколько раз был сыгран данный тип комбинации за забег. */
    int timesPlayed(com.balatro.core.hand.HandType type);

    /** Счётчик срабатываний конкретного джокера (для масштабируемых эффектов). */
    int triggerCount(String jokerId);

    /** Увеличивает счётчик на {@code delta}. Нужно масштабируемым джокерам. */
    void bumpTrigger(String jokerId, int delta);

    /** Сбрасывает счётчик в ноль. */
    void clearTrigger(String jokerId);

    /** Сколько карт сейчас в руке. */
    int handSizeNow();

    /** Размер колоды без учёта карт в руке. */
    int deckSize();

    /** Изменяет количество денег (может быть отрицательным). */
    void addMoney(int amount);

    /** Сколько копий указанной карты в колоде (издание Crowded). */
    int copiesOf(com.balatro.core.card.Card card);
}
