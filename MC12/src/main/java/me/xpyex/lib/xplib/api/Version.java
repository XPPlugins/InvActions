package me.xpyex.lib.xplib.api;

import java.util.TreeSet;
import me.xpyex.lib.xplib.util.strings.StrUtil;
import me.xpyex.lib.xplib.util.value.ValueUtil;

public class Version implements Comparable<Version> {
    private final int[] versions;
    private final String betaInfo;
    private final String origin;

    public Version(String origin) {
        ValueUtil.notNull("ver不应为空", origin);
        this.origin = origin;
        String mainVer;
        if (origin.contains("-")) {
            mainVer = origin.split("-")[0];
            betaInfo = origin.substring((mainVer + "-").length());
        } else {
            mainVer = origin;
            betaInfo = "";
        }

        if (StrUtil.containsIgnoreCaseOr(mainVer, "version", "ver", "v")) {
            mainVer = mainVer.replace("version", "");
            mainVer = mainVer.replace("ver", "");
            mainVer = mainVer.replace("v", "");
        }

        String[] vers = mainVer.split("\\.");
        versions = new int[vers.length];
        for (int i = 0; i < vers.length; i++) {
            try {
                versions[i] = Integer.parseInt(vers[i]);
            } catch (NumberFormatException ignored) {
                versions[i] = 0;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Version) {
            return this.compareTo((Version) o) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return origin;
        //
    }

    /**
     * 比较两个Version
     *
     * @param version 目标Version
     * @return 返回0相等，返回1表示this较新于version，返回-1表示this较旧于version
     */
    public int compareTo(Version version) {
        if (version == null) return -1;

        if (this == version) return 0;

        if (version.versions.length > this.versions.length) {
            return -1;
        }
        if (version.versions.length < this.versions.length) {  //1.19.3比1.19新
            return 1;
        }

        for (int i = 0; i < versions.length; i++) {
            if (version.versions[i] > this.versions[i]) {
                return -1;  //this较旧
            }
            if (version.versions[i] == this.versions[i]) {
                if (i != versions.length - 1) {  //还没检查完的情况
                    continue;
                }
                //往下就是主版本已经检查完了
                if (version.betaInfo.equals(this.betaInfo)) {  //Beta版本是否一致
                    return 0;
                }
                TreeSet<String> compare = new TreeSet<>();  //排序betaInfo
                compare.add(version.betaInfo);
                compare.add(this.betaInfo);
                if (compare.first().equals(this.betaInfo)) {  //this较新
                    return 1;
                } else {
                    return -1;
                }
            }
            if (version.versions[i] < this.versions[i]) {
                return 1;  //this较新
            }
        }

        return 0;
    }

    public String getVersion() {
        return origin;
        //
    }

    public int getVersion(int index) {
        return versions[index];
        //
    }
}
