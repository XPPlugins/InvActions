package me.xpyex.plugin.invactions.bukkit.module;

import java.util.Objects;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.util.reflect.ClassUtil;
import me.xpyex.lib.xplib.util.reflect.MethodUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import me.xpyex.plugin.invactions.bukkit.util.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

public class ReplaceBrokenTool extends RootModule {
    @Override
    public boolean canLoad() {
        return MethodUtil.exist(PlayerInventory.class, "getItem", EquipmentSlot.class)
                   && ClassUtil.exist("org.bukkit.entity.ThrowableProjectile");
    }

    public void replaceTool(Player p, ItemStack before) {
        ItemStack i2 = p.getInventory().getItemInMainHand();
        EquipmentSlot slot = before.isSimilar(i2) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
        if (serverEnabled() && playerEnabled(p)) {  //如果玩家开启了替换手中道具
            Bukkit.getScheduler().runTaskLater(InvActions.getInstance(), () -> {
                if (p.getInventory().getItem(slot).getType() == Material.AIR) {
                    for (ItemStack content : p.getInventory().getContents()) {  //不遍历盔甲
                        if (content == null) continue;

                        if (content.isSimilar(before)) {
                            ItemStack copied = new ItemStack(content);
                            p.getInventory().setItem(slot, copied);
                            content.setAmount(0);
                            MsgUtil.sendActionBar(p, getMessageWithSuffix("run_out"));
                            return;
                        }
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent event) {
        replaceTool(event.getPlayer(), new ItemStack(event.getItem()));
        //
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemBreak(PlayerItemBreakEvent event) {
        if (!serverEnabled() || !playerEnabled(event.getPlayer())) {  //如果玩家未开启替换手中道具
            return;
        }
        EquipmentSlot slot = null;
        for (EquipmentSlot value : EquipmentSlot.values()) {
            if (event.getBrokenItem().equals(event.getPlayer().getInventory().getItem(value))) {
                slot = value;
                break;
            }
        }
        if (slot != null) {
            switch (slot) {
                case HAND:
                case OFF_HAND:
                    break;  //仅接受手中道具和副手道具损坏的情况
                default:
                    return;
            }
            EquipmentSlot finalSlot = slot;  //传给下面lambda的
            ItemStack brokenItem = new ItemStack(event.getBrokenItem());  //复制一份，因为接下来是2tick后执行，确保安全

            Bukkit.getScheduler().runTaskLater(InvActions.getInstance(), () -> {
                for (int i = 0; i < event.getPlayer().getInventory().getContents().length; i++) {
                    ItemStack content = event.getPlayer().getInventory().getItem(i);  //背包里的那份道具
                    if (content == null || content.getType() == Material.AIR) continue;

                    System.out.println("content.getType() = " + content.getType());
                    System.out.println("brokenItem.getType() = " + brokenItem.getType());
                    if (Objects.equals(content.getType(), brokenItem.getType())) {  //同一种类型的道具
                        System.out.println("stop");
                        InvUtil.swapSlot(event.getPlayer(), finalSlot, i);
                        MsgUtil.sendActionBar(event.getPlayer(), getMessageWithSuffix("broken"));
                        return;
                    }
                }
            }, 2L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {  //玩家射东西了
            if (event.getEntity() instanceof ThrowableProjectile && event.getEntity().getType() != EntityType.TRIDENT) {  //扔的还是投掷物
                ItemStack before = new ItemStack(((ThrowableProjectile) event.getEntity()).getItem());
                Player p = (Player) event.getEntity().getShooter();
                replaceTool(p, before);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        if (event.getItem() == null) return;

        if (event.getItem().hasItemMeta() && event.getItem().getItemMeta() instanceof Damageable)
            return;  //可破坏的物品留给上面处理，不在这处理

        if (event.getItem().getType() == Material.EGG || event.getItem().getType() == Material.ENDER_PEARL || event.getItem().getType() == Material.SPLASH_POTION || event.getItem().getType() == Material.SNOWBALL) {
            return;  //蛋、末影珍珠、喷溅型药水、雪球，  在onShoot方法处理
        }

        ItemStack before = new ItemStack(event.getItem());
        replaceTool(event.getPlayer(), before);
    }
}
