package dev.xkmc.modulargolems.content.entity.metalgolem;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MGLightShieldItem extends MetalGolemShieldItem{
    public MGLightShieldItem(Properties properties, EquipmentSlot slot, Supplier<EntityType<?>> type, Consumer<ImmutableMultimap.Builder<Attribute, AttributeModifier>> attr, double defence) {
        super(properties, slot, type, attr, defence);
    }
}

