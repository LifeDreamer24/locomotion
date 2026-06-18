
package com.trainguy9512.locomotion.access;

import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.pose.ModelPartSpacePose;

import java.util.Optional;

public interface EntityRenderStateAccess {
    void animationOverhaul$setInterpolatedAnimationPose(ModelPartSpacePose interpolatedPose);
    Optional<ModelPartSpacePose> animationOverhaul$getInterpolatedAnimationPose();

    void animationOverhaul$setEntityJointAnimator(EntityJointAnimator<?, ?> entityJointAnimator);
    Optional<EntityJointAnimator<?, ?>> animationOverhaul$getEntityJointAnimator();
}
