package com.mumfrey.worldeditcui.mixins;

import com.mumfrey.worldeditcui.ModWorldEditCUI;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    protected abstract void setupCameraTransform(float partialTicks, int pass);

    @Inject(
            method = "renderWorldPass(IFJ)V",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    args = "ldc=litParticles"
            )
    )
    private void onPostRenderEntities(int pass, float partialTicks, long timeSlice, CallbackInfo ci) {
        ModWorldEditCUI.INSTANCE.onPostRenderEntities(partialTicks);
    }


    @Inject(
            method = "renderWorld(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endSection()V",
                    ordinal = 0
            )
    )
    private void onPostRender(float partialTicks, long timeSlice, CallbackInfo ci) {
        this.setupCameraTransform(partialTicks, 0);
        ModWorldEditCUI.INSTANCE.onPostRender(partialTicks);
    }
}
