package com.trainguy9512.locomotion.mixin.render;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Shadow(aliases = {"field_4050", "client", "f_109299_"}) @Final private Minecraft minecraft;

    @Inject(
            method = {
                    // Mojang/official name used by 26.x deobfuscated sources
                    "renderHandsWithItems",
                    "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    // Minecraft 26.2 keeps moving render-queue methods toward submit* naming.
                    // Keep these aliases non-critical so whichever one exists in the current runtime wins.
                    "submitHandsWithItems",
                    "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "submitHands",
                    "submitHands(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "submitItemsInHand",
                    "submitItemsInHand(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    // Yarn and intermediary names used by Fabric runtime/remap paths
                    "renderItem(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "method_22976(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V",
                    // Mojang obfuscated selector retained as a last-resort fallback when no refmap is present
                    "m_109314_(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    @Dynamic
    public void locomotion$overrideFirstPersonRendering(
            float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight, CallbackInfo ci
    ) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            return;
        }
        if (((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().isEmpty()) {
            return;
        }
        FirstPersonPlayerRenderer renderer = ((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().get();
        renderer.renderLocomotionArmWithItem(partialTick, poseStack, nodeCollector, player, packedLight, InteractionHand.OFF_HAND);
        renderer.renderLocomotionArmWithItem(partialTick, poseStack, nodeCollector, player, packedLight, InteractionHand.MAIN_HAND);
        // Once Locomotion draws both hands, cancel vanilla's first-person hand/item renderer.
        // This avoids depending on the more fragile private renderArmWithItem hook just to hide vanilla arms.
        ci.cancel();
    }

    // Disables camera bob rotation if Locomotion's first person animations are enabled.
    @Redirect(
            method = {
                    "renderHandsWithItems",
                    "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "submitHandsWithItems",
                    "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "submitHands",
                    "submitHands(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "submitItemsInHand",
                    "submitItemsInHand(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "renderItem(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "method_22976(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
                    "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V",
                    "m_109314_(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"
            },
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"),
            require = 0
    )
    @Dynamic
    public void removeVanillaCameraBob(PoseStack instance, Quaternionfc pose) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            instance.mulPose(pose);
        }
    }

    // Cancel rendering the vanilla hand with item if Locomotion's first person animations are enabled.
    @Inject(
            method = {
                    "renderArmWithItem",
                    "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    "submitArmWithItem",
                    "submitArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    "renderFirstPersonItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    "submitFirstPersonItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    "method_3228(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    "method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
                    "m_109371_(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    @Dynamic
    public void locomotion$renderLocomotionArmsWithItems(
            AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci
    ) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ci.cancel();
        }
    }
}
