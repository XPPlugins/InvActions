package me.xpyex.plugin.invactions.bukkit.module;

import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

public class QuickShulkerBox extends RootModule {
    private static final String METADATA_KEY = "InvActions_Shulker";

    private static Inventory openShulkerBoxItem(Player player, int slot) {
        if (isOpenedShulkerBoxByMe(player)) return null;  // 已经打开过潜影盒，但触发了事件，忽略

        ItemStack stack = player.getInventory().getItem(slot);
        if (stack == null) return null;
        if (stack.getAmount() != 1) return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof BlockStateMeta) {  //此处同时判断 != null
            BlockState state = ((BlockStateMeta) meta).getBlockState();
            if (state instanceof ShulkerBox) {  // 判断 潜影盒 和 != null
                Inventory boxInv = ((ShulkerBox) state).getInventory();
                player.setMetadata(METADATA_KEY, new FixedMetadataValue(InvActions.getInstance(), slot));
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

    private static boolean isOpenedShulkerBoxByMe(Metadatable sth) {
        return sth.hasMetadata(METADATA_KEY);
    }

    private static void playerCloseShulkerBox(Player player, Inventory inventoryWillBeClosed) {
        if (isOpenedShulkerBoxByMe(player)) {  // 当前是InvActions提供的潜影盒界面
            ItemStack stack = player.getInventory().getItem(player.getMetadata(METADATA_KEY).get(0).asInt());  // 因为不允许玩家移动潜影盒，所以ItemStack应当是安全的、无变化的
            if (stack != null && stack.getType().toString().endsWith("SHULKER_BOX")) {
                ItemMeta meta = stack.getItemMeta();
                if (meta instanceof BlockStateMeta) {  //此处同时判断 != null
                    BlockState state = ((BlockStateMeta) meta).getBlockState();
                    if (state instanceof ShulkerBox) {
                        ((ShulkerBox) state).getInventory().setContents(inventoryWillBeClosed.getContents());
                        ((BlockStateMeta) meta).setBlockState(state);
                        stack.setItemMeta(meta);  // 覆写ItemStack的Inv内容

                        inventoryWillBeClosed.clear();  // 清除临时打开的界面，避免被偷东西
                        player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 1f, 1f);
                        Bukkit.getScheduler().runTaskLater(InvActions.getInstance(), player::updateInventory, 2L);
                    }
                }
            }
            player.removeMetadata(METADATA_KEY, InvActions.getInstance());  // 移除标记
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (serverEnabled() && playerEnabled((Player) event.getWhoClicked())) {
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) return;  // 光标上有东西
            if (!event.getWhoClicked().getInventory().equals(event.getClickedInventory())) return;  // 点击的是容器

            if (event.getAction() == InventoryAction.CLONE_STACK) {  // 鼠标中键
                if (openShulkerBoxItem((Player) event.getWhoClicked(), event.getSlot()) != null) {
                    event.setCancelled(true);
                }
            } else if (event.isShiftClick() && event.isRightClick()) {  // Shift + 右键
                if (openShulkerBoxItem((Player) event.getWhoClicked(), event.getSlot()) != null) {
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
            if (openShulkerBoxItem(event.getPlayer(), event.getHand() == EquipmentSlot.OFF_HAND ? 40 : event.getPlayer().getInventory().getHeldItemSlot()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (isOpenedShulkerBoxByMe(event.getPlayer())) {
            if (event.getItemDrop().getItemStack().getType().toString().endsWith("SHULKER_BOX")) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(InvActions.getInstance(), () -> event.getPlayer().updateInventory(), 2L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onShulkerInvClick(InventoryClickEvent event) {
        if (isOpenedShulkerBoxByMe(event.getWhoClicked())) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType().toString().endsWith("SHULKER_BOX")) {  // 不允许点击潜影盒
                event.setCancelled(true);
            }
            if (event.getAction().toString().startsWith("HOTBAR_")) {  // 不允许快捷键交换
                event.setCancelled(true);
            }

            // 交换副手无需处理
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (isOpenedShulkerBoxByMe(event.getPlayer())) {
            event.setCancelled(true);
            MsgUtil.sendActionBar(event.getPlayer(), "[InvActions] &cIllegal command");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAnyClickWithoutInv(PlayerInteractEvent event) {
        if (isOpenedShulkerBoxByMe(event.getPlayer())) {
            event.setCancelled(true);
            MsgUtil.sendActionBar(event.getPlayer(), "[InvActions] &cIllegal click");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (isOpenedShulkerBoxByMe(event.getPlayer())) {
            event.setCancelled(true);
            MsgUtil.sendActionBar(event.getPlayer(), "[InvActions] &cIllegal chat");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event) {
        if (isOpenedShulkerBoxByMe(event.getPlayer())) {
            event.setCancelled(true);
            MsgUtil.sendActionBar(event.getPlayer(), "[InvActions] &cIllegal break");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        if (isOpenedShulkerBoxByMe(event.getPlayer())) {
            event.setCancelled(true);
            MsgUtil.sendActionBar(event.getPlayer(), "[InvActions] &cIllegal place");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        playerCloseShulkerBox((Player) event.getPlayer(), event.getInventory());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerCloseShulkerBox(event.getPlayer(), event.getPlayer().getOpenInventory().getTopInventory());
    }
}
