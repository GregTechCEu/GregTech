package gregtech.common.metatileentities.miner;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.model.miningpipe.MiningPipeModel;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LargeMinerType {

    @NotNull
    TraceabilityPredicate getCasing();

    @NotNull
    TraceabilityPredicate getFrame();

    @SideOnly(Side.CLIENT)
    @NotNull
    ICubeRenderer getFrontOverlay();

    @SideOnly(Side.CLIENT)
    @NotNull
    ICubeRenderer getBaseTexture(@Nullable IMultiblockPart sourcePart);

    @SideOnly(Side.CLIENT)
    @NotNull
    MiningPipeModel getMiningPipeModel();
}
