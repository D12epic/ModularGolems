package dev.xkmc.modulargolems.content.entity.dog;

import dev.xkmc.modulargolems.content.config.GolemMaterialConfig;
import dev.xkmc.modulargolems.content.entity.common.SweepGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class DogGolemEntity extends SweepGolemEntity<DogGolemEntity, DogGolemPartType> {

    public DogGolemEntity(EntityType<DogGolemEntity> type, Level level) {
        super(type, level);
        this.maxUpStep = 1.0F;
    }

    protected boolean performDamageTarget(Entity target, float damage, double kb) {
        if (target instanceof LivingEntity le) {
            le.setLastHurtByPlayer(getOwner());
        }
        boolean succeed = target.hurt(DamageSource.mobAttack(this), damage);
        if (succeed) {
            double d1 = Math.max(0.0D, 1.0D - kb);
            double dokb = getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, dokb * d1, 0.0D));
            this.doEnchantDamageEffects(this, target);
        }
        return succeed;
    }

    // sit

    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
    private boolean orderedToSit;

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    public void addAdditionalSaveData(CompoundTag p_21819_) {
        super.addAdditionalSaveData(p_21819_);
        p_21819_.putBoolean("Sitting", this.orderedToSit);
    }

    public void readAdditionalSaveData(CompoundTag p_21815_) {
        super.readAdditionalSaveData(p_21815_);
        this.orderedToSit = p_21815_.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean p_21838_) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (p_21838_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
        }

    }

    // ------ vanilla golem behavior

    private int attackAnimationTick;

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        registerTargetGoals();
    }

    protected void doPush(Entity p_28839_) {
        if (p_28839_ instanceof Enemy && !(p_28839_ instanceof Creeper) && this.getRandom().nextInt(20) == 0) {
            this.setTarget((LivingEntity) p_28839_);
        }
        super.doPush(p_28839_);
    }

    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            --this.attackAnimationTick;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > (double) 2.5000003E-7F && this.random.nextInt(5) == 0) {
            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY() - (double) 0.2F);
            int k = Mth.floor(this.getZ());
            BlockPos pos = new BlockPos(i, j, k);
            BlockState blockstate = this.level.getBlockState(pos);
            if (!blockstate.isAir()) {
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(pos), this.getX() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), this.getY() + 0.1D, this.getZ() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), 4.0D * ((double) this.random.nextFloat() - 0.5D), 0.5D, ((double) this.random.nextFloat() - 0.5D) * 4.0D);
            }
        }
    }

    public boolean doHurtTarget(Entity target) {
        this.attackAnimationTick = 10;
        this.level.broadcastEntityEvent(this, (byte) 4);
        float rawDamage = this.getAttackDamage();
        float damage = (int) rawDamage > 0 ? rawDamage / 2.0F + (float) this.random.nextInt((int) rawDamage) : rawDamage;
        double kb;
        if (target instanceof LivingEntity livingentity) {
            kb = livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        } else {
            kb = 0;
        }
        boolean flag = performRangedDamage(target, damage, kb);
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return flag;
    }

    public boolean hurt(DamageSource source, float amount) {
        IronGolem.Crackiness crack = this.getCrackiness();
        boolean flag = super.hurt(source, amount);
        if (flag && this.getCrackiness() != crack) {
            this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
        }
        return flag;
    }

    public IronGolem.Crackiness getCrackiness() {
        return IronGolem.Crackiness.byFraction(this.getHealth() / this.getMaxHealth());
    }

    public void handleEntityEvent(byte event) {
        if (event == 4) {
            this.attackAnimationTick = 10;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        } else {
            super.handleEntityEvent(event);
        }

    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    protected SoundEvent getHurtSound(DamageSource p_28872_) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    protected void playStepSound(BlockPos p_28864_, BlockState p_28865_) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }

    public boolean checkSpawnObstruction(LevelReader p_28853_) {
        BlockPos blockpos = this.blockPosition();
        BlockPos blockpos1 = blockpos.below();
        BlockState blockstate = p_28853_.getBlockState(blockpos1);
        if (!blockstate.entityCanStandOn(p_28853_, blockpos1, this)) {
            return false;
        } else {
            for (int i = 1; i < 3; ++i) {
                BlockPos blockpos2 = blockpos.above(i);
                BlockState blockstate1 = p_28853_.getBlockState(blockpos2);
                if (!NaturalSpawner.isValidEmptySpawnBlock(p_28853_, blockpos2, blockstate1, blockstate1.getFluidState(), EntityType.IRON_GOLEM)) {
                    return false;
                }
            }

            return NaturalSpawner.isValidEmptySpawnBlock(p_28853_, blockpos, p_28853_.getBlockState(blockpos), Fluids.EMPTY.defaultFluidState(), EntityType.IRON_GOLEM) && p_28853_.isUnobstructed(this);
        }
    }

    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, 0.875F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
    }

    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (getMaterials().size() != DogGolemPartType.values().length)
            return super.mobInteract(player, hand);
        var mat = getMaterials().get(DogGolemPartType.BODY.ordinal());
        Ingredient ing = GolemMaterialConfig.get().ingredients.get(mat.id());
        if (!ing.test(itemstack)) {
            return super.mobInteract(player, hand);
        } else {
            float f = this.getHealth();
            this.heal(getMaxHealth() / 4f);
            if (this.getHealth() == f) {
                return InteractionResult.PASS;
            } else {
                float f1 = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
                this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, f1);
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
    }

}

