package dev.jacktym.crashpatch.mixins;

import dev.jacktym.crashpatch.crashes.StateManager;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements StateManager.IResettable {

    @Shadow private boolean isDrawing;

    @Shadow public abstract void finishDrawing();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInitEnd(int bufferSizeIn, CallbackInfo ci) {
        StateManager.resettableRefs.add(new WeakReference<>(this));
    }

    @Override
    public void resetState() {
        if (isDrawing) finishDrawing();
    }
}