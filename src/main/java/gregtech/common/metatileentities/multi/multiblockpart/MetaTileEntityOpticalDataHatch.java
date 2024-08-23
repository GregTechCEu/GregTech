package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.data.IDataAccess;
import gregtech.api.capability.data.IStandardDataAccess;
import gregtech.api.capability.data.query.DataQueryObject;
import gregtech.api.capability.data.query.IBridgeable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.reference.WeakHashSet;
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

public class MetaTileEntityOpticalDataHatch extends MetaTileEntityMultiblockNotifiablePart implements
                                            IMultiblockAbilityPart<IStandardDataAccess>,
                                            IStandardDataAccess {

    private final WeakHashSet<DataQueryObject> recentQueries = new WeakHashSet<>();

    private final boolean isTransmitter;

    public MetaTileEntityOpticalDataHatch(ResourceLocation metaTileEntityId, boolean isTransmitter) {
        super(metaTileEntityId, GTValues.LuV, false);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityOpticalDataHatch(metaTileEntityId, this.isTransmitter);
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % 20 == 0) recentQueries.trim();
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        if (!supportsQuery(queryObject) || !recentQueries.add(queryObject)) return false;
        if (isAttachedToMultiBlock()) {
            if (isTransmitter()) {
                MultiblockControllerBase controller = getController();
                if (!controller.isActive()) return false;

                if (IDataAccess.accessData(controller.getAbilities(MultiblockAbility.DATA_ACCESS_HATCH), queryObject))
                    return true;

                List<IStandardDataAccess> reception = controller.getAbilities(MultiblockAbility.OPTICAL_DATA_RECEPTION);
                if (queryObject instanceof IBridgeable bridgeable && reception.size() > 1) {
                    bridgeable.setBridged();
                }
                return IDataAccess.accessData(reception, queryObject);
            } else {
                TileEntity tileEntity = getNeighbor(getFrontFacing());
                if (tileEntity == null) return false;
                IDataAccess cap = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS,
                        getFrontFacing().getOpposite());
                return cap != null && cap.accessData(queryObject);
            }
        }
        return false;
    }

    @Override
    public boolean isTransmitter() {
        return this.isTransmitter;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {
            return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public MultiblockAbility<IStandardDataAccess> getAbility() {
        return isTransmitter() ? MultiblockAbility.OPTICAL_DATA_TRANSMISSION : MultiblockAbility.OPTICAL_DATA_RECEPTION;
    }

    @Override
    public void registerAbilities(@NotNull List<IStandardDataAccess> abilityList) {
        abilityList.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }
}
