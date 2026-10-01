package com.balatro.core;

import com.balatro.core.blind.BossBlind;
import com.balatro.core.blind.BossRules;
import com.balatro.core.card.Card;
import com.balatro.core.card.CardEdition;
import com.balatro.core.card.CardSeal;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;
import com.balatro.core.consumable.SpectralCard;
import com.balatro.core.consumable.TarotApplier;
import com.balatro.core.consumable.TarotCard;
import com.balatro.core.joker.impl.JokerRegistry;
import com.balatro.core.run.RunPhase;
import com.balatro.core.run.RunSnapshot;
import com.balatro.core.run.RunState;
import com.balatro.core.run.StartingDeck;
import com.balatro.core.shop.BoosterPack;
import com.balatro.core.shop.Voucher;

import java.util.ArrayList;
import java.util.List;

import static com.balatro.core.ScoringHelper.cards;

/** Тесты новых систем: магазин, купоны, боссы, таро, издания. */
public final class FeatureTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        testShop();
        testVouchers();
        testBossRules();
        testEditions();
        testTarot();
        testPackContents();
        testWinCondition();
        testDeckLimits();
        testSerialization();

        System.out.println();
        System.out.println("=== FEATURES: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testShop() {
        System.out.println("-- Shop --");
        RunState run = new RunState(StartingDeck.RED, 100L);
        run.restockShop();
        checkTrue("Shop has jokers", run.shop().jokers().size() > 0,
                "" + run.shop().jokers().size());
        checkTrue("Shop has two vouchers", run.shop().vouchers().size() == 2,
                "" + run.shop().vouchers().size());
        checkTrue("Shop has a pack", run.shop().packs().size() == 1,
                "" + run.shop().packs().size());

        run.addMoney(50);
        run.setPhase(RunPhase.SHOP);
        int before = run.shop().jokers().size();
        run.buyJoker(run.shop().jokers().get(0));
        checkTrue("Bought shop joker", run.jokers().size() == 1, "" + run.jokers().size());
        checkTrue("Bought joker left the shop",
                run.shop().jokers().size() == before - 1, "" + run.shop().jokers().size());

        int cost = run.shop().rerollCost();
        checkTrue("Reroll is affordable", run.canReroll(), "cost=" + cost);
        run.reroll();
        checkTrue("Reroll raises its cost", run.shop().rerollCost() > cost,
                run.shop().rerollCost() + " vs " + cost);
    }

    private static void testVouchers() {
        System.out.println("-- Vouchers --");
        RunState run = new RunState(StartingDeck.RED, 200L);
        run.addMoney(200);
        run.setPhase(RunPhase.SHOP);

        int hands = run.handsPerRound();
        checkTrue("Bought Grabber", run.buyVoucher(Voucher.GRABBER), "");
        checkTrue("Grabber adds a hand", run.handsPerRound() == hands + 1,
                run.handsPerRound() + " vs " + hands);
        checkTrue("Cannot buy the same voucher twice",
                !run.buyVoucher(Voucher.GRABBER), "");

        int discards = run.discardsPerRound();
        checkTrue("Bought Drunkard", run.buyVoucher(Voucher.DRUNKARD), "");
        checkTrue("Drunkard adds a discard",
                run.discardsPerRound() == discards + 1,
                run.discardsPerRound() + " vs " + discards);

        int size = run.handSize();
        checkTrue("Bought Narcissist", run.buyVoucher(Voucher.NARCISSIST), "");
        checkTrue("Narcissist grows the hand", run.handSize() == size + 3,
                run.handSize() + " vs " + size);

        int slots = run.jokerSlots();
        checkTrue("Bought Ceramic", run.buyVoucher(Voucher.CERAMIC), "");
        checkTrue("Ceramic adds joker slots", run.jokerSlots() == slots + 3,
                run.jokerSlots() + " vs " + slots);
        checkTrue("Ceramic scales joker mult", run.jokerBaseMultScale() == 0.5,
                "" + run.jokerBaseMultScale());

        checkTrue("All vouchers have descriptions",
                java.util.Arrays.stream(Voucher.values())
                        .allMatch(v -> !v.description().isEmpty()), "");
        checkTrue("All vouchers cost something",
                java.util.Arrays.stream(Voucher.values())
                        .allMatch(v -> v.baseCost() > 0), "");
    }
private static void testBossRules() {
        System.out.println("-- Boss rules --");
        checkTrue("Psychic rejects 4 cards",
                !BossRules.isValidPlay(BossBlind.THE_PSYCHIC, 4), "");
        checkTrue("Psychic allows 5 cards",
                BossRules.isValidPlay(BossBlind.THE_PSYCHIC, 5), "");
        checkTrue("Wall doubles the requirement",
                BossBlind.THE_WALL.requirementScale() == 2.0, "");
        checkTrue("Manacle shrinks the hand",
                BossRules.handSizeModifier(BossBlind.THE_MANACLE) == -1, "");
        checkTrue("Serpent trades size for hands",
                BossRules.handSizeModifier(BossBlind.THE_SERPENT) == -3
                        && BossRules.handsModifier(BossBlind.THE_SERPENT) == 3, "");
        checkTrue("Mouth limits to one hand",
                BossRules.handsModifier(BossBlind.THE_MOUTH) <= -1, "");
        checkTrue("Tower caps the hand at 3",
                BossRules.maxHandSize(BossBlind.THE_TOWER) == 3, "");
        checkTrue("Goad restricts hand types",
                BossRules.restrictsHandType(BossBlind.THE_GOAD), "");
        checkTrue("Water debuffs everything",
                BossRules.debuffsAllCards(BossBlind.THE_WATER), "");
        checkTrue("Window targets the Ace of Diamonds",
                BossRules.debuffedRank(BossBlind.THE_WINDOW) == Rank.ACE
                        && BossRules.debuffedSuit(BossBlind.THE_WINDOW) == Suit.DIAMONDS,
                "");
        checkTrue("Every real boss has a description",
                java.util.Arrays.stream(BossBlind.values())
                        .filter(b -> b != BossBlind.NONE)
                        .allMatch(b -> !BossRules.description(b).isEmpty()), "");

        Card ace = new Card(Rank.ACE, Suit.DIAMONDS);
        checkTrue("Window debuffs the Ace of Diamonds",
                BossRules.applyPermanentDebuff(BossBlind.THE_WINDOW, ace).debuffed(), "");
        Card two = new Card(Rank.TWO, Suit.HEARTS);
        checkTrue("Window spares other cards",
                !BossRules.applyPermanentDebuff(BossBlind.THE_WINDOW, two).debuffed(), "");

        RunState run = new RunState(StartingDeck.RED, 300L);
        checkTrue("No boss on the small blind", run.bossBlind() == BossBlind.NONE,
                "" + run.bossBlind());
    }

    private static void testEditions() {
        System.out.println("-- Card editions --");
        Card plain = new Card(Rank.TEN, Suit.HEARTS);
        Card foil = plain.withEdition(CardEdition.FOIL);
        checkTrue("Edition is stored", foil.edition() == CardEdition.FOIL, "");
        checkTrue("Edition keeps rank and suit",
                foil.rank() == plain.rank() && foil.suit() == plain.suit(), "");
        checkTrue("Edition keeps the seal",
                plain.withSeal(CardSeal.GOLD).withEdition(CardEdition.FOIL).seal()
                        == CardSeal.GOLD, "");
        checkTrue("Edition survives a suit change",
                foil.withSuit(Suit.CLUBS).edition() == CardEdition.FOIL, "");
        checkTrue("Edition survives a rank change",
                foil.withRank(Rank.ACE).edition() == CardEdition.FOIL, "");
        checkTrue("Edition survives bonus chips",
                foil.withBonusChips(5).edition() == CardEdition.FOIL, "");
        checkTrue("plain() clears the edition",
                foil.plain().edition() == CardEdition.NONE, "");
        checkTrue("Negative grants a hand", CardEdition.NEGATIVE.grantsExtraHand(), "");
        checkTrue("All editions have names",
                java.util.Arrays.stream(CardEdition.values())
                        .allMatch(e -> !e.displayName().isEmpty()), "");
        checkTrue("All seals are non-null",
                java.util.Arrays.stream(CardSeal.values())
                        .allMatch(java.util.Objects::nonNull), "");
        checkTrue("Edition lookup works",
                CardEdition.byName("polychrome") == CardEdition.POLYCHROME, "");
        checkTrue("Seal lookup works", CardSeal.byName("gold") == CardSeal.GOLD, "");
    }
private static void testTarot() {
        System.out.println("-- Tarot --");
        RunState run = new RunState(StartingDeck.RED, 500L);
        run.addTarot(TarotCard.THE_MAGICIAN);
        checkTrue("Tarot added to consumables",
                run.consumables().contains("tarot:the_magician"), "" + run.consumables());

        Card target = new Card(Rank.SEVEN, Suit.CLUBS);
        String result = TarotApplier.apply(run, TarotCard.THE_MAGICIAN,
                cards(target, new Card(Rank.NINE, Suit.HEARTS)));
        checkTrue("Magician applied", !result.contains("не найдена"), result);
        checkTrue("Magician consumed its slot",
                !run.consumables().contains("tarot:the_magician"), "" + run.consumables());

        String again = TarotApplier.apply(run, TarotCard.THE_MAGICIAN, cards(target));
        checkTrue("Second use is rejected", again.contains("не найдена"), again);

        RunState run2 = new RunState(StartingDeck.RED, 501L);
        run2.addTarot(TarotCard.DEATH);
        String tooFew = TarotApplier.apply(run2, TarotCard.DEATH,
                cards(new Card(Rank.TWO, Suit.HEARTS)));
        checkTrue("Death needs two cards", tooFew.contains("Нужно выбрать"), tooFew);
        checkTrue("Card stays on failure",
                run2.consumables().contains("tarot:death"), "" + run2.consumables());

        checkTrue("All 22 tarots exist", TarotCard.values().length == 22,
                "" + TarotCard.values().length);
        checkTrue("All tarots have effect text",
                java.util.Arrays.stream(TarotCard.values())
                        .allMatch(t -> !t.effect().isEmpty()), "");
        checkTrue("Tarot pool excludes creator cards",
                !TarotCard.pool().contains(TarotCard.THE_FOOL), "");
        checkTrue("All 18 spectrals exist", SpectralCard.values().length == 18,
                "" + SpectralCard.values().length);
        checkTrue("All spectrals have effect text",
                java.util.Arrays.stream(SpectralCard.values())
                        .allMatch(s -> !s.effect().isEmpty()), "");
    }

    private static void testPackContents() {
        System.out.println("-- Booster packs --");
        checkTrue("Joker pack holds jokers", BoosterPack.JOKER_PACK.containsJokers(), "");
        checkTrue("Tarot pack holds consumables",
                BoosterPack.TAROT_PACK.containsConsumables(), "");
        checkTrue("Spectral pack holds spectrals",
                BoosterPack.SPECTRAL_PACK.containsSpectrals(), "");
        checkTrue("Packs are internally consistent",
                java.util.Arrays.stream(BoosterPack.values())
                        .allMatch(p -> p.picks() > 0 && p.picks() <= p.cardCount()), "");

        RunState run = new RunState(StartingDeck.RED, 600L);
        int jokers = run.jokers().size();
        run.grantPack(BoosterPack.JOKER_PACK);
        checkTrue("Joker pack grants a joker", run.jokers().size() == jokers + 1,
                run.jokers().size() + " vs " + jokers);

        RunState run2 = new RunState(StartingDeck.RED, 601L);
        int cards = run2.deck().totalSize();
        run2.grantPack(BoosterPack.STANDARD_PACK);
        checkTrue("Standard pack grants a card",
                run2.deck().totalSize() == cards + 1,
                run2.deck().totalSize() + " vs " + cards);
    }
private static void testWinCondition() {
        System.out.println("-- Win condition --");
        RunState run = new RunState(StartingDeck.RED, 700L);
        checkTrue("Starts at ante 1", run.ante() == 1, "" + run.ante());
        checkTrue("Run is not finished at start", !run.phase().isFinished(),
                "" + run.phase());

        int guard = 0;
        while (!run.phase().isFinished() && guard++ < 300) {
            if (run.phase() == RunPhase.SHOP) {
                run.nextRound();
                continue;
            }
            if (run.phase() != RunPhase.PLAYING) {
                break;
            }
            // Некоторые боссы требуют сыграть ровно 5 карт.
            int pick = BossRules.isValidPlay(run.bossBlind(), run.hand().size())
                    ? run.hand().size() : 5;
            pick = Math.min(pick, run.hand().size());
            List<Card> chosen = new ArrayList<>(run.hand().subList(0, pick));
            if (run.playHand(chosen) == null) {
                break;
            }
            if (run.roundWon()) {
                run.completeRound();
            }
        }
        checkTrue("Run reaches a finished phase", run.phase().isFinished(),
                run.phase() + " after " + guard + " steps");
        checkTrue("Finished phase is win or loss",
                run.phase() == RunPhase.GAME_OVER || run.phase() == RunPhase.GAME_WON,
                "" + run.phase());
        checkTrue("Ante stays within bounds", run.ante() <= RunState.WIN_ANTE,
                "" + run.ante());
    }

    private static void testDeckLimits() {
        System.out.println("-- Deck limits --");
        RunState run = new RunState(StartingDeck.RED, 800L);
        int added = 0;
        for (int i = 0; i < 100; i++) {
            if (run.addCardToDeck(new Card(Rank.TWO, Suit.HEARTS))) {
                added++;
            }
        }
        checkTrue("Deck growth is capped at 60",
                run.deck().totalSize() <= RunState.MAX_DECK_SIZE,
                "" + run.deck().totalSize());
        checkTrue("Capped deck rejected extras", added < 100, "" + added);
        checkTrue("Joker registry has 50+ entries", JokerRegistry.all().size() >= 50,
                "" + JokerRegistry.all().size());
        checkTrue("All jokers have ids and names",
                JokerRegistry.all().stream()
                        .allMatch(j -> j.id() != null && !j.id().isEmpty()
                                && j.displayName() != null && !j.displayName().isEmpty()),
                "");
        checkTrue("All jokers have a rarity",
                JokerRegistry.all().stream().allMatch(j -> j.rarity() != null), "");
        checkTrue("Joker ids are unique",
                JokerRegistry.all().stream().map(j -> j.id()).distinct().count()
                        == JokerRegistry.all().size(), "");
    }

    private static void testSerialization() {
        System.out.println("-- Snapshot round-trip --");
        RunState run = new RunState(StartingDeck.RED, 1234L);
        run.addMoney(37);
        run.addTarot(TarotCard.THE_EMPRESS);
        run.addPlanet(com.balatro.core.consumable.PlanetCard.JUPITER);
        run.setPhase(RunPhase.SHOP);
        run.buyVoucher(Voucher.GRABBER);
        run.restockShop();
        run.playHand(new ArrayList<>(run.hand().subList(0, 5)));
        run.discard(new ArrayList<>(run.hand().subList(0, 2)));

        RunSnapshot snap = run.toSnapshot();
        checkTrue("Snapshot captures ante", snap.ante == run.ante(), "");
        checkTrue("Snapshot captures money", snap.money == run.money(), "");
        checkTrue("Snapshot captures hand", snap.hand.size() == run.hand().size(),
                snap.hand.size() + " vs " + run.hand().size());
        checkTrue("Snapshot captures deck",
                snap.deckCards.size() + snap.discardCards.size() == run.deck().totalSize(),
                (snap.deckCards.size() + snap.discardCards.size())
                        + " vs " + run.deck().totalSize());
        checkTrue("Snapshot captures consumables",
                snap.consumables.size() == run.consumables().size(), "");
        checkTrue("Snapshot captures vouchers", !snap.ownedVouchers.isEmpty(), "");

        RunState loaded = RunState.fromSnapshot(snap);
        checkTrue("Ante survives", loaded.ante() == run.ante(),
                loaded.ante() + " vs " + run.ante());
        checkTrue("Money survives", loaded.money() == run.money(),
                loaded.money() + " vs " + run.money());
        checkTrue("Phase survives", loaded.phase() == run.phase(),
                loaded.phase() + " vs " + run.phase());
        checkTrue("Blind survives", loaded.blindType() == run.blindType(), "");
        checkTrue("Hand survives", loaded.hand().size() == run.hand().size(),
                loaded.hand().size() + " vs " + run.hand().size());
        checkTrue("Hand cards survive",
                loaded.hand().get(0).equals(run.hand().get(0)), "");
        checkTrue("Deck size survives",
                loaded.deck().totalSize() == run.deck().totalSize(),
                loaded.deck().totalSize() + " vs " + run.deck().totalSize());
        checkTrue("Consumables survive",
                loaded.consumables().equals(run.consumables()),
                loaded.consumables() + " vs " + run.consumables());
        checkTrue("Vouchers survive", loaded.ownedVouchers().size()
                == run.ownedVouchers().size(), "");
        checkTrue("Reroll cost survives",
                loaded.shop().rerollCost() == run.shop().rerollCost(), "");
        checkTrue("Bonus hand survives",
                loaded.handsPerRound() == run.handsPerRound(), "");
    }

    private static void checkTrue(String name, boolean condition, String detail) {
        if (condition) {
            passed++;
            System.out.println("  [OK]   " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + " -> " + detail);
        }
    }

    private FeatureTest() {
    }
}