package me.xpyex.lib.xplib.bukkit.version;

import lombok.Getter;
import me.xpyex.lib.xplib.api.Version;
import me.xpyex.lib.xplib.util.RootUtil;
import org.bukkit.Bukkit;

public class VersionUtil extends RootUtil {
    @Getter
    private static final Version mcVer = new Version(Bukkit.getBukkitVersion());
    @Getter
    private static final int mainVersion = mcVer.getVersion(0) == 1 ? mcVer.getVersion(1) : mcVer.getVersion(0);

    public static String getServerVersion() {
        return Bukkit.getVersion().split("-")[0];
        //
    }
}
