package me.xpyex.plugin.invactions.bukkit.module;

import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.util.reflect.MethodUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BetterLoyalty extends RootModule {
    //不会有人视距设为1吧？不会吧不会吧
    private static final int SERVER_DIS = (Math.max(Bukkit.getServer().getViewDistance() - 1, 1) * 16) + 10;

    @Override
    protected boolean canLoad() {
        try {
            Enchantment.LOYALTY.getMaxLevel();
            return MethodUtil.exist(Trident.class, "getItem");
        } catch (NoSuchFieldError ignored) {
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!serverEnabled()) {  //服务器未启用
            return;
        }
        if (event.getEntity() instanceof Trident) {
            if (event.getEntity().getShooter() instanceof Player) {
                Player shooter = (Player) event.getEntity().getShooter();
                if (!playerEnabled(shooter)) {  //玩家未启用
                    return;
                }
                if (((Trident) event.getEntity()).getItem().getEnchantments().containsKey(Enchantment.LOYALTY)) {
                    //附魔忠诚了
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!event.getEntity().isValid()) {  //三叉戟跑出视距外了，或已收回到主人手上，不再处理，也没办法处理
                                cancel();
                                return;
                            }
                            if (event.getEntity().isOnGround() || ((Trident) event.getEntity()).isInBlock()) {
                                cancel();
                                return;
                            }
                            if (shooter.getWorld() != event.getEntity().getWorld()) {
                                syncTeleport(event.getEntity(), shooter.getWorld().getHighestBlockAt(event.getEntity().getLocation()).getLocation());
                                cancel();
                                return;
                            }
                            if (!shooter.isOnline()) {  //玩家离线，把三叉戟传送到玩家离线位置
                                syncTeleport(event.getEntity(), shooter.getWorld().getHighestBlockAt(event.getEntity().getLocation()).getLocation());
                                cancel();
                                return;
                            }
                            double distance = getDistance(event.getEntity(), shooter);
                            if (distance >= SERVER_DIS) {  //距离即将过远，把三叉戟传送回来
                                syncTeleport(event.getEntity(), shooter.getWorld().getHighestBlockAt(event.getEntity().getLocation()).getLocation());
                                MsgUtil.sendActionBar(shooter, getMessageWithSuffix("return"));
                                cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(InvActions.getInstance(), 5L, 5L);
                }
            }
        }
    }

    private double getDistance(Entity e1, Entity e2) {
        if (e1 == null || e2 == null) {
            return 0;
        }
        if (e1.getWorld() != e2.getWorld()) return Integer.MAX_VALUE;
        double x1 = e1.getLocation().getX();
        double z1 = e1.getLocation().getZ();
        double x2 = e2.getLocation().getX();
        double z2 = e2.getLocation().getZ();
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2));  //√[(x2-x1)²+(z2-z1)²] 两点间距离 忽略高度y
    }

    private void syncTeleport(Entity entity, Location target) {
        Bukkit.getScheduler().runTask(InvActions.getInstance(), () -> entity.teleport(target));
    }
}
