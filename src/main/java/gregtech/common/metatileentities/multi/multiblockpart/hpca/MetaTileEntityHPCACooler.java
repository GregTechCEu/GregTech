package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.capability.IHPCACoolantProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityHPCACooler extends MetaTileEntityHPCAComponent implements IHPCACoolantProvider {

    private final boolean advanced;

    public MetaTileEntityHPCACooler(ResourceLocation metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCACooler(metaTileEntityId, advanced);
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public SimpleOverlayRenderer getFrontOverlay() {
        return advanced ? Textures.HPCA_ACTIVE_COOLER_OVERLAY : Textures.HPCA_HEAT_SINK_OVERLAY;
    }

    @Override
    public SimpleOverlayRenderer getFrontActiveOverlay() {
        return advanced ? Textures.HPCA_ACTIVE_COOLER_ACTIVE_OVERLAY : getFrontOverlay();
    }

    @Override
    public int getUpkeepEUt() {
        return advanced ? GTValues.VA[GTValues.IV] : 0;
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }

    @Override
    public int getCoolingAmount() {
        return advanced ? 2 : 1;
    }

    @Override
    public boolean isActiveCooler() {
        return advanced;
    }

    @Override
    public int getMaxCoolantPerTick() {
        return advanced ? 64 : 0;
    }
}
