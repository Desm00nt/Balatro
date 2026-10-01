# Balatro для Minecraft

Покерный roguelike **Balatro** как одиночный игровой режим внутри Minecraft.
Мод написан на Java 21 + Fabric для Minecraft 1.21.

## Что реализовано

### Ядро игры (`com.balatro.core`, чистый Java без Minecraft)

| Компонент | Описание |
|---|---|
| `card/` | Ранги, масти, карты (с бонусами, эффектами, debuff), колода со взбором и перемешиванием |
| `hand/` | 12 типов комбинаций Balatro и их определение, включая Five of a Kind, Flush House, Flush Five и «колесо» A-2-3-4-5 |
| `blind/` | Small/Big/Boss Blind с ростом требования ~1.6x за каждый Ante |
| `joker/` | 20 джокеров (постоянные, по масти, по типу комбинации) с системой редкости и весами в магазине |
| `run/` | Полный забег: слепые, деньги, уровни комбинаций, экономика, покупка/продажа джокеров |

### Формула очков

```
chips  = chipsOfHand(level) + Σ card.chips + Σ jokerChips
mult   = multOfHand(level)  + Σ card.mult  + Σ jokerMult
points = chips × mult × Π jokerMultipliers
```

### Базовые значения комбинаций (уровень 1)

| Комбинация | Chips | Mult | +Chips/ур. | +Mult/ур. |
|---|---|---|---|---|
| High Card | 5 | 1 | 10 | 1 |
| Pair | 10 | 2 | 15 | 2 |
| Two Pair | 20 | 2 | 20 | 2 |
| Three of a Kind | 30 | 3 | 20 | 3 |
| Straight | 30 | 4 | 30 | 3 |
| Flush | 35 | 4 | 15 | 2 |
| Full House | 40 | 4 | 25 | 2 |
| Four of a Kind | 60 | 7 | 30 | 3 |
| Straight Flush | 100 | 8 | 40 | 4 |
| Five of a Kind | 120 | 12 | 35 | 3 |
| Flush House | 140 | 14 | 40 | 4 |
| Flush Five | 160 | 16 | 50 | 3 |

### Стартовые колоды

| Колода | Бонус |
|---|---|
| Red | +1 сброс за раунд |
| Blue | +1 рука за раунд |
| Yellow | Начинаешь с $10 |
| Green | +$2 за неиспользованную руку, +$1 за сброс, без процента |
| Black | +1 слот джокера, −1 рука за раунд |
| Abandoned | Без карт с картинками (40 карт) |

### Minecraft-слой

- Команда `/balatro` с подкомандами `start`, `stop`, `status`, `decks`

## Команды

```
/balatro start [deck]   # начать забег (deck: RED, BLUE, YELLOW, GREEN, BLACK, ABANDONED)
/balatro stop           # прервать забег
/balatro status         # текущее состояние
/balatro decks          # список стартовых колод
```

## Сборка

```bash
./gradlew build        # готовый .jar в build/libs/
./gradlew coreTest     # 54 теста ядра игры
```

Требуется JDK 21.

## Roadmap

- [ ] GUI-экран стола с картами, кнопками Play Hand / Discard
- [ ] Сетевая синхронизация состояния забега
- [ ] Тара-карты (22) и спектральные карты (18)
- [ ] Карты-планеты с повышением уровней комбинаций
- [ ] Магазин, купоны, бустеры
- [ ] Босс-эффекты слепых
- [ ] Сохранение забега между сессиями
- [ ] Локализация на русский и английский

## Лицензия

MIT
