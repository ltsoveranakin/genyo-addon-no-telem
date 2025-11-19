package com.genyo.commands;

import com.genyo.core.exporter.InfoExporter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class ExportCommand extends Command {

    public ExportCommand() {
        super("export", "Exports Genyo infos for the website to use.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            InfoExporter exporter = new InfoExporter();
            exporter.export();

            return SINGLE_SUCCESS;
        });
    }
}
