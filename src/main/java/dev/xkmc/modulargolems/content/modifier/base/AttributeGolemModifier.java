package dev.xkmc.modulargolems.content.modifier.base;

import dev.xkmc.modulargolems.content.core.GolemStatType;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * should not be used in materials
 */
public class AttributeGolemModifier extends GolemModifier {

	public record AttrEntry(Supplier<GolemStatType> type, DoubleSupplier value) {

		public double getValue(int level) {
			return value.getAsDouble() * level;
		}
	}

	public final AttrEntry[] entries;

	public AttributeGolemModifier(int max, AttrEntry... entries) {
		super(StatFilterType.MASS, max);
		this.entries = entries;
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		List<MutableComponent> ans = new ArrayList<>();
		for (AttrEntry ent : entries) {
			ans.add(ent.type.get().getAdderTooltip(ent.getValue(v)));
		}
		return ans;
	}
}
