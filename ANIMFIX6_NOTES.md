# Locomotion 26.2 Animfix 6

This patch fixes the `MixinGameRenderer` crash caused by the 26.2 `renderItemInHand` descriptor change.

The previous fallback hook used the older descriptor:

```java
(float, boolean, Matrix4f, CallbackInfo)
```

The crash log shows 26.2 expects:

```java
(CameraRenderState, float, Matrix4fc, CallbackInfo)
```

Changes:

- Updated the GameRenderer first-person arm fallback injection to target:
  `renderItemInHand(CameraRenderState, float, Matrix4fc)`.
- Removed the obsolete `sleeping` parameter check from that hook.
- Converted `Matrix4fc` to `Matrix4f` before applying it to the fallback `PoseStack`.
- Scoped the fallback method to `>=26` so older Stonecutter versions do not compile against `CameraRenderState`.

Build with:

```powershell
.\gradlew.bat clean chiseledBuildFabric
```
