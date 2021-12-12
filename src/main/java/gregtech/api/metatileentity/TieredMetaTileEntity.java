package gregtech.api.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerHandler.IEnergyChangeListener;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public abstract class TieredMetaTileEntity extends MetaTileEntity implements IEnergyChangeListener, ITieredMetaTileEntity {

    private final int tier;
    protected IEnergyContainer energyContainer;

    public TieredMetaTileEntity(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        reinitializeEnergyContainer();
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[tier];
        if (isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else this.energyContainer = EnergyContainerHandler.receiverContainer(this,
                tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
    }

    @Override
    public int getActualComparatorValue() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return MathHelper.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    @Override
    public void onEnergyChanged(IEnergyContainer container, boolean isInitialChange) {
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Textures.VOLTAGE_CASINGS[tier];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColor());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getBaseRenderer().render(renderState, translation, colouredPipeline);
    }

    /**
     * Tier of machine determines it's input voltage, storage and generation rate
     *
     * @return tier of this machine
     */
    @Override
    public int getTier() {
        return tier;
    }

    /**
     * Determines max input or output amperage used by this meta tile entity
     * if emitter, it determines size of energy packets it will emit at once
     * if receiver, it determines max input energy per request
     *
     * @return max amperage received or emitted by this machine
     */
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    /**
     * Determines if this meta tile entity is in energy receiver or emitter mode
     *
     * @return true if machine emits energy to network, false it it accepts energy from network
     */
    protected boolean isEnergyEmitter() {
        return false;
    }

}
