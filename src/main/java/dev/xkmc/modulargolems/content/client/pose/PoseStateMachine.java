package dev.xkmc.modulargolems.content.client.pose;
import dev.xkmc.modulargolems.content.entity.metalgolem.MetalGolemEntity;
import dev.xkmc.modulargolems.content.item.equipments.MetalGolemWeaponItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EquipmentSlot;
import static dev.xkmc.modulargolems.content.client.pose.PoseStateMachine.State.*;
public class PoseStateMachine {
    private final MetalGolemEntity mg;
    public PoseStateMachine(MetalGolemEntity mg){
        this.mg=mg;
    }
    public enum State {RaiseArms, LayArmsDown, StartAttacking}
    private PoseStateMachine.State state = LayArmsDown;
    private AnimationState as = new AnimationState();
    public void tick() {
        int atkTick = mg.getAttackAnimationTick();
        if (state == LayArmsDown) {
            if (mg.isAggressive()) {
                state = RaiseArms;
            }
        } else if (state == RaiseArms) {
            if (atkTick > 0) {
                state = StartAttacking;
                switchStates(mg).start(mg.tickCount);
                mg.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
            }
        } else if (state == StartAttacking) {
            if (!(atkTick > 0)) {
                state = LayArmsDown;
            }
        }
    }
    public AnimationState switchStates(MetalGolemEntity pEntity) {
        if (pEntity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof MetalGolemWeaponItem wi) {
            switch (wi.getGolemWeaponType(wi)) {
                case AXE, SWORD -> as = pEntity.axeAttackAnimationState;
                case SPEAR -> as = pEntity.spearAttackAnimationState;
            }
        }else{
            as =pEntity.unArmAttackAnimationState;
        }
        return as;
    }
}
