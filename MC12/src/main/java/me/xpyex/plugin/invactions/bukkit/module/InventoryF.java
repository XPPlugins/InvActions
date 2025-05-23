package me.xpyex.plugin.invactions.bukkit.module;

import me.xpyex.plugin.invactions.bukkit.util.InvUtil;
import me.xpyex.plugin.invactions.bukkit.util.SortUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryF implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPressFWithInv(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if ("SWAP_OFFHAND".equals(event.getClick().toString())) {  //在1.12没有这东西，要1.16才有，只能用字符串对比
            if (event.getClickedInventory() == event.getWhoClicked().getInventory() || InvUtil.isNotMenu(event.getClickedInventory())) {  //仅允许整理自己的背包，或是方块的界面，不允许整理菜单
                event.setCancelled(true);
                SortUtil.sortInv(event.getClickedInventory());
                if (event.getWhoClicked() instanceof Player) {
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }
                event.getWhoClicked().getInventory().setItemInOffHand(event.getWhoClicked().getInventory().getItemInOffHand());  //刷新副手
            }
        }
    }
}
