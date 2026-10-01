package com.balatro.net;

import com.balatro.core.card.Card;
import com.balatro.core.run.RunState;
import com.balatro.core.run.ScoreResult;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** Применяет действия игрока к состоянию забега. */
public final class RunActions {

    private RunActions() {
    }

    /** Выполняет действие и возвращает текст ответа для игрока. */
    public static String apply(RunState run, String action, List<Integer> selection,
                               ServerPlayer player) {
        List<Card> cards = resolve(run, selection);
        return switch (action) {
            case BalatroPayloads.Actions.PLAY_HAND -> {
                if (cards.isEmpty()) {
                    yield "Сначала выберите карты";
                }
                ScoreResult result = run.playHand(cards);
                if (result == null) {
                    yield "Ход невозможен";
                }
                yield result.handType().displayName() + ": +" + result.points()
                        + (run.roundWon() ? " — слепой побеждён!" : "");
            }
            case BalatroPayloads.Actions.DISCARD -> {
                if (cards.isEmpty()) {
                    yield "Сначала выберите карты";
                }
                yield run.discard(cards) ? "Сброс выполнен" : "Сброс недоступен";
            }
            case BalatroPayloads.Actions.USE_CONSUMABLE -> {
                int slot = selection.size() > 1 ? selection.get(1) : 0;
                yield useConsumable(run, slot, cards);
            }
            case BalatroPayloads.Actions.BUY_JOKER -> {
                int idx = selection.isEmpty() ? -1 : selection.get(0);
                if (idx < 0 || idx >= run.shop().jokers().size()) {
                    yield "Джокер не найден";
                }
                yield run.buyJoker(run.shop().jokers().get(idx))
                        ? "Джокер куплен" : "Недостаточно денег или нет места";
            }
            case BalatroPayloads.Actions.SELL_JOKER -> {
                int idx = selection.isEmpty() ? -1 : selection.get(0);
                yield run.sellJoker(idx) ? "Джокер продан за $1" : "Джокер не найден";
            }
            case BalatroPayloads.Actions.BUY_VOUCHER -> {
                int idx = selection.isEmpty() ? -1 : selection.get(0);
                if (idx < 0 || idx >= run.shop().vouchers().size()) {
                    yield "Купон не найден";
                }
                yield run.buyVoucher(run.shop().vouchers().get(idx))
                        ? "Купон куплен" : "Недостаточно денег";
            }
            case BalatroPayloads.Actions.BUY_PACK -> {
                int idx = selection.isEmpty() ? -1 : selection.get(0);
                if (idx < 0 || idx >= run.shop().packs().size()) {
                    yield "Пак не найден";
                }
                yield run.buyPack(run.shop().packs().get(idx))
                        ? "Пак открыт" : "Недостаточно денег";
            }
            case BalatroPayloads.Actions.REROLL ->
                    run.reroll() ? "Ассортимент обновлён" : "Недостаточно денег";
            case BalatroPayloads.Actions.NEXT_ROUND -> {
                run.nextRound();
                yield "";
            }
            case BalatroPayloads.Actions.SKIP_BLIND -> {
                run.skipBlind();
                yield "Слепой пропущен";
            }
            case BalatroPayloads.Actions.OPEN_SHOP -> {
                if (player != null) {
                    BalatroNetworking.openScreen(player, BalatroPayloads.Screens.SHOP);
                }
                yield "";
            }
            default -> "";
        };
    }

    private static String useConsumable(RunState run, int slot, List<Card> cards) {
        if (slot < 0 || slot >= run.consumables().size()) {
            return "Слот расходника пуст";
        }
        boolean ok = run.useConsumable(run.consumables().get(slot), cards);
        String result = run.lastConsumableResult();
        if (result != null && !result.isEmpty()) {
            return result;
        }
        return ok ? "Расходник использован" : "Не удалось использовать";
    }

    /** Преобразует индексы выбранных карт в объекты. */
    private static List<Card> resolve(RunState run, List<Integer> indices) {
        List<Card> cards = new ArrayList<>();
        List<Card> hand = run.hand();
        for (int i : indices) {
            if (i >= 0 && i < hand.size()) {
                cards.add(hand.get(i));
            }
        }
        return cards;
    }
}