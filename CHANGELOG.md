# 更新日志 / Changelog

## 0.1.0 — 首个版本 / First release

适用于 Minecraft 1.20.1 + Forge 47.x，服务端和客户端都要安装。 / Minecraft 1.20.1, Forge 47.x, required on server and client.

**方块 / Blocks**
- 食物槽（1×1）、长食物槽（1×2）：动物自动繁殖，喂幼崽，收集经验，空手右键查看范围内动物统计。
  Feed trough (1×1) and long trough (1×2): breeding, baby feeding, XP collection, and an animal count on empty-hand click.
- 剪毛机、挤奶机、集蛋机（1×2，2 格高）：自动剪毛、挤奶、集蛋。 / Shearer, milker, egg collector (1×2, 2 high).
- 钓鱼机（1×2）：放鱼竿后在水边自动钓鱼，有鱼竿和没鱼竿两种外观。 / Auto fisher (1×2) with a rod / no-rod model.
- 漏斗兼容；拆除时内容物掉落；产出带经验。 / Hopper support, contents drop on break, XP from output.

**动物 / Animals**
- 支持所有原版可繁殖动物；马、驴、骡只认金色食物；其他 mod 的动物可通过食物标签 `autofeeder:feed` 追加。
  All vanilla breedable animals; horses only on golden food; other mods through the `autofeeder:feed` tag.

**防卡服 / Safety**
- 同种数量上限、每轮喂食数量、间隔、范围、物种黑名单均可配置；幼崽只在还要长超过 300 秒时才喂。
  Per-species cap, per-round budget, interval, range, species blacklist; babies fed only while they still need > 300 s.
