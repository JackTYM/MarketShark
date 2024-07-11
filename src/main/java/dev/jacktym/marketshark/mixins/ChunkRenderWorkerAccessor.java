package dev.jacktym.marketshark.mixins;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkRenderWorker.class)
public interface ChunkRenderWorkerAccessor {
    @Invoker("processTask")
    public void invokeProcessTask(final ChunkCompileTaskGenerator generator);
}
