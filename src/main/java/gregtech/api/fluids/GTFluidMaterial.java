package gregtech.api.fluids;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;

import org.jetbrains.annotations.NotNull;

public class GTFluidMaterial extends MaterialLiquid {

    private final boolean blocksMovement;

    public GTFluidMaterial(@NotNull MapColor color, boolean blocksMovement) {
        super(color);
        this.blocksMovement = blocksMovement;
    }

    @Override
    public boolean blocksMovement() {
        return blocksMovement;
    }
}
