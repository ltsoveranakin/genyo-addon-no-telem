package com.genyo.mixin.accessor;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Input.class)
public interface AccessorInput {
    @Accessor("movementVector")
    Vec2f getMovementVector();

    @Mutable
    @Accessor("movementVector")
    void setMovementVector(Vec2f vec);
}

