package me.xpyex.plugin.invactions.bukkit.command;

import java.util.WeakHashMap;
import me.xpyex.lib.xplib.bukkit.config.ConfigUtil;
import me.xpyex.lib.xplib.bukkit.inventory.InvBuilder;
import me.xpyex.lib.xplib.bukkit.inventory.ItemUtil;
import me.xpyex.lib.xplib.bukkit.inventory.Menu;
import me.xpyex.lib.xplib.bukkit.inventory.button.UnmodifiableButton;
import me.xpyex.lib.xplib.bukkit.language.LangUtil;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.bukkit.version.VersionUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import me.xpyex.plugin.invactions.bukkit.config.InvActionsServerConfig;
import me.xpyex.plugin.invactions.bukkit.message.InvActionsMessage;
import me.xpyex.plugin.invactions.bukkit.module.RootModule;
import me.xpyex.plugin.invactions.bukkit.util.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HandleCmd implements CommandExecutor {
    public static final ItemStack MENU_WOOL_RED;
    public static final ItemStack MENU_WOOL_GREEN;
    private static final String[] menuID = "abcdefghijklmnopqrstuvwxyz".split("");
    private static final WeakHashMap<Player, Menu> MENU_CACHE = new WeakHashMap<>();
    private static final ItemStack MENU_GLASS_PANE;
    private static final ItemStack MENU_WOOL_ORANGE;

    static {
        MENU_WOOL_ORANGE = ItemUtil.getItemStack(Material.getMaterial((VersionUtil.getMainVersion() >= 13 ? "ORANGE_" : "") + "WOOL"), " ");
        MENU_WOOL_RED = ItemUtil.getItemStack(Material.getMaterial((VersionUtil.getMainVersion() >= 13 ? "RED_" : "") + "WOOL"), " ");
        MENU_WOOL_GREEN = ItemUtil.getItemStack(Material.getMaterial((VersionUtil.getMainVersion() >= 13 ? "LIME_" : "") + "WOOL"), " ");
        MENU_GLASS_PANE = ItemUtil.getItemStack(Material.getMaterial((VersionUtil.getMainVersion() >= 13 ? "BLACK_" : "") + "STAINED_GLASS_PANE"), " ");
        if (VersionUtil.getMainVersion() < 13) {
            MENU_WOOL_ORANGE.setDurability((short) 1);
            MENU_WOOL_GREEN.setDurability((short) 5);
            MENU_WOOL_RED.setDurability((short) 14);
            MENU_GLASS_PANE.setDurability((short) 7);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(InvActions.getInstance(), () -> {
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("InvActions.admin")) {
                        sender.sendMessage("你没有权限");
                        return;
                    }
                    ConfigUtil.reload(InvActions.getInstance());
                    InvActions.getInstance().updateServerConfig();
                    sender.sendMessage(MsgUtil.getColorMsg(LangUtil.getMessage("Reload.reload")));
                    return;
                }
                if (args[0].equalsIgnoreCase("addInvs")) {
                    if (!sender.hasPermission("InvActions.admin")) {
                        sender.sendMessage("你没有权限");
                        return;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("参数不足");
                        return;
                    }
                    if (args.length > 2) {
                        sender.sendMessage("Invs内容不可包含空格，多余的参数已被忽略");
                    }
                    InvActionsServerConfig.getCurrent().getAllowInvs().add(args[1]);
                    ConfigUtil.saveConfig(InvActions.getInstance(), "config", InvActionsServerConfig.getCurrent(), true);
                    ConfigUtil.reload(InvActions.getInstance());
                    sender.sendMessage("已允许整理 " + args[1]);
                    return;
                }
            }
            if (sender instanceof Player) {
                if (MENU_CACHE.containsKey(sender)) {
                    Bukkit.getScheduler().runTask(InvActions.getInstance(), () -> {
                        MENU_CACHE.get(sender).open(1);
                    });
                    return;
                }

                Menu menu = new Menu((Player) sender);
                menu.setPage(1, new InvBuilder("InvActions-设定-" + sender.getName(),
                            "#########",
                            "#abcdefg#",
                            "#hijk   #",
                            "########A"
                        )
                                    .setSign("#", MENU_GLASS_PANE)
                                    .setSign(" ", InvUtil.AIR_STACK)
                    )
                    .setSign("A", new UnmodifiableButton(menu, (player -> player.hasPermission("InvActions.admin") ? 1 : 0))
                                      .addMode(1, ItemUtil.getItemStack(MENU_WOOL_ORANGE,
                                          InvActionsMessage.getCurrent().getReload().getMenu().getName(),
                                          InvActionsMessage.getCurrent().getReload().getMenu().getLore().toArray(new String[0])))
                                      .addMode(0, MENU_GLASS_PANE)
                                      .setClickEffect(((player, clickType, itemStack) -> {
                                          if (!player.hasPermission("InvActions.admin")) {  //玩家没权限的时候
                                              return;
                                          }
                                          Bukkit.getScheduler().runTask(InvActions.getInstance(), () -> {
                                              Bukkit.dispatchCommand(player, "InvActions reload");
                                              player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                                          });
                                      }))
                        , 1);
                for (int i = 0; i < RootModule.modules.size(); i++) {
                    RootModule module = RootModule.modules.get(i);
                    menu.setSign(menuID[i], module.getMenuButton(menu), 1);
                }
                Bukkit.getScheduler().runTask(InvActions.getInstance(), () -> {
                    menu.open(1);
                });
                MENU_CACHE.put((Player) sender, menu);
            }
        });
        return true;
    }
}
