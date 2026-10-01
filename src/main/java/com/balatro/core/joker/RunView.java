package com.balatro.core.joker;

/** Представление состояния забега, доступное джокерам (read-only). */
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
}
