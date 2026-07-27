package com.komorebi.tester_mod.command;

import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.komorebi.tester_mod.entity.tester.TesterEntity;

public class RemoveTesterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("removetester")
            .then(Commands.literal("self")
                .requires(source -> source.hasPermission(0))
                .executes(context -> removeSelf(context.getSource())))
            .then(Commands.literal("all")
                .requires(source -> source.hasPermission(2))
                .executes(context -> removeAll(context.getSource())))
        );
    }

    private static int removeSelf(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("command.tester_mod.only_players"));
            return 0;
        }
        UUID playerUUID = player.getUUID();
        ServerLevel level = source.getLevel();

        int count = 0;
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof TesterEntity tester) {
                if (tester.getOwnerUUID().map(uuid -> uuid.equals(playerUUID)).orElse(false)) {
                    tester.remove(Entity.RemovalReason.KILLED);
                    count++;
                }
            }
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.translatable("command.tester_mod.removed_self", finalCount), true);
        return count;
    }

    private static int removeAll(CommandSourceStack source) {
        ServerLevel level = source.getLevel();

        int count = 0;
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof TesterEntity tester) {
                tester.remove(Entity.RemovalReason.KILLED);
                count++;
            }
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.translatable("command.tester_mod.removed_all", finalCount), true);
        return count;
    }
}
