# Auto Feeder · 自动喂食槽

Animal-farming automation for **Minecraft Forge 1.20.1**: a **feed trough** that makes animals breed on their own, and a **shearer, milker, egg collector and fisher** that collect wool, milk, eggs and fish for you. Everything has a population cap and an interval setting, so it will not grind a server to a halt.

> 中文：[README.md](README.md)　|　License: [MIT](LICENSE)　|　Changelog: [CHANGELOG.md](CHANGELOG.md)

> ⚠️ **This is an early version (0.1.0).** Please report problems in [Issues](https://github.com/xiaofeiwuuu/auto-feeder/issues).

## Installation

1. Install **Minecraft 1.20.1** and **Forge 47.x**.
2. Put `auto-feeder-forge-1.20.1-<version>.jar` into `mods/` (**delete any older version first**).
3. **Install the same version on the server and on every client.** The mod adds blocks, so a player without it cannot join a server that has it.
4. No other mod is required.

## What it adds

| Block | Size | What it does |
|---|---|---|
| **Feed Trough** | 1×1 | Put food in: animals nearby go into love mode and breed. Also feeds babies so they grow faster, and collects the XP that breeding drops |
| **Long Feed Trough** | 1×2 | The same with twice the storage (18 slots) |
| **Shearing Machine** | 1×2, 2 high | Load shears: shears the sheep, mooshrooms and other shearable animals in range |
| **Milking Machine** | 1×2, 2 high | Load empty buckets: milks the adult cows, mooshrooms and goats in range |
| **Egg Collector** | 1×2, 2 high | Picks up the eggs chickens lay in range; needs no input |
| **Auto Fisher** | 1×2 | Load a fishing rod and put it beside water: it fishes by itself; the rod and line reach out over the water in front |

### Recipes

| Block | Recipe |
|---|---|
| Feed Trough | 7 stone + 1 barrel (`S S / SBS / SSS`) |
| Long Feed Trough | 2 Feed Troughs (shapeless) |
| Shearer / Milker / Egg Collector / Fisher | 7 stone + 1 barrel + in the top middle: **shears / bucket / hopper / fishing rod** (`SIS / SBS / SSS`) |

### Feed Trough

- **Right-click with food**: put it in. **Sneak + right-click with an empty hand**: take all the food back.
- **Right-click with an empty hand**: take the stored XP; if there is none, the chat lists each species around it, how many can breed, and whether the cap is reached.
- Every 5 seconds (configurable) it looks 6 blocks around (configurable):
  - a species is only fed when **at least 2** adults are ready, so food is not wasted;
  - once a species (babies included) reaches the cap (default 12) it is not fed;
  - at most 4 animals per round.
- **Babies** are fed only while they still need more than 300 seconds to grow up. Vanilla takes **10% of the remaining time** off per feeding, so late feedings buy almost nothing.
- **XP**: the orbs that breeding drops are absorbed into the trough for **45 seconds after a feeding** (up to 2000 points by default). Outside that window it leaves orbs alone, so it will not steal XP from mobs you kill next to it.
- Hoppers can fill it (it only accepts items in the feed tag).

### Using the machines

- **Right-click with the matching item**: load it (shears, bucket, fishing rod). The egg collector has no input.
- **Right-click with an empty hand**: take all the output and the stored XP.
- **Sneak + right-click with an empty hand**: take the loaded tool back.
- Hoppers can insert into the input and extract from the output (9 slots). **Attach the hopper to the block you clicked when placing** (the lowest block of the rear half), not the other one.
- Breaking any block of a machine removes the whole machine, drops its contents, and gives one item.
- To place one, every block it covers (including the ones above) must be free; the direction you face decides which way it extends.

| Machine | How it works |
|---|---|
| Shearer | One animal at a time, 1 durability off the shears; produces wool, mushrooms, ... |
| Milker | One empty bucket per animal, produces milk buckets; **output is limited only by the buckets you load**; stops when the output is full |
| Egg Collector | Only takes eggs, other dropped items are left alone |
| Fisher | One catch every 30 s by default, from the vanilla fishing loot table (so biomes matter); each level of **Lure** takes 5 s off (minimum 5 s), **Luck of the Sea** improves the loot; 1 durability off the rod per catch; 1–6 XP per catch, stored in the machine |

### Placing the fisher

- The block **in front of the machine's second half** should be water: the bobber is drawn on the water surface, designed for water **one block below** the machine.
- It needs at least **9 water blocks** around it (3 blocks horizontally, 3 down; configurable).
- Without a rod it does not work and the model shows no rod.

## Supported animals

The trough asks each animal what it eats, so every vanilla breedable animal works:

| Animal | Food |
|---|---|
| Cow, sheep, goat, mooshroom | Wheat |
| Pig | Carrot, potato, beetroot |
| Rabbit | Carrot, golden carrot, dandelion |
| Chicken | Wheat/melon/pumpkin/beetroot/torchflower seeds, pitcher pod |
| Sniffer | Torchflower seeds |
| Horse, donkey, mule | Golden apple, enchanted golden apple, golden carrot |
| Llama | Hay bale |
| Camel | Cactus |
| Fox | Sweet berries, glow berries |
| Panda | Bamboo |
| Turtle | Seagrass |
| Bee | Any flower |
| Frog | Slimeball |
| Strider / Hoglin | Warped fungus / crimson fungus |
| Cat, ocelot | Raw cod, raw salmon |
| Wolf | Any raw or cooked meat, rotten flesh |

- **Horses, donkeys and mules** only go into love mode on golden food (in vanilla, wheat, sugar and hay only heal and age them), so the trough is not cheaper than doing it by hand.
- **Wolves and cats** only breed once tamed, which means your pets. Put them on the blacklist if you do not want that.
- **Animals from other mods** work if they extend the vanilla breeding classes and implement "what do I eat" properly; add their food to the feed tag (below).

## Configuration

In the **world's** `serverconfig/autofeeder-server.toml` (`world/serverconfig/` on a dedicated server):

| Option | Default | Meaning |
|---|---|---|
| `range` | 6 | Trough range (a cube around the block, this far in every direction; 1–16) |
| `intervalTicks` | 100 | Ticks between two feeding rounds of a trough (20 ticks = 1 s) |
| `maxSameTypeNearby` | 12 | Stop feeding a species once this many (babies included) are in range |
| `maxFedPerCycle` | 4 | Animals fed per round |
| `babyFeedMinSeconds` | 300 | Babies are fed only while they need more than this many seconds |
| `blacklist` | `[]` | Entity ids never fed, e.g. `["minecraft:wolf", "alexsmobs:grizzly_bear"]`; they still count towards the cap |
| `collectXp` | `true` | Absorb the XP orbs breeding drops |
| `maxStoredXp` | 2000 | XP one trough or machine can hold |
| `machineRange` | 6 | Range of the shearer, milker and egg collector |
| `machineIntervalTicks` | 100 | Ticks between their rounds (the egg collector is fixed at 40) |
| `machineMaxPerCycle` | 4 | Animals the shearer / milker handle per round |
| `fisherIntervalTicks` | 600 | Ticks between two catches (600 = 30 s) |
| `fisherMinWater` | 9 | Water blocks needed around the fisher |
| `fisherRange` | 3 | Horizontal radius searched for water (and 3 blocks down) |

The range is a cube around **the block you clicked when placing** (the lowest block of the rear half), not the machine's midpoint.

### Adding more feeds

A trough only accepts items in the item tag `autofeeder:feed`. Add a file in a datapack or modpack (KubeJS works too):

```json
// data/autofeeder/tags/items/feed.json
{ "replace": false, "values": ["somemod:special_feed"] }
```

The tag only decides what can be put *in*; whether an animal eats it is the animal's own decision, so nothing gets fed wrongly.

## Known limitations

- **Forge 1.20.1 only** (47.x); no Fabric, no other versions; **needed on the server and on every client**.
- It only works on animals in **loaded chunks**; when you walk away and the chunks unload the machines stop (like vanilla redstone contraptions).
- **The fisher catches no treasure** (enchanted books, saddles, lily pads, name tags, ...): treasure needs the hook to be in open water and the machine has no real hook. It catches fish and junk.
- The fisher only *counts* water blocks and does not check for open water; a small puddle will do. Raise `fisherMinWater` if that is too lenient.
- All models are built from **vanilla textures**; the fisher's rod is a staircase of small blocks, not a smooth pole; the eggs are white blocks. Replace the images in a resource pack to use your own.
- Axolotls (they eat buckets of tropical fish) and parrots (not breedable animals) are not supported.
- **Block states may change between 0.x versions**: machines placed earlier may look wrong after an update and need to be broken and replaced. Take their contents out first.
- This is an early version; feedback is welcome.

## Building from source

JDK 17 needed.

```bash
./gradlew build          # compile + run unit tests; jar in build/libs/
./gradlew runClient
./gradlew runServer
```

## Reporting problems

Please open an [issue](https://github.com/xiaofeiwuuu/auto-feeder/issues) with: game and Forge version, other animal mods installed, `latest.log` (or the crash report), which machine, and which animals were around.

## License

[MIT](LICENSE) © 2026 xiaofeiwu
