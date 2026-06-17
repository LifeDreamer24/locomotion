package com.trainguy9512.locomotion.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemUseAnimation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LocomotionMultiVersionWrappers {

    public static ItemUseAnimation getTridentUseAnimation() {
        //? if >= 1.21.11 {
        return ItemUseAnimation.TRIDENT;
        //?} else {
        /*return ItemUseAnimation.SPEAR;
        *///?}
    }

    public static ItemUseAnimation getSpearUseAnimation() {
        //? if >= 1.21.11 {
        return ItemUseAnimation.SPEAR;
        //?} else {
        /*throw new RuntimeException("1.21.11 feature attempted to be used in older version");
         *///?}
    }

    /**
     * Minecraft 26.2 moved screen access from Minecraft to Minecraft#gui.
     * Keep this reflective so the same common source can still compile for
     * 1.21.11/26.1.x and 26.2 without directly referencing removed members.
     */
    public static boolean hasScreenOpen() {
        return getCurrentScreen() != null;
    }

    public static Screen getCurrentScreen() {
        Minecraft minecraft = Minecraft.getInstance();

        Object minecraftScreen = getFieldValue(minecraft, "screen");
        if (minecraftScreen instanceof Screen screen) {
            return screen;
        }

        Object gui = getFieldValue(minecraft, "gui");
        if (gui == null) {
            return null;
        }

        for (String methodName : new String[]{"getCurrentScreen", "getScreen", "screen"}) {
            Object screen = invokeNoArg(gui, methodName);
            if (screen instanceof Screen currentScreen) {
                return currentScreen;
            }
        }

        for (String fieldName : new String[]{"currentScreen", "screen"}) {
            Object screen = getFieldValue(gui, fieldName);
            if (screen instanceof Screen currentScreen) {
                return currentScreen;
            }
        }

        return null;
    }

    public static void setScreen(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        ReflectiveOperationException lastFailure = null;

        Object gui = getFieldValue(minecraft, "gui");
        if (gui != null) {
            try {
                invoke(gui, "setScreen", screen);
                return;
            } catch (ReflectiveOperationException exception) {
                lastFailure = exception;
            }
        }

        try {
            invoke(minecraft, "setScreen", screen);
            return;
        } catch (ReflectiveOperationException exception) {
            if (lastFailure != null) {
                exception.addSuppressed(lastFailure);
            }
            throw new IllegalStateException("Unable to set the Minecraft screen on this version", exception);
        }
    }


    /**
     * Minecraft 26.x queues entity/hand rendering through GameRenderer's
     * SubmitNodeStorage. The first-person fallback renderer needs the real
     * frame collector rather than a fresh local collector, otherwise submitted
     * arm/item nodes never reach the frame.
     */
    public static SubmitNodeCollector getSubmitNodeCollector(Minecraft minecraft) {
        Object gameRenderer = getFieldValue(minecraft, "gameRenderer");
        if (gameRenderer == null) {
            return null;
        }

        for (String methodName : new String[]{"getSubmitNodeStorage", "getEntityRenderCommandQueue", "getSubmitNodeCollector", "method_72910", "m_420280_"}) {
            Object collector = invokeNoArg(gameRenderer, methodName);
            if (collector instanceof SubmitNodeCollector submitNodeCollector) {
                return submitNodeCollector;
            }
        }

        for (String fieldName : new String[]{"submitNodeStorage", "orderedRenderCommandQueue", "H", "field_61733", "f_412354_"}) {
            Object collector = getFieldValue(gameRenderer, fieldName);
            if (collector instanceof SubmitNodeCollector submitNodeCollector) {
                return submitNodeCollector;
            }
        }

        return null;
    }

    /**
     * 26.x moved/removed a few immediate-mode flush helpers used by the older
     * first-person renderer. Reflection keeps older builds working while making
     * the call a safe no-op on versions where the old hooks are gone.
     */
    public static void renderAndFlushFeatureBuffers(Minecraft minecraft) {
        Object gameRenderer = getFieldValue(minecraft, "gameRenderer");
        Object featureRenderDispatcher = gameRenderer == null ? null : invokeNoArg(gameRenderer, "getFeatureRenderDispatcher");
        if (featureRenderDispatcher != null) {
            invokeNoArg(featureRenderDispatcher, "renderAllFeatures");
        }

        Object renderBuffers = invokeNoArg(minecraft, "renderBuffers");
        Object bufferSource = renderBuffers == null ? null : invokeNoArg(renderBuffers, "bufferSource");
        if (bufferSource != null) {
            invokeNoArg(bufferSource, "endBatch");
        }
    }

    private static Object getFieldValue(Object target, String fieldName) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException | SecurityException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            return invoke(target, methodName);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invoke(Object target, String methodName, Object... parameters) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), methodName, getParameterTypes(parameters));
        method.setAccessible(true);
        try {
            return method.invoke(target, parameters);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw exception;
        }
    }

    private static Method findMethod(Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> currentType = type;
        while (currentType != null) {
            try {
                return currentType.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                currentType = currentType.getSuperclass();
            }
        }
        throw new NoSuchMethodException(type.getName() + "#" + methodName);
    }

    private static Class<?>[] getParameterTypes(Object[] parameters) {
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            parameterTypes[index] = parameters[index] == null || parameters[index] instanceof Screen ? Screen.class : parameters[index].getClass();
        }
        return parameterTypes;
    }

}
