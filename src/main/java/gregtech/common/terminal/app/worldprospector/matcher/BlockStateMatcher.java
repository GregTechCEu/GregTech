package gregtech.common.terminal.app.worldprospector.matcher;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockStateMatcher implements IMatcher {

    private final IBlockState reference;
    private final List<IProperty<?>> properties = new ArrayList<>();
    private final int meta;
    private final int color;

    public BlockStateMatcher(IBlockState state, int color) {
        this.reference = state;
        for (final IProperty<?> property : state.getPropertyKeys()) {
            if (Objects.equals(property.getName(), "variant") || // Vanilla Minecraft.
                    Objects.equals(property.getName(), "type") || // E.g. ThermalFoundation, TiCon, IC2, Immersive
                                                                  // Engineering.
                    Objects.equals(property.getName(), "ore") || // E.g. BigReactors.
                    Objects.equals(property.getName(), "oretype") || // E.g. DeepResonance.
                    Objects.equals(property.getName(), "stone_type") || // E.g. gtce.
                    Objects.equals(property.getName(), "basictype")) { // Galacticraft.
                properties.add(property);
            }
        }
        this.meta = reference.getBlock().getMetaFromState(reference);
        this.color = color;
    }

    public boolean match(final IBlockState state) {
        if (reference.getBlock() != state.getBlock()) {
            return false;
        }

        if (state.getBlock().getMetaFromState(state) != meta) {
            return false;
        }

        if (properties.isEmpty()) {
            return true;
        }

        for (final IProperty<?> property : properties) {
            if (!state.getPropertyKeys().contains(property)) {
                continue;
            }
            if (!Objects.equals(state.getValue(property), reference.getValue(property))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockStateMatcher) {
            return match(((BlockStateMatcher) obj).reference);
        }
        return super.equals(obj);
    }
}
