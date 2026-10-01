package com.balatro.core.shop;

import com.balatro.core.run.RunState;

/**
 * Купон (voucher) — постоянное улучшение забега, покупается в магазине.
 * Каждый купон действует один раз; {@link #apply} меняет состояние забега.
 */
public enum Voucher {

    POLYJUICE("Polyjuice", 10) {
        @Override
        public void apply(RunState run) {
            run.addHandsPerRound(1);
            run.addMoney(-10);
        }
    },
    GRABBER("Grabber", 8) {
        @Override
        public void apply(RunState run) {
            run.addHandsPerRound(1);
        }
    },
    JOCKSTRAPS("Jockstraps", 4) {
        @Override
        public void apply(RunState run) {
            run.addHandsPerRound(1);
            run.addDiscardsPerRound(1);
        }
    },
    DRUNKARD("Drunkard", 4) {
        @Override
        public void apply(RunState run) {
            run.addDiscardsPerRound(1);
        }
    },
    DIET_COLA("Diet Cola", 5) {
        @Override
        public void apply(RunState run) {
            run.addDiscardsPerRound(1);
        }
    },
    NARCISSIST("Narcissist", 6) {
        @Override
        public void apply(RunState run) {
            run.addHandSize(3);
        }
    },
    GOLDEN_NEEDLE("Golden Needle", 7) {
        @Override
        public void apply(RunState run) {
            run.addHandSize(3);
            run.addDiscardsPerRound(-1);
        }
    },
    PASSWORD("Password", 4) {
        @Override
        public void apply(RunState run) {
            run.addHandSize(20);
        }
    },
    CERAMIC("Ceramic", 10) {
        @Override
        public void apply(RunState run) {
            run.setJokerBaseMultScale(0.5);
            run.addJokerSlots(3);
        }
    },
    ANKH("Ankh", 10) {
        @Override
        public void apply(RunState run) {
            run.setAnkhUsed(true);
        }
    },
    BULL_AND_BEAR("Bull and Bear", 30) {
        @Override
        public void apply(RunState run) {
            run.setBullBear(true);
        }
    },
    CASTLE("Castle", 10) {
        @Override
        public void apply(RunState run) {
            run.setVoucherDiscount(0.5);
        }
    },
    HYPERNOVA("Hypernova", 5) {
        @Override
        public void apply(RunState run) {
            run.setExtraCardsPerHand(5);
        }
    },
    TAROT_MERCHANT("Tarot Merchant", 4) {
        @Override
        public void apply(RunState run) {
            run.addTarotSlots(1);
        }
    },
    PLANET_MERCHANT("Planet Merchant", 4) {
        @Override
        public void apply(RunState run) {
            run.addPlanetSlots(1);
        }
    },
    RED_TAROT("Red Tarot", 3) {
        @Override
        public void apply(RunState run) {
            run.addConsumableSlots(1);
        }
    },
    BLUE_TAROT("Blue Tarot", 3) {
        @Override
        public void apply(RunState run) {
            run.addConsumableSlots(1);
        }
    };

    private final String displayName;
    private final int baseCost;

    Voucher(String displayName, int baseCost) {
        this.displayName = displayName;
        this.baseCost = baseCost;
    }

    public String displayName() {
        return displayName;
    }

    /** Базовая цена до применения скидок. */
    public int baseCost() {
        return baseCost;
    }

    public String description() {
        return switch (this) {
            case POLYJUICE -> "+1 Hand this round, costs $10 less";
            case GRABBER -> "+1 Hand per round";
            case JOCKSTRAPS -> "+1 Hand and +1 Discard per round";
            case DRUNKARD -> "+1 Discard per round";
            case DIET_COLA -> "+1 Discard per round";
            case NARCISSIST -> "+3 hand size";
            case GOLDEN_NEEDLE -> "+3 hand size, -1 Discard per round";
            case PASSWORD -> "+20 hand size for the first hand";
            case CERAMIC -> "Base jokers give 50% less mult, +3 Joker slots";
            case ANKH -> "Every Ace is worth +20 Mult";
            case BULL_AND_BEAR -> "+$20 after Boss Blinds, -$20 otherwise";
            case CASTLE -> "All vouchers and jokers are 50% off";
            case HYPERNOVA -> "+5 cards drawn at the start of the run";
            case TAROT_MERCHANT -> "+1 Tarot slot";
            case PLANET_MERCHANT -> "+1 Planet slot";
            case RED_TAROT -> "+1 consumable slot";
            case BLUE_TAROT -> "+1 consumable slot";
        };
    }

    /** Текущая цена с учётом скидки Castle. */
    public int price(RunState run) {
        double v = baseCost * run.voucherDiscount();
        return (int) Math.max(1, Math.round(v));
    }

    /** Можно ли купить купон прямо сейчас. */
    public boolean canBuy(RunState run) {
        return run.money() >= price(run);
    }

    /** Применяет эффект купона. */
    public void apply(RunState run) {
        // По умолчанию ничего не делает.
    }

    public static Voucher byName(String name) {
        for (Voucher v : values()) {
            if (v.name().equalsIgnoreCase(name)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown voucher: " + name);
    }
}