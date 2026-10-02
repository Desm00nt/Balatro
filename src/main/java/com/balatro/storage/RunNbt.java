package com.balatro.storage;

import com.balatro.core.blind.BlindType;
import com.balatro.core.blind.BossBlind;
import com.balatro.core.card.Card;
import com.balatro.core.card.CardEdition;
import com.balatro.core.card.CardSeal;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;
import com.balatro.core.hand.HandType;
import com.balatro.core.run.RunPhase;
import com.balatro.core.run.RunSnapshot;
import com.balatro.core.shop.BoosterPack;
import com.balatro.core.shop.Voucher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Преобразует снимок забега в NBT и обратно.
 * В Minecraft 26.x методы чтения NBT возвращают Optional,
 * поэтому используются хелперы со значениями по умолчанию.
 */
public final class RunNbt {

    private static final String KEY_DECK = "deck";
    private static final String KEY_DISCARD = "discard";
    private static final String KEY_HAND = "hand";

    private RunNbt() {
    }

    // --- Хелперы чтения ---

    private static String str(CompoundTag tag, String key, String fallback) {
        return tag.getStringOr(key, fallback);
    }

    private static int num(CompoundTag tag, String key, int fallback) {
        return tag.getInt(key).orElse(fallback);
    }

    private static double numD(CompoundTag tag, String key, double fallback) {
        return tag.getDoubleOr(key, fallback);
    }

    private static boolean flag(CompoundTag tag, String key) {
        return tag.getBooleanOr(key, false);
    }

    private static CompoundTag compound(CompoundTag tag, String key) {
        return tag.getCompound(key).orElse(new CompoundTag());
    }

    private static ListTag list(CompoundTag tag, String key) {
        return tag.getListOrEmpty(key);
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
            CompoundTag tag = list.getCompound(i).orElse(null);
            if (tag != null) {
                cards.add(cardFromNbt(tag));
            }
        }
        return cards;
    }

    private static ListTag stringsToNbt(List<String> values) {
        ListTag list = new ListTag();
        for (String v : values) {
            list.add(StringTag.valueOf(v));
        }
        return list;
    }

    private static List<String> stringsFromNbt(ListTag list) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            list.getString(i).ifPresent(values::add);
        }
        return values;
    }

    private static ListTag enumsToNbt(Iterable<? extends Enum<?>> enums) {
        ListTag list = new ListTag();
        for (Enum<?> e : enums) {
            list.add(StringTag.valueOf(e.name()));
        }
        return list;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Enum<T>> void readIntMap(CompoundTag tag, Class<T> type,
                                                       Map<T, Integer> out) {
        for (String key : tag.keySet()) {
            T parsed = parseEnum(type, key, null);
            if (parsed != null) {
                out.put(parsed, num(tag, key, 0));
            }
        }
    }

    // --- Карты ---

    public static CompoundTag toNbt(Card card) {
        CompoundTag tag = new CompoundTag();
        tag.putString("rank", card.rank().name());
        tag.putString("suit", card.suit().name());
        tag.putInt("chips", card.bonusChips());
        tag.putInt("mult", card.bonusMult());
        tag.put("effects", stringsToNbt(List.of(card.bonusEffects())));
        tag.putBoolean("debuffed", card.debuffed());
        tag.putInt("forcedOrder", card.forcedRankOrder());
        tag.putString("edition", card.edition().name());
        tag.putString("seal", card.seal().name());
        return tag;
    }

    public static Card cardFromNbt(CompoundTag tag) {
        Rank rank = parseEnum(Rank.class, str(tag, "rank", ""), Rank.TWO);
        Suit suit = parseEnum(Suit.class, str(tag, "suit", ""), Suit.HEARTS);
        String[] effects = stringsFromNbt(list(tag, "effects")).toArray(new String[0]);
        return new Card(rank, suit, num(tag, "chips", 0), num(tag, "mult", 0), effects,
                flag(tag, "debuffed"), num(tag, "forcedOrder", 0),
                parseEnum(CardEdition.class, str(tag, "edition", ""), CardEdition.NONE),
                parseEnum(CardSeal.class, str(tag, "seal", ""), CardSeal.NONE));
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
        t.put("vouchers", enumsToNbt(s.ownedVouchers));

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
        t.put("shopVouchers", enumsToNbt(s.shopVouchers));
        t.put("shopPacks", stringsToNbt(s.shopPacks));
        t.putInt("rerollCost", s.rerollCost);
        t.putInt("rerollsUsed", s.rerollsUsed);
        return t;
    }

    /** Восстанавливает снимок из NBT. */
    public static RunSnapshot fromNbt(CompoundTag t) {
        RunSnapshot s = new RunSnapshot();
        s.startingDeck = str(t, "startingDeck", "");
        s.seed = t.getLong("seed").orElse(0L);
        s.phase = parseEnum(RunPhase.class, str(t, "phase", ""), RunPhase.PLAYING);
        s.ante = num(t, "ante", 1);
        s.blindType = parseEnum(BlindType.class, str(t, "blindType", ""), BlindType.SMALL);
        s.bossBlind = parseEnum(BossBlind.class, str(t, "bossBlind", ""), BossBlind.NONE);
        s.scoreThisRound = num(t, "score", 0);
        s.money = num(t, "money", 0);
        s.handsLeft = num(t, "handsLeft", 4);
        s.discardsLeft = num(t, "discardsLeft", 3);
        s.handsPerRound = num(t, "handsPerRound", 4);
        s.discardsPerRound = num(t, "discardsPerRound", 3);
        s.handSize = num(t, "handSize", 8);
        s.jokerSlots = num(t, "jokerSlots", 5);
        s.consumableSlots = num(t, "consumableSlots", 2);
        s.extraCardSlots = num(t, "extraCardSlots", 0);
        s.extraCardsPerHand = num(t, "extraCardsPerHand", 0);
        s.discardsUsedThisRound = num(t, "discardsUsed", 0);
        s.roundsPlayed = num(t, "roundsPlayed", 0);
        s.roundWon = flag(t, "roundWon");
        s.gameOver = flag(t, "gameOver");
        s.bullBear = flag(t, "bullBear");
        s.ankhUsed = flag(t, "ankhUsed");
        s.dietColaBought = flag(t, "dietCola");
        s.voucherDiscount = numD(t, "voucherDiscount", 1.0);
        s.jokerBaseMultScale = numD(t, "jokerScale", 1.0);
        s.lastConsumableId = str(t, "lastConsumable", "");

        s.deckCards.addAll(cardsFromNbt(list(t, KEY_DECK)));
        s.discardCards.addAll(cardsFromNbt(list(t, KEY_DISCARD)));
        s.hand.addAll(cardsFromNbt(list(t, KEY_HAND)));
        s.jokers.addAll(stringsFromNbt(list(t, "jokers")));
        s.consumables.addAll(stringsFromNbt(list(t, "consumables")));
        for (String name : stringsFromNbt(list(t, "vouchers"))) {
            s.ownedVouchers.add(parseEnum(Voucher.class, name, Voucher.CASTLE));
        }
        readIntMap(compound(t, "handLevels"), HandType.class, s.handLevels);
        readIntMap(compound(t, "playCounts"), HandType.class, s.playCounts);
        CompoundTag trig = compound(t, "triggers");
        for (String key : trig.keySet()) {
            s.triggers.put(key, num(trig, key, 0));
        }

        s.shopJokers.addAll(stringsFromNbt(list(t, "shopJokers")));
        for (String name : stringsFromNbt(list(t, "shopVouchers"))) {
            s.shopVouchers.add(parseEnum(Voucher.class, name, Voucher.CASTLE));
        }
        for (String name : stringsFromNbt(list(t, "shopPacks"))) {
            s.shopPacks.add(parseEnum(BoosterPack.class, name,
                    BoosterPack.STANDARD_PACK).name());
        }
        s.rerollCost = Math.max(1, num(t, "rerollCost", 5));
        s.rerollsUsed = num(t, "rerollsUsed", 0);
        return s;
    }
}