package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityPumpHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidTank> {

    private static final int FLUID_TANK_SIZE = 1000;
    private final FluidTank waterTank;
    private final ItemStackHandler containerInventory;

    public MetaTileEntityPumpHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 0);
        this.waterTank = new FluidTank(FLUID_TANK_SIZE);
        this.containerInventory = new ItemStackHandler(2);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPumpHatch(metaTileEntityId);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ContainerInventory", containerInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.containerInventory.deserializeNBT(data.getCompoundTag("ContainerInventory"));
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, containerInventory);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            pushFluidsIntoNearbyHandlers(getFrontFacing());
            fillContainerFromInternalTank(containerInventory, containerInventory, 0, 1);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay())
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, waterTank);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.PUMP_FLUID_HATCH;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.add(waterTank);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return Textures.VOLTAGE_CASINGS[0];
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null; // TODO UI
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.pump_hatch.tooltip")); // TODO Add this tooltip
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", FLUID_TANK_SIZE));
    }
}
