package com.komorebi.tester_mod.entity.tester;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TesterEntity extends Mob implements VillagerDataHolder {

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
        SynchedEntityData.defineId(TesterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_KNOCKBACKABLE =
        SynchedEntityData.defineId(TesterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HIDE_ZERO_DAMAGE =
        SynchedEntityData.defineId(TesterEntity.class, EntityDataSerializers.BOOLEAN);

    private VillagerData villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);

    public static final ResourceKey<DamageType> REMOVE_TESTER =
        ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("tester_mod", "remove_tester"));

    public TesterEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 1000.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_KNOCKBACKABLE, false);
        builder.define(DATA_HIDE_ZERO_DAMAGE, false);
    }

    public void setKnockbackable(boolean knockbackable) {
        this.entityData.set(DATA_KNOCKBACKABLE, knockbackable);
    }

    public boolean isKnockbackable() {
        return this.entityData.get(DATA_KNOCKBACKABLE);
    }

    public void setHideZeroDamage(boolean hideZeroDamage) {
        this.entityData.set(DATA_HIDE_ZERO_DAMAGE, hideZeroDamage);
    }

    public boolean isHideZeroDamage() {
        return this.entityData.get(DATA_HIDE_ZERO_DAMAGE);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.villagerData;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        this.villagerData = data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        getOwnerUUID().ifPresent(uuid -> compound.putUUID("Owner", uuid));
        compound.putBoolean("Knockbackable", isKnockbackable());
        compound.putBoolean("HideZeroDamage", isHideZeroDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(compound.getUUID("Owner")));
        }
        if (compound.contains("Knockbackable")) {
            this.entityData.set(DATA_KNOCKBACKABLE, compound.getBoolean("Knockbackable"));
        }
        if (compound.contains("HideZeroDamage")) {
            this.entityData.set(DATA_HIDE_ZERO_DAMAGE, compound.getBoolean("HideZeroDamage"));
        }
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.invulnerableTime <= 0) {
            this.lastHurt = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && random.nextFloat() < 0.05f) {
            level().addParticle(ParticleTypes.INSTANT_EFFECT,
                getX() + (random.nextDouble() - 0.5) * 0.5,
                getY() + random.nextDouble() * 1.5,
                getZ() + (random.nextDouble() - 0.5) * 0.5,
                0, 0, 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(REMOVE_TESTER)) {
            if (!level().isClientSide()) {
                this.remove(Entity.RemovalReason.KILLED);
            }
            return true;
        }

        if (source.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }

        float actualDamage = amount;

        if (this.invulnerableTime > 0 && !source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (amount <= this.lastHurt) {
                actualDamage = 0;
            } else {
                actualDamage = amount - this.lastHurt;
            }
        }

        if (!source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (actualDamage > 0) {
                this.lastHurt = amount;
                this.invulnerableTime = 10;
            }
        } else {
            this.lastHurt = amount;
            this.invulnerableTime = 10;
        }

        final float displayActual = actualDamage;

        if (!this.level().isClientSide()) {
            if (!(this.isHideZeroDamage() && displayActual == 0)) {
                getOwnerUUID().ifPresent(uuid -> {
                    Player player = level().getPlayerByUUID(uuid);
                    if (player != null) {
                        long gameTick = level().getGameTime();
                        String damageTypeName = source.getMsgId();
                        player.sendSystemMessage(Component.translatable(
                            "chat.tester_mod.damage_info",
                            this.getName().getString(),
                            String.format("%.1f", displayActual),
                            String.format("%.1f", amount),
                            damageTypeName,
                            String.valueOf(gameTick)
                        ));
                    }
                });
                ((ServerLevel) this.level()).playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_HURT, this.getSoundSource(),
                    this.getSoundVolume(), this.getVoicePitch());
            }
        }

        if (this.isKnockbackable()) {
            if (!source.is(DamageTypeTags.NO_KNOCKBACK) && source.getSourcePosition() != null) {
                double dx = source.getSourcePosition().x() - this.getX();
                double dz = source.getSourcePosition().z() - this.getZ();
                this.knockback(0.4, dx, dz);
            }
            if (!source.is(DamageTypeTags.NO_IMPACT)) {
                this.markHurt();
                if (source.getSourcePosition() != null) {
                    this.indicateDamage(
                        source.getSourcePosition().x() - this.getX(),
                        source.getSourcePosition().z() - this.getZ()
                    );
                }
            }
        }

        return false;
    }

    @Override
    public boolean isPushable() {
        return this.isKnockbackable();
    }

    @Override
    public boolean isPushedByFluid() {
        return this.isKnockbackable();
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        if (this.isKnockbackable()) {
            super.doPush(entity);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }
}