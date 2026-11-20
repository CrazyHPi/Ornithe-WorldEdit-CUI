package com.mumfrey.worldeditcui.mixins;

import com.mumfrey.worldeditcui.ModWorldEditCUI;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Timer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private Timer timer;

    @Shadow
    @Nullable
    public abstract Entity getRenderViewEntity();

    @Inject(
            method = "runGameLoop()V",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;updateCameraAndRender(FJ)V"
            )
    )
    private void onTick(CallbackInfo ci) {
        boolean clock = this.timer.elapsedTicks > 0;

        // Flag indicates whether we are in game at the moment
        Entity renderViewEntity = this.getRenderViewEntity();
        boolean inGame = renderViewEntity != null && renderViewEntity.world != null;

        ModWorldEditCUI.INSTANCE.onTick(((Minecraft) (Object) this), inGame, clock);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitComplete(CallbackInfo ci) {
        ModWorldEditCUI.INSTANCE.onInitCompleted((Minecraft) (Object) this);
    }
}
