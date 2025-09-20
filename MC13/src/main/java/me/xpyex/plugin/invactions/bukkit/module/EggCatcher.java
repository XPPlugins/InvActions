package me.xpyex.plugin.invactions.bukkit.module;

import java.util.Random;
import me.xpyex.lib.xplib.bukkit.inventory.ItemUtil;
import me.xpyex.lib.xplib.bukkit.language.LangUtil;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.bukkit.version.VersionUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import me.xpyex.plugin.invactions.bukkit.config.InvActionsServerConfig;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EggCatcher extends RootModule {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static Item catchEntity(Entity entity) {
        Material spawnEggType = Material.getMaterial(entity.getType() + "_SPAWN_EGG");
        if (spawnEggType != null) {
            Item item = entity.getWorld().dropItem(entity.getLocation(), ItemUtil.getItemStack(spawnEggType, entity.getName()));
            entity.remove();
            return item;
        }
        return null;
    }

    @Override
    protected boolean canLoad() {
        return VersionUtil.getMainVersion() >= 13;
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!serverEnabled()) return;

        if (event.getHitEntity() != null) {
            if (event.getEntity().getType() == EntityType.EGG) {
                if (event.getEntity().getShooter() instanceof Player) {
                    Player p = (Player) event.getEntity().getShooter();
                    if (playerEnabled(p)) {
                        EntityType hitEntityType = event.getHitEntity().getType();
                        MsgUtil.debugLog(InvActions.getInstance(), "EggCatcher: 玩家 " + p.getName() + " 用鸡蛋命中 " + hitEntityType);
                        Integer chance = InvActionsServerConfig.getCurrent().getEggCatcher_Chance().get(hitEntityType.toString());
                        if (chance == null || chance == 100) {
                            if (catchEntity(event.getHitEntity()) != null) {
                                MsgUtil.sendActionBar(p, getMessageWithSuffix("caught", LangUtil.getTranslationName(hitEntityType)));
                            }
                        } else if (chance > 0 && chance < 100) {
                            if (RANDOM.nextInt(100) < chance) {
                                if (catchEntity(event.getHitEntity()) != null) {
                                    MsgUtil.sendActionBar(p, getMessageWithSuffix("caught", LangUtil.getTranslationName(hitEntityType)));
                                }
                            } else {
                                MsgUtil.sendActionBar(p, getMessageWithSuffix("failed", LangUtil.getTranslationName(hitEntityType)));
                            }
                        } else if (chance < 0 || chance > 100) {
                            MsgUtil.debugLog(InvActions.getInstance(), "EggCatcher: 玩家 " + p.getName() + " 用鸡蛋命中 " + hitEntityType + " 时，几率为非法值: chance=" + chance);
                        } // else就是chance = 0，什么也不做
                    }
                }
            }
        }
    }
}
