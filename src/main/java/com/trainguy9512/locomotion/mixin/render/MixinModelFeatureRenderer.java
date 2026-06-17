package com.trainguy9512.locomotion.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.render.LocomotionWrappedRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ModelFeatureRenderer.class)
public class MixinModelFeatureRenderer<S> {

    @WrapOperation(
            // 26.2 moved this setupAnim call out of renderModel and into prepareModel.
            // Keeping both names lets older chiseled targets still work while 26.2 unwraps the render state before vanilla models see it.
            method = {"renderModel", "prepareModel"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V"),
            require = 0
    )
    public void redirectSetupAnim(Model<S> instance, S renderState, Operation<Void> original) {
        if (renderState instanceof LocomotionWrappedRenderState<?> wrappedRenderState) {
            original.call(instance, wrappedRenderState.getInnerValue());
            Optional<AnimationDataContainer> potentialDataContainer = wrappedRenderState.getDataContainer();
            if (potentialDataContainer.isPresent()) {
                AnimationDataContainer dataContainer = potentialDataContainer.get();
                float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
                Pose pose = dataContainer.computePose(partialTicks);
                dataContainer.setupAnimWithAnimationPose(instance, partialTicks);
            }
        } else {
            original.call(instance, renderState);
        }
    }

}
