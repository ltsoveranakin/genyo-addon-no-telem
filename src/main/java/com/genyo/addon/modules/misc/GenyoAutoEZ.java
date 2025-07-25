package com.genyo.addon.modules.misc;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.systems.incombat.CombatPerson;
import com.genyo.addon.systems.incombat.InCombatSystem;
import com.genyo.addon.utils.MathUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.*;

public class GenyoAutoEZ extends GenyoModule {

    public GenyoAutoEZ() {
        super(GenyoAddon.GENYO, "genyo-auto-ez", "igen igen igen, dikta mamo tyibori.-----------------------------------------------------------------");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDeath = settings.createGroup("Death");

    //--------------------General--------------------//
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Enemy Range")
        .description("Only send message if enemy died inside this range.")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("How many ticks to wait between sending messages.")
        .defaultValue(10)
        .min(0)
        .max(100)
        .build()
    );

    private final Setting<Boolean> trackPlayers = sgGeneral.add(new BoolSetting.Builder()
        .name("Track Players")
        .description("követi, hogy kit öltél meg ewkgnwekjghhewjkfhew")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> combatFocus = sgGeneral.add(new BoolSetting.Builder()
        .name("Combat Focus")
        .description("csak akivel combatban vagy (elvileg)")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> popMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("Pop Messages")
        .description("van egy ped0fil a szobában")
        .defaultValue(List.of("ez pop <NAME> <COUNT>", "pop <NAME> <COUNT>", "i love kiwi pop <NAME> <COUNT>"))
        .build()
    );

    private final Setting<Boolean> enableDeath = sgDeath.add(new BoolSetting.Builder()
        .name("Message on Death")
        .description("hihihi hahaha huhuhu")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> keepPops = sgDeath.add(new BoolSetting.Builder()
        .name("Keep Pops")
        .description("rlkjg krehg kjhre gjk hrej ghjkreh gjkh rekjg hre ")
        .defaultValue(true)
        .visible(enableDeath::get)
        .build()
    );

    private final Setting<List<String>> deathMessages = sgDeath.add(new StringListSetting.Builder()
        .name("Death Messages")
        .description("itt is <NAME> <COUNT> van")
        .visible(enableDeath::get)
        .defaultValue(List.of("<NAME> needed Hulkenberg's nut only <COUNT> times to get Hulkenberg'd", "nemtom <NAME> <COUNT>"))
        .build()
    );

    private final Random r = new Random();
    private int lastPop;
    private final List<Message> messageQueue = new LinkedList<>();
    private final HashMap<PlayerEntity, Integer> taggedPlayers = new HashMap<>();
    private int timer = 0;

    @Override
    public void onActivate() {
        taggedPlayers.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null && mc.world == null) return;

        if (!messageQueue.isEmpty()) timer++;

        if (timer >= tickDelay.get() && !messageQueue.isEmpty()) {
            Message msg = messageQueue.get(0);
            ChatUtils.sendPlayerMsg(msg.message);
            timer = 0;

            if (msg.kill) messageQueue.clear();
            else messageQueue.removeFirst();
        }
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 35) {                                             //----Pop----//
                Entity entity = packet.getEntity(mc.world);
                if (mc.player != null && mc.world != null && entity instanceof PlayerEntity playerEntity) {
                    if (entity != mc.player && mc.player.getPos().distanceTo(entity.getPos()) <= range.get()) {

                        // Combat Focus
                        if (combatFocus.get()) {
                            if (!InCombatSystem.get().contains(new CombatPerson(playerEntity))) return;
                        }

                        if (trackPlayers.get()) {
                            if (taggedPlayers.containsKey(playerEntity)) {
                                int count = taggedPlayers.get(playerEntity) + 1;

                                taggedPlayers.replace(playerEntity, count);
                                sendPopMessage(playerEntity.getName().getString(), count);
                            } else {
                                taggedPlayers.put(playerEntity, 1);
                                sendPopMessage(playerEntity.getName().getString(), 1);
                            }
                        } else {
                            sendPopMessage(playerEntity.getName().getString(), 0);
                        }
                    }
                }
            } else if (packet.getStatus() == 3 && enableDeath.get()) {                   //----Death----//
                Entity entity = packet.getEntity(mc.world);
                if (mc.player != null && mc.world != null && entity instanceof PlayerEntity playerEntity) {
                    if (entity != mc.player && checkPersonValidity(playerEntity)) {
                        if (taggedPlayers.containsKey(playerEntity)) {
                            if (!keepPops.get()) taggedPlayers.replace(playerEntity, 0);

                            sendDeathMessage(playerEntity.getName().getString(), taggedPlayers.get(playerEntity));
                        }
                    }
                }
            }
        }
    }

    private boolean checkPersonValidity(PlayerEntity player) {
        return taggedPlayers.containsKey(player);

        //TODO: if we want to display this for everyone
    }


    private void sendPopMessage(String name, int count) {
        if (popMessages.get().isEmpty()) return;

        String messageString = popMessages.get().get(MathUtil.pickRandom(popMessages.get())).replace("<NAME>", name);
        String countString = String.valueOf(count);

        if (count > 0) {
            messageString = messageString.replace("<COUNT>", "+" + countString);
        } else {
            messageString = messageString.replace("<COUNT>", "+1");
        }

        Message message = new Message(messageString, false);
        messageQueue.add(message);
    }

    private void sendDeathMessage(String name, int pops) {
        if (deathMessages.get().isEmpty()) return;

        String msgString = deathMessages.get().get(MathUtil.pickRandom(deathMessages.get())); // pont leszarom hogyha ugyan azt az üzenetet húzza
        msgString = msgString.replace("<COUNT>", String.valueOf(pops));
        msgString = msgString.replace("<NAME>", name);

        Message message = new Message(msgString, false);
        messageQueue.add(message);
    }


    private record Message(String message, boolean kill) {
    }
}
