package me.xpyex.plugin.invactions.bukkit.module;

import java.util.Collection;
import lombok.experimental.ExtensionMethod;
import me.xpyex.lib.xplib.bukkit.inventory.ItemUtil;
import me.xpyex.lib.xplib.bukkit.language.LangUtil;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.util.strings.StrUtil;
import me.xpyex.plugin.invactions.bukkit.config.InvActionsServerConfig;
import me.xpyex.plugin.invactions.bukkit.util.EventUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@ExtensionMethod(EventUtil.class)
public class AutoFarmer extends RootModule {
    @Override
    protected boolean canLoad() {
        try {
            Block.class.getMethod("getBlockData");
            return true;
        } catch (NoSuchMethodError | NoSuchMethodException e) {
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (serverEnabled() && playerEnabled(event.getPlayer())) {  //如果玩家开启自动收割
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!isEndedCooldown(event.getPlayer(), 100)) return;  //每2tick一次

                if (ItemUtil.typeIsOr(event.getClickedBlock(), Material.MELON, Material.PUMPKIN)) {
                    if (!InvActionsServerConfig.getCurrent().isAutoFarmer_AllowPumpkinAndMelon()) return;

                    if (event.getItem() != null && event.getItem().getType() == Material.SHEARS) return;

                    if (new BlockBreakEvent(event.getClickedBlock(), event.getPlayer()).callEvent().isCancelled())
                        return;
                    event.setCancelled(true);
                    MsgUtil.sendActionBar(event.getPlayer(), getMessageWithSuffix("harvest", LangUtil.getItemName(event.getClickedBlock().getType())));
                    event.getClickedBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
                    return;
                }

                BlockData blockData = event.getClickedBlock().getBlockData();
                if (blockData instanceof Ageable && ((Ageable) blockData).getAge() >= ((Ageable) blockData).getMaximumAge()) {
                    if (StrUtil.endsWithIgnoreCaseOr(event.getClickedBlock().getType().toString(), "_STEM", "FIRE")) {  //西瓜、南瓜的茎; 火
                        return;
                    }
                    if (new BlockBreakEvent(event.getClickedBlock(), event.getPlayer()).callEvent().isCancelled()) {
                        return;  //防止收割被保护的地方
                    }
                    if (new BlockPlaceEvent(event.getClickedBlock(), event.getClickedBlock().getState(), event.getClickedBlock(), event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer(), true, event.getHand()).callEvent().isCancelled()) {
                        return;  //防止收割被保护的地方
                    }
                    event.setCancelled(true);
                    Collection<ItemStack> result = event.getClickedBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand());
                    for (ItemStack drop : result) {
                        if (ItemUtil.typeIsOr(drop, Material.WHEAT, Material.BEETROOT, Material.POISONOUS_POTATO))
                            continue;

                        drop.setAmount(drop.getAmount() - 1);
                    }
                    ((Ageable) blockData).setAge(0);  //先判断能不能处理，再收割
                    event.getClickedBlock().setBlockData(blockData);
                    for (ItemStack drop : result) {
                        if (drop == null || drop.getAmount() == 0) {
                            continue;
                        }
                        event.getClickedBlock().getLocation().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), drop);
                    }
                    MsgUtil.sendActionBar(event.getPlayer(), getMessageWithSuffix("harvest_and_plant", LangUtil.getItemName(event.getClickedBlock().getType())));
                }
            }
        }
    }
}
