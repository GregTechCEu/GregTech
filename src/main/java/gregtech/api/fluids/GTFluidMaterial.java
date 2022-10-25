package gregtech.api.fluids;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;

import javax.annotation.Nonnull;

public class GTFluidMaterial extends MaterialLiquid {

    private final boolean blocksMovement;

    public GTFluidMaterial(@Nonnull MapColor color, boolean blocksMovement) {
        super(color);
        this.blocksMovement = blocksMovement;
    }

    @Override
    public boolean blocksMovement() {
        return blocksMovement;
    }
}
