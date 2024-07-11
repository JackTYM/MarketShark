package dev.jacktym.marketshark.mixins;

import dev.jacktym.marketshark.config.FlipConfig;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherMixin {
    @Redirect(method = "updateChunkNow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderWorker;processTask(Lnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V"))
    private void redirectProcessTask(ChunkRenderWorker chunkRenderWorker, ChunkCompileTaskGenerator generator) {
        if (!FlipConfig.antiRender) {
            ((ChunkRenderWorkerAccessor) chunkRenderWorker).invokeProcessTask(generator);
        }
    }
}