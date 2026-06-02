package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import net.minecraft.util.ResourceLocation;

public class MetaTileEntityHPCAEmpty extends MetaTileEntityHPCAComponent {

    public MetaTileEntityHPCAEmpty(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity copy() {
        return new MetaTileEntityHPCAEmpty(metaTileEntityId);
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public SimpleOverlayRenderer getFrontOverlay() {
        return Textures.HPCA_EMPTY_OVERLAY;
    }

    @Override
    public int getUpkeepEUt() {
        return 0;
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }
}
