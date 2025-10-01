package me.xpyex.plugin.invactions.bukkit.util;

import me.xpyex.lib.xplib.util.RootUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class EventUtil extends RootUtil {
    /**
     * 因为目标是兼容到1.12的版本
     * 旧版本Paper好像没有给事件打补丁
     * 可能无法callEvent
     * 为了兼容性，自己写一个
     *
     * @param event the new event to call
     * @return itself
     */
    public static <T extends Event> T callEvent(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
