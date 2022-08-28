package dev.xkmc.modulargolems.events;

import dev.xkmc.modulargolems.content.config.GolemMaterial;
import dev.xkmc.modulargolems.content.config.GolemMaterialConfig;
import dev.xkmc.modulargolems.content.core.IGolemPart;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.item.GolemHolder;
import dev.xkmc.modulargolems.content.item.GolemPart;
import dev.xkmc.modulargolems.content.upgrades.UpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CraftEvents {

	@SubscribeEvent
	public static void onAnvilCraft(AnvilUpdateEvent event) {
		ItemStack stack = event.getLeft();
		ItemStack block = event.getRight();
		if (stack.getItem() instanceof GolemPart part && part.count <= block.getCount()) {
			var mat = GolemMaterial.getMaterial(block);
			if (mat.isPresent()) {
				ItemStack new_stack = stack.copy();
				GolemPart.setMaterial(new_stack, mat.get());
				event.setOutput(new_stack);
				event.setMaterialCost(part.count);
				event.setCost(1);
			}
		}
		if (stack.getItem() instanceof GolemHolder<?, ?> holder) {
			if (block.getItem() instanceof UpgradeItem upgrade) {
				//TODO
			} else {
				fixGolem(event, holder, stack);
			}
		}
	}

	private static <T extends AbstractGolemEntity<T, P>, P extends IGolemPart<P>>
	void fixGolem(AnvilUpdateEvent event, GolemHolder<T, P> holder, ItemStack stack) {
		if (stack.getTag() == null || !stack.getTag().contains(GolemHolder.KEY_ENTITY)) return;
		float max = GolemHolder.getMaxHealth(stack);
		float health = GolemHolder.getHealth(stack);
		if (health >= max) return;
		var mats = GolemHolder.getMaterial(stack);
		var type = holder.getEntityType();
		P part = type.getBodyPart();
		if (mats.size() <= part.ordinal()) return;
		var mat = mats.get(part.ordinal());
		var ing = GolemMaterialConfig.get().ingredients.get(mat.id());
		ItemStack repairStack = event.getRight();
		if (ing == null || !ing.test(repairStack)) return;
		int maxFix = Math.min(repairStack.getCount(), (int) Math.ceil((max - health) / max * 4));
		event.setMaterialCost(maxFix);
		event.setCost(maxFix);
		ItemStack result = stack.copy();
		GolemHolder.setHealth(result, Math.min(max, health + max / 4 * maxFix));
		event.setOutput(result);
	}

}
