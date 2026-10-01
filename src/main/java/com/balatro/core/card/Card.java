package com.balatro.core.card;

/**
 * Игровая карта. Помимо ранга и масти может нести модификаторы
 * (бонусные фишки/множитель), полученные от таро-карт, и гейминг-эффекты.
 *
 * <p>Класс неизменяемый: любой апгрейд возвращает новый экземпляр.
 */
public final class Card {

    private final Rank rank;
    private final Suit suit;
    private final int bonusChips;
    private final int bonusMult;
    /** Эффекты, накопленные от таро (например "при выборе: +4 фишки"). */
    private final String[] bonusEffects;
    private final boolean debuffed;
    /** Аура-реакция на конкретный ранг/масть, задаваемая джокером. */
    private final int forcedRankOrder;
    private final CardEdition edition;
    private final CardSeal seal;

    public Card(Rank rank, Suit suit) {
        this(rank, suit, 0, 0, new String[0], false, 0,
                CardEdition.NONE, CardSeal.NONE);
    }

    public Card(Rank rank, Suit suit, int bonusChips, int bonusMult,
                String[] bonusEffects, boolean debuffed, int forcedRankOrder) {
        this(rank, suit, bonusChips, bonusMult, bonusEffects, debuffed, forcedRankOrder,
                CardEdition.NONE, CardSeal.NONE);
    }

    public Card(Rank rank, Suit suit, int bonusChips, int bonusMult,
                String[] bonusEffects, boolean debuffed, int forcedRankOrder,
                CardEdition edition, CardSeal seal) {
        this.rank = rank;
        this.suit = suit;
        this.bonusChips = bonusChips;
        this.bonusMult = bonusMult;
        this.bonusEffects = bonusEffects;
        this.debuffed = debuffed;
        this.forcedRankOrder = forcedRankOrder;
        this.edition = edition == null ? CardEdition.NONE : edition;
        this.seal = seal == null ? CardSeal.NONE : seal;
    }

    /** Копия карты с другим изданием/печатью. */
    private Card with(CardEdition newEdition, CardSeal newSeal) {
        return new Card(rank, suit, bonusChips, bonusMult, bonusEffects, debuffed,
                forcedRankOrder, newEdition, newSeal);
    }

    public CardEdition edition() {
        return edition;
    }

    public CardSeal seal() {
        return seal;
    }

    public Card withEdition(CardEdition newEdition) {
        return with(newEdition, seal);
    }

    public Card withSeal(CardSeal newSeal) {
        return with(edition, newSeal);
    }

    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    public int bonusChips() {
        return bonusChips;
    }

    public int bonusMult() {
        return bonusMult;
    }

    public String[] bonusEffects() {
        return bonusEffects.clone();
    }

    public boolean debuffed() {
        return debuffed;
    }

    public int forcedRankOrder() {
        return forcedRankOrder;
    }

    /** Порядок ранга с учётом возможного переопределения (таро The Hanged Man). */
    public int order() {
        return forcedRankOrder > 0 ? forcedRankOrder : rank.order();
    }

    /** Итоговые фишки карты. Отрицательные значения обнуляются. */
    public int chips() {
        if (debuffed) {
            return 0;
        }
        return Math.max(0, rank.chips() + bonusChips);
    }

    /** Итоговый множитель карты. */
    public int mult() {
        return debuffed ? 0 : bonusMult;
    }

    public boolean hasEffect(String id) {
        for (String e : bonusEffects) {
            if (e.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Card withBonusChips(int delta) {
        return new Card(rank, suit, bonusChips + delta, bonusMult,
                bonusEffects, debuffed, forcedRankOrder, edition, seal);
    }

    public Card withBonusMult(int delta) {
        return new Card(rank, suit, bonusChips, bonusMult + delta,
                bonusEffects, debuffed, forcedRankOrder, edition, seal);
    }

    public Card withEffect(String effectId) {
        String[] copy = new String[bonusEffects.length + 1];
        System.arraycopy(bonusEffects, 0, copy, 0, bonusEffects.length);
        copy[bonusEffects.length] = effectId;
        return new Card(rank, suit, bonusChips, bonusMult, copy, debuffed,
                forcedRankOrder, edition, seal);
    }

    public Card withSuit(Suit newSuit) {
        return new Card(rank, newSuit, bonusChips, bonusMult,
                bonusEffects, debuffed, forcedRankOrder, edition, seal);
    }

    public Card withRank(Rank newRank) {
        return new Card(newRank, suit, bonusChips, bonusMult,
                bonusEffects, debuffed, forcedRankOrder, edition, seal);
    }

    /** Устанавливает переопределение ранга (таро The Hanged Man). */
    public Card withForcedOrder(int order) {
        return new Card(rank, suit, bonusChips, bonusMult, bonusEffects, debuffed,
                order, edition, seal);
    }

    public Card debuffed(boolean value) {
        return new Card(rank, suit, bonusChips, bonusMult, bonusEffects, value,
                forcedRankOrder, edition, seal);
    }

    /** Убирает все гейминг-эффекты, оставляя голую карту (таро Death). */
    public Card stripped() {
        return new Card(rank, suit, 0, 0, new String[0], debuffed, 0,
                edition, seal);
    }

    /** Полностью очищает карту, включая издание и печать. */
    public Card plain() {
        return new Card(rank, suit, 0, 0, new String[0], debuffed, 0,
                CardEdition.NONE, CardSeal.NONE);
    }

    /** Копия этой же карты, но другой масти (для поиска замен в колоде). */
    public Card copyWith(Suit newSuit) {
        return new Card(rank, newSuit, bonusChips, bonusMult, bonusEffects, debuffed,
                forcedRankOrder, edition, seal);
    }

    /** Сработает ли издание Lucky: сумма фишек и множителя карты равна 7. */
    public boolean isLucky(com.balatro.core.joker.ScoringContext ctx) {
        if (ctx == null) {
            return false;
        }
        int sum = rank.chips() + bonusChips + bonusMult;
        return sum == 7;
    }

    /** Сколько копий этой карты в колоде (для издания Crowded). */
    public int copiesInDeck(com.balatro.core.joker.ScoringContext ctx) {
        if (ctx == null || ctx.run() == null) {
            return 1;
        }
        return Math.max(1, ctx.run().copiesOf(this));
    }

    /** Осталась ли карта в руке (для издания Glass). */
    public boolean isHeld(com.balatro.core.joker.ScoringContext ctx) {
        return ctx != null && ctx.held().contains(this);
    }

    public String displayName() {
        return rank.symbol() + String.valueOf(suit.symbol());
    }

    /** Уникальный ключ вида "HA" без учёта модификаторов. */
    public String key() {
        return rank.name() + "_" + suit.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card other)) {
            return false;
        }
        return rank == other.rank && suit == other.suit;
    }

    @Override
    public int hashCode() {
        return rank.ordinal() * 4 + suit.ordinal();
    }

    @Override
    public String toString() {
        return displayName();
    }
}
