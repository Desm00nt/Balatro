package com.balatro.client.screen;

import com.balatro.client.BalatroClient;
import com.balatro.core.joker.Joker;
import com.balatro.core.joker.impl.JokerRegistry;
import com.balatro.core.run.RunSnapshot;
import com.balatro.core.shop.BoosterPack;
import com.balatro.core.shop.Voucher;
import com.balatro.net.BalatroPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Магазин: покупка джокеров, купонов, паков и реролл ассортимента. */
public class ShopScreen extends Screen {

    private RunSnapshot state;

    public ShopScreen(RunSnapshot state) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font,
                Component.literal("Shop"));
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
        int y = height - 30;

        int x = 12;
        for (int i = 0; i < state.shopJokers.size(); i++) {
            final int index = i;
            Joker joker = JokerRegistry.byId(state.shopJokers.get(i));
            int price = joker == null ? 0 : (int) Math.max(1,
                    Math.round((joker.basePrice() + state.ante * 0.5)
                            * state.voucherDiscount));
            addRenderableWidget(Button.builder(
                            Component.literal(joker == null ? "?" : joker.displayName()
                                    + " $"), b -> buyJoker(index))
                    .bounds(x, y, 110, 20).build());
            x += 116;
        }

        int vx = 12;
        for (int i = 0; i < state.shopVouchers.size(); i++) {
            final int index = i;
            Voucher voucher = state.shopVouchers.get(i);
            addRenderableWidget(Button.builder(
                            Component.literal(voucher.displayName() + " $"
                                    + priceOf(voucher)), b -> buyVoucher(index))
                    .bounds(vx, height - 58, 110, 20).build());
            vx += 116;
        }

        int px = 12;
        for (int i = 0; i < state.shopPacks.size(); i++) {
            final int index = i;
            BoosterPack pack = BoosterPack.valueOf(state.shopPacks.get(i));
            addRenderableWidget(Button.builder(
                            Component.literal(pack.displayName() + " $"
                                    + packPrice(pack)), b -> buyPack(index))
                    .bounds(px, height - 86, 110, 20).build());
            px += 116;
        }

        addRenderableWidget(Button.builder(
                        Component.literal("Reroll $" + state.rerollCost),
                        b -> BalatroClient.sendAction(BalatroPayloads.Actions.REROLL))
                .bounds(width - 130, y, 118, 20).build());
        addRenderableWidget(Button.builder(
                        Component.literal("Следующий раунд"),
                        b -> BalatroClient.sendAction(BalatroPayloads.Actions.NEXT_ROUND))
                .bounds(width / 2 - 60, height - 120, 120, 20).build());
    }

    private int priceOf(Voucher voucher) {
        return (int) Math.max(1, Math.round(voucher.baseCost() * state.voucherDiscount));
    }

    private int packPrice(BoosterPack pack) {
        return (int) Math.max(1, Math.round(pack.basePrice() * state.voucherDiscount));
    }

    private void buyJoker(int index) {
        BalatroClient.sendAction(BalatroPayloads.Actions.BUY_JOKER, index);
    }

    private void buyVoucher(int index) {
        BalatroClient.sendAction(BalatroPayloads.Actions.BUY_VOUCHER, index);
    }

    private void buyPack(int index) {
        BalatroClient.sendAction(BalatroPayloads.Actions.BUY_PACK, index);
    }
@Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xFF1B1327);
        renderHeader(g);
        renderJokers(g);
        renderVouchers(g);
        renderPacks(g);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private void renderHeader(GuiGraphicsExtractor g) {
        g.text(font, "МАГАЗИН", 12, 14, 0xFFFFE082, false);
        g.text(font, "$" + state.money, width - 70, 14, 0xFFFFD54F, false);
        g.text(font, "Ante " + state.ante, 12, 28, 0xFFFFFFFF, false);
    }

    private void renderJokers(GuiGraphicsExtractor g) {
        int y = 60;
        g.text(font, "Джокеры", 12, y, 0xFFFFE082, false);
        y += 13;
        if (state.shopJokers.isEmpty()) {
            g.text(font, "Нет в продаже", 12, y, 0xFF9E9E9E, false);
            return;
        }
        for (String id : state.shopJokers) {
            Joker joker = JokerRegistry.byId(id);
            if (joker == null) {
                continue;
            }
            g.text(font, joker.displayName(), 12, y, 0xFFFFFFFF, false);
            g.text(font, "   " + joker.description(), 12, y + 10, 0xFF9E9E9E, false);
            y += 24;
        }
    }

    private void renderVouchers(GuiGraphicsExtractor g) {
        int x = width / 2;
        int y = 60;
        g.text(font, "Купоны", x, y, 0xFFFFE082, false);
        y += 13;
        if (state.shopVouchers.isEmpty()) {
            g.text(font, "Нет в продаже", x, y, 0xFF9E9E9E, false);
            return;
        }
        for (Voucher v : state.shopVouchers) {
            g.text(font, v.displayName(), x, y, 0xFFFFFFFF, false);
            g.text(font, "   " + v.description(), x, y + 10, 0xFF9E9E9E, false);
            y += 24;
        }
    }

    private void renderPacks(GuiGraphicsExtractor g) {
        int x = width - 190;
        int y = 60;
        g.text(font, "Пакеты", x, y, 0xFFFFE082, false);
        y += 13;
        if (state.shopPacks.isEmpty()) {
            g.text(font, "Нет в продаже", x, y, 0xFF9E9E9E, false);
            return;
        }
        for (String name : state.shopPacks) {
            try {
                BoosterPack pack = BoosterPack.valueOf(name);
                g.text(font, pack.displayName(), x, y, 0xFFFFFFFF, false);
                g.text(font, "   " + pack.cardCount() + " карт, выбрать "
                        + pack.picks(), x, y + 10, 0xFF9E9E9E, false);
            } catch (IllegalArgumentException e) {
                g.text(font, name, x, y, 0xFFFFFFFF, false);
            }
            y += 24;
        }
    }
}