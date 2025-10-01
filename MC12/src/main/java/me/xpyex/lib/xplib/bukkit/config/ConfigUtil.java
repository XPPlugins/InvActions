package me.xpyex.lib.xplib.bukkit.config;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import me.xpyex.lib.xplib.bukkit.strings.MsgUtil;
import me.xpyex.lib.xplib.util.RootUtil;
import me.xpyex.lib.xplib.util.files.FileUtil;
import me.xpyex.lib.xplib.util.gson.GsonUtil;
import org.bukkit.plugin.Plugin;

public class ConfigUtil extends RootUtil {
    private static final HashMap<String, Object> CONFIGS = new HashMap<>();
    //Key: PluginName/FilePath(ConvertType)
    //Value: Config Instance in type

    public static <T> T getConfig(Plugin plugin, Class<T> type) {
        return getConfig(plugin, "config", type);
        //
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfig(Plugin plugin, String path, Class<T> type) {
        String key = plugin.getName() + "/" + path + "(" + type.getSimpleName() + ")";
        if (!CONFIGS.containsKey(key)) {
            try {
                CONFIGS.put(key, GsonUtil.parseJson(FileUtil.readFile(new File(plugin.getDataFolder(), path + ".json")), type));
            } catch (IOException e) {
                MsgUtil.debugLog(plugin, new IllegalStateException("配置文件访问异常", e));
                return null;
            }
        }
        return (T) CONFIGS.get(key);
    }

    public static void reload() {
        CONFIGS.clear();
        //
    }

    /**
     * 清除有关该插件的缓存，获取配置文件时都会重新加载
     *
     * @param plugin 插件实例
     */
    public static void reload(Plugin plugin) {
        HashMap<String, Object> copied = new HashMap<>(CONFIGS);
        for (String s : copied.keySet()) {
            if (s.startsWith(plugin.getName() + "/")) {
                CONFIGS.remove(s);
            }
        }
    }

    public static void saveConfig(Plugin plugin, String path, Object obj, boolean replaced) {
        saveConfig(plugin, path, obj, replaced, true);
        //
    }

    public static void saveConfig(Plugin plugin, String path, Object obj, boolean replaced, boolean cache) {
        File file = new File(plugin.getDataFolder(), path + ".json");
        if (!replaced && file.exists()) return;
        try {
            FileUtil.writeFile(file, GsonUtil.parseStr(obj));
            if (cache) CONFIGS.put(plugin.getName() + "/" + path + "(" + obj.getClass().getSimpleName() + ")", obj);
        } catch (Throwable e) {
            MsgUtil.debugLog(plugin, e);
        }
    }

    public static void saveConfig(Plugin plugin, String path, boolean replaced) {
        for (String key : CONFIGS.keySet()) {
            if (key.startsWith(plugin.getName() + "/" + path + "(")) {
                saveConfig(plugin, path, CONFIGS.get(key), true);
                return;
            }
        }
    }
}
