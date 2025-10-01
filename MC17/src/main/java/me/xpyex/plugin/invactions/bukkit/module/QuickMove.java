package me.xpyex.plugin.invactions.bukkit.module;

import lombok.experimental.ExtensionMethod;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.util.reflect.MethodUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import me.xpyex.plugin.invactions.bukkit.util.EventUtil;
import me.xpyex.plugin.invactions.bukkit.util.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

@ExtensionMethod(EventUtil.class)
public class QuickMove extends RootModule {

    private static final String METADATA_CLICK = "InvActions_CallingClick";
    private static final String METADATA_DROP = "InvActions_QuickDropping";

    @Override
    public boolean canLoad() {
        return MethodUtil.exist(HumanEntity.class, "dropItem");
        //
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!serverEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!playerEnabled(((Player) event.getWhoClicked()))) return;
        if (event.getWhoClicked().hasMetadata(METADATA_CLICK)) return;  //正在广播事件给插件处理，自己无需处理
        if (event.getWhoClicked().hasMetadata(METADATA_DROP)) return;
        if (!isEndedCooldown(((Player) event.getWhoClicked()), 500)) return;  //一秒只能触发两次

        if (event.getClick() == ClickType.MIDDLE) {  //鼠标中键
            if (event.getCursor() == null || Material.AIR.equals(event.getCursor().getType())) {  //光标拿着的物品
                return;
            }
            ItemStack item = new ItemStack(event.getCursor());
            if (event.getClickedInventory() == null) {  //这就是丢出
                ItemStack tool;
                ItemStack i2 = event.getWhoClicked().getInventory().getItemInMainHand();
                if (!item.isSimilar(i2)) {
                    tool = new ItemStack(event.getWhoClicked().getInventory().getItemInMainHand());  //保存手上道具，待会set回去，下面丢出会覆盖主手道具
                } else {
                    tool = null;
                }
                event.getWhoClicked().setMetadata(METADATA_DROP, new FixedMetadataValue(InvActions.getInstance(), true));
                for (ItemStack content : event.getWhoClicked().getInventory().getStorageContents()) {
                    if (content == null || Material.AIR.equals(content.getType())) continue;

                    if (content.isSimilar(item)) {
                        ItemStack copied = new ItemStack(content);
                        event.getWhoClicked().getInventory().setItemInMainHand(copied);
                        content.setAmount(0);
                        event.getWhoClicked().dropItem(true);
                    }
                }

                if (event.getWhoClicked().getOpenInventory().getTopInventory() != event.getWhoClicked().getInventory()) {  //点击界面外，且打开了某个物品栏
                    event.getWhoClicked().setMetadata(METADATA_CLICK, new FixedMetadataValue(InvActions.getInstance(), true));
                    for (int i = 0; i < event.getWhoClicked().getOpenInventory().getTopInventory().getStorageContents().length; i++) {
                        ItemStack content = event.getWhoClicked().getOpenInventory().getTopInventory().getItem(i);
                        if (content == null || Material.AIR.equals(content.getType())) continue;

                        if (content.isSimilar(item)) {
                            if (new InventoryClickEvent(event.getView(), InventoryType.SlotType.OUTSIDE, i, ClickType.DROP, InventoryAction.DROP_ALL_SLOT).callEvent().isCancelled()) {
                                continue;
                            }
                            ItemStack copied = new ItemStack(content);
                            event.getWhoClicked().getInventory().setItemInMainHand(copied);
                            content.setAmount(0);
                            event.getWhoClicked().dropItem(true);
                        }
                    }
                }

                if (new InventoryClickEvent(event.getView(), InventoryType.SlotType.OUTSIDE, event.getWhoClicked().getInventory().getHeldItemSlot(), ClickType.DROP, InventoryAction.DROP_ALL_SLOT).callEvent().isCancelled()) {
                    event.getWhoClicked().removeMetadata(METADATA_CLICK, InvActions.getInstance());
                    event.getWhoClicked().removeMetadata(METADATA_DROP, InvActions.getInstance());
                    return;
                }
                event.getCursor().setAmount(0);
                event.getWhoClicked().getInventory().setItemInMainHand(item);
                event.getWhoClicked().dropItem(true);  //丢出光标道具

                event.getWhoClicked().getInventory().setItemInMainHand(tool);  //复原主手
                ((Player) event.getWhoClicked()).updateInventory();
                MsgUtil.sendActionBar((Player) event.getWhoClicked(), getMessageWithSuffix("drop"));
                event.getWhoClicked().removeMetadata(METADATA_CLICK, InvActions.getInstance());
                event.getWhoClicked().removeMetadata(METADATA_DROP, InvActions.getInstance());
            } else if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
                if (event.getWhoClicked().getOpenInventory().getTopInventory() == event.getWhoClicked().getInventory()) {
                    return;
                }
                if (InvUtil.isNotMenu(event.getWhoClicked().getOpenInventory().getTopInventory())) {
                    event.getWhoClicked().setMetadata(METADATA_CLICK, new FixedMetadataValue(InvActions.getInstance(), true));
                    for (int i = 0; i < event.getWhoClicked().getOpenInventory().getTopInventory().getStorageContents().length; i++) {
                        ItemStack content = event.getWhoClicked().getOpenInventory().getTopInventory().getItem(i);
                        int firstEmpty = event.getWhoClicked().getInventory().firstEmpty();
                        if (firstEmpty == -1) break;

                        if (content == null || Material.AIR.equals(content.getType())) continue;

                        if (content.isSimilar(item)) {
                            if (new InventoryClickEvent(event.getView(), InventoryType.SlotType.CONTAINER, i, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY).callEvent().isCancelled()) {
                                continue;
                            }
                            ItemStack copied = new ItemStack(content);
                            event.getWhoClicked().getInventory().setItem(firstEmpty, copied);
                            content.setAmount(0);
                        }
                    }
                    MsgUtil.sendActionBar((Player) event.getWhoClicked(), getMessageWithSuffix("move_to_player"));
                    event.getWhoClicked().removeMetadata(METADATA_CLICK, InvActions.getInstance());
                }
            } else if (event.getClickedInventory() == event.getWhoClicked().getOpenInventory().getTopInventory()) {
                if (InvUtil.isNotMenu(event.getClickedInventory())) {
                    event.getWhoClicked().setMetadata(METADATA_CLICK, new FixedMetadataValue(InvActions.getInstance(), true));
                    for (int i = 0; i < event.getWhoClicked().getInventory().getStorageContents().length; i++) {
                        ItemStack content = event.getWhoClicked().getInventory().getItem(i);

                        int firstEmpty = event.getWhoClicked().getOpenInventory().getTopInventory().firstEmpty();
                        if (firstEmpty == -1) break;

                        if (content == null || Material.AIR.equals(content.getType())) continue;

                        if (content.isSimilar(item)) {
                            if (new InventoryClickEvent(event.getView(), InventoryType.SlotType.CONTAINER, i, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY).callEvent().isCancelled()) {  //从玩家背包移动到容器
                                continue;
                            }
                            ItemStack copied = new ItemStack(content);
                            event.getWhoClicked().getOpenInventory().getTopInventory().setItem(firstEmpty, copied);
                            content.setAmount(0);
                        }
                    }
                    MsgUtil.sendActionBar((Player) event.getWhoClicked(), getMessageWithSuffix("move_to_container"));
                    event.getWhoClicked().removeMetadata(METADATA_CLICK, InvActions.getInstance());
                }
            }
        }
    }
}
