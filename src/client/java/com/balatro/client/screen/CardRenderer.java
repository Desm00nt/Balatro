package com.balatro.client.screen;

import com.balatro.core.card.Card;
import com.balatro.core.card.CardEdition;
import com.balatro.core.card.CardSeal;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Общая отрисовка карт для всех экранов мода. */
public final class CardRenderer {

    public static final int W = 42;
    public static final int H = 58;
    public static final int GAP = 4;
    public static final int LIFT = 12;

    private CardRenderer() {
    }

    /** Рисует карту в координатах (x, y). */
    public static void draw(GuiGraphicsExtractor g, Font font, Card card, int x, int y,
                            boolean selected, boolean debuffed) {
        int top = selected ? y - LIFT : y;
        int bg = card.debuffed() || debuffed ? 0xFF3A3A3A
                : card.suit().isRed() ? 0xFFD94A4A : 0xFF2B2B33;

        if (selected) {
            g.fill(x - 2, top - 2, x + W + 2, top + H + 2, 0xFFFFD54A);
        }
        g.fill(x, top, x + W, top + H, bg);
        g.fill(x, top, x + W, top + 2, 0xFFFFFFFF);
        g.fill(x, top + H - 2, x + W, top + H, 0xFF000000);

        int textColor = (card.debuffed() || debuffed) ? 0xFF808080 : 0xFFFFFFFF;
        String rank = String.valueOf(card.rank().symbol());
        String suit = String.valueOf(card.suit().symbol());
        g.text(font, rank, x + 4, top + 5, textColor, false);
        g.text(font, suit, x + W - 12, top + 5, textColor, false);
        g.text(font, suit, x + W / 2 - 4, top + H / 2 - 4, textColor, false);

        // Издание и печать отмечаются в углу.
        String mark = editionMark(card.edition());
        if (mark != null) {
            g.text(font, mark, x + 4, top + 16, 0xFFFFD54F, false);
        }
        if (!card.seal().isEmpty()) {
            g.text(font, "S", x + W - 9, top + 18, 0xFF9CCC65, false);
        }
        if (card.bonusChips() > 0 || card.bonusMult() > 0) {
            String bonus = "+" + card.bonusChips() + "/" + card.bonusMult();
            g.text(font, bonus, x + 2, top + H - 12, 0xFF7CFC00, false);
        }
    }

    private static String editionMark(CardEdition edition) {
        return switch (edition) {
            case NONE -> null;
            case FOIL -> "F";
            case HOLOGRAPHIC -> "H";
            case POLYCHROME -> "P";
            case NEGATIVE -> "N";
            case GLASS -> "G";
            case LUCKY -> "L";
            case CROWDED -> "C";
        };
    }

    /** Горизонтальная позиция левого края руки. */
    public static int handStartX(int screenWidth, int count) {
        int total = count * W + Math.max(0, count - 1) * GAP;
        return (screenWidth - total) / 2;
    }

    public static int cardX(int startX, int index) {
        return startX + index * (W + GAP);
    }

    /** Индекс карты под курсором или -1. */
    public static int hitTest(double mouseX, double mouseY, int startX, int handTop,
                              int count, boolean[] selected) {
        for (int i = 0; i < count; i++) {
            int x = cardX(startX, i);
            int y = selected[i] ? handTop - LIFT : handTop;
            if (mouseX >= x && mouseX <= x + W && mouseY >= y && mouseY <= y + H) {
                return i;
            }
        }
        return -1;
    }
}