package com.balatro.client.screen;

import com.balatro.core.card.Card;
import com.balatro.core.hand.HandEvaluator;
import com.balatro.core.hand.HandType;
import com.balatro.core.run.RunState;
import com.balatro.core.run.ScoreResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Игровой стол Balatro: рука карт, выбор комбинации, кнопки
 * Play Hand / Discard и панель счёта.
 */
public class BalatroScreen extends Screen {

    private static final int CARD_W = 42;
    private static final int CARD_H = 58;
    private static final int CARD_GAP = 4;
    private static final int LIFT = 12;

    private final RunState run;
    private final Set<Card> selected = new HashSet<>();
    private final List<String> messages = new ArrayList<>();

    private ScoreResult lastResult;
    private long messageExpiry;

    public BalatroScreen(RunState run) {
        super(Component.literal("Balatro"));
        this.run = run;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int buttonY = height - 40;
        addRenderableWidget(Button.builder(Component.literal("Play Hand"),
                        b -> onPlayHand())
                .bounds(cx - 110, buttonY, 105, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Discard"),
                        b -> onDiscard())
                .bounds(cx + 5, buttonY, 105, 20).build());
    }

    private void onPlayHand() {
        if (selected.isEmpty()) {
            notify("Выберите карты");
            return;
        }
        ScoreResult result = run.playHand(new ArrayList<>(selected));
        if (result == null) {
            notify("Не удалось сыграть комбинацию");
            return;
        }
        lastResult = result;
        selected.clear();
        notify(result.handType().displayName() + ": +" + result.points());
        if (run.gameOver()) {
            notify("ЗАБЕГ ПРОИГРАН");
        } else if (run.roundWon()) {
            notify("Слепой побеждён!");
        }
    }

    private void onDiscard() {
        if (selected.isEmpty()) {
            notify("Выберите карты для сброса");
            return;
        }
        run.discard(new ArrayList<>(selected));
        selected.clear();
        notify("Сброс выполнен");
    }

    private void notify(String text) {
        messages.add(text);
        messageExpiry = System.currentTimeMillis() + 4000L;
        while (messages.size() > 4) {
            messages.remove(0);
        }
    }

    private int handStartX() {
        List<Card> hand = run.hand();
        int totalW = hand.size() * CARD_W + Math.max(0, hand.size() - 1) * CARD_GAP;
        return (width - totalW) / 2;
    }

    private static int handY(int screenHeight) {
        return screenHeight - 150;
    }

    private boolean handleCardClick(double mouseX, double mouseY) {
        List<Card> hand = run.hand();
        int startX = handStartX();
        int y = handY(height);
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int x = startX + i * (CARD_W + CARD_GAP);
            int cardY = selected.contains(card) ? y - LIFT : y;
            if (mouseX >= x && mouseX <= x + CARD_W
                    && mouseY >= cardY && mouseY <= cardY + CARD_H) {
                if (!selected.remove(card)) {
                    selected.add(card);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleCardClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderCard(GuiGraphics g, Card card, int x, int y) {
        boolean isSelected = selected.contains(card);
        int cardY = isSelected ? y - LIFT : y;
        int bg = card.suit().isRed() ? 0xFFD94A4A : 0xFF2B2B33;
        if (isSelected) {
            g.fill(x - 2, cardY - 2, x + CARD_W + 2, cardY + CARD_H + 2, 0xFFFFD54A);
        }
        g.fill(x, cardY, x + CARD_W, cardY + CARD_H, bg);
        g.fill(x, cardY, x + CARD_W, cardY + 2, 0xFFFFFFFF);
        g.fill(x, cardY + CARD_H - 2, x + CARD_W, cardY + CARD_H, 0xFF000000);

        String rank = String.valueOf(card.rank().symbol());
        String suit = String.valueOf(card.suit().symbol());
        g.drawString(font, rank, x + 4, cardY + 5, 0xFFFFFFFF, false);
        g.drawString(font, suit, x + CARD_W - 12, cardY + 5, 0xFFFFFFFF, false);
        g.drawString(font, suit, x + CARD_W / 2 - 4, cardY + CARD_H / 2 - 4, 0xFFFFFFFF, false);

        if (card.bonusChips() > 0) {
            g.drawString(font, "+" + card.bonusChips(), x + 3,
                    cardY + CARD_H - 12, 0xFF7CFC00, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xFF14251A);

        renderBlindPanel(g);
        renderJokerPanel(g);
        renderHandPreview(g);
        renderHand(g);

        if (lastResult != null) {
            g.drawString(font, "Прошлый ход: " + lastResult, 12, height - 220, 0xFFFFFFFF, false);
        }

        if (System.currentTimeMillis() < messageExpiry) {
            int my = height - 100;
            for (String msg : messages) {
                g.drawString(font, msg, width / 2 - 40, my, 0xFFFFFFFF, false);
                my += 10;
            }
        }

        super.render(g, mouseX, mouseY, delta);
    }

    private void renderBlindPanel(GuiGraphics g) {
        int y = 16;
        g.drawString(font, run.currentBlind().displayName(), 12, y, 0xFFFFE082, false);
        g.drawString(font, "Цель: " + run.currentBlind().scoreTarget(), 12, y + 12,
                0xFFFFFFFF, false);
        g.drawString(font, "Очки: " + run.scoreThisRound(), 12, y + 24, 0xFF81C784, false);
        g.drawString(font, "Руки: " + run.handsLeft() + "   Сбросы: " + run.discardsLeft(),
                12, y + 36, 0xFFFFFFFF, false);
        g.drawString(font, "Деньги: $" + run.money(), 12, y + 48, 0xFFFFD54F, false);
        g.drawString(font, "Колода: " + run.startingDeck().displayName(), 12, y + 60,
                0xFFB0BEC5, false);
    }

    private void renderJokerPanel(GuiGraphics g) {
        int x = width - 160;
        int y = 16;
        g.drawString(font, "Джокеры (" + run.jokers().size() + "/" + run.jokerSlots() + ")",
                x, y, 0xFFFFE082, false);
        int jy = y + 14;
        for (com.balatro.core.joker.Joker joker : run.jokers()) {
            if (jy > height - 260) {
                break;
            }
            g.drawString(font, joker.displayName(), x, jy, 0xFFFFFFFF, false);
            g.drawString(font, "  " + joker.description(), x, jy + 10, 0xFF9E9E9E, false);
            jy += 22;
        }
    }

    private void renderHandPreview(GuiGraphics g) {
        HandType type = selected.isEmpty()
                ? null
                : HandEvaluator.evaluate(new ArrayList<>(selected));
        int y = height - 175;
        if (type != null) {
            int level = run.handLevel(type);
            g.drawString(font, type.displayName() + " (ур. " + level + ")",
                    width / 2 - 60, y, 0xFFFFE082, false);
            g.drawString(font, type.chips(level) + " x " + type.mult(level),
                    width / 2 - 10, y, 0xFF81C784, false);
        } else {
            g.drawString(font, "Выберите карты", width / 2 - 40, y, 0xFF9E9E9E, false);
        }
    }

    private void renderHand(GuiGraphics g) {
        List<Card> hand = run.hand();
        int startX = handStartX();
        int y = handY(height);
        for (int i = 0; i < hand.size(); i++) {
            renderCard(g, hand.get(i), startX + i * (CARD_W + CARD_GAP), y);
        }
    }
}
