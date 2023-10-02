
package dev.xkmc.modulargolems.compat.curio;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.event.CurioUnequipEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.mixin.core.AccessorEntity;

import javax.annotation.Nonnull;

public class CurioSlot extends SlotItemHandler {

  private final String identifier;
  private final LivingEntity player;
  private final SlotContext slotContext;

  private NonNullList<Boolean> renderStatuses;
  private boolean canToggleRender;

  public CurioSlot(LivingEntity player, IDynamicStackHandler handler, int index, String identifier,
                   int xPosition, int yPosition, NonNullList<Boolean> renders,
                   boolean canToggleRender) {
    super(handler, index, xPosition, yPosition);
    this.identifier = identifier;
    this.renderStatuses = renders;
    this.player = player;
    this.canToggleRender = canToggleRender;
    this.slotContext = new SlotContext(identifier, player, index, false, renders.get(index));
    this.setBackground(InventoryMenu.BLOCK_ATLAS,
        player.getCommandSenderWorld().isClientSide() ?
            CuriosApi.getIconHelper().getIcon(identifier)
            : new ResourceLocation(Curios.MODID, "slot/empty_curio_slot"));
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public boolean canToggleRender() {
    return this.canToggleRender;
  }

  public boolean getRenderStatus() {

    if (!this.canToggleRender) {
      return true;
    }
    return this.renderStatuses.size() > this.getSlotIndex() &&
        this.renderStatuses.get(this.getSlotIndex());
  }

  @OnlyIn(Dist.CLIENT)
  public String getSlotName() {
    return I18n.get("curios.identifier." + this.identifier);
  }

  @Override
  public void set(@Nonnull ItemStack stack) {
    ItemStack current = this.getItem();
    boolean flag = current.isEmpty() && stack.isEmpty();
    super.set(stack);

    if (!flag && !ItemStack.matches(current, stack) &&
        !((AccessorEntity) this.player).getFirstTick()) {
      CuriosApi.getCuriosHelper().getCurio(stack)
          .ifPresent(curio -> curio.onEquipFromUse(this.slotContext));
    }
  }

  @Override
  public boolean mayPlace(@Nonnull ItemStack stack) {
    CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
    MinecraftForge.EVENT_BUS.post(equipEvent);
    Event.Result result = equipEvent.getResult();

    if (result == Event.Result.DENY) {
      return false;
    }
    return result == Event.Result.ALLOW ||
        (CuriosApi.getCuriosHelper().isStackValid(slotContext, stack) &&
            CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(slotContext))
                .orElse(true) && super.mayPlace(stack));
  }

  @Override
  public boolean mayPickup(Player playerIn) {
    ItemStack stack = this.getItem();
    CurioUnequipEvent unequipEvent = new CurioUnequipEvent(stack, slotContext);
    MinecraftForge.EVENT_BUS.post(unequipEvent);
    Event.Result result = unequipEvent.getResult();

    if (result == Event.Result.DENY) {
      return false;
    }
    return result == Event.Result.ALLOW ||
        ((stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack)) &&
            CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio.canUnequip(slotContext))
                .orElse(true) && super.mayPickup(playerIn));
  }
}
