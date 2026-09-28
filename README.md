# PerfectLuckyPillar API

PerfectLuckyPillar（幸运之柱）的第三方开发接口。用于读取对局状态、监听对局与玩家的生命周期，以及**注册自定义事件（AreaEvent）**

---

## 安装

通过 [JitPack](https://jitpack.io) 引入。

### Gradle

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    // API 只需 compileOnly，运行期由 PerfectLuckyPillar 主插件提供
    compileOnly 'com.github.YXMAX:perfectluckypillar-api:v3.4.55'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.YXMAX</groupId>
    <artifactId>perfectluckypillar-api</artifactId>
    <version>v3.4.55</version>
    <scope>provided</scope>
</dependency>
```

---

## 包结构

| 包 | 内容 |
|---|---|
| `com.yxmax.pillar.api` | 入口类 `PerfectLuckyPillarAPI`、各管理器接口、`AreaEvent` |
| `com.yxmax.pillar.api.enums` | `GameStatus` |
| `com.yxmax.pillar.api.event.game` | 对局生命周期事件 |
| `com.yxmax.pillar.api.event.player` | 参赛玩家相关事件 |

---

## 入口类：PerfectLuckyPillarAPI

所有管理器通过静态方法获取：

| 方法 | 说明 |
|---|---|
| `getGamePlayerManager()` | 玩家管理器 |
| `getGameStatusManager()` | 对局状态管理器 |
| `getGameWorldManager()` | 世界管理器 |
| `getGameEventManager()` | 事件调度管理器 |
| `getPlayerLocaleManager()` | 多语言管理器 |
| `register(...)` | **仅主插件调用**，首次赋值生效，重复调用被忽略 |
| `unregister()` | **仅主插件调用**，主插件 `onDisable()` 时清空 |

主插件未启用（或已被卸载）时 getter 返回 `null`。把 PerfectLuckyPillar 写进 `softdepend`
并在 `onEnable()` 之后再取管理器，正常情况下无需判空；若你的插件在主插件之前加载，请自行判空。

---

## 管理器接口

### IGamePlayerManager

```java
Set<Player> getSurvivedGamers();       // 当前存活的参赛玩家
Player      getWinner();               // 获胜者
Player      getAttacker(Player player); // 对该玩家造成最后一次伤害的玩家
```

- `getSurvivedGamers()` 返回内部集合的**快照拷贝**，可以安全持有和遍历；修改它不会影响对局。
  只包含存活的参赛者，旁观者和已淘汰玩家不在其中。
- `getWinner()` 仅在场上**恰好剩 1 名**玩家时返回该玩家，否则返回 `null`。对局进行中、或结算时无人存活，都是 `null`。
- `getAttacker(player)` 用于归因击杀，按以下顺序解析最后一次伤害来源：
    1. 直接由玩家造成 → 返回该玩家；
    2. 由玩家生成的生物 / 玩家发射的弹射物造成 → 返回其背后的玩家（例如刷怪蛋召唤的凋灵、别人射出的箭）；
    3. 无法归因（纯环境伤害、自杀）或归因结果是自己 → 返回 `null`。

  该玩家本局从未受到过伤害时也返回 `null`，调用方**必须判空**。

### IGameStatusManager

```java
GameStatus getGameStatus();
```

`GameStatus`（`com.yxmax.pillar.api.enums.GameStatus`）取值：

| 值 | 含义 |
|---|---|
| `WAITING` | 等待玩家加入 / 倒计时中 |
| `RUNNING` | 对局进行中 |
| `FINISHED` | 已结算，播放烟花、准备重置 |
| `RESET` | 正在卸载并重载游戏世界 |

### IGameWorldManager

```java
World    getWorld();          // 游戏世界
World    getWaitWorld();      // 等待大厅世界
Location getCenterLocation(); // 获取游戏世界的中心位置
```

世界会在每局结束后被卸载重载，**不要缓存 `World` 实例**，每次用时重新取。

### IGameEventManager

```java
AreaEvent getCurrentEvent();               // 当前生效的事件
AreaEvent getNextEvent();                  // 下一个待触发的事件
int       getRunEventAmount();             // 本局已执行的有效事件次数
void      registerEvent(AreaEvent event);  // 将自定义事件注册到插件中
int       getDamageMultiply();             // 获取玩家之间的伤害倍率
void      setDamageMultiply(int multiply); // 设置玩家之间的伤害倍率
```

- 在固定顺序的事件模式 (`Default`) 和 有事件次数的随机事件模式下 (`Random` & `amount >= 0`) 的情况下:
- `getNextEvent()` 在所有事件完成后返回 `null`。
- `getCurrentEvent()` 在开局前返回内置的 `none_event`；所有事件完成后返回内置的 `event_finished`。
- `getRunEventAmount()` 不统计 `none_event`（随机模式下抽到「无事发生」不计数）。
- 伤害倍率是**全局单值**，只作用于玩家对玩家的近战伤害，每局开始时重置为 `1`。
  内置的 `double_damage`（×2）和 `instant_kill`（×60）同样写这个值，
  它们的 `eventEnd` 会无条件写回 `1` —— 自定义事件若也改倍率，注意别互相覆盖。

### IPlayerLocaleManager

```java
String getMessage(String locale, String path);
```

- 按语言代码读取 `locale/<locale>.yml` 里的文本。
- `locale` 用 `player.getLocale()`（如 `zh_cn`、`en_us`）；未收录的语言代码回退到 `en_us`。
- 若目标语言文件里**不存在该路径**，返回字符串 `MISSING VARIABLE`

```java
String msg = PerfectLuckyPillarAPI.getPlayerLocaleManager()
        .getMessage(player.getLocale(), "event.my_custom_event");
```

---

## Bukkit 事件

用标准 Bukkit 方式监听即可。

### 对局事件 `com.yxmax.pillar.api.event.game`

| 事件 | 触发时机 | 可取消 | 可用信息 |
|---|---|:-:|---|
| `GameStartEvent` | 倒计时结束、对局状态转为 `RUNNING` 时 | | `getGamers()` |
| `GameFightEvent` | 开局 3 秒后解除保护、PvP 开启时 | | `getGamers()` |
| `GameFinishEvent` | 对局状态转为 `FINISHED`、结算完成时 | | `getWinner()` |


```java
public class MyGameListener implements Listener {

    @EventHandler
    public void onStart(GameStartEvent event) {
        for (Player player : event.getGamers()) {
            player.sendMessage("祝你好运!");
        }
    }

    @EventHandler
    public void onFight(GameFightEvent event) {
        for (Player player : event.getGamers()) {
            player.sendMessage("PvP 已开启!");
        }
    }

    @EventHandler
    public void onFinish(GameFinishEvent event) {
        Player winner = event.getWinner();
        if (winner != null) {
            Bukkit.broadcastMessage(winner.getName() + " 获胜!");
        }
    }
}
```

### 玩家事件 `com.yxmax.pillar.api.event.player`

| 事件 | 触发时机           | 可取消 | 可用信息 |
|---|----------------|:-:|---|
| `GamerJoinEvent` | 玩家加入游戏 (游戏未开始) | | `getPlayer()` |
| `GamerLeaveEvent` | 幸存者离开游戏        | | `getPlayer()` |
| `GamerDeathEvent` | 参赛玩家即将被判定淘汰时   | ✅ | `getPlayer()` `getKiller()` |
| `GamerDamageEvent` | 参赛玩家受到伤害时      | ✅ | `getPlayer()` `getDamager()` `getPlayerDamager()` |

**`GamerDeathEvent`**

- `getKiller()` 为 `null` 表示自杀 / 环境死亡，否则是归因到的击杀者（同 `getAttacker()` 的解析规则）。
- 事件在主插件扣血判定之后、执行淘汰流程之前触发。取消它意味着：不播死亡音效、不广播死亡消息、不计击杀数、玩家**不会**被切成旁观者。
- 造成致死伤害的那次 Bukkit 伤害此时已经被主插件取消了，所以取消本事件后玩家会以**当前血量存活**，而不是被扣到 0。
- ⚠️ 掉入虚空触发的死亡被取消时，主插件不会再把玩家传回中心点，玩家会继续下落并反复触发本事件。取消虚空死亡时请自行传送玩家。

**`GamerDamageEvent`**

- `getDamager()` 是直接伤害源实体，可能为 `null`（摔落、火焰、窒息等环境伤害）。
- `getPlayerDamager()` 是「该算在谁头上」：`getDamager()` 本身是玩家就返回它，否则回退到 `getAttacker(target)`
  解析出生物主人 / 弹射物射手。可能为 `null`。
- 取消本事件会连带取消底层的 Bukkit 伤害事件（伤害不生效）。注意此时主插件的伤害倍率、致死判定**已经跑完**，
  所以本事件不适合用来改伤害数值，只适合做「这次伤害整体作废」的拦截。

```java
public class MyPlayerListener implements Listener {

    @EventHandler
    public void onDeath(GamerDeathEvent event) {
        Player killer = event.getKiller();
        if (killer != null) {
            killer.sendMessage("你击杀了 " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onDamage(GamerDamageEvent event) {
        // 示例: 让持有特定物品的玩家免疫一次非玩家伤害
        if (event.getPlayerDamager() == null && hasShieldCharm(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
```

---

## AreaEvent：自定义事件

`AreaEvent` 是「幸运之柱游戏事件」, 主插件每隔一段时间挑一个事件执行 —— 下箭雨、发物资、缩边界之类。你可以注册自己的事件，和内置事件一起参与抽取。

### 接口定义

```java
public interface AreaEvent {

    String getEventName(Player player);

    String getEventId();

    void eventStart(World world, Set<Player> players);

    void eventEnd(World world, Set<Player> players);
}
```

| 方法 | 说明                                                        |
|---|-----------------------------------------------------------|
| `getEventId()` | 唯一标识。不能与插件内置事件 ID 冲突，`none_event` 与 `event_finished` 为保留值 |
| `getEventName(Player)` | 显示给该玩家的事件名（用于 title 提示、计分板）。按玩家语言返回，**会被异步调用**            |
| `eventStart(world, players)` | 事件开始                                                      |
| `eventEnd(world, players)` | 事件结束，用于撤销 `eventStart` 的副作用                               |

`eventStart` / `eventEnd` 由主插件在**全局区域线程**上调用，可以直接用 Bukkit API；
`players` 是调用时刻的存活参赛者，不要长期持有。`getEventName` 在**异步线程**被调用，里面不要碰 Bukkit API。

### 注册自定义事件

#### 第一步：设置该插件为 `softdepend` (`plugin.yml`)

```yaml
softdepend:
  - PerfectLuckyPillar
```

#### 第二步：创建属于自己的自定义事件类 (示例)

```java
/**
 * 陨石雨：在每位玩家头顶反复落下点燃的 TNT，持续约 10 秒。
 */
public class MeteorShowerEvent implements AreaEvent {

    private final Random random = new Random();

    @Override
    public String getEventId() {
        return "meteor_shower";
    }

    @Override
    public String getEventName(Player player) {
        // 若不区分玩家客户端语言 只需要返回对应的语言字符串
        return "陨石雨";
        // 若需要区分玩家客户端语言 可自己实现获取语言
        // 或使用插件内置的玩家语言管理类
        // 但请务必在所有语言文件中添加对应ID的消息: en_us, zh_cn, zh_tw, zh_hk
        return PerfectLuckyPillarAPI.getPlayerLocaleManager()
                .getMessage(player.getLocale(), "event.meteor_shower");
    }

    @Override
    public void eventStart(World world, Set<Player> players) {
        for (Player player : players) {
            if (!player.isOnline()) {
                continue;
            }
            Location loc = player.getLocation().add(
                    random.nextDouble() * 4 - 2, 12, random.nextDouble() * 4 - 2);
            world.spawnEntity(loc, EntityType.TNT);
        }
    }

    @Override
    public void eventEnd(World world, Set<Player> players) {
        // 一次性事件，无需撤销
    }
}
```

#### 第三步：调用主插件API并注册

```
PerfectLuckyPillarAPI.getGameEventManager().registerEvent(new MeteorShowerEvent());
```

在你的插件 `onEnable()` 里注册即可。主插件在每局开局时才根据 `config.yml` 组装事件列表，
所以只要在第一局开始前注册完成就会生效。

> 载入成功时控制台会打印 `成功载入自定义事件: <id>` / `Loaded custom event: <id>`，可据此确认。

#### 第四步: 添加 事件ID 至 `config.yml`

```yaml
game:
  event:
    list:
      - 'meteor_shower'
```

未写进 `list` 的事件即使注册成功也不会被抽到。

---

## 内置事件清单

位于主模块 `com.yxmax.pillar.event`。切勿与内置 事件ID 冲突。

| 事件 ID | 类 | 效果                      | 最低版本 | `eventEnd` 有撤销 |
|---|---|-------------------------|---|:-:|
| `arrow_rain` | `ArrowRainEvent` | 每位玩家头顶持续落箭，持续大概 15 秒    | — | |
| `auto_rotate` | `AutoRotateEvent` | 强制转动玩家视角约 9 秒           | — | |
| `border_shrank` | `BorderShrankEvent` | 边界直接收缩到最小值              | — | |
| `creeper_strike` | `CreeperStrikeEvent` | 每位玩家身边生成引信 40 tick 的苦力怕 | — | |
| `crossbow_firework` | `CrossbowFireworkEvent` | 发放弩 + 烟花火箭              | — | |
| `double_damage` | `DoubleDamageEvent` | 伤害倍率 ×2                 | — | ✅ |
| `elytra_travel` | `ElytraTravelEvent` | 发放鞘翅 + 3 个烟花火箭          | — | |
| `enderdragon_strike` | `EnderDragonStrikeEvent` | 生成末影龙                   | — | |
| `ghast_strike` | `GhastStrikeEvent` | 在地图中心生成恶魂               | — | |
| `instant_kill` | `InstantKillEvent` | 伤害倍率 ×60                | — | ✅ |
| `interact_range` | `InteractRangeEvent` | 交互距离提升到 6 / 7.5         | 1.20.5 | ✅ |
| `jump_boost` | `JumpBoostEvent` | 跳跃提升 V，240 tick         | — | |
| `large_player` | `LargePlayerEvent` | 体型 ×2.2 并发光             | 1.20.5 | ✅ |
| `levitation` | `LevitationEvent` | 飘浮 I，200 tick           | — | |
| `mace_all` | `MaceAllEvent` | 发放重锤                    | 1.20.5 | |
| `mini_player` | `MiniPlayerEvent` | 体型 ×0.5 并发光             | 1.20.5 | ✅ |
| `monster_strike` | `MonsterStrikeEvent` | 入夜并在每位玩家身边生成 5 只怪       | — | ✅ 恢复时间 |
| `phantom_strike` | `PhantomStrikeEvent` | 入夜并在中心生成 3 只幻翼          | — | ✅ 恢复时间 |
| `rainy_day` | `RainyDayEvent` | 下雨并发放激流 III 三叉戟         | — | |
| `respawn_anchor` | `RespawnAnchorEvent` | 发放重生锚 + 荧石              | — | |
| `shield_all` | `ShieldAllEvent` | 发放盾牌                    | — | |
| `slow_falling` | `SlowFallingEvent` | 缓降，200 tick             | — | |
| `spear_all` | `SpearAllEvent` | 发放黄金长矛                  | 1.21.11 | |
| `tnt_all` | `TntAllEvent` | 发放 TNT                  | — | |
| `totem_all` | `TotemAllEvent` | 发放不死图腾                  | — | |
| `wind_charge` | `WindChargeEvent` | 发放风弹                    | 1.20.5 | |
| `wither_strike` | `WitherStrikeEvent` | 生成凋灵                    | — | |
| `wool_all` | `WoolAllEvent` | 发放 16 个黄绿色羊毛            | — | |

版本不满足的事件会在载入时被跳过，不影响其余事件。

### 保留事件

| ID | 类 | 说明 |
|---|---|---|
| `none_event` | `event.exception.NoneEvent` | 随机模式下「无事发生」，不计入已执行次数，不显示 title |
| `event_finished` | `event.exception.FinishedEvent` | 事件序列已全部执行完毕，不显示 title |

---

## 常见实现

**1. 发物资** — 优先进背包，满了掉在脚下

```java
@Override
public void eventStart(World world, Set<Player> players) {
    for (Player player : players) {
        ItemStack item = new ItemStack(Material.TNT);
        HashMap<Integer, ItemStack> rest = player.getInventory().addItem(item);
        if (!rest.isEmpty()) {
            world.dropItem(player.getLocation(), item);
        }
    }
}
```

**2. 加药水效果** — 靠时长自然过期，`eventEnd` 留空

```java
@Override
public void eventStart(World world, Set<Player> players) {
    PotionEffect effect = new PotionEffect(PotionEffectType.LEVITATION, 200, 0, true, false);
    for (Player player : players) {
        player.addPotionEffect(effect, null);
    }
}
```

**3. 改属性 / 全局开关** — `eventEnd` 还原

```java
@Override
public void eventStart(World world, Set<Player> players) {
    for (Player player : players) {
        player.getAttribute(Attribute.valueOf("SCALE")).setBaseValue(2.2);
    }
}

@Override
public void eventEnd(World world, Set<Player> players) {
    for (Player player : players) {
        player.getAttribute(Attribute.valueOf("SCALE")).setBaseValue(1);
    }
}
```

**4. 刷怪 / 改世界**

```java
@Override
public void eventStart(World world, Set<Player> players) {
    world.spawnEntity(centerLocation, EntityType.GHAST);
}
```

**5. 改伤害倍率** — `eventEnd` 写回 `1`

```java
@Override
public void eventStart(World world, Set<Player> players) {
    PerfectLuckyPillarAPI.getGameEventManager().setDamageMultiply(3);
}

@Override
public void eventEnd(World world, Set<Player> players) {
    PerfectLuckyPillarAPI.getGameEventManager().setDamageMultiply(1);
}
```