<!-- Modrinth 项目页「Description」直接整段复制粘贴。英文在前，中文在后。
     Paste everything below into the Modrinth Description box. English first, Chinese second. -->

# Auto Feeder

**Animal-farming automation for Forge 1.20.1.** A **feed trough** that makes animals breed on their own, plus a **shearer, milker, egg collector and fisher** that collect wool, milk, eggs and fish for you. Every machine has a population cap and an interval setting, so a big farm does not grind the server to a halt.

> ⚠️ **Early version (0.1.0).** It adds blocks, so it must be installed on the **server and on every client**. No other mod is required. Bug reports are very welcome.

**Requirements:** Minecraft 1.20.1 · Forge 47.x · Java 17

## What it adds

| Block | Size | What it does |
|---|---|---|
| **Feed Trough** | 1×1 | Put food in: animals around it go into love mode and breed. Also feeds babies and collects the XP that breeding drops |
| **Long Feed Trough** | 1×2 | The same, with twice the storage |
| **Shearing Machine** | 1×2, 2 high | Load shears: shears sheep, mooshrooms and other shearable animals in range |
| **Milking Machine** | 1×2, 2 high | Load empty buckets: milks adult cows, mooshrooms and goats in range |
| **Egg Collector** | 1×2, 2 high | Picks up the eggs chickens lay; needs no input |
| **Auto Fisher** | 1×2 | Load a fishing rod beside water: it fishes by itself. The rod and line reach out over the water in front, and the model shows no rod while it is empty |

All machines work with **hoppers** (insert into the input, extract from the 9-slot output), drop their contents when broken, and store the XP they produce: right-click with an empty hand to take it.

## Why it will not lag your server

- **A species is only fed when at least 2 adults are ready**, so food is not wasted on a lone animal.
- **Population cap**: once a species (babies included) reaches 12 within range, the trough stops feeding it.
- **Per-round budget and interval** (4 animals every 5 seconds by default), and a **blacklist** to keep chosen species, such as rare mod animals, from multiplying.
- **Babies are fed only while they still need more than 5 minutes to grow up.** Vanilla takes 10% of the *remaining* time off per feeding, so late feedings are mostly wasted food.
- The trough only collects XP in the **45 seconds after a feeding**, so it will not steal the XP of mobs you kill next to it.

## Recipes

| Block | Recipe |
|---|---|
| Feed Trough | 7 stone + 1 barrel |
| Long Feed Trough | 2 Feed Troughs |
| Shearer / Milker / Egg Collector / Fisher | 7 stone + 1 barrel + **shears / bucket / hopper / fishing rod** in the top middle |

## How to use

- **Trough:** right-click with food to fill it; sneak + empty hand takes the food back; **empty hand takes the XP, or, if there is none, lists the animals around it** (count, how many can breed, whether the cap is reached).
- **Machines:** right-click with the matching item to load it; **empty hand takes the output and XP**; sneak + empty hand takes the tool back. Attach hoppers to the block you clicked when placing.
- **Fisher:** the block in front of its second half should be water, one block below the machine. It needs at least 9 water blocks around it, fishes every 30 s by default, uses the vanilla fishing loot table (so biomes matter), is sped up by **Lure** and helped by **Luck of the Sea**, and wears the rod down by 1 per catch.

## Supported animals

The trough asks every animal what it eats, so **every vanilla breedable animal works**: cows, sheep, goats, pigs, rabbits, chickens, horses, donkeys, mules, llamas, camels, foxes, pandas, turtles, bees, frogs, sniffers, striders, hoglins, cats, ocelots and wolves. Horses, donkeys and mules only breed on golden food, as in vanilla.

**Animals from other mods** work if they extend the vanilla breeding classes. Add their food to the item tag `autofeeder:feed` with a datapack or KubeJS:

```json
// data/autofeeder/tags/items/feed.json
{ "replace": false, "values": ["somemod:special_feed"] }
```

## Configuration

`serverconfig/autofeeder-server.toml` in the world folder: `range`, `intervalTicks`, `maxSameTypeNearby`, `maxFedPerCycle`, `babyFeedMinSeconds`, `blacklist`, `collectXp`, `maxStoredXp`, `machineRange`, `machineIntervalTicks`, `machineMaxPerCycle`, `fisherIntervalTicks`, `fisherMinWater`, `fisherRange`. Details and defaults are in the README on GitHub.

## Limitations (please read)

- **Forge 1.20.1 only.** No Fabric, no other Minecraft versions. **Server and every client need it.**
- It only works on animals in **loaded chunks**: when you walk away and the chunks unload, the machines stop.
- **The fisher catches no treasure** (enchanted books, saddles, name tags, ...): that needs a real hook in open water. It catches fish and junk. It only counts water blocks and does not check for open water.
- Models are built from **vanilla textures**: the rod is a staircase of small blocks, and the eggs are white blocks. Replace the images in a resource pack for your own look.
- **Wolves and cats** only breed once tamed, which means your pets: put them on the blacklist if you do not want that. Axolotls and parrots are not supported.
- **Block states may change between 0.x versions**, so machines placed earlier may look wrong after an update. Take their contents out first and re-place them.

## Links

- Source code & bug reports: https://github.com/xiaofeiwuuu/auto-feeder
- When reporting a bug please include the game and Forge version, other animal mods installed, `latest.log` (or the crash report), which machine, and which animals were around.
- License: MIT.

---

# 自动喂食槽

**Forge 1.20.1 的动物养殖自动化 mod。** **食物槽**让动物自己繁殖，**剪毛机、挤奶机、集蛋机、钓鱼机**替你收羊毛、牛奶、鸡蛋和鱼。每台机器都有数量上限和间隔配置，养大规模农场也不会把服务器拖垮。

> ⚠️ **0.1.0 早期版本。** 它新增了方块，所以**服务端和每个客户端都要装**，不依赖其他 mod。非常欢迎反馈问题。

**运行环境：** Minecraft 1.20.1 · Forge 47.x · Java 17

## 内容

| 方块 | 大小 | 作用 |
|---|---|---|
| **食物槽** | 1×1 | 放进食物，周围动物自动进入繁殖状态；顺便喂幼崽、收集繁殖掉落的经验 |
| **长食物槽** | 1×2 | 同上，容量是两倍 |
| **剪毛机** | 1×2，2 格高 | 放入剪刀，自动剪范围内的羊、蘑菇牛等可剪动物 |
| **挤奶机** | 1×2，2 格高 | 放入空桶，自动挤范围内的成年牛、蘑菇牛、山羊 |
| **集蛋机** | 1×2，2 格高 | 吸收范围内鸡下的蛋，不需要输入 |
| **钓鱼机** | 1×2 | 放入钓鱼竿，在水边自动钓鱼；鱼竿和线伸到前方的水面上，没鱼竿时模型上也没有鱼竿 |

所有机器都支持**漏斗**（往输入格灌、从 9 格输出里抽），拆除时内容物掉落，并存下产生的经验：空手右键取出。

## 为什么不会卡服

- **同种动物里至少有 2 只成年、不在冷却的才喂**，不会把食物浪费在落单的动物身上。
- **数量上限**：范围内同种动物（含幼崽）达到 12 只，就不再喂。
- **每轮名额和间隔**（默认每 5 秒最多喂 4 只），还有**黑名单**：不想让某些物种（比如其他 mod 的稀有动物）泛滥。
- **幼崽只在还要长超过 5 分钟时才喂。** 原版每次喂食只缩短**剩余时间**的 10%，越到后面越浪费食物。
- 食物槽只在**喂食后的 45 秒内**收集经验，不会抢走你在旁边打怪掉的经验。

## 合成

| 方块 | 合成 |
|---|---|
| 食物槽 | 7 石头 + 1 木桶 |
| 长食物槽 | 2 个食物槽 |
| 剪毛机 / 挤奶机 / 集蛋机 / 钓鱼机 | 7 石头 + 1 木桶 + 中间上方放**剪刀 / 桶 / 漏斗 / 钓鱼竿** |

## 怎么用

- **食物槽：** 手持食物右键放入；潜行 + 空手取回食物；**空手右键取出经验，没有经验时列出周围动物**（数量、可繁殖数量、是否已达上限）。
- **机器：** 手持对应物品右键装入；**空手右键取走产物和经验**；潜行 + 空手右键取回工具。漏斗接在你放置时点的那一格上。
- **钓鱼机：** 第二格前面那一格应当是水，水面比机器低一格。周围要有至少 9 格水，默认 30 秒钓一次，读取原版钓鱼战利品表（群系不同掉落不同），**饵钓**加快、**海之眷顾**提高好东西概率，每钓一次鱼竿磨损 1 点。

## 支持的动物

食物槽读取每种动物自己的"能吃什么"，所以**原版所有能繁殖的动物都支持**：牛、羊、山羊、猪、兔子、鸡、马、驴、骡、羊驼、骆驼、狐狸、熊猫、海龟、蜜蜂、青蛙、嗅探兽、炽足兽、疣猪兽、猫、豹猫和狼。马、驴、骡和原版一样只认金色食物。

**其他 mod 的动物**：只要继承了原版的繁殖类就能用，把它们的食物加进物品标签 `autofeeder:feed` 即可（数据包或 KubeJS）：

```json
// data/autofeeder/tags/items/feed.json
{ "replace": false, "values": ["somemod:special_feed"] }
```

## 配置

存档里的 `serverconfig/autofeeder-server.toml`：`range`、`intervalTicks`、`maxSameTypeNearby`、`maxFedPerCycle`、`babyFeedMinSeconds`、`blacklist`、`collectXp`、`maxStoredXp`、`machineRange`、`machineIntervalTicks`、`machineMaxPerCycle`、`fisherIntervalTicks`、`fisherMinWater`、`fisherRange`。详细说明和默认值见 GitHub 的 README。

## 限制与注意（请先看）

- **仅支持 Forge 1.20.1**，不支持 Fabric 和其他版本。**服务端和每个客户端都要装。**
- 只处理**已加载区块**里的动物：你走远、区块卸载后，机器就停了。
- **钓鱼机钓不到宝藏**（附魔书、鞍、命名牌等）：那需要开阔水域里的真实鱼钩。产出的是鱼和垃圾。它只数水方块的数量，不检查是否开阔水域。
- 外观**全部用原版贴图拼成**：鱼竿是小方块拼的阶梯形，鸡蛋是白色方块。想换成自己的样子，在资源包里替换图片即可。
- **狼和猫**只有驯服的才会繁殖，也就是你的宠物：不想让它们被自动繁殖就加进黑名单。不支持蝾螈和鹦鹉。
- **0.x 版本之间方块的状态可能改变**，升级后已放置的旧机器可能显示异常：先把里面的东西取出来，再重新放置。

## 链接

- 源码与问题反馈：https://github.com/xiaofeiwuuu/auto-feeder
- 反馈问题时请附上：游戏和 Forge 版本、装了哪些其他动物类 mod、`latest.log`（崩溃时附崩溃报告）、是哪台机器、周围有哪些动物。
- 许可证：MIT。
