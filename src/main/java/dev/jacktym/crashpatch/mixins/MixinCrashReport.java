package dev.jacktym.crashpatch.mixins;

import dev.jacktym.crashpatch.hooks.CrashReportHook;
import dev.jacktym.crashpatch.crashes.ModIdentifier;
import dev.jacktym.crashpatch.hooks.StacktraceDeobfuscator;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrashReport.class, priority = 500)
public class MixinCrashReport implements CrashReportHook {
    @Shadow
    @Final
    private Throwable cause;
    private String crashpatch$suspectedMod;

    @Override
    public String getSuspectedCrashPatchMods() {
        return crashpatch$suspectedMod;
    }

    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void afterPopulateEnvironment(CallbackInfo ci) {
        ModContainer susMod = ModIdentifier.identifyFromStacktrace(cause);
        crashpatch$suspectedMod = (susMod == null ? "Unknown" : susMod.getName());

        System.out.println("Crashed from sus mod: " + crashpatch$suspectedMod);
    }

    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    private void beforePopulateEnvironment(CallbackInfo ci) {
        StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(cause);
    }
}
