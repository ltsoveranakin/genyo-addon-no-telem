package com.genyo.commands.arguments;

import com.genyo.systems.enemies.Enemies;
import com.genyo.systems.enemies.Enemy;
import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.CommandSource.suggestMatching;

public class EnemyArgumentType implements ArgumentType<String> {

    private static final EnemyArgumentType INSTANCE = new EnemyArgumentType();
    private static final Collection<String> EXAMPLES = List.of("Hulkenberg", "Tsunoda");

    public static EnemyArgumentType create() {
        return INSTANCE;
    }

    public static Enemy get(CommandContext<?> context) {
        return Enemies.get().get(context.getArgument("enemy", String.class));
    }

    private EnemyArgumentType() {}

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggestMatching(Streams.stream(Enemies.get())
            .filter(Objects::nonNull)
            .map(Enemy::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
