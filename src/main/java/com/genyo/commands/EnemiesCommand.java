package com.genyo.commands;

import com.genyo.commands.arguments.EnemyArgumentType;
import com.genyo.commands.arguments.PlayerListEntryArgumentTypeEx;
import com.genyo.systems.enemies.Enemies;
import com.genyo.systems.enemies.Enemy;
import com.genyo.utils.GenyoChatUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

public class EnemiesCommand extends Command {

    public EnemiesCommand() {
        super("enemies", "Adjust enemy settings.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
            .then((argument("player", PlayerListEntryArgumentTypeEx.create())
                .executes(context -> {
                    GameProfile profile = PlayerListEntryArgumentTypeEx.get(context).getProfile();
                    if (profile.name().equals(mc.player.getGameProfile().name())) {
                        GenyoChatUtils.sendError("You can't add yourself as enemy.");
                        return SINGLE_SUCCESS;
                    }

                    Enemy enemy = new Enemy(profile.name(), profile.id());

                    if (Enemies.get().add(enemy)) {
                        GenyoChatUtils.sendMessage(Formatting.GRAY + "Added %s to enemies.".formatted(enemy.getName()));
                    } else error("Already enemies with that kind individual.");

                    return SINGLE_SUCCESS;
                })
            ))
        );

        builder.then(literal("remove")
            .then(argument("enemy", EnemyArgumentType.create())
                .executes(context -> {
                    Enemy enemy = EnemyArgumentType.get(context);
                    if (enemy == null) {
                        GenyoChatUtils.sendError("Not enemies with that player.");
                        return SINGLE_SUCCESS;
                    }

                    if (Enemies.get().remove(enemy)) {
                        GenyoChatUtils.sendMessage(Formatting.GRAY + "Removed %s from enemies.".formatted(enemy.getName()));
                    } else GenyoChatUtils.sendError("Failed to remove enemy.");

                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("list").executes(context -> {
                GenyoChatUtils.sendMessage(Formatting.GOLD + "--- Enemies (%s) ---".formatted(Enemies.get().count()), "genyo-enemies");
                Enemies.get().forEach(enemy -> GenyoChatUtils.sendMessage("%s".formatted(enemy.getName()), enemy.getName()));
                return SINGLE_SUCCESS;
            })
        );
    }

}
