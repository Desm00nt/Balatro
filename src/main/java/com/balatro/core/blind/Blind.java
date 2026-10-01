package com.balatro.core.blind;

/**
 * Слепой — цель раунда. Требуемое количество очков растёт с каждым
 * «ante» (уровнем) примерно в 1.6 раза.
 */
public final class Blind {

    private final int ante;
    private final BlindType type;
    private final int reward;
    private final int scoreTarget;

    public Blind(int ante, BlindType type, int reward) {
        this.ante = ante;
        this.type = type;
        this.reward = reward;
        this.scoreTarget = calculateTarget(ante, type);
    }

    /** Требование к очкам для заданного ante и типа слепого. */
    public static int calculateTarget(int ante, BlindType type) {
        double scaled = type.baseRequirement() * Math.pow(1.6, ante - 1);
        // Округление до «красивого» числа, как в оригинале.
        int rounded = (int) (scaled / 100.0) * 100;
        return Math.max(100, rounded);
    }

    public int ante() {
        return ante;
    }

    public BlindType type() {
        return type;
    }

    public int reward() {
        return reward;
    }

    public int scoreTarget() {
        return scoreTarget;
    }

    public String displayName() {
        return "Ante " + ante + " — " + type.displayName();
    }

    @Override
    public String toString() {
        return displayName() + " (" + scoreTarget + ")";
    }
}
