package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorRegistry;
import com.trainguy9512.locomotion.util.LocomotionMultiVersionWrappers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
//? if >= 26 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow @Final private Minecraft minecraft;

    /**
     * Computes and saves the interpolated animation pose prior to rendering.
     */
    //? if >= 26 {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void computePosePriorToRendering(CallbackInfo ci) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
            jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().ifPresent(dataContainer ->
                    JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                            jointAnimator -> jointAnimatorDispatcher.calculateInterpolatedFirstPersonPlayerPose(dataContainer, this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true))
                    ));
        }
    }
    //?} else {
    /*@Inject(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractCamera(F)V")
    )
    private void computePosePriorToRendering(DeltaTracker deltaTracker, CallbackInfo ci){
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
            jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().ifPresent(dataContainer ->
                    JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                            jointAnimator -> jointAnimatorDispatcher.calculateInterpolatedFirstPersonPlayerPose(dataContainer, deltaTracker.getGameTimeDeltaPartialTick(true))
                    ));
        }
    }*/
    //?}


    /**
     * 26.2 fallback: the old ItemInHandRenderer hand hook can miss silently when the
     * first-person hand method is routed through GameRenderer's queued renderer. The
     * 26.2 descriptor is:
     * renderItemInHand(CameraRenderState, float, Matrix4fc).
     */
    //? if >= 26 {
    @Inject(
            method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    @Dynamic
    private void locomotion$renderFirstPersonArmsFromGameRenderer(CameraRenderState cameraRenderState, float partialTick, Matrix4fc positionMatrix, CallbackInfo ci) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            return;
        }
        if (this.minecraft.player == null || !this.minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        SubmitNodeCollector nodeCollector = LocomotionMultiVersionWrappers.getSubmitNodeCollector(this.minecraft);
        if (nodeCollector == null) {
            return;
        }
        ((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().ifPresent(firstPersonPlayerRenderer -> {
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(new Matrix4f(positionMatrix));
            firstPersonPlayerRenderer.render(partialTick, poseStack, nodeCollector, this.minecraft.player, 0xF000F0);
            ci.cancel();
        });
    }
    //?}

    /**
     * Transform the camera pose stack based on the first person player's camera joint, prior to bobHurt and bobView.
     */
    @Inject(
            method = "bobHurt",
            at = @At("HEAD")
    )
    //? if >= 26 {
    private void addCameraRotation(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo ci) {
    //?} else {
    /*private void addCameraRotation(PoseStack poseStack, float partialTicks, CallbackInfo ci) {*/
    //?}
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ((FirstPersonPlayerRendererGetter)this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().ifPresent(firstPersonPlayerRenderer -> firstPersonPlayerRenderer.transformCamera(poseStack));
        }
    }

    /**
     * Remove the view bobbing animation, as the animation pose provides its own.
     */
    @Inject(
            method = "bobView",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void removeViewBobbing(CallbackInfo ci) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ci.cancel();
        }
    }
}
