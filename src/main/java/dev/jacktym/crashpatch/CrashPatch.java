package dev.jacktym.crashpatch;

import cc.polyfrost.oneconfig.utils.Multithreading;
import dev.jacktym.crashpatch.crashes.CrashHelper;
import dev.jacktym.crashpatch.crashes.DeobfuscatingRewritePolicy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class CrashPatch {
    public static final Logger logger = LogManager.getLogger(CrashPatch.class);
    public static final File mcDir = new File(System.getProperty("user.dir"));

    public static final File gameDir = initGameDir();

    private static boolean isSkyclient() {
        return false;
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        DeobfuscatingRewritePolicy.install();
        Multithreading.runAsync(() -> {
            logger.info("Is SkyClient: " + isSkyclient());
            if (!CrashHelper.loadJson()) {
                logger.error("CrashHelper failed to preload crash data JSON!");
            }
        });
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        //CrashPatchConfig.init();
        // Uncomment to test init screen crashes
        // throw new Throwable("java.lang.NoClassDefFoundError: xyz/matthewtgm/requisite/keybinds/KeyBind at lumien.custommainmenu.configuration.ConfigurationLoader.load(ConfigurationLoader.java:142) club.sk1er.bossbarcustomizer.BossbarMod.loadConfig cc.woverflow.hytils.handlers.chat.modules.modifiers.DefaultChatRestyler Failed to login: null The Hypixel Alpha server is currently closed! net.kdt.pojavlaunch macromodmodules");
    }

    private static File initGameDir() {
        File file = mcDir;
        try {
            if (file.getParentFile() != null && (file.getParentFile().getName().equals(".minecraft") || file.getParentFile().getName().equals("minecraft"))) {
                return file.getParentFile();
            } else {
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }
}