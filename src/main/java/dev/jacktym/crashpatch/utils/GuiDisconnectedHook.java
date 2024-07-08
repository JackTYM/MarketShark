package dev.jacktym.crashpatch.utils;

import dev.jacktym.crashpatch.crashes.CrashHelper;
import dev.jacktym.crashpatch.crashes.CrashScan;
import dev.jacktym.crashpatch.mixins.AccessorGuiDisconnected;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiDisconnectedHook {
    public static void onGUIDisplay(GuiScreen i, CallbackInfo ci) {
        if (i instanceof GuiDisconnected) {
            AccessorGuiDisconnected gui = (AccessorGuiDisconnected) i;
            CrashScan scan = CrashHelper.scanReport(gui.getMessage().getFormattedText(), true);
            if (scan != null && scan.getSolutions().size() > 1) {
                ci.cancel();
                //Minecraft.getMinecraft().displayGuiScreen(new CrashGui(gui.getMessage().getFormattedText(), null, gui.getReason(), CrashGui.GuiType.DISCONNECT));
            }
        }
    }
}