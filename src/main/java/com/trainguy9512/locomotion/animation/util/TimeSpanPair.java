package com.trainguy9512.locomotion.animation.util;

/**
 * Small project-owned replacement for Minecraft's old net.minecraft.util.Tuple.
 * Minecraft 26.x removed that helper class, so this keeps animation timing
 * code independent from vanilla utility classes.
 */
public record TimeSpanPair(TimeSpan previous, TimeSpan current) {
    public TimeSpan getA() {
        return previous;
    }

    public TimeSpan getB() {
        return current;
    }
}
