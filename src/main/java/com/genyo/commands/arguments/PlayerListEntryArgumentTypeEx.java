package com.genyo.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlayerListEntryArgumentTypeEx implements ArgumentType<PlayerListEntry> {
    private static final PlayerListEntryArgumentTypeEx INSTANCE = new PlayerListEntryArgumentTypeEx();
    private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(name -> Text.literal("Player list entry with name " + name + " doesn't exist."));

    private static final Collection<String> EXAMPLES = List.of("wuritz", "Barnika18");

    public static PlayerListEntryArgumentTypeEx create() {
        return INSTANCE;
    }

    public static PlayerListEntry get(CommandContext<?> context) {
        return context.getArgument("player", PlayerListEntry.class);
    }

    private PlayerListEntryArgumentTypeEx() {}

    @Override
    public PlayerListEntry parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        PlayerListEntry playerListEntry = null;

        for (PlayerListEntry p : mc.getNetworkHandler().getPlayerList()) {
            if (p.getProfile().name().equalsIgnoreCase(argument)) {
                playerListEntry = p;
                break;
            }
        }
        if (playerListEntry == null) throw NO_SUCH_PLAYER.create(argument);

        return playerListEntry;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Collection<PlayerListEntry> playerListEntries = mc.getNetworkHandler().getPlayerList();
        PlayerListEntry clientEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().name());
        if (playerListEntries.contains(clientEntry)) playerListEntries.remove(clientEntry);

        // just made this to exclude yourself from the list, it was kinda annoying.

        return CommandSource.suggestMatching(playerListEntries.stream().map(playerListEntry -> playerListEntry.getProfile().name()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
