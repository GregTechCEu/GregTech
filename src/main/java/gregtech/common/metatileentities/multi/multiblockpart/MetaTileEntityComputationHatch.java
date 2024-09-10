package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.data.IComputationDataAccess;
import gregtech.api.capability.data.IComputationProvider;
import gregtech.api.capability.data.IDataAccess;
import gregtech.api.capability.data.query.DataQueryObject;
import gregtech.api.capability.data.query.IBridgeable;
import gregtech.api.capability.data.query.IComputationQuery;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityComputationHatch extends MetaTileEntityMultiblockPart
                                            implements IMultiblockAbilityPart<IComputationDataAccess>,
                                            IComputationDataAccess {

    private final boolean isTransmitter;

    public MetaTileEntityComputationHatch(ResourceLocation metaTileEntityId, boolean isTransmitter) {
        super(metaTileEntityId, GTValues.ZPM);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityComputationHatch(metaTileEntityId, isTransmitter);
    }

    @Override
    public boolean isTransmitter() {
        return this.isTransmitter;
    }

    @Override
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        if (isAttachedToMultiBlock()) {
            if (isTransmitter()) {
                MultiblockControllerBase controller = getController();
                if (!controller.isActive()) return false;

                if (controller instanceof IComputationProvider provider &&
                        queryObject instanceof IComputationQuery cq) {
                    cq.registerProvider(provider);
                }
                List<IComputationDataAccess> reception = controller
                        .getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION);
                if (queryObject instanceof IBridgeable bridgeable && reception.size() > 1) {
                    bridgeable.setBridged();
                }
                return IDataAccess.accessData(reception, queryObject);
            } else {
                TileEntity tileEntity = getNeighbor(getFrontFacing());
                if (tileEntity == null) return false;

                IDataAccess cap = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS,
                        getFrontFacing().getOpposite());
                if (queryObject.traverseTo(cap)) return cap.accessData(queryObject);
            }
        }
        return false;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            // todo make its own texture
            Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IComputationDataAccess> getAbility() {
        return isTransmitter() ? MultiblockAbility.COMPUTATION_DATA_TRANSMISSION :
                MultiblockAbility.COMPUTATION_DATA_RECEPTION;
    }

    @Override
    public void registerAbilities(List<IComputationDataAccess> abilityList) {
        abilityList.add(this);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {
            return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }
}
