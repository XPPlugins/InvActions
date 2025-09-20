package me.xpyex.plugin.invactions.bukkit.enums;

import me.xpyex.lib.xplib.util.strings.StrUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum ItemType {
    ARMOR,  //装备
    ARROW,
    BLOCK,  //方块
    BOAT,  //船
    BUCKET,  //桶
    CARPET,
    CRAY,  //黏土
    CUSTOM_ITEM,
    ENCHANTED_BOOK,  //附魔书
    FENCE,  //栅栏
    FISH,  //鱼
    FOOD,  //食物
    HEAD,  //头颅
    ICE, //冰相关
    INGOT,  //锭
    LADDER,  //梯子
    LOG,  //原木
    MINE_CART,  //矿车
    NUGGET,  //金粒铁粒等
    ORE_BLOCK,  //矿石
    PLANKS,  //木板
    POTION,  //药水
    RAIL,  //铁轨
    RAW_ORE,  //粗矿
    RECORD,  //唱片
    SLAB,  //半砖
    STAIR,  //楼梯
    TOOL,  //工具
    WEAPON,  //武器
    WOOL,
    OTHER;

    public static boolean isAir(Material material) {
        switch (material.toString().toUpperCase()) {  //兼容1.16-，使用字符串对比
            case "AIR":
            case "CAVE_AIR":
            case "VOID_AIR":
            case "LEGACY_AIR":
                return true;
            default:
                return false;
        }
    }

    @NotNull
    public static ItemType getType(ItemStack stack) {
        if (stack.getType().isRecord())  //唱片
            return RECORD;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "RAIL"))  //铁轨
            return RAIL;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_WOOL"))
            return WOOL;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_CARPET"))
            return CARPET;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_SLAB"))  //半砖
            return SLAB;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_STAIRS"))  //楼梯
            return STAIR;

        else if (stack.getType() == Material.LADDER)  //梯子
            return LADDER;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "BOAT"))  //船
            return BOAT;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "MINECART"))  //矿车
            return MINE_CART;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_LOG", "_WOOD"))  //原木
            return LOG;

        else if (StrUtil.containsIgnoreCaseOr(stack.getType().toString(), "_FENCE"))  //栅栏、栅栏门
            return FENCE;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_SWORD", "BOW", "TRIDENT"))  //剑、弓、弩、三叉戟
            return WEAPON;

        else if (stack.getType() == Material.ENCHANTED_BOOK)  //附魔书
            return ENCHANTED_BOOK;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS"))  //防具
            return ARMOR;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_AXE", "_PICKAXE", "_SHOVEL", "_HOE", "FISHING_ROD"))  //工具
            return TOOL;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_HEAD", "_SKULL"))  //头颅
            return HEAD;

        else if (stack.getType().isEdible())  //食物
            return FOOD;

        else if (StrUtil.containsIgnoreCaseOr(stack.getType().toString(), "ARROW"))
            return ARROW;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_PLANKS"))
            return PLANKS;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "COD", "SALMON", "FISH"))  //鱼
            return FISH;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_INGOT"))  //锭
            return INGOT;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_NUGGET"))  //粒
            return NUGGET;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_ORE"))  //矿石
            return ORE_BLOCK;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_TERRACOTTA", "_CONCRETE"))  //陶瓦、混凝土
            return CRAY;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "POTION"))  //药水
            return POTION;

        else if (StrUtil.startsWithIgnoreCaseOr(stack.getType().toString(), "RAW_"))
            return RAW_ORE;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "BUCKET"))
            return BUCKET;

        else if (StrUtil.endsWithIgnoreCaseOr(stack.getType().toString(), "_ICE") || stack.getType() == Material.ICE)
            return ICE;

        else if (stack.getType().isBlock())  //其余方块
            return BLOCK;

        else if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
            return CUSTOM_ITEM;

        else
            return OTHER;
    }
}
