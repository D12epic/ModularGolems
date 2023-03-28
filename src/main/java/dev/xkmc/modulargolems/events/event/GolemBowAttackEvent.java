package dev.xkmc.modulargolems.events.event;

import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class GolemBowAttackEvent extends GolemItemUseEvent {

	private AbstractArrow entity;
	private boolean noPickup, noConsume;
	private double speed = 3, gravity = 0.05;

	public GolemBowAttackEvent(HumanoidGolemEntity golem, ItemStack stack, InteractionHand hand, AbstractArrow entity, boolean infinite) {
		super(golem, stack, hand);
		this.entity = entity;
		this.noPickup = infinite;
		this.noConsume = infinite;
	}

	public void setArrow(AbstractArrow entity) {
		this.entity = entity;
	}

	public void setArrow(AbstractArrow entity, boolean noPickup, boolean noConsume) {
		this.entity = entity;
		this.noPickup = noPickup;
		this.noConsume = noConsume;
	}

	public void setParams(double speed, double gravity) {
		this.speed = speed;
		this.gravity = gravity;
	}

	public AbstractArrow getArrow() {
		return entity;
	}

	public boolean isNoPickup() {
		return noPickup;
	}

	public boolean isNoConsume() {
		return noConsume;
	}

	public double speed() {
		return speed;
	}

	public double gravity() {
		return gravity;
	}

}
