# Locomotion 26.2 compatibility notes

This copy was updated for the Fabric 26.2 toolchain and dependency set.

## Main changes

- Stonecutter version matrix now includes `26.2` and uses it as the active version.
- Gradle wrapper updated to `9.5.1`.
- Architectury Loom updated to `1.17.477`.
- Architectury plugin updated to `3.5.167`.
- Fabric Loader property added for `26.2`: `0.19.3`.
- Fabric API property added for `26.2`: `0.152.1+26.2`.
- YACL property added for `26.2`: `3.9.4+26.2`.
- Mod Menu property added for `26.2`: `20.0.0-beta.2`.
- Sodium property added for `26.2`: `mc26.2-0.9.0-fabric`.
- Added a local `net.fabricmc:intermediary:26.2` identity/stub artifact to match the existing 26.x build workaround.
- Replaced direct screen API usage with a compatibility wrapper so the common source does not directly call removed/moved 26.2 screen members.
- Removed the hardcoded `org.gradle.java.home` path from `gradle.properties`.
- Updated 26.x Java source/target compatibility to Java 25.

## Build notes

To build on Windows, install JDK 25 and set `JAVA_HOME` to that installation. `org.gradle.java.home` is intentionally not hardcoded in `gradle.properties` because Gradle treats it as an exact local path and fails if that folder does not exist. Gradle's default Java location is derived from `JAVA_HOME` or the `java` on your PATH when `org.gradle.java.home` is not set.

Typical Fabric build command:

```powershell
.\\gradlew.bat chiseledBuildFabric
```

I could not complete a local Gradle build in the sandbox because the sandbox could not resolve `services.gradle.org` to download Gradle 9.5.1.

## Follow-up compile patch

Patched additional Minecraft 26.2 source incompatibilities reported by `:26.2:compileJava`:

- Replaced the removed `net.minecraft.util.Tuple` dependency with project-owned `TimeSpanPair`.
- Removed unused `MultiBufferSource` imports from disabled/commented mixins.
- Removed the obsolete `SubmitNodeStorage.ModelSubmit` local from `MixinModelFeatureRenderer`.
- Switched vanilla entity/block entity constants that disappeared in 26.2 to registry lookups through `BuiltInRegistries`.
- Removed references to removed `ItemTags.COPPER_CHESTS` and `ItemTags.DOORS` constants.
- Wrapped older first-person renderer buffer/feature flushing calls in reflection so removed 26.2 methods do not block compilation.

The sandbox still cannot execute the Gradle wrapper because it cannot resolve `services.gradle.org`, so validate locally with:

```powershell
.\gradlew.bat chiseledBuildFabric
```

## 2026-06-17 runtime mixin refmap fix

- Added explicit refmap names to common/fabric/forge/neoforge mixin configs.
- Configured Architectury Loom `mixin.defaultRefmapName` for each source set so production jars include the generated refmap.
- Fixed `processResources` file patterns from `*.mixin.json` to `*.mixins.json` so placeholders like `${mod_id}` are actually expanded.
- Marked the first-person hand renderer injection points as `require = 0` so a future rename in `ItemInHandRenderer` does not crash the client during mixin preparation.

## Runtime mixin safety hotfix

The 26.2 client crash log showed `MixinModelFeatureRenderer` failing because its `renderModel` injection target could not be found in `ModelFeatureRenderer`. For the 26.2 compatibility build, the common mixin injector requirement is relaxed to `defaultRequire = 0`, and the fragile model feature renderer hook also has `require = 0`.

This prevents startup crashes when Mojang's 26.x renderer pipeline moves or renames internal methods. It may temporarily disable some animation hooks until the exact 26.2 render targets are re-mapped, but it keeps the mod loadable for testing.


## Runtime fix 3

- Updated `MixinModelFeatureRenderer` to target both `renderModel` and `prepareModel`. Minecraft 26.2 now routes `Model.setupAnim(...)` through `prepareModel`, and the old-only hook allowed `LocomotionWrappedRenderState` to reach vanilla model code.
- This fixes the `LocomotionWrappedRenderState cannot be cast to java.lang.Float` crash seen while rendering chest models.

## Runtime animation pass 4

The previous runtime fixes made fragile mixins non-critical so the client could boot, but that could leave the first-person animation hooks inactive. This pass restores the most important animation paths by:

- Adding Mojang/Yarn/intermediary fallback selectors for `ItemInHandRenderer.renderHandsWithItems` / `renderItem` / `method_22976`.
- Cancelling vanilla first-person hand rendering after Locomotion has rendered both animated hands, so the mod no longer depends solely on the private `renderArmWithItem` hook to hide vanilla arms.
- Adding fallback selectors for `ModelPart.resetPose` / `resetTransform` and `ModelPart.translateAndRotate` / `applyTransform`, which are required for Locomotion's matrix-based arm/item transforms to actually affect rendered model parts.
- Adding fallback selectors for `LevelChunk.updateBlockEntityTicker` / `updateTicker` / `method_31723` so chest and shulker animation data can continue ticking when the runtime path uses non-Mojang names.

