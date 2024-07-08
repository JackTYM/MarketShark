package dev.jacktym.crashpatch.crashes;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ModIdentifier {

    public static ModContainer identifyFromStacktrace(Throwable e) {
        Map<File, Set<ModContainer>> modMap = makeModMap();

        // Get the set of classes
        Set<String> classes = new LinkedHashSet<>();
        if (e != null) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int index = 0; index < stackTrace.length && index < 4; index++) {
                StackTraceElement stackTraceElement = stackTrace[index];
                classes.add(stackTraceElement.getClassName());
            }
        }
        Set<ModContainer> mods = new LinkedHashSet<>();
        for (String className : classes) {
            Set<ModContainer> classMods = identifyFromClass(className, modMap);
            if (!classMods.isEmpty()) {
                mods.addAll(classMods);
            }
        }
        return mods.stream().findFirst().orElse(null);
    }

    private static Set<ModContainer> identifyFromClass(String className, Map<File, Set<ModContainer>> modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) return new HashSet<>();

        // Get the URL of the class
        String untransformedName = untransformName(Launch.classLoader, className);
        URL url = Launch.classLoader.getResource(untransformedName.replace('.', '/') + ".class");
        System.out.println(className + " = " + untransformedName + " = " + url);
        if (url == null) {
            System.out.println("Failed to identify " + className + " (untransformed name: " + untransformedName + ")");
            return new HashSet<>();
        }

        // Get the mod containing that class
        try {
            if ("jar".equals(url.getProtocol())) {
                url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
            }
            File file = new File(url.toURI()).getCanonicalFile();
            return modMap.getOrDefault(file, new HashSet<>());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<File, Set<ModContainer>> makeModMap() {
        Map<File, Set<ModContainer>> modMap = new HashMap<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            Set<ModContainer> currentMods = modMap.getOrDefault(mod.getSource(), new HashSet<>());
            currentMods.add(mod);
            try {
                modMap.put(mod.getSource().getCanonicalFile(), currentMods);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            modMap.remove(Loader.instance().getMinecraftModContainer().getSource()); // Ignore minecraft jar (minecraft)
            modMap.remove(Loader.instance().getIndexedModList().get("FML").getSource()); // Ignore forge jar (FML, forge)
        } catch (NullPointerException ignored) {
            // Workaround for https://github.com/MinecraftForge/MinecraftForge/issues/4919
        }
        return modMap;
    }

    public static String untransformName(LaunchClassLoader launchClassLoader, String className) {
        try {
            java.lang.reflect.Method untransformNameMethod =
                    LaunchClassLoader.class.getDeclaredMethod("untransformName", String.class);
            untransformNameMethod.setAccessible(true);
            return (String) untransformNameMethod.invoke(launchClassLoader, className);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}