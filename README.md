# InvActions

[![bStats](https://img.shields.io/badge/bStats-17118-brightgreen)](https://bstats.org/plugin/bukkit/InvActions/17118)
[![GitHub release](https://img.shields.io/github/v/release/XPPlugins/InvActions)](https://github.com/XPPlugins/InvActions/releases)

**InvActions** 是一个 Minecraft 背包管理插件，提供一键整理、快速存取、自动补全等十余项实用功能，全面提升你的物品栏操作效率。

> 适用版本：Minecraft 1.9+ | 服务端：Spigot / Paper 及衍生端

---

## 功能一览

### 🎒 一键整理
| 操作 | 效果 |
|------|------|
| `F`（背包界面内） | 整理自身背包 |
| `Shift + F` | 整理看向的容器（箱子、木桶等） |
| 低头至最低 + `Shift + F` | 整理自身背包（不看向容器时） |

> 需服务端 1.16+ 支持 `SWAP_OFFHAND` 按键

### 📦 快捷潜影盒(QuickShulkerBox)
无需放置潜影盒，直接打开:
- **鼠标中键** 点击背包中的潜影盒
- **Shift + 右键** 点击背包中的潜影盒
- **Shift + 右键** 手持潜影盒时

> 打开期间会阻止聊天、命令、破坏/放置方块等操作，防止物品丢失

### 🛡️ 盔甲自动补全(ReplaceBrokenArmor)
身上盔甲损坏时，自动从背包中查找同类型装备并穿戴

### 🔧 工具自动补全(ReplaceBrokenTool)
手上工具/武器损坏或用尽时，自动从背包中查找同类型物品补全

### 🏹 更好的无限附魔(BetterInfinity)(1.21 以下)
弓带有 `无限` 附魔时，射箭不再需要背包中有箭支

### 🔱 更好的忠诚附魔(BetterLoyalty)
三叉戟带有 `忠诚` 附魔时，如果因距离过远或玩家离线而无法返回，会自动传送回玩家附近

### 🎯 快捷丢弃(QuickDrop)
潜行时按 `Q` 丢弃一个物品，自动将背包内所有相同物品一并丢出

### 📥 快捷移动(QuickDrop)
鼠标中键点击物品，快速将背包与容器间的所有相同物品一次性移动:
- 在玩家背包点击 → 从容器拉取至背包
- 在容器界面点击 → 从背包推送至容器
- 点击界面外 → 丢弃所有相同物品

### 💡 动态光源(DynamicLight)
手持光源类物品（火把、灯笼、荧石等）时，自动在玩家眼部位置模拟光源
发射火焰弹/火箭等带火焰的弹射物也会产生动态光效

### ⚒️ 自动切换工具(AutoTool)
挖掘方块时自动切换到背包中最适合的工具

### 🌾 自动播种(AutoFarm)
右键作物收获并自动补种

### 🥚 鸡蛋捕捉(EggCatcher)
投掷鸡蛋命中实体后，将实体捕获成生物蛋

### 🏭 合成掉落(CraftDrop)
在合成台合成物品，可以按住 `Ctrl+Q` 一次性合成所有并丢出

---

## 命令 & 权限

### 主命令
| 命令                   | 说明                             |
|----------------------|--------------------------------|
| `/InvActions`        | 打开功能设置 GUI 界面                  |
| `/InvActions reload` | 重载插件配置(需要`InvActions.admin`权限) |

> 别名: `/SortItems`  (老插件名)

### 权限节点
```
InvActions.use.module.<模块名>
```
如 `InvActions.use.module.BetterInfinity`
---

## 安装

1. 从 [Releases](https://github.com/XPPlugins/InvActions/releases) 下载最新版本
2. 将 `InvActions-*.jar` 放入服务器的 `plugins/` 目录
3. 重启或重载服务器
4. 使用 `/InvActions` 打开设置界面，按需开关各项功能

---

## 配置

### 服务端配置 (`config.json`)
管理员可在此控制每个功能的全局开关

### 玩家配置 (`players/<uuid>.json`)
每位玩家可独立开关各项功能，通过游戏内 GUI 或直接编辑 JSON 文件调整

### 语言文件 (`lang/`)
默认提供简体中文 (`zh_cn.json`)和英文(`en_us.json`)，可自行创建其他语言文件并在 `config.json` 中切换

---

### 项目结构
```
├── MC12/     # 主模块（核心功能 + 1.12+ 功能）
├── MC13/     # 1.13+ 功能（DynamicLight / EggCatcher / AutoFarmer）
└── MC17/     # 1.17+ 功能（QuickDrop / QuickMove / BetterLoyalty 等）
```

---

## 致谢

- 作者：**XPYEX** | QQ: 1723275529
- GitHub：[XPPlugins/InvActions](https://github.com/XPPlugins/InvActions)
- 数据统计：[bStats](https://bstats.org/plugin/bukkit/InvActions/17118)
