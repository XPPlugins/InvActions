package me.xpyex.plugin.invactions.bukkit.util;

import java.util.ArrayList;
import java.util.TreeMap;
import me.xpyex.lib.xplib.api.Pair;
import me.xpyex.plugin.invactions.bukkit.enums.ItemType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SortUtil {

    public static void sortPlayerInv(PlayerInventory inv) {
        Inventory i = Bukkit.createInventory(inv.getHolder(), 27);
        int start_p = 9;
        for (int start_i = 0; start_i < 27; start_i++) {
            i.setItem(start_i, inv.getItem(start_p));
            start_p++;
        }
        sortInv(i);
        start_p = 9;
        for (int start_i = 0; start_i < 27; start_i++) {
            inv.setItem(start_p, i.getItem(start_i));
            start_p++;
        }
    }

    public static void sortInv(Inventory inv) {
        if (inv instanceof PlayerInventory) {
            sortPlayerInv((PlayerInventory) inv);
            return;
        }

        TreeMap<String, Pair<ItemStack, Integer>> items = new TreeMap<>();
        for (ItemStack is0 : inv.getStorageContents()) {
            if (is0 == null) {
                continue;
            }
            ItemStack is = new ItemStack(is0);
            int amount = is.getAmount();
            is.setAmount(1);
            if (items.containsKey(is.toString())) {
                items.put(is.toString(), Pair.of(is, items.get(is.toString()).getValue() + amount));
            } else {
                items.put(is.toString(), Pair.of(is, amount));
            }
        }

        TreeMap<ItemType, ArrayList<Pair<ItemStack, Integer>>> computed = new TreeMap<>();

        for (ItemType t : ItemType.values()) {
            computed.put(t, new ArrayList<>());
        }

        items.values().forEach((pair) -> {
            computed.get(ItemType.getType(pair.getKey())).add(pair);
            //分类过程
        });

        //整理过程
        int slot = 0;
        inv.clear();
        for (ArrayList<Pair<ItemStack, Integer>> list : computed.values()) {
            for (Pair<ItemStack, Integer> pair : list) {
                int amount = pair.getValue();
                while (amount > pair.getKey().getMaxStackSize()) {
                    ItemStack result = new ItemStack(pair.getKey());
                    result.setAmount(result.getMaxStackSize());
                    inv.setItem(slot, result);
                    amount = amount - result.getMaxStackSize();
                    slot++;
                }
                if (amount <= pair.getKey().getMaxStackSize() && amount > 0) {
                    ItemStack result = new ItemStack(pair.getKey());
                    result.setAmount(amount);
                    inv.setItem(slot, result);
                    slot++;
                }
            }
        }
    }
}
