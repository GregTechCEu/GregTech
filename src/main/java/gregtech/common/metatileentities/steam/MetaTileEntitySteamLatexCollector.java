package gregtech.common.metatileentities.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer.RenderSide;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class MetaTileEntitySteamLatexCollector extends MetaTileEntity {
    private final int energyPerTick = 16;
    private final int tankSize = 16000;
    private final long latexCollectionAmount = 3L;
    private boolean hasRubberLog;
    private EnumFacing outputFacingFluids;


    public MetaTileEntitySteamLatexCollector(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.initializeInventory();
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamLatexCollector(this.metaTileEntityId);
    }

    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000).setFilter(CommonFluidFilters.STEAM));
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{new FluidTank(this.tankSize)});
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline, multiplier);
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, coloredPipeline);

        Textures.LATEX_COLLECTOR_OVERLAY.renderOrientedState(renderState, translation, coloredPipeline, this.getFrontFacing(), this.isActive(), true);
        if (this.getOutputFacingFluids() != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacingFluids(), renderState, translation, coloredPipeline);
        }
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(RenderSide.TOP), this.getPaintingColorForRendering());
    }
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(false), 175, 176);
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = (new TankWidget(this.exportFluids.getTankAt(0), 69, 52, 18, 18)).setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 16777215);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 16777215);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 16777215);
        builder.widget((new AdvancedTextWidget(11, 50, this::addDisplayText, 16777215)).setMaxWidthLimit(84));
        return builder.label(6, 6, this.getMetaFullName()).widget((new FluidContainerSlotWidget(this.importItems, 0, 90, 17, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT_STEAM.get(false), GuiTextures.IN_SLOT_OVERLAY})).widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON)).widget((new SlotWidget(this.exportItems, 0, 90, 54, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT_STEAM.get(false), GuiTextures.OUT_SLOT_OVERLAY})).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(false), 10).build(this.getHolder(), entityPlayer);

    }

    void addDisplayText(List<ITextComponent> textList) {
        if (!this.drainEnergy(true)) {
            textList.add((new TextComponentTranslation("gregtech.machine.latex_collector.steam", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)));
        }
    }

    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.latex_collector.tooltip", this.latexCollectionAmount));
    }

    public boolean drainEnergy(boolean simulate) {
        int resultSteam = this.importFluids.getTankAt(0).getFluidAmount() - this.energyPerTick;
        if ((long)resultSteam >= 0L && resultSteam <= this.importFluids.getTankAt(0).getCapacity()) {
            if (!simulate) {
                this.importFluids.getTankAt(0).drain(this.energyPerTick, true);
            }
            return true;
        } else {
            return false;
        }
    }

    public void update() {
        super.update();

        //if ~client world skip update
        if (this.getWorld().isRemote) return;

        //if rubber log and energy (steam)
        if (this.hasRubberLog && this.drainEnergy(true)) {
            //inspect tank, amount, and capacity
            IFluidTank tank = this.exportFluids.getTankAt(0);
            assert tank != null;

            int stored = tank.getFluidAmount();
            int capacity = tank.getCapacity();

            //collection
            if (stored < capacity) {
                //is pseudo full if full collection amount can't be inserted
                boolean isOutputFull = stored + this.latexCollectionAmount >= capacity;

                //handle cases
                if (isOutputFull) {
                    tank.fill(Materials.Latex.getFluid(capacity - stored), true);
                } else {
                    tank.fill(Materials.Latex.getFluid((int) this.latexCollectionAmount), true);
                }

                //take energy (steam)
                this.drainEnergy(false);
            }
        }

        //attempt pumping every 5 ticks
        if (this.getOffsetTimer() % 5L == 0L) {
            if(this.getOutputFacingFluids() != null){
                this.pushFluidsIntoNearbyHandlers(new EnumFacing[]{this.getOutputFacingFluids()});
            }
            this.fillContainerFromInternalTank();
        }
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        this.checkAdjacentBlocks();
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        this.checkAdjacentBlocks();
    }

    public void onNeighborChanged() {
        super.onNeighborChanged();
        this.checkAdjacentBlocks();
    }

    public void checkAdjacentBlocks(){
        if(this.getWorld() != null){
            this.hasRubberLog = false;
            if(!this.getWorld().isRemote) {
                EnumFacing back = this.getFrontFacing().getOpposite();

                Block block = this.getWorld().getBlockState(this.getPos().offset(back)).getBlock();
                if (block == MetaBlocks.RUBBER_LOG) {
                    this.hasRubberLog = true;
                }
            }
        }
    }

    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        this.onNeighborChanged();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("OutputFacingF", this.getOutputFacingFluids().getIndex());
        data.setBoolean("hasRubberLogs", this.hasRubberLog);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("hasRubberLogs")) {
            this.hasRubberLog = data.getBoolean("hasRubberLogs");
        }
        if (data.hasKey("OutputFacingF")) {
            this.outputFacingFluids = EnumFacing.byIndex(data.getInteger("OutputFacingF"));
        }
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.getOutputFacingFluids().getIndex());
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        }
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getOutputFacingFluids() == facing) {
                return false;
            } else if (this.hasFrontFacing() && (facing == this.getFrontFacing() || facing == this.getFrontFacing().getOpposite())) {
                return false;
            } else {
                if (!this.getWorld().isRemote) {
                    this.setOutputFacingFluids(facing);
                }
                return true;
            }
        } else {
            boolean wrenchClickResult = false;
            if (this.getOutputFacingFluids() != facing.getOpposite() && this.getOutputFacingFluids() != facing) wrenchClickResult = super.onWrenchClick(playerIn, hand, facing, hitResult);
            this.checkAdjacentBlocks();
            return wrenchClickResult;
        }
    }

    public void setOutputFacingFluids(EnumFacing outputFacing) {
        this.outputFacingFluids = outputFacing;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(100, (buf) -> {
                buf.writeByte(this.outputFacingFluids.getIndex());
            });
            this.markDirty();
        }

    }

    public EnumFacing getOutputFacingFluids() {
        return this.outputFacingFluids == null ? EnumFacing.SOUTH : this.outputFacingFluids;
    }

    public boolean needsSneakToRotate() {
        return true;
    }
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
