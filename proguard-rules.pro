# 0) Don’t fail on missing library classes
-ignorewarnings
-dontwarn net.minecraft.**
-dontwarn net.fabricmc.**
-dontwarn meteordevelopment.**

# 1) Preserve ALL annotation metadata
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# 2) Keep Mixin annotation & your mixins
-keep @interface org.spongepowered.asm.mixin.Mixin
-keep class com.genyo.addon.mixin.** { *; }

# 3) Keep **all** of your addon’s code (so Meteor’s reflection can see your Settings, Commands, etc.)
-keep class com.genyo.addon.** { *; }

# 4) Keep your addon’s main entrypoint (by name)
-keep class com.genyo.GenyoAddon {
    public <init>();
}

# 5) Keep Fabric & Meteor APIs you use reflectively
-dontwarn net.fabricmc.**
-keep class net.fabricmc.** { *; }

-dontwarn meteordevelopment.**
-keep class meteordevelopment.** { *; }

# 6) Shrink/optimize flags
-dontshrink
-dontoptimize
-allowaccessmodification
