package com.balatro.client.screen;

import com.balatro.client.BalatroClient;
import com.balatro.core.blind.BossBlind;
import com.balatro.core.blind.BossRules;
import com.balatro.core.blind.BlindType;
import com.balatro.core.card.Card;
import com.balatro.core.consumable.PlanetCard;
import com.balatro.core.consumable.SpectralCard;
import com.balatro.core.consumable.TarotCard;
import com.balatro.core.hand.HandEvaluator;
import com.balatro.core.hand.HandType;
import com.balatro.core.run.RunPhase;
import com.balatro.core.run.RunSnapshot;
import com.balatro.net.BalatroPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Игровой стол: рука карт, предпросмотр комбинации, джокеры
 * и расходники. Все действия отправляются на сервер.
 */
public class BalatroScreen extends Screen {

    private RunSnapshot state;
    private final boolean[] selected = new boolean[64];
    private int selectedCount;

    public BalatroScreen(RunSnapshot state) {
        super(Component.literal("Balatro"));
        this.state = state;
    }

    private void refresh() {
        RunSnapshot fresh = BalatroClient.state();
        if (fresh != null) {
            this.state = fresh;
        }
    }

    @Override
    protected void init() {
        refresh();
        int cx = width / 2;
        int y = height - 36;
        addRenderableWidget(Button.builder(Component.literal("Play Hand"),
                        b -> send(BalatroPayloads.Actions.PLAY_HAND))
                .bounds(cx - 165, y, 105, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Discard"),
                        b -> send(BalatroPayloads.Actions.DISCARD))
                .bounds(cx - 55, y, 105, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shop"),
                        b -> send(BalatroPayloads.Actions.OPEN_SHOP))
                .bounds(cx + 55, y, 105, 20).build());
        clearSelection();
    }

    private void clearSelection() {
        java.util.Arrays.fill(selected, false);
        selectedCount = 0;
    }

    private void send(String action) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < state.hand.size(); i++) {
            if (i < selected.length && selected[i]) {
                list.add(i);
            }
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        BalatroClient.sendAction(action, arr);
        clearSelection();
    }

    /** Использует расходник из указанного слота с выбранными картами. */
    private void useConsumable(int slot) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < state.hand.size(); i++) {
            if (i < selected.length && selected[i]) {
                list.add(i);
            }
        }
        list.add(0, slot);
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        BalatroClient.sendAction(BalatroPayloads.Actions.USE_CONSUMABLE, arr);
        clearSelection();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        refresh();
        List<Card> hand = state.hand;
        if (!hand.isEmpty()) {
            int startX = CardRenderer.handStartX(width, hand.size());
            int top = height - 150;
            int hit = CardRenderer.hitTest(mouseX, mouseY, startX, top,
                    Math.min(hand.size(), selected.length), selected);
            if (hit >= 0) {
                selected[hit] = !selected[hit];
                selectedCount += selected[hit] ? 1 : -1;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private List<Card> selectedCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < state.hand.size() && i < selected.length; i++) {
            if (selected[i]) {
                cards.add(state.hand.get(i));
            }
        }
        return cards;
    }

    private String blindTitle() {
        String title = "Ante " + state.ante + " — " + blindName();
        if (state.bossBlind != BossBlind.NONE) {
            title = state.bossBlind.displayName();
        }
        return title;
    }

    private String blindName() {
        return switch (state.blindType) {
            case SMALL -> "Small Blind";
            case BIG -> "Big Blind";
            case BOSS -> "Boss Blind";
        };
    }

    private int blindTarget() {
        int base = (int) (switch (state.blindType) {
            case SMALL -> 300;
            case BIG -> 600;
            case BOSS -> 800;
        } * Math.pow(1.6, state.ante - 1));
        base = base / 100 * 100;
        if (state.bossBlind != BossBlind.NONE) {
            base = (int) (base * state.bossBlind.requirementScale());
            base = Math.max(100, base / 100 * 100);
        }
        return base;
    }
@Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xFF14251A);
        renderBlind(g);
        renderJokers(g);
        renderConsumables(g);
        renderPreview(g);
        renderHand(g);
        renderStatus(g);
        super.render(g, mouseX, mouseY, delta);
    }

    private void renderBlind(GuiGraphics g) {
        int y = 14;
        g.drawString(font, blindTitle(), 10, y, 0xFFFFE082, false);
        String rule = state.bossBlind == BossBlind.NONE ? ""
                : BossRules.description(state.bossBlind);
        if (!rule.isEmpty()) {
            g.drawString(font, rule, 10, y + 11, 0xFFFF8A80, false);
        }
        int target = blindTarget();
        g.drawString(font, "Очки: " + state.scoreThisRound + " / " + target,
                10, y + 24, state.scoreThisRound >= target ? 0xFF81C784 : 0xFFFFFFFF, false);
        g.drawString(font, "Руки: " + state.handsLeft + "   Сбросы: " + state.discardsLeft,
                10, y + 36, 0xFFFFFFFF, false);
        g.drawString(font, "Деньги: $" + state.money, 10, y + 48, 0xFFFFD54F, false);
        g.drawString(font, "Колода: " + state.startingDeck.replace('_', ' '),
                10, y + 60, 0xFFB0BEC5, false);
    }

    private void renderJokers(GuiGraphics g) {
        int x = width - 165;
        int y = 14;
        g.drawString(font, "Джокеры (" + state.jokers.size() + "/" + state.jokerSlots + ")",
                x, y, 0xFFFFE082, false);
        int jy = y + 13;
        for (String id : state.jokers) {
            if (jy > height - 300) {
                g.drawString(font, "...", x, jy, 0xFFFFFFFF, false);
                break;
            }
            com.balatro.core.joker.Joker joker =
                    com.balatro.core.joker.impl.JokerRegistry.byId(id);
            g.drawString(font, joker == null ? id : joker.displayName(), x, jy,
                    0xFFFFFFFF, false);
            if (joker != null) {
                g.drawString(font, "  " + joker.description(), x, jy + 9, 0xFF9E9E9E, false);
            }
            jy += 21;
        }
    }

    private void renderConsumables(GuiGraphics g) {
        int x = 10;
        int y = height - 235;
        g.drawString(font, "Расходники (" + state.consumables.size() + "/"
                + state.consumableSlots + ")", x, y, 0xFFFFE082, false);
        int cy = y + 13;
        for (int i = 0; i < state.consumables.size(); i++) {
            String label = describeConsumable(state.consumables.get(i));
            g.drawString(font, label, x, cy, 0xFFB0BEC5, false);
            cy += 11;
        }
    }

    private String describeConsumable(String id) {
        try {
            if (id.startsWith("planet:")) {
                PlanetCard p = PlanetCard.valueOf(id.substring(7).toUpperCase());
                return "Планета: " + p.displayName();
            }
            if (id.startsWith("tarot:")) {
                TarotCard t = TarotCard.byName(id.substring(6).toUpperCase());
                return "Таро: " + t.displayName();
            }
            if (id.startsWith("spectral:")) {
                SpectralCard s = SpectralCard.byName(id.substring(9).toUpperCase());
                return "Спектр: " + s.displayName();
            }
        } catch (IllegalArgumentException e) {
            return id;
        }
        return id;
    }

    private void renderPreview(GuiGraphics g) {
        List<Card> cards = selectedCards();
        int y = height - 178;
        if (cards.isEmpty()) {
            g.drawString(font, "Выберите карты (" + selectedCount + ")", width / 2 - 50,
                    y, 0xFF9E9E9E, false);
            return;
        }
        HandType type = HandEvaluator.evaluate(cards);
        if (type == null) {
            g.drawString(font, "—", width / 2, y, 0xFF9E9E9E, false);
            return;
        }
        int level = state.handLevels.getOrDefault(type, 1);
        g.drawString(font, type.displayName() + " ур." + level, width / 2 - 75, y,
                0xFFFFE082, false);
        g.drawString(font, type.chips(level) + " x " + type.mult(level), width / 2 - 15,
                y, 0xFF81C784, false);
    }

    private void renderHand(GuiGraphics g) {
        List<Card> hand = state.hand;
        if (hand.isEmpty()) {
            g.drawString(font, "Рука пуста", width / 2 - 25, height - 130,
                    0xFF9E9E9E, false);
            return;
        }
        int startX = CardRenderer.handStartX(width, hand.size());
        int top = height - 150;
        for (int i = 0; i < hand.size() && i < selected.length; i++) {
            CardRenderer.draw(g, font, hand.get(i), CardRenderer.cardX(startX, i),
                    top, selected[i], false);
        }
    }

    private void renderStatus(GuiGraphics g) {
        if (state.phase != RunPhase.PLAYING) {
            String text = switch (state.phase) {
                case SHOP -> "Слепой побеждён — откройте магазин";
                case GAME_OVER -> "ЗАБЕГ ПРОИГРАН";
                case GAME_WON -> "ЗАБЕГ ПРОЙДЕН";
                default -> "";
            };
            if (!text.isEmpty()) {
                g.drawString(font, text, width / 2 - 55, height - 100, 0xFFFFC107, false);
            }
        }
    }
}