package com.balatro.core.shop;

import com.balatro.core.joker.Joker;

import java.util.ArrayList;
import java.util.List;

/**
 * Состояние магазина между раундами: список джокеров, купонов,
 * паков и текущая стоимость реролла.
 */
public final class Shop {

    public static final int SHOP_SIZE = 2;

    private final List<Joker> jokers = new ArrayList<>();
    private final List<Voucher> vouchers = new ArrayList<>();
    private final List<BoosterPack> packs = new ArrayList<>();

    private int rerollCost = 5;
    private int rerollsUsed;

    /** Наполняет магазин стандартным ассортиментом. */
    public void restock(List<Joker> jokerStock, List<Voucher> voucherStock,
                        List<BoosterPack> packStock) {
        jokers.clear();
        vouchers.clear();
        packs.clear();
        jokers.addAll(jokerStock);
        vouchers.addAll(voucherStock);
        packs.addAll(packStock);
    }

    /** Удваивает цену следующего реролла. */
    public void increaseRerollCost() {
        rerollsUsed++;
        rerollCost = 5 + rerollsUsed;
    }

    public int rerollCost() {
        return rerollCost;
    }

    public int rerollsUsed() {
        return rerollsUsed;
    }

    /** Скидывает стоимость реролла (событие/купон). */
    public void discountReroll(int delta) {
        rerollCost = Math.max(1, rerollCost - delta);
    }

    /** Восстанавливает стоимость реролла из снимка. */
    public void restoreReroll(int cost, int used) {
        rerollCost = Math.max(1, cost);
        rerollsUsed = Math.max(0, used);
    }

    public List<Joker> jokers() {
        return jokers;
    }

    public List<Voucher> vouchers() {
        return vouchers;
    }

    public List<BoosterPack> packs() {
        return packs;
    }

    /** Убирает купленного джокера из ассортимента. */
    public boolean removeJoker(Joker joker) {
        return jokers.remove(joker);
    }

    public boolean removeVoucher(Voucher voucher) {
        return vouchers.remove(voucher);
    }

    public boolean removePack(BoosterPack pack) {
        int removed = 0;
        while (packs.remove(pack)) {
            removed++;
        }
        return removed > 0;
    }

    public boolean isEmpty() {
        return jokers.isEmpty() && vouchers.isEmpty() && packs.isEmpty();
    }
}