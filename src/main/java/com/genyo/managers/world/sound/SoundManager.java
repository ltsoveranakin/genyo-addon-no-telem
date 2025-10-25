package com.genyo.managers.world.sound;

import com.genyo.Genyo;
import com.genyo.systems.modules.misc.GenyoSounds;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SoundManager {

    //public static final SoundEvent VINE = registerSound("vine", ".ogg");

    public void playSound(SoundEvent sound) {
        if (mc.player != null && mc.world != null)
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, (float) Modules.get().get(GenyoSounds.class).volume.get() / 100f, 1f);
    }

    private static SoundEvent registerSound(String name, String extension)
    {
        Identifier id = Identifier.of(Genyo.MOD_ID, "sounds/" + name + extension);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

}
