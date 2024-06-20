package dev.jacktym.marketshark.mixins;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiDisconnected.class)
public interface GuiDisconnectedAccessor {
    @Accessor
    IChatComponent getMessage();
    @Accessor
    String getReason();
}
