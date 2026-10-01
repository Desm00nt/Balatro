package com.balatro.storage;

import com.balatro.core.blind.BlindType;
import com.balatro.core.blind.BossBlind;
import com.balatro.core.card.Card;
import com.balatro.core.card.CardEdition;
import com.balatro.core.card.CardSeal;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;
import com.balatro.core.consumable.TarotCard;
import com.balatro.core.hand.HandType;
import com.balatro.core.run.RunPhase;
import com.balatro.core.run.RunSnapshot;
import com.balatro.core.shop.BoosterPack;
import com.balatro.core.shop.Voucher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Преобразует снимок забега в NBT и обратно. */
public final class RunNbt {

    private static final String KEY_DECK = "deck";
    private static final String KEY_DISCARD = "discard";
    private static final String KEY_HAND = "hand";

    private RunNbt() {
    }

    // --- Карты ---

    public static CompoundTag toNbt(Card card) {
        CompoundTag tag = new CompoundTag();
        tag.putString("rank", card.rank().name());
        tag.putString("suit", card.suit().name());
        tag.putInt("chips", card.bonusChips());
        tag.putInt("mult", card.bonusMult());
        ListTag effects = new ListTag();
        for (String e : card.bonusEffects()) {
            effects.add(net.minecraft.nbt.StringTag.valueOf(e));
        }
        tag.put("effects", effects);
        tag.putBoolean("debuffed", card.debuffed());
        tag.putInt("forcedOrder", card.forcedRankOrder());
        tag.putString("edition", card.edition().name());
        tag.putString("seal", card.seal().name());
        return tag;
    }

    public static Card cardFromNbt(CompoundTag tag) {
        Rank rank = parseEnum(Rank.class, tag.getString("rank"), Rank.TWO);
        Suit suit = parseEnum(Suit.class, tag.getString("suit"), Suit.HEARTS);
        String[] effects = new String[0];
        if (tag.contains("effects", Tag.TAG_LIST)) {
            ListTag list = tag.getList("effects", Tag.TAG_STRING);
            effects = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                effects[i] = list.getString(i);
            }
        }
        return new Card(rank, suit, tag.getInt("chips"), tag.getInt("mult"), effects,
                tag.getBoolean("debuffed"), tag.getInt("forcedOrder"),
                parseEnum(CardEdition.class, tag.getString("edition"), CardEdition.NONE),
                parseEnum(CardSeal.class, tag.getString("seal"), CardSeal.NONE));
    }

    private static ListTag cardsToNbt(List<Card> cards) {
        ListTag list = new ListTag();
        for (Card c : cards) {
            list.add(toNbt(c));
        }
        return list;
    }

    private static List<Card> cardsFromNbt(ListTag list) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            cards.add(cardFromNbt(list.getCompound(i)));
        }
        return cards;
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String name, T fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static ListTag stringsToNbt(List<String> values) {
        ListTag list = new ListTag();
        for (String v : values) {
            list.add(net.minecraft.nbt.StringTag.valueOf(v));
        }
        return list;
    }

    private static List<String> stringsFromNbt(ListTag list) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            values.add(list.getString(i));
        }
        return values;
    }
/** Снимок забега в NBT. */
    public static CompoundTag toNbt(RunSnapshot s) {
        CompoundTag t = new CompoundTag();
        t.putString("startingDeck", s.startingDeck);
        t.putLong("seed", s.seed);
        t.putString("phase", s.phase.name());
        t.putInt("ante", s.ante);
        t.putString("blindType", s.blindType.name());
        t.putString("bossBlind", s.bossBlind.name());
        t.putInt("score", s.scoreThisRound);
        t.putInt("money", s.money);
        t.putInt("handsLeft", s.handsLeft);
        t.putInt("discardsLeft", s.discardsLeft);
        t.putInt("handsPerRound", s.handsPerRound);
        t.putInt("discardsPerRound", s.discardsPerRound);
        t.putInt("handSize", s.handSize);
        t.putInt("jokerSlots", s.jokerSlots);
        t.putInt("consumableSlots", s.consumableSlots);
        t.putInt("extraCardSlots", s.extraCardSlots);
        t.putInt("extraCardsPerHand", s.extraCardsPerHand);
        t.putInt("discardsUsed", s.discardsUsedThisRound);
        t.putInt("roundsPlayed", s.roundsPlayed);
        t.putBoolean("roundWon", s.roundWon);
        t.putBoolean("gameOver", s.gameOver);
        t.putBoolean("bullBear", s.bullBear);
        t.putBoolean("ankhUsed", s.ankhUsed);
        t.putBoolean("dietCola", s.dietColaBought);
        t.putDouble("voucherDiscount", s.voucherDiscount);
        t.putDouble("jokerScale", s.jokerBaseMultScale);
        t.putString("lastConsumable", s.lastConsumableId);

        t.put(KEY_DECK, cardsToNbt(s.deckCards));
        t.put(KEY_DISCARD, cardsToNbt(s.discardCards));
        t.put(KEY_HAND, cardsToNbt(s.hand));
        t.put("jokers", stringsToNbt(s.jokers));
        t.put("consumables", stringsToNbt(s.consumables));

        ListTag vouchers = new ListTag();
        for (Voucher v : s.ownedVouchers) {
            vouchers.add(net.minecraft.nbt.StringTag.valueOf(v.name()));
        }
        t.put("vouchers", vouchers);

        CompoundTag levels = new CompoundTag();
        s.handLevels.forEach((k, v) -> levels.putInt(k.name(), v));
        t.put("handLevels", levels);

        CompoundTag counts = new CompoundTag();
        s.playCounts.forEach((k, v) -> counts.putInt(k.name(), v));
        t.put("playCounts", counts);

        CompoundTag trig = new CompoundTag();
        s.triggers.forEach(trig::putInt);
        t.put("triggers", trig);

        t.put("shopJokers", stringsToNbt(s.shopJokers));
        ListTag shopVouchers = new ListTag();
        for (Voucher v : s.shopVouchers) {
            shopVouchers.add(net.minecraft.nbt.StringTag.valueOf(v.name()));
        }
        t.put("shopVouchers", shopVouchers);
        t.put("shopPacks", stringsToNbt(s.shopPacks));
        t.putInt("rerollCost", s.rerollCost);
        t.putInt("rerollsUsed", s.rerollsUsed);
        return t;
    }
/** Восстанавливает снимок из NBT. */
    public static RunSnapshot fromNbt(CompoundTag t) {
        RunSnapshot s = new RunSnapshot();
        s.startingDeck = t.getString("startingDeck");
        s.seed = t.getLong("seed");
        s.phase = parseEnum(RunPhase.class, t.getString("phase"), RunPhase.PLAYING);
        s.ante = t.getInt("ante");
        s.blindType = parseEnum(BlindType.class, t.getString("blindType"), BlindType.SMALL);
        s.bossBlind = parseEnum(BossBlind.class, t.getString("bossBlind"), BossBlind.NONE);
        s.scoreThisRound = t.getInt("score");
        s.money = t.getInt("money");
        s.handsLeft = t.getInt("handsLeft");
        s.discardsLeft = t.getInt("discardsLeft");
        s.handsPerRound = t.getInt("handsPerRound");
        s.discardsPerRound = t.getInt("discardsPerRound");
        s.handSize = t.getInt("handSize");
        s.jokerSlots = t.getInt("jokerSlots");
        s.consumableSlots = t.getInt("consumableSlots");
        s.extraCardSlots = t.getInt("extraCardSlots");
        s.extraCardsPerHand = t.getInt("extraCardsPerHand");
        s.discardsUsedThisRound = t.getInt("discardsUsed");
        s.roundsPlayed = t.getInt("roundsPlayed");
        s.roundWon = t.getBoolean("roundWon");
        s.gameOver = t.getBoolean("gameOver");
        s.bullBear = t.getBoolean("bullBear");
        s.ankhUsed = t.getBoolean("ankhUsed");
        s.dietColaBought = t.getBoolean("dietCola");
        s.voucherDiscount = t.contains("voucherDiscount") ? t.getDouble("voucherDiscount") : 1.0;
        s.jokerBaseMultScale = t.contains("jokerScale") ? t.getDouble("jokerScale") : 1.0;
        s.lastConsumableId = t.getString("lastConsumable");

        s.deckCards.addAll(cardsFromNbt(t.getList(KEY_DECK, Tag.TAG_COMPOUND)));
        s.discardCards.addAll(cardsFromNbt(t.getList(KEY_DISCARD, Tag.TAG_COMPOUND)));
        s.hand.addAll(cardsFromNbt(t.getList(KEY_HAND, Tag.TAG_COMPOUND)));
        s.jokers.addAll(stringsFromNbt(t.getList("jokers", Tag.TAG_STRING)));
        s.consumables.addAll(stringsFromNbt(t.getList("consumables", Tag.TAG_STRING)));

        ListTag vouchers = t.getList("vouchers", Tag.TAG_STRING);
        for (int i = 0; i < vouchers.size(); i++) {
            s.ownedVouchers.add(parseEnum(Voucher.class, vouchers.getString(i),
                    Voucher.CASTLE));
        }
        readIntMap(t.getCompound("handLevels"), HandType.class, s.handLevels);
        readIntMap(t.getCompound("playCounts"), HandType.class, s.playCounts);
        CompoundTag trig = t.getCompound("triggers");
        for (String key : trig.getAllKeys()) {
            s.triggers.put(key, trig.getInt(key));
        }

        s.shopJokers.addAll(stringsFromNbt(t.getList("shopJokers", Tag.TAG_STRING)));
        ListTag shopVouchers = t.getList("shopVouchers", Tag.TAG_STRING);
        for (int i = 0; i < shopVouchers.size(); i++) {
            s.shopVouchers.add(parseEnum(Voucher.class, shopVouchers.getString(i),
                    Voucher.CASTLE));
        }
        ListTag packs = t.getList("shopPacks", Tag.TAG_STRING);
        for (int i = 0; i < packs.size(); i++) {
            s.shopPacks.add(parseEnum(BoosterPack.class, packs.getString(i),
                    BoosterPack.STANDARD_PACK).name());
        }
        s.rerollCost = Math.max(1, t.getInt("rerollCost"));
        s.rerollsUsed = t.getInt("rerollsUsed");
        return s;
    }

    private static <T extends Enum<T>> void readIntMap(CompoundTag tag, Class<T> type,
                                                     Map<T, Integer> out) {
        if (tag == null) {
            return;
        }
        for (String key : tag.getAllKeys()) {
            T parsed = parseEnum(type, key, null);
            if (parsed != null) {
                out.put(parsed, tag.getInt(key));
            }
        }
    }
}