package dev.xkmc.modulargolems.content.entity.metalgolem;

import com.google.common.collect.ImmutableMultimap;
import dev.xkmc.modulargolems.content.item.equipments.GolemEquipmentItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MetalGolemShieldItem extends GolemEquipmentItem {
    protected final double defence;
    public MetalGolemShieldItem(Properties properties, EquipmentSlot slot, Supplier<EntityType<?>> type,
     Consumer<ImmutableMultimap.Builder<Attribute, AttributeModifier>> attr,double defence) {
        super(properties, slot, type, attr);
        this.defence = defence;
    }
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(pStack, pTooltip);
    }

    public Double getDefence() {
        return defence;
    }
}
