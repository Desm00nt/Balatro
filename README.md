# Balatro для Minecraft

Полный порт покерного roguelike **Balatro** как одиночного игрового режима
внутри Minecraft. Fabric, Minecraft 1.21, Java 21.

Игра полностью серверная: клиент только рисует состояние, все решения
принимаются на сервере. Прогресс сохраняется в мир и переживает реконнект.

## Команды

```
/balatro start [deck]   # начать забег (RED, BLUE, YELLOW, GREEN, BLACK, ABANDONED)
/balatro open           # открыть игровой стол
/balatro shop           # открыть магазин
/balatro status         # текущее состояние
/balatro stop           # прервать забег
/balatro decks          # список стартовых колод
```

## Как играть

1. `/balatro start` — выберите стартовую колоду
2. `/balatro open` — откроется стол с картами
3. Кликайте по картам, чтобы выбрать комбинацию, затем **Play Hand** или **Discard**
4. Победите слепого, зайдите в магазин, затем **Следующий раунд**
5. Играйте, пока не пройдёте Ante 8 (победа) или не кончатся руки (поражение)

## Архитектура

```
com.balatro.core     — чистый Java, ноль зависимостей от Minecraft (тестируется отдельно)
com.balatro.net      — сетевые пакеты Fabric Networking API
com.balatro.storage  — сохранение забега в NBT (SavedData)
com.balatro.command  — команда /balatro
com.balatro.client   — экраны и приём состояния (клиент)
```

## Реализованный функционал

### Ядро игры

| Компонент | Содержимое |
|---|---|
| `card` | Ранги, масти, карты с бонусами, изданиями и печатями; колода со взбором и перемешиванием |
| `hand` | 12 комбинаций Balatro с ростом по уровням; определение с учётом Five of a Kind, Flush House, Flush Five и «колеса» A-2-3-4-5 |
| `blind` | Small/Big/Boss Blind, рост требования ~1.6x за Ante |
| `joker` | **50 джокеров**: постоянные, по масти, по типу комбинации, масштабирующиеся и мультипликативные; система редкости с весами для магазина |
| `consumable` | 12 карт-планет, 22 таро-карты, 18 спектральных карт |
| `shop` | Магазин с рероллом, 18 купонов (vouchers), 5 типов боастер-паков |
| `blind/BossRules` | 17 боссов со своими правилами: The Wall, Psychic, Goad, Water, Window, Manacle, Eye, Mouth, Fish, Serpent, Palladium, Shrine, Tower, Cloud, House, Skill, Stone |
| `card/CardEdition` | Foil, Holographic, Polychrome, Negative, Glass, Lucky, Crowded |
| `card/CardSeal` | Red, Gold, Blue, Purple |
| `run` | Фазы цикла (игра → магазин → следующий раунд), экономика, проценты, условие победы |

### Формула очков

```
chips  = chipsOfHand(level) + Σ card.chips + Σ jokerChips
mult   = multOfHand(level)  + Σ card.mult  + Σ jokerMult
points = chips × mult × Π jokerMultipliers × Π editionMultipliers
```

### Экономика

- Награда за слепого + процент (`$1` за каждые полные `$5`, максимум `$5`)
- Зелёная колода вместо процента даёт `$2` за руку и `$1` за сброс
- Купон Castle снижает цены на 50%, Bull and Bear меняет доход после боссов
- Цены растут с каждым Ante

### Сохранение

Полный снимок забега (колода, рука, джокеры, расходники, купоны, магазин,
счётчики, уровни комбинаций) сериализуется в NBT и хранится в мире.
Забег подгружается при входе на сервер и сохраняется при выходе.

## Тесты

```bash
./gradlew coreTest      # 81 проверка: комбинации, очки, джокеры, слепые, колоды
./gradlew featureTest   # 90 проверок: магазин, купоны, боссы, таро, сохранение
./gradlew build         # сборка + оба набора тестов
```

Всего **171 проверка**, все проходят.

## Сборка

```bash
./gradlew build
# -> build/libs/balatro-0.1.0.jar
```

Требуется JDK 21. Для запуска Gradle используется JDK 25
(Fabric Loom 1.18 требует JVM 25, сам мод компилируется под Java 21).

## Лицензия

MIT