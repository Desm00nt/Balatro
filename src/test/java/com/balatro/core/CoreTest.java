package com.balatro.core;

import com.balatro.core.card.Card;
import com.balatro.core.card.Rank;
import com.balatro.core.card.Suit;
import com.balatro.core.consumable.PlanetCard;
import com.balatro.core.hand.HandEvaluator;
import com.balatro.core.hand.HandType;
import com.balatro.core.joker.impl.BasicJokers;
import com.balatro.core.joker.impl.JokerRegistry;
import com.balatro.core.blind.Blind;
import com.balatro.core.blind.BlindType;
import com.balatro.core.run.RunState;
import com.balatro.core.run.ScoreResult;
import com.balatro.core.run.StartingDeck;

import java.util.ArrayList;
import java.util.List;

import static com.balatro.core.ScoringHelper.c;
import static com.balatro.core.ScoringHelper.cards;
import static com.balatro.core.ScoringHelper.score;

/**
 * РђРІС‚РѕРЅРѕРјРЅС‹Р№ С‚РµСЃС‚ СЏРґСЂР° РёРіСЂС‹. Р—Р°РїСѓСЃРє: {@code java com.balatro.core.CoreTest}
 * РџСЂРѕРІРµСЂСЏРµС‚ РѕРїСЂРµРґРµР»РµРЅРёРµ РєРѕРјР±РёРЅР°С†РёР№, РїРѕРґСЃС‡С‘С‚ РѕС‡РєРѕРІ Рё РёРіСЂРѕРІРѕР№ С†РёРєР».
 */
public final class CoreTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        testHandDetection();
        testScoring();
        testJokers();
        testBlinds();
        testGameLoop();
        testDecks();
        testPlanets();

        System.out.println();
        System.out.println("=== RESULT: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testHandDetection() {
        System.out.println("-- Hand detection --");
        check("Pair", HandType.PAIR, cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.NINE, Suit.DIAMONDS),
                c(Rank.KING, Suit.HEARTS)));
        check("Two Pair", HandType.TWO_PAIR, cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.FIVE, Suit.DIAMONDS),
                c(Rank.KING, Suit.HEARTS)));
        check("Three of a Kind", HandType.THREE_OF_A_KIND, cards(
                c(Rank.SEVEN, Suit.HEARTS), c(Rank.SEVEN, Suit.SPADES),
                c(Rank.SEVEN, Suit.CLUBS), c(Rank.KING, Suit.DIAMONDS),
                c(Rank.QUEEN, Suit.HEARTS)));
        check("Full House", HandType.FULL_HOUSE, cards(
                c(Rank.THREE, Suit.HEARTS), c(Rank.THREE, Suit.SPADES),
                c(Rank.THREE, Suit.CLUBS), c(Rank.ACE, Suit.DIAMONDS),
                c(Rank.ACE, Suit.HEARTS)));
        check("Four of a Kind", HandType.FOUR_OF_A_KIND, cards(
                c(Rank.NINE, Suit.HEARTS), c(Rank.NINE, Suit.SPADES),
                c(Rank.NINE, Suit.CLUBS), c(Rank.NINE, Suit.DIAMONDS),
                c(Rank.ACE, Suit.HEARTS)));
        // Flush в Balatro — это 3-4 карты одной масти; при 5 картах
        // одной масти игра уже засчитывается как Flush Five (сильнее).
        check("Flush (4 cards)", HandType.FLUSH, cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.FIVE, Suit.HEARTS),
                c(Rank.NINE, Suit.HEARTS), c(Rank.JACK, Suit.HEARTS)));
        check("Flush (5 cards is Flush Five)", HandType.FLUSH_FIVE, cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.FIVE, Suit.HEARTS),
                c(Rank.NINE, Suit.HEARTS), c(Rank.JACK, Suit.HEARTS),
                c(Rank.QUEEN, Suit.HEARTS)));
        check("Straight", HandType.STRAIGHT, cards(
                c(Rank.FIVE, Suit.HEARTS), c(Rank.SIX, Suit.SPADES),
                c(Rank.SEVEN, Suit.CLUBS), c(Rank.EIGHT, Suit.DIAMONDS),
                c(Rank.NINE, Suit.HEARTS)));
        check("Wheel straight A-2-3-4-5", HandType.STRAIGHT, cards(
                c(Rank.ACE, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.THREE, Suit.CLUBS), c(Rank.FOUR, Suit.DIAMONDS),
                c(Rank.FIVE, Suit.HEARTS)));
        check("Five of a Kind", HandType.FIVE_OF_A_KIND, cards(
                c(Rank.QUEEN, Suit.HEARTS), c(Rank.QUEEN, Suit.SPADES),
                c(Rank.QUEEN, Suit.CLUBS), c(Rank.QUEEN, Suit.DIAMONDS),
                c(Rank.QUEEN, Suit.HEARTS)));
        check("Straight Flush", HandType.STRAIGHT_FLUSH, cards(
                c(Rank.FIVE, Suit.HEARTS), c(Rank.SIX, Suit.HEARTS),
                c(Rank.SEVEN, Suit.HEARTS), c(Rank.EIGHT, Suit.HEARTS),
                c(Rank.NINE, Suit.HEARTS)));
        check("Flush Five", HandType.FLUSH_FIVE, cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.FIVE, Suit.HEARTS),
                c(Rank.NINE, Suit.HEARTS), c(Rank.JACK, Suit.HEARTS),
                c(Rank.QUEEN, Suit.HEARTS), c(Rank.ACE, Suit.HEARTS)));
        check("Flush House", HandType.FLUSH_HOUSE, cards(
                c(Rank.FIVE, Suit.HEARTS), c(Rank.FIVE, Suit.HEARTS),
                c(Rank.FIVE, Suit.HEARTS), c(Rank.ACE, Suit.HEARTS),
                c(Rank.ACE, Suit.HEARTS)));
    }



    private static void testScoring() {
        System.out.println("-- Scoring --");
        // High Card lvl1: 5 chips base + 11+10+10+5+3 = 44 chips, 1 mult
        ScoreResult r = score(cards(
                c(Rank.ACE, Suit.HEARTS), c(Rank.KING, Suit.SPADES),
                c(Rank.TEN, Suit.CLUBS), c(Rank.FIVE, Suit.DIAMONDS),
                c(Rank.THREE, Suit.HEARTS)),
                HandType.HIGH_CARD, 1, List.of());
        checkTrue("High card total chips = 44", r.totalChips() == 44, r.toString());
        checkTrue("High card mult = 1", r.totalMult() == 1, r.toString());
        checkTrue("High card points = 44", r.points() == 44, r.toString());

        // Pair lvl1: 10 chips base + 2+2+5+9+10 = 38 chips, 2 mult
        ScoreResult p = score(cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.NINE, Suit.DIAMONDS),
                c(Rank.KING, Suit.HEARTS)),
                HandType.PAIR, 1, List.of());
        checkTrue("Pair total chips = 38", p.totalChips() == 38, p.toString());
        checkTrue("Pair mult = 2", p.totalMult() == 2, p.toString());
        checkTrue("Pair points = 76", p.points() == 76, p.toString());

        // Level scaling: Pair lvl2 => 10+15=25 chips, 2+2=4 mult
        ScoreResult p2 = score(cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.NINE, Suit.DIAMONDS),
                c(Rank.KING, Suit.HEARTS)),
                HandType.PAIR, 2, List.of());
        checkTrue("Pair lvl2 chips = 53", p2.totalChips() == 53, p2.toString());
        checkTrue("Pair lvl2 mult = 4", p2.totalMult() == 4, p2.toString());
    }

    private static void testJokers() {
        System.out.println("-- Jokers --");
        ScoreResult r = score(cards(
                c(Rank.ACE, Suit.HEARTS), c(Rank.KING, Suit.SPADES),
                c(Rank.TEN, Suit.CLUBS), c(Rank.FIVE, Suit.DIAMONDS),
                c(Rank.THREE, Suit.HEARTS)),
                HandType.HIGH_CARD, 1, List.of(BasicJokers.JOKER, BasicJokers.MISPRINT));
        checkTrue("Joker(+4) + Misprint(+23) => mult 28", r.totalMult() == 28, r.toString());
        checkTrue("Points scale with mult", r.points() == 44 * 28, r.toString());

        ScoreResult jolly = score(cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.NINE, Suit.DIAMONDS),
                c(Rank.KING, Suit.HEARTS)),
                HandType.PAIR, 1, List.of(JokerRegistry.byId("jolly")));
        checkTrue("Jolly gives +8 mult on a pair", jolly.totalMult() == 2 + 8, jolly.toString());

        ScoreResult sly = score(cards(
                c(Rank.TWO, Suit.HEARTS), c(Rank.TWO, Suit.SPADES),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.NINE, Suit.DIAMONDS),
                c(Rank.KING, Suit.HEARTS)),
                HandType.PAIR, 1, List.of(JokerRegistry.byId("sly")));
        checkTrue("Sly gives +50 chips on a pair", sly.totalChips() == 38 + 50, sly.toString());

        // Greedy Joker: +3 chips per diamond played
        ScoreResult greedy = score(cards(
                c(Rank.TWO, Suit.DIAMONDS), c(Rank.FOUR, Suit.DIAMONDS),
                c(Rank.FIVE, Suit.CLUBS), c(Rank.NINE, Suit.SPADES),
                c(Rank.KING, Suit.HEARTS)),
                HandType.HIGH_CARD, 1, List.of(JokerRegistry.byId("greedy")));
        checkTrue("Greedy: 2 diamonds => +6 chips", greedy.totalChips() == 5 + 30 + 6,
                greedy.toString());
    }


    private static void testBlinds() {
        System.out.println("-- Blind targets --");
        checkTrue("Ante 1 small = 300", Blind.calculateTarget(1, BlindType.SMALL) == 300,
                "" + Blind.calculateTarget(1, BlindType.SMALL));
        checkTrue("Ante 1 big = 600", Blind.calculateTarget(1, BlindType.BIG) == 600, "");
        checkTrue("Ante 1 boss = 800", Blind.calculateTarget(1, BlindType.BOSS) == 800, "");
        int a2 = Blind.calculateTarget(2, BlindType.SMALL);
        checkTrue("Ante 2 small > 300 (" + a2 + ")", a2 > 300, "" + a2);
        int a8 = Blind.calculateTarget(8, BlindType.SMALL);
        checkTrue("Ante 8 small reaches thousands (" + a8 + ")", a8 > 1000, "" + a8);
        checkTrue("Ante 3 boss beats ante 1 boss",
                Blind.calculateTarget(3, BlindType.BOSS)
                        > Blind.calculateTarget(1, BlindType.BOSS), "");
        checkTrue("Boss target beats small target in same ante",
                Blind.calculateTarget(1, BlindType.BOSS)
                        > Blind.calculateTarget(1, BlindType.SMALL), "");
    }

    private static void testGameLoop() {
        System.out.println("-- Game loop --");
        RunState run = new RunState(StartingDeck.RED, 999L);
        checkTrue("Hand has 8 cards", run.hand().size() == 8, "size=" + run.hand().size());
        checkTrue("Red deck gives 4 hands", run.handsLeft() == 4, "hands=" + run.handsLeft());
        checkTrue("Red deck gives 4 discards", run.discardsLeft() == 4,
                "discards=" + run.discardsLeft());

        int target = run.currentBlind().scoreTarget();
        int guard = 0;
        while (!run.roundWon() && !run.gameOver() && guard++ < 20) {
            if (run.playHand(new ArrayList<>(run.hand())) == null) {
                break;
            }
        }
        checkTrue("Blind beaten (" + run.scoreThisRound() + "/" + target + ")", run.roundWon(),
                "score=" + run.scoreThisRound());
        checkTrue("No negative score", run.scoreThisRound() >= 0, "" + run.scoreThisRound());

        int moneyBefore = run.money();
        run.completeRound();
        checkTrue("Money grew after round", run.money() > moneyBefore,
                moneyBefore + " -> " + run.money());
        checkTrue("Hands reset", run.handsLeft() == 4, "hands=" + run.handsLeft());
        checkTrue("Advanced to big blind", run.blindType() == BlindType.BIG,
                "blind=" + run.blindType());

        int discardsBefore = run.discardsLeft();
        run.discard(new ArrayList<>(run.hand().subList(0, 1)));
        checkTrue("Discard consumed one", run.discardsLeft() == discardsBefore - 1,
                "now=" + run.discardsLeft());

        RunState shop = new RunState(StartingDeck.YELLOW, 7L);
        int cash = shop.money();
        checkTrue("Yellow deck starts with $10", cash == 10, "" + cash);
        if (shop.buyJoker(BasicJokers.JOKER)) {
            checkTrue("Bought joker for $3", shop.money() == cash - 3, "" + shop.money());
            checkTrue("Joker count is 1", shop.jokers().size() == 1, "");
        } else {
            checkTrue("Purchase succeeded", false, "buyJoker returned false");
        }
    }

    private static void testDecks() {
        System.out.println("-- Starting decks --");
        checkTrue("Red deck: 4 hands", new RunState(StartingDeck.RED, 1L).handsLeft() == 4, "");
        checkTrue("Blue deck: 5 hands", new RunState(StartingDeck.BLUE, 1L).handsLeft() == 5, "");
        RunState black = new RunState(StartingDeck.BLACK, 1L);
        checkTrue("Black deck: 3 hands (4-1)", black.handsLeft() == 3, "" + black.handsLeft());
        checkTrue("Black deck: 3 discards", black.discardsLeft() == 3, "" + black.discardsLeft());
        checkTrue("Black deck: 6 joker slots", black.jokerSlots() == 6, "" + black.jokerSlots());
        checkTrue("Yellow deck: $10", new RunState(StartingDeck.YELLOW, 1L).money() == 10, "");
        RunState abandoned = new RunState(StartingDeck.ABANDONED, 1L);
        boolean noFaces = abandoned.deck().drawPileView().stream()
                .noneMatch(cd -> cd.rank().isFace());
        checkTrue("Abandoned deck has no face cards", noFaces, "");
        // 40 карт всего: 8 в руке + 32 в колоде.
        checkTrue("Abandoned deck: 40 cards total",
                abandoned.deck().totalSize() + abandoned.hand().size() == 40,
                "deck=" + abandoned.deck().totalSize() + " hand=" + abandoned.hand().size());
        RunState std = new RunState(StartingDeck.RED, 1L);
        checkTrue("Standard deck: 52 cards total",
                std.deck().totalSize() + std.hand().size() == 52,
                "deck=" + std.deck().totalSize() + " hand=" + std.hand().size());
    }

    private static void testPlanets() {
        System.out.println("-- Planet cards --");
        RunState run = new RunState(StartingDeck.RED, 5L);
        checkTrue("Pair starts at level 1", run.handLevel(HandType.PAIR) == 1,
                "" + run.handLevel(HandType.PAIR));
        checkTrue("Added Mercury to consumables", run.addPlanet(PlanetCard.MERCURY),
                "" + run.consumables());
        checkTrue("Used Mercury", run.usePlanet(PlanetCard.MERCURY), "");
        checkTrue("Pair is now level 2", run.handLevel(HandType.PAIR) == 2,
                "" + run.handLevel(HandType.PAIR));
        checkTrue("Consumable slot freed", run.consumables().isEmpty(),
                "" + run.consumables());
        checkTrue("Cannot reuse consumed planet", !run.usePlanet(PlanetCard.MERCURY), "");

        // Секретная планета недоступна, пока комбинация не сыграна.
        RunState secret = new RunState(StartingDeck.RED, 6L);
        checkTrue("Eris hidden before playing Flush Five",
                !secret.isPlanetAvailable(PlanetCard.ERIS), "");
        checkTrue("Mars (not secret) is available",
                secret.isPlanetAvailable(PlanetCard.MARS), "");
        checkTrue("Added Eris", secret.addPlanet(PlanetCard.ERIS), "");
        checkTrue("Eris cannot be used before playing the hand",
                !secret.usePlanet(PlanetCard.ERIS), "");
        checkTrue("Eris still in consumables", secret.consumables().size() == 1,
                "" + secret.consumables());

        checkTrue("Every hand type has a planet",
                PlanetCard.values().length == HandType.values().length,
                PlanetCard.values().length + " vs " + HandType.values().length);
        for (HandType type : HandType.values()) {
            checkTrue("Planet exists for " + type.displayName(),
                    PlanetCard.forHand(type).handType() == type, "");
        }
    }

    // --- helpers ---

    private static void check(String name, HandType expected, List<Card> input) {
        HandType actual = HandEvaluator.evaluate(input);
        checkTrue(name, actual == expected, "expected " + expected + ", got " + actual);
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

    private static void checkTrue(String name, boolean condition) {
        checkTrue(name, condition, "");
    }

    private CoreTest() {
    }
}