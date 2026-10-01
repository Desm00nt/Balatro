package com.balatro.core.joker;

/**
 * Пассивный джокер. Эффекты применяются в два этапа:
 * {@link #onCard} — для каждой сыгранной карты,
 * {@link #onHand} — один раз за сыгранную комбинацию.
 */
public interface Joker {

    String id();

    String displayName();

    String description();

    JokerRarity rarity();

    default int basePrice() {
        return 3;
    }

    /** Фишки за каждую сыгранную карту. */
    default int onCard(ScoringContext ctx, com.balatro.core.card.Card card) {
        return 0;
    }

    /** Множитель за каждую сыгранную карту. */
    default int onCardMult(ScoringContext ctx, com.balatro.core.card.Card card) {
        return 0;
    }

    /** Фишки один раз за комбинацию. */
    default int onHand(ScoringContext ctx) {
        return 0;
    }

    /** Множитель один раз за комбинацию. */
    default int onHandMult(ScoringContext ctx) {
        return 0;
    }

    /** Финальный множитель (перемножается, а не складывается). */
    default double onHandMultMultiplier(ScoringContext ctx) {
        return 1.0;
    }

    /** Вызывается в конце раунда (например, задержанная выгода). */
    default void onRoundEnd(RunView run) {
    }

    /** Срабатывает, когда джокер сбрасывается (Red Card). */
    default void onDiscard(RunView run) {
    }
}
