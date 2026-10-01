package com.balatro.core.consumable;

import com.balatro.core.card.Card;
import com.balatro.core.card.CardEdition;
import com.balatro.core.card.CardSeal;
import com.balatro.core.card.Suit;
import com.balatro.core.run.RunState;

import java.util.List;

/**
 * Применяет эффекты таро-карт к забегу: усиление карт, смену масти,
 * разрушение, генерацию расходников и купонов.
 */
public final class TarotApplier {

    private TarotApplier() {
    }

    /** Применяет таро-карту. Возвращает текст результата для чата. */
    public static String apply(RunState run, TarotCard tarot, List<Card> selected) {
        String id = "tarot:" + tarot.name().toLowerCase();
        if (!run.consumables().contains(id)) {
            return "Таро-карта не найдена в слотах";
        }
        if (selected == null || selected.size() < tarot.requiredSelection()) {
            return "Нужно выбрать " + tarot.requiredSelection() + " карт(у)";
        }
        run.consumables().remove(id);
        run.setLastConsumableId(id);
        run.bumpTrigger("tarots_used", 1);
        return switch (tarot) {
            case THE_MAGICIAN -> enhanceChipsMult(run, selected, 10, 20);
            case THE_EMPRESS -> enhanceChipsMult(run, selected, 0, 3);
            case STRENGTH -> enhanceChipsMult(run, selected, 25, 0);
            case THE_HIEROPHANT -> hierophant(run, selected.get(0));
            case THE_LOVERS -> convertSuit(run, selected.get(0), Suit.HEARTS);
            case THE_CHARIOT -> convertSuit(run, selected.get(0), Suit.DIAMONDS);
            case JUSTICE -> convertSuit(run, selected.get(0), Suit.CLUBS);
            case THE_HANGED_MAN -> hangTheMan(run);
            case DEATH -> death(run, selected.get(0), selected.get(1));
            case THE_TOWER -> tower(run, selected);
            case THE_DEVIL -> applySeal(run, selected.get(0), CardSeal.GOLD);
            case JUDGEMENT -> applyEdition(run, selected.get(0), CardEdition.HOLOGRAPHIC);
            case THE_HERMIT -> hermit(run);
            case THE_WHEEL_OF_FORTUNE -> wheelOfFortune(run);
            case TEMPERANCE -> temperance(run);
            case THE_STAR -> star(run);
            case THE_MOON -> moon(run);
            case THE_HIGH_PRIESTESS -> highPriestess(run);
            case THE_EMPEROR -> emperor(run);
            case THE_WORLD -> world(run);
            case THE_FOOL -> fool(run);
            case THE_SUN -> sun(run, selected.get(0));
        };
    }

    private static String enhanceChipsMult(RunState run, List<Card> selected,
                                           int chips, int mult) {
        int count = 0;
        for (Card target : selected) {
            for (Card existing : run.deck().allCards()) {
                if (existing.equals(target)) {
                    run.deck().replace(existing,
                            existing.withBonusChips(chips).withBonusMult(mult));
                    count++;
                    break;
                }
            }
        }
        return "Усилено карт: " + count;
    }

    private static String hierophant(RunState run, Card target) {
        for (Card existing : run.deck().allCards()) {
            if (existing.equals(target)) {
                Card boosted = existing.withBonusMult(20);
                run.deck().replace(existing, boosted);
                run.addCardToDeck(boosted);
                run.addCardToDeck(boosted);
                return "Иерарх: карта усилена и удвоена";
            }
        }
        return "Иерарх: карта не найдена";
    }

    private static String convertSuit(RunState run, Card target, Suit suit) {
        for (Card existing : run.deck().allCards()) {
            if (existing.equals(target)) {
                run.deck().replace(existing, existing.copyWith(suit));
                return "Масть изменена на " + suit.displayName();
            }
        }
        return "Карта не найдена";
    }

    private static String hangTheMan(RunState run) {
        run.deck().transformAll(c -> c.copyWith(oppositeSuit(c.suit())));
        return "Масти всех карт перевёрнуты";
    }

    private static Suit oppositeSuit(Suit suit) {
        return switch (suit) {
            case HEARTS -> Suit.DIAMONDS;
            case DIAMONDS -> Suit.HEARTS;
            case CLUBS -> Suit.SPADES;
            case SPADES -> Suit.CLUBS;
        };
    }

    private static String death(RunState run, Card left, Card right) {
        for (Card existing : run.deck().allCards()) {
            if (existing.equals(right)) {
                run.deck().replace(existing, existing.withRank(left.rank()));
                return "Карта стала " + left.rank().symbol();
            }
        }
        return "Карта не найдена";
    }

    private static String tower(RunState run, List<Card> selected) {
        int destroyed = 0;
        for (Card target : selected) {
            if (run.deck().remove(target)) {
                destroyed++;
            }
        }
        return "Уничтожено карт: " + destroyed;
    }
private static String applySeal(RunState run, Card target, CardSeal seal) {
        for (Card existing : run.deck().allCards()) {
            if (existing.equals(target)) {
                run.deck().replace(existing, existing.withSeal(seal));
                return "Наложена печать: " + seal.displayName();
            }
        }
        return "Карта не найдена";
    }

    private static String applyEdition(RunState run, Card target, CardEdition edition) {
        for (Card existing : run.deck().allCards()) {
            if (existing.equals(target)) {
                run.deck().replace(existing, existing.withEdition(edition));
                return "Наложено издание: " + edition.displayName();
            }
        }
        return "Карта не найдена";
    }

    private static String hermit(RunState run) {
        int gain = Math.min(20, run.money());
        run.addMoney(gain);
        return "Отшельник: +$" + gain;
    }

    private static String temperance(RunState run) {
        int total = run.jokers().size();
        run.addMoney(total);
        return "Воздержание: +$" + total;
    }

    private static String star(RunState run) {
        for (int i = 0; i < 3; i++) {
            run.grantRandomTarot();
        }
        return "Звезда: 3 таро-карты";
    }

    private static String moon(RunState run) {
        for (int i = 0; i < 3; i++) {
            run.grantRandomPlanet();
        }
        return "Луна: 3 карты-планеты";
    }

    private static String highPriestess(RunState run) {
        for (int i = 0; i < 2; i++) {
            run.grantRandomPlanet();
        }
        return "Жрица: 2 карты-планеты";
    }

    private static String emperor(RunState run) {
        return "Император: купоны ждут в магазине";
    }

    private static String world(RunState run) {
        run.restockShop();
        return "Мир: джокер добавлен в магазин";
    }

    private static String sun(RunState run, Card target) {
        for (Card existing : run.deck().allCards()) {
            if (existing.equals(target)) {
                run.deck().replace(existing, existing.withEffect("tarot_card"));
                return "Солнце: карта стала таро";
            }
        }
        return "Карта не найдена";
    }

    private static String fool(RunState run) {
        String last = run.lastConsumableId();
        if (last == null || last.isEmpty()) {
            run.grantRandomTarot();
            return "Шут: случайное таро-карта";
        }
        if (last.startsWith("tarot:")) {
            run.addTarot(TarotCard.byName(last.substring(6).toUpperCase()));
        } else if (last.startsWith("planet:")) {
            run.addPlanet(PlanetCard.valueOf(last.substring(7).toUpperCase()));
        } else {
            run.grantRandomTarot();
        }
        return "Шут: повторён последний расходник";
    }

    private static String wheelOfFortune(RunState run) {
        int roll = (int) (System.nanoTime() % 4);
        return switch (Math.abs(roll)) {
            case 0 -> {
                run.addHandsPerRound(1);
                yield "Колесо фортуны: +1 рука";
            }
            case 1 -> {
                run.addDiscardsPerRound(1);
                yield "Колесо фортуны: +1 сброс";
            }
            case 2 -> {
                run.addMoney(1);
                yield "Колесо фортуны: +$1";
            }
            default -> {
                run.addExtraCardSlots(1);
                yield "Колесо фортуны: +1 слот карты";
            }
        };
    }
}