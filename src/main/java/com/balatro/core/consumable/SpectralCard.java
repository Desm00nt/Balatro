package com.balatro.core.consumable;

/**
 * Спектральная карта: мощное и опасное изменение забега.
 * Обычно попадает в колоду, а не используется с руки.
 */
public enum SpectralCard {

    ANKH("Ankh", 4),
    AURA("Aura", 4),
    WRAITH("Wraith", 4),
    SIGIL("Sigil", 4),
    OUIJA("Ouija", 4),
    ECTOPLASM("Ectoplasm", 4),
    IMMOLATE("Immolate", 4),
    ANTELUCIDEAN("Antelucidan", 4),
    DEJA_VU("Deja Vu", 4),
    HEX("Hex", 4),
    TRANCE("Trance", 4),
    MEDIUM("Medium", 4),
    SOUL("Soul", 4),
    CRYPTID("Cryptid", 4),
    FAMILIAR("Familiar", 4),
    GLASS_HOUSE("Glass House", 4),
    APPARITION("Apparition", 4),
    HEX_TRINITY("Hex Trinity", 4);

    private final String displayName;
    private final int basePrice;

    SpectralCard(String displayName, int basePrice) {
        this.displayName = displayName;
        this.basePrice = basePrice;
    }

    public String displayName() {
        return displayName;
    }

    public int basePrice() {
        return basePrice;
    }

    /** Текст эффекта для интерфейса. */
    public String effect() {
        return switch (this) {
            case ANKH -> "Raises the rank of all Aces by 1";
            case AURA -> "Adds +1 Mult for every card in the deck, per Joker";
            case WRAITH -> "Converts left card to the rank of the right card";
            case SIGIL -> "Links all cards in the deck to the suit of the first card";
            case OUIJA -> "Adds 6 random Tarot cards to the deck";
            case ECTOPLASM -> "Adds a Glass card to the deck for each selected card";
            case IMMOLATE -> "Destroys 5 selected cards, gain $20";
            case ANTELUCIDEAN -> "All cards become Glass, -1 hand size permanently";
            case DEJA_VU -> "Creates a Tarot card that was used earlier this run";
            case HEX -> "Adds a cursed card, −1 Mult per Hex";
            case TRANCE -> "25% Tarot, 25% Planet, 50% nothing";
            case MEDIUM -> "Adds a Medium Tarot card to the deck";
            case SOUL -> "Enhances a random Joker with its Soul edition";
            case CRYPTID -> "Adds a cursed Cryptid card";
            case FAMILIAR -> "Adds 3 random Tarot cards to the deck";
            case GLASS_HOUSE -> "x0.25 Mult for each Glass card held";
            case APPARITION -> "Extra +1 Mult per spectral card in the deck";
            case HEX_TRINITY -> "Adds 3 cursed cards, -1 discard";
        };
    }

    /** Сколько выбранных карт требуется. */
    public int requiredSelection() {
        return switch (this) {
            case ECTOPLASM, IMMOLATE, WRAITH -> 2;
            case HEX -> 1;
            default -> 0;
        };
    }

    /** Спектральные карты обычно кладутся в колоду, а не используются. */
    public boolean isAddedToDeck() {
        return switch (this) {
            case OUIJA, FAMILIAR, MEDIUM, ANKH, HEX, CRYPTID, ANTELUCIDEAN,
                 APPARITION, HEX_TRINITY -> true;
            default -> false;
        };
    }

    public static SpectralCard byName(String name) {
        for (SpectralCard s : values()) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown spectral: " + name);
    }

    public static java.util.List<SpectralCard> pool() {
        return java.util.Arrays.asList(values());
    }
}