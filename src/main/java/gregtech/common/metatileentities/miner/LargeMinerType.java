package gregtech.common.metatileentities.miner;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface LargeMinerType {

    @Nonnull
    TraceabilityPredicate getCasing();

    @Nonnull
    TraceabilityPredicate getFrame();

    @SideOnly(Side.CLIENT)
    @Nonnull
    ICubeRenderer getFrontOverlay();

    @SideOnly(Side.CLIENT)
    @Nonnull
    ICubeRenderer getBaseTexture(@Nullable IMultiblockPart sourcePart);
}
