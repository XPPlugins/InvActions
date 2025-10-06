package me.xpyex.plugin.invactions.bukkit.module;

import me.xpyex.plugin.invactions.bukkit.InvActions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class QuickShulkerBox extends RootModule {
    private static final String METADATA_KEY = "InvActions_Shulker";

    private static Inventory openShulkerBoxItem(Player player, ItemStack stack) {
        if (stack == null) return null;
        if (stack.getAmount() != 1) return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof BlockStateMeta) {  //此处同时判断 != null
            BlockState state = ((BlockStateMeta) meta).getBlockState();
            if (state instanceof ShulkerBox) {
                Inventory boxInv = ((ShulkerBox) state).getInventory();
                player.setMetadata(METADATA_KEY, new FixedMetadataValue(InvActions.getInstance(), stack));
                player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1f, 1f);
                try {
                    player.openInventory(boxInv);
                    return boxInv;
                } catch (Throwable e) {
                    Inventory inventory = Bukkit.createInventory(player, 27);
                    inventory.setContents(boxInv.getContents());
                    player.openInventory(inventory);
                    return inventory;
                }
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (serverEnabled() && playerEnabled((Player) event.getWhoClicked())) {
            if (event.isShiftClick() && event.isRightClick()) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) return;

                if (openShulkerBoxItem((Player) event.getWhoClicked(), event.getCurrentItem()) != null) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (!serverEnabled() || !playerEnabled(event.getPlayer())) {
            return;
        }
        if (event.getPlayer().isSneaking() && event.getAction().toString().startsWith("RIGHT_")) {
            if (openShulkerBoxItem(event.getPlayer(), event.getItem()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().hasMetadata(METADATA_KEY)) {
            if (event.getItemDrop().getItemStack().getType().toString().endsWith("SHULKER_BOX")) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(InvActions.getInstance(), () -> event.getPlayer().updateInventory(), 2L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked().hasMetadata(METADATA_KEY)) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType().toString().endsWith("SHULKER_BOX")) {  // 不允许点击潜影盒
                event.setCancelled(true);
            }
            if (event.getAction().toString().startsWith("HOTBAR_")) {  // 不允许快捷键交换
                event.setCancelled(true);
            }

            // 交换副手无需处理，交换完也在玩家包里
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (event.getPlayer().hasMetadata(METADATA_KEY)) {  // 当前是InvActions提供的潜影盒界面
            ItemStack stack = (ItemStack) event.getPlayer().getMetadata(METADATA_KEY).get(0).value();  // 因为不允许玩家移动潜影盒，所以ItemStack应当是安全的、无变化的
            if (stack != null) {
                ItemMeta meta = stack.getItemMeta();
                if (meta instanceof BlockStateMeta) {  //此处同时判断 != null
                    BlockState state = ((BlockStateMeta) meta).getBlockState();
                    if (state instanceof ShulkerBox) {
                        ((ShulkerBox) state).getInventory().setContents(event.getInventory().getContents());
                        ((BlockStateMeta) meta).setBlockState(state);
                        stack.setItemMeta(meta);  // 覆写ItemStack的Inv内容

                        event.getInventory().clear();  // 清除临时打开的界面，避免被偷东西
                        event.getPlayer().removeMetadata(METADATA_KEY, InvActions.getInstance());  // 移除标记
                        ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 1f, 1f);
                        Bukkit.getScheduler().runTaskLater(InvActions.getInstance(), () -> {
                            ((Player) event.getPlayer()).updateInventory();
                        }, 2L);
                    }
                }
            }
        }
    }
}
