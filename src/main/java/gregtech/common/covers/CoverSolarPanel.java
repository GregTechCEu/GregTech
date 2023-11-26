package gregtech.common.covers;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverSolarPanel extends CoverBase implements ITickable {

    private final long EUt;

    public CoverSolarPanel(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                           @NotNull EnumFacing attachedSide, long EUt) {
        super(definition, coverableView, attachedSide);
        this.EUt = EUt;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return getAttachedSide() == EnumFacing.UP &&
                coverable.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.SOLAR_PANEL.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        CoverableView coverable = getCoverableView();
        World world = coverable.getWorld();
        BlockPos blockPos = coverable.getPos();
        if (GTUtility.canSeeSunClearly(world, blockPos)) {
            IEnergyContainer energyContainer = coverable.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                    null);
            if (energyContainer != null) {
                energyContainer.acceptEnergyFromNetwork(null, EUt, 1);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[GTUtility.getTierByVoltage(this.EUt)]
                .getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
