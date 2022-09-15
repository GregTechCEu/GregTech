package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.sound.GTSounds;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockFireboxCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;
import static net.minecraft.util.text.TextFormatting.*;

public class MetaTileEntityLargeBoiler extends MultiblockWithDisplayBase {

    public final BoilerType boilerType;
    protected BoilerRecipeLogic recipeLogic;

    private FluidTankList fluidImportInventory;
    private ItemHandlerList itemImportInventory;
    private FluidTankList steamOutputTank;

    private int throttlePercentage = 100;

    public MetaTileEntityLargeBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.recipeLogic = new BoilerRecipeLogic(this);
        resetTileAbilities();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeBoiler(metaTileEntityId, boilerType);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.throttlePercentage = 100;
        this.recipeLogic.invalidate();
        replaceFireboxAsActive(false);
    }

    private void initializeAbilities() {
        this.fluidImportInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.steamOutputTank = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    private void resetTileAbilities() {
        this.fluidImportInventory = new FluidTankList(true);
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.steamOutputTank = new FluidTankList(true);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote && isStructureFormed()) {
            replaceFireboxAsActive(false);
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            int efficiency = recipeLogic.getHeatScaled();
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.efficiency",
                    (efficiency == 0 ? DARK_RED : efficiency <= 40 ? RED : efficiency == 100 ? GREEN : YELLOW).toString() + efficiency + "%"));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", recipeLogic.getLastTickSteam()));

            ITextComponent throttleText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle",
                    AQUA.toString() + getThrottle() + "%");
            withHoverTextTranslate(throttleText, "gregtech.multiblock.large_boiler.throttle.tooltip");
            textList.add(throttleText);

            ITextComponent buttonText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle_modify");
            buttonText.appendText(" ");
            buttonText.appendSibling(withButton(new TextComponentString("[-]"), "sub"));
            buttonText.appendText(" ");
            buttonText.appendSibling(withButton(new TextComponentString("[+]"), "add"));
            textList.add(buttonText);
        }
    }

    @Override
    protected void handleDisplayClick(String componentData, ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int result = componentData.equals("add") ? 5 : -5;
        this.throttlePercentage = MathHelper.clamp(throttlePercentage + result, 25, 100);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && recipeLogic.isActive() && recipeLogic.isWorkingEnabled();
    }

    //FIXME this is basically the same method in RecipeMapMultiblockController
    public void replaceFireboxAsActive(boolean isActive) {
        int id = getWorld().provider.getDimension();
        BlockPos centerPos = getPos().offset(getFrontFacing().getOpposite()).down();
        writeCustomData(GregtechDataCodes.VARIANT_RENDER_UPDATE, buf -> {
            buf.writeInt(id);
            buf.writeBoolean(isActive);
            //9 is the number of blocks we will be passing as 'active'
            buf.writeInt(9);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos blockPos = centerPos.add(x, 0, z);
                    if (isActive) {
                        VariantActiveBlock.ACTIVE_BLOCKS.get(id).add(blockPos);
                    } else {
                        VariantActiveBlock.ACTIVE_BLOCKS.get(id).remove(blockPos);
                    }
                    buf.writeBlockPos(blockPos);
                }
            }
        });
    }

    //FIXME this is exactly the same method in RecipeMapMultiblockController.
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.VARIANT_RENDER_UPDATE) {
            int minX;
            int minY;
            int minZ;
            minX = minY = minZ = Integer.MAX_VALUE;
            int maxX;
            int maxY;
            int maxZ;
            maxX = maxY = maxZ = Integer.MIN_VALUE;

            int id = buf.readInt();
            boolean isActive = buf.readBoolean();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                BlockPos blockPos = buf.readBlockPos();
                if (isActive) {
                    VariantActiveBlock.ACTIVE_BLOCKS.get(id).add(blockPos);
                } else {
                    VariantActiveBlock.ACTIVE_BLOCKS.get(id).remove(blockPos);
                }
                minX = Math.min(minX, blockPos.getX());
                minY = Math.min(minY, blockPos.getY());
                minZ = Math.min(minZ, blockPos.getZ());
                maxX = Math.max(maxX, blockPos.getX());
                maxY = Math.max(maxY, blockPos.getY());
                maxZ = Math.max(maxZ, blockPos.getZ());
            }
            if (getWorld().provider.getDimension() == id) {
                getWorld().markBlockRangeForRenderUpdate(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
            }
        }
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return sourcePart == null ? 0 : isActive() ? 15 : 0;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return boilerType == null ? null : FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "CCC")
                .aisle("XXX", "CPC", "CPC", "CCC")
                .aisle("XXX", "CSC", "CCC", "CCC")
                .where('S', selfPredicate())
                .where('P', states(boilerType.pipeState))
                .where('X', states(boilerType.fireboxState).setMinGlobalLimited(4)
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(autoAbilities())) // muffler, maintenance
                .where('C', states(boilerType.casingState).setMinGlobalLimited(20)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_boiler.description")};
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.large_boiler.rate_tooltip", (int) (boilerType.steamPerTick() * 20 * boilerType.runtimeBoost(20) / 20.0), boilerType.steamPerTick()));
        tooltip.add(I18n.format("gregtech.multiblock.large_boiler.heat_time_tooltip", boilerType.getTicksToBoiling() / 20.0));
        tooltip.add(I18n.format("gregtech.multiblock.large_boiler.explosion_tooltip"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(), recipeLogic.isWorkingEnabled());
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return boilerType.frontOverlay;
    }

    private boolean isFireboxPart(IMultiblockPart sourcePart) {
        return isStructureFormed() && (((MetaTileEntity) sourcePart).getPos().getY() < getPos().getY());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart != null && isFireboxPart(sourcePart)) {
            return isActive() ? boilerType.fireboxActiveRenderer : boilerType.fireboxIdleRenderer;
        }
        return boilerType.casingRenderer;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public SoundEvent getSound() {
        return GTSounds.BOILER;
    }

    @Override
    protected void updateFormedValid() {
        this.recipeLogic.update();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("ThrottlePercentage", throttlePercentage);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        throttlePercentage = data.getInteger("ThrottlePercentage");
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(throttlePercentage);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        throttlePercentage = buf.readVarInt();
    }

    public int getThrottle() {
        return throttlePercentage;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return itemImportInventory;
    }

    @Override
    public FluidTankList getImportFluids() {
        return fluidImportInventory;
    }

    @Override
    public FluidTankList getExportFluids() {
        return steamOutputTank;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof BoilerRecipeLogic);
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
