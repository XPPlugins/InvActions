package me.xpyex.lib.xplib.bukkit.inventory;

import java.util.Arrays;
import java.util.stream.Collectors;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.util.RootUtil;
import me.xpyex.lib.xplib.util.strings.StrUtil;
import me.xpyex.lib.xplib.util.value.ValueUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtil extends RootUtil {
    /**
     * 获取新的ItemStack
     *
     * @param stack 可复制ItemStack
     * @param name  修改显示名称
     * @param lore  修改Lore
     * @return 全新的ItemStack
     */
    public static ItemStack getItemStack(ItemStack stack, String name, String... lore) {
        ValueUtil.notNull("stack参数不应为null", stack);
        ItemStack out = new ItemStack(stack);
        ItemMeta meta = out.getItemMeta();
        if (name != null) meta.setDisplayName(MsgUtil.getColorMsg(name));
        if (lore != null && lore.length > 0) {
            meta.setLore(Arrays.stream(lore).map(MsgUtil::getColorMsg).collect(Collectors.toList()));
        }
        out.setItemMeta(meta);
        return out;
    }

    /**
     * 获取新的ItemStack
     *
     * @param material 新的ItemStack的类型
     * @param name     修改显示名称
     * @param lore     修改Lore
     * @return 全新的ItemStack
     */
    public static ItemStack getItemStack(Material material, String name, String... lore) {
        ValueUtil.notNull("material参数不应为null", material);
        return getItemStack(new ItemStack(material), name, lore);
        //
    }

    /**
     * 检查target是否为某种类型之一
     *
     * @param target    目标类型
     * @param materials 待检查的类型
     * @return target是materials之一
     */
    public static boolean typeIsOr(Material target, Material... materials) {
        if (ValueUtil.isEmpty(target, materials)) {
            return false;
        }

        for (Material material : materials) {
            if (target == material) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查target是否为某种类型之一
     *
     * @param target    目标类型
     * @param materials 待检查的类型
     * @return target是materials之一
     */
    public static boolean typeIsOr(Material target, String... materials) {
        return StrUtil.equalsIgnoreCaseOr(target.toString(), materials);
        //
    }

    /**
     * 检查ItemStack的类型是否为某种类型之一
     *
     * @param stack     目标ItemStack
     * @param materials 待检查的类型
     * @return stack的类型是materials之一
     */
    public static boolean typeIsOr(ItemStack stack, String... materials) {
        return typeIsOr(stack.getType(), materials);
        //
    }

    /**
     * 检查ItemStack的类型是否为某种类型之一
     *
     * @param stack     目标ItemStack
     * @param materials 待检查的类型
     * @return stack的类型是materials之一
     */
    public static boolean typeIsOr(ItemStack stack, Material... materials) {
        return typeIsOr(stack.getType(), materials);
    }

    /**
     * 检查Block的类型是否为某种类型之一
     *
     * @param block     目标ItemStack
     * @param materials 待检查的类型
     * @return block的类型是materials之一
     */
    public static boolean typeIsOr(Block block, Material... materials) {
        return typeIsOr(block.getType(), materials);
        //
    }

    /**
     * 检查Block的类型是否为某种类型之一
     *
     * @param block     目标ItemStack
     * @param materials 待检查的类型
     * @return block的类型是materials之一
     */
    public static boolean typeIsOr(Block block, String... materials) {
        return typeIsOr(block.getType(), materials);
        //
    }
}
