package com.komorebi.tester_mod.item.tester;

import com.komorebi.tester_mod.entity.ModEntities;
import com.komorebi.tester_mod.entity.tester.TesterEntity;
import com.komorebi.tester_mod.event.SelfTesterHandler;
import com.komorebi.tester_mod.network.OpenSelfTesterConfigPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class TesterSetterItem extends Item {

    public TesterSetterItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof TesterEntity) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
                if (player.isShiftKeyDown()) {
                    if (SelfTesterHandler.isEnabled(serverPlayer)) {
                        PacketDistributor.sendToPlayer(serverPlayer, new OpenSelfTesterConfigPayload(
                            SelfTesterHandler.hasDamageImmunity(serverPlayer),
                            SelfTesterHandler.shouldOutputZeroDamage(serverPlayer)
                        ));
                    } else {
                        serverPlayer.displayClientMessage(
                            Component.translatable("chat.tester_mod.self_tester.not_enabled"),
                            true
                        );
                    }
                } else {
                    SelfTesterHandler.toggle(serverPlayer);
                }
            } else {
                serverPlayer.displayClientMessage(
                    Component.translatable("chat.tester_mod.self_tester.survival_only"),
                    true
                );
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Direction facing = context.getHorizontalDirection();
        ServerLevel serverLevel = (ServerLevel) level;

        TesterEntity tester = ModEntities.TESTER.get().create(serverLevel);
        if (tester != null) {
            tester.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            tester.setYRot(facing.toYRot());
            tester.setYHeadRot(facing.toYRot());
            if (context.getPlayer() != null) {
                tester.setOwner(context.getPlayer());
            }
            serverLevel.addFreshEntity(tester);
        }

        return InteractionResult.CONSUME;
    }
}
