package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoverSolarPanel extends CoverBehavior implements ITickable {

    private final long EUt;

    public CoverSolarPanel(ICoverable coverHolder, EnumFacing attachedSide, long EUt) {
        super(coverHolder, attachedSide);
        this.EUt = EUt;
    }

    @Override
    public boolean canAttach() {
        return attachedSide == EnumFacing.UP && coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.SOLAR_PANEL.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        World world = coverHolder.getWorld();
        BlockPos blockPos = coverHolder.getPos();
        if (GTUtility.canSeeSunClearly(world, blockPos)) {
            IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (energyContainer != null) {
                energyContainer.acceptEnergyFromNetwork(null, EUt, 1);
            }
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[GTUtility.getTierByVoltage(this.EUt)].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
