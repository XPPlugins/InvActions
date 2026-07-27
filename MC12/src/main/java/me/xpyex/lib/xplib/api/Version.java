package me.xpyex.lib.xplib.api;


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
            mainVer = mainVer.replace("version", "")
                          .replace("ver", "")
                          .replace("v", "");
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
     * @param target 目标Version
     * @return 返回0相等，返回1表示this较新于target，返回-1表示this较旧于target
     */
    public int compareTo(Version target) {
        if (target == null) return -1;

        if (this == target) return 0;

        if (origin.equals(target.origin)) return 0;

        for (int i = 0; i < versions.length && i < target.versions.length; i++) {
            if (target.versions[i] > this.versions[i]) {
                return -1;  //this较旧
            }

            if (target.versions[i] == this.versions[i]) {
                if (versions.length != target.versions.length) {
                    if (i == versions.length - 1 || i == target.versions.length - 1) {  //有其中一个到达极限了
                        return this.versions.length > target.versions.length ? 1 : -1;
                        // 举例。如果判断到第1位，都是1，不满足该条件，直接跳过
                        // 如果判断到第2位，都是1.19，那总长度短的是老版本，即target是老版本
                    } else {  // 都还没到达极限
                        continue;
                    }
                }
                //确定两个长度相等
                if (i != versions.length - 1) continue;
                //还没检查到最后一个


                //往下就是主版本已经检查完了
                if (target.betaInfo.equals(this.betaInfo)) {  //Beta版本是否一致
                    return 0;
                }
                if (this.betaInfo.isEmpty()) {  //正式版永远最新
                    return 1;
                }
                if (target.betaInfo.isEmpty()) {
                    return -1;
                }
                return this.betaInfo.compareTo(target.betaInfo);
            }
            if (target.versions[i] < this.versions[i]) {
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
