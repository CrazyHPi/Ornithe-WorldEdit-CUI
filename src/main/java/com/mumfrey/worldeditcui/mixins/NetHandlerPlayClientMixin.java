package com.mumfrey.worldeditcui.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mumfrey.worldeditcui.ModWorldEditCUI;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketJoinGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin {
    @Inject(method = "handleJoinGame", at = @At("TAIL"))
    private void onJoinGame(SPacketJoinGame packetIn, CallbackInfo ci) {
        ModWorldEditCUI.INSTANCE.onJoinGame();
    }

    @Inject(
            method = "handleCustomPayload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onWECuiCustomPayload(SPacketCustomPayload packetIn, CallbackInfo ci) {
        if (ModWorldEditCUI.CHANNEL_WECUI.equalsIgnoreCase(packetIn.getChannelName())) {
            PacketBuffer data = packetIn.getBufferData();
            if (data != null) {
                ModWorldEditCUI.INSTANCE.onCustomPayload(data);
            }
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "handleCustomPayload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/play/server/SPacketCustomPayload;getChannelName()Ljava/lang/String;"
            )
    )
    private String getChannelNames(String original) {
        ModWorldEditCUI.remotePluginChannels.add(original);
        return original;
    }
}
