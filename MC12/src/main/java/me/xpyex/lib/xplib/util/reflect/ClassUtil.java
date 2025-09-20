package me.xpyex.lib.xplib.util.reflect;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import me.xpyex.lib.xplib.api.Pair;
import me.xpyex.lib.xplib.util.RootUtil;
import me.xpyex.plugin.invactions.bukkit.InvActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassUtil extends RootUtil {
    private static final WeakHashMap<String, Class<?>> CLASS_CACHE = new WeakHashMap<>();

    @NotNull
    public static Class<?> getClass(String name, @Deprecated boolean needInitClass, boolean isDeepSearch) throws ClassNotFoundException {
        if (!CLASS_CACHE.containsKey(name)) {
            try {
                if (needInitClass) CLASS_CACHE.put(name, Class.forName(name));
                else
                    CLASS_CACHE.put(name, Class.forName(name, false, getCallerClass().getClassLoader()));
            } catch (ReflectiveOperationException ignored) {
                try {
                    CLASS_CACHE.put(name, Class.forName(name, needInitClass, ClassLoader.getSystemClassLoader()));
                } catch (ReflectiveOperationException ignored1) {
                    if (isDeepSearch) {
                        for (Thread thread : Thread.getAllStackTraces().keySet()) {
                            try {
                                CLASS_CACHE.put(name, Class.forName(name, needInitClass, thread.getContextClassLoader()));
                                break;
                            } catch (ReflectiveOperationException ignored3) {
                            }
                        }
                    }
                }
            }
        }
        Class<?> ret = CLASS_CACHE.get(name);
        if (ret == null) throw new ClassNotFoundException("无法找到类 " + name);
        return ret;
    }

    public static void clearCache() {
        CLASS_CACHE.clear();
        //
    }

    public static boolean exist(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static List<Class<?>> getClasses(String packagePath) {
        List<Class<?>> classList = new ArrayList<>();
        for (File file : InvActions.getInstance().getDataFolder().getParentFile().listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try (JarFile jar = new JarFile(file)) {
                    Enumeration<JarEntry> enumFiles = jar.entries();
                    while (enumFiles.hasMoreElements()) {
                        try {
                            JarEntry entry = enumFiles.nextElement();
                            if (!entry.getName().contains("META-INF")) {
                                String classPath = entry.getName().replace("/", ".");
                                if (classPath.endsWith(".class")) {
                                    String className = classPath.substring(0, classPath.length() - 6);
                                    if (className.contains(packagePath)) {
                                        classList.add(Class.forName(className));
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return classList;
    }

    /**
     * 在所有jar内扫描，返回包含packagePath的类的实例
     *
     * @param packagePath 包名，仅做contains检查
     * @param parentClass 扫描到的Class必须继承parentClass，若填入null则忽略此条件
     * @return 包含packagePath的类的实例，或Pair< Class< ? >, Throwable>，表示无法构建实例
     */
    public static List<Object> scanAndGetClassInstances(String packagePath, @Nullable Class<?> parentClass) {
        if (parentClass == null) parentClass = Object.class;
        return getClasses(packagePath).stream()
                   .filter(parentClass::isAssignableFrom)
                   .filter(clazz -> !clazz.isInterface())
                   .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                   .filter(clazz -> !clazz.isPrimitive())
                   .filter(clazz -> !clazz.isArray())
                   .filter(clazz -> !clazz.isAnnotation())
                   .filter(clazz -> !clazz.isEnum())
                   .map(clazz -> {
                       try {
                           return clazz.getConstructor().newInstance();
                       } catch (Throwable e) {
                           return Pair.of(clazz, e);
                       }
                   })
                   .collect(Collectors.toList());
    }

    protected static boolean classEquals(Class<?> class1, Class<?> class2) {
        if (class1.isPrimitive() != class2.isPrimitive() && (class1.getName().startsWith("java.lang.") || class2.getName().startsWith("java.lang."))) {
            return class1.getSimpleName().equalsIgnoreCase(class2.getSimpleName());
        }  // 两者只有其中之一是基元类，另一个是java.lang包下的装箱

        // 两者都是基元类，或两者均非，则直接比较
        return Objects.equals(class1, class2);
    }

    /**
     * @since Java17
     * @return Caller Class
     */
    private static Class<?> getCallerClass() {
        try {
            return new SecurityManager() {
                public Class<?> getCallerClass() {
                    return getClassContext()[2];
                }
            }.getCallerClass();
        } catch (Exception e) {
            return null;
        }
    }
}
