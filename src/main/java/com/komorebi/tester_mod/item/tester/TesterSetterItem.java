package com.komorebi.tester_mod.item.tester;

import com.komorebi.tester_mod.entity.ModEntities;
import com.komorebi.tester_mod.entity.tester.TesterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TesterSetterItem extends Item {

    public TesterSetterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof TesterEntity tester && player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (target instanceof TesterEntity tester) {
            if (player.isShiftKeyDown()) {
                boolean newState = !tester.isHideZeroDamage();
                tester.setHideZeroDamage(newState);
                player.displayClientMessage(Component.translatable(
                    "chat.tester_mod.hide_zero_damage." + (newState ? "enabled" : "disabled")), true);
            } else {
                boolean newState = !tester.isKnockbackable();
                tester.setKnockbackable(newState);
                player.displayClientMessage(Component.translatable(
                    "chat.tester_mod.knockbackable." + (newState ? "enabled" : "disabled")), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
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

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.CONSUME;
    }
}
