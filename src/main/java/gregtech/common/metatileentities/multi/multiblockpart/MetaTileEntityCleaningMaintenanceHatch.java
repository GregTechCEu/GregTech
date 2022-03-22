package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Sets;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

public class MetaTileEntityCleaningMaintenanceHatch extends MetaTileEntityAutoMaintenanceHatch {

    private static final CleaningMaintenanceHatchDummyCleanroom DUMMY_CLEANROOM = new CleaningMaintenanceHatchDummyCleanroom();

    public MetaTileEntityCleaningMaintenanceHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCleaningMaintenanceHatch(metaTileEntityId);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (controllerBase instanceof ICleanroomReceiver && ((ICleanroomReceiver) controllerBase).getCleanroom() == null) {
            ((ICleanroomReceiver) controllerBase).setCleanroom(DUMMY_CLEANROOM);
        }
    }

    @Override
    public int getTier() {
        return GTValues.UV;
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            return this.hatchTexture = controller.getBaseTexture(this);
        } else if (this.hatchTexture != null) {
            if (hatchTexture != Textures.getInactiveTexture(hatchTexture)) {
                return this.hatchTexture = Textures.getInactiveTexture(hatchTexture);
            }
            return this.hatchTexture;
        } else {
            return Textures.VOLTAGE_CASINGS[getTier()];
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        getBaseTexture().render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        if (shouldRenderOverlay()) Textures.MAINTENANCE_OVERLAY_CLEANING.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        // does nothing here so the Auto Maintenance Hatch can put this right after it
    }

    private static class CleaningMaintenanceHatchDummyCleanroom implements ICleanroomProvider {

        public CleaningMaintenanceHatchDummyCleanroom() {

        }

        @Override
        public boolean isClean() {
            return true;
        }

        @Override
        public boolean drainEnergy(boolean simulate) {
            return true;
        }

        @Override
        public long getEnergyInputPerSecond() {
            return 0;
        }

        @Override
        public int getEnergyTier() {
            return 0;
        }

        @Override
        public Set<CleanroomType> getTypes() {
            return Sets.newHashSet(CleanroomType.CLEANROOM);
        }

        @Override
        public void setClean(boolean isClean) {

        }
    }
}
