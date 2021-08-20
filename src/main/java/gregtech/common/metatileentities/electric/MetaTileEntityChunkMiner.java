package gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.IMiner;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class MetaTileEntityChunkMiner extends TieredMetaTileEntity implements IMiner {

    private final int inventorySize;
    private final long energyPerTick;
    private AtomicLong x = new AtomicLong(Long.MAX_VALUE), y = new AtomicLong(Long.MAX_VALUE), z = new AtomicLong(Long.MAX_VALUE);
    private final ItemStackHandler containerInventory;

    public MetaTileEntityChunkMiner(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier];
        this.containerInventory = new ItemStackHandler(2);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityChunkMiner(metaTileEntityId, getTier());
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000)
                .setFillPredicate(fluidStack -> Objects.requireNonNull(Materials.DrillingFluid.getFluid(0)).isFluidEqual(fluidStack)));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI.Builder builder = ModularUI.defaultBuilder();

        if (getTier() == 3) {
            builder = new ModularUI.Builder(GuiTextures.BORDERED_BACKGROUND, 195, 176);
            builder.bindPlayerInventory(entityPlayer.inventory, 94);

            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(new SlotWidget(exportItems, index, 153 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT));
                }
            }

        } else {
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(new SlotWidget(exportItems, index, 143 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT));
                }
            }
            builder.bindPlayerInventory(entityPlayer.inventory);
        }

        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY)
                .label(10, 5, getMetaFullName());

        TankWidget tankWidget = new TankWidget(importFluids.getTankAt(0), 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);


        builder.widget(new FluidContainerSlotWidget(containerInventory, 0, 90, 18, true)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(containerInventory, 1, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY));


        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else {
                Textures.CHUNK_MINER_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            }
        }
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(inventorySize);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(0);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VN[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.miner.description"));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", this.getDrillingFluidConsumedPerTick(), I18n.format(Materials.DrillingFluid.getFluid(0).getUnlocalizedName())));
    }

    public boolean drainEnergy() {
        FluidStack drillingFluid = Materials.DrillingFluid.getFluid(this.getDrillingFluidConsumedPerTick());
        FluidStack canDrain = this.importFluids.drain(drillingFluid, false);
        if (energyContainer.getEnergyStored() >= energyPerTick && canDrain != null && canDrain.amount == this.getDrillingFluidConsumedPerTick()) {
            energyContainer.removeEnergy(energyPerTick);
            this.importFluids.drain(drillingFluid, true);
            return true;
        }
        return false;
    }


    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillInternalTankFromFluidContainer(containerInventory, containerInventory, 0, 1);
            if (!drainEnergy()) {
                return;
            }

            WorldServer world = (WorldServer) this.getWorld();
            Chunk chuck = world.getChunk(getPos());
            ChunkPos chunkPos = chuck.getPos();
            if (x.get() == Long.MAX_VALUE || x.get() == 0) {
                x.set(chunkPos.getXStart());
            }
            if (z.get() == Long.MAX_VALUE || z.get() == 0) {
                z.set(chunkPos.getZStart());
            }
            if (y.get() == Long.MAX_VALUE || y.get() == 0) {
                y.set(getPos().getY());
            }

            List<BlockPos> blockPos = IMiner.getBlockToMinePerChunk(this, x, y, z, chuck.getPos());
            blockPos.forEach(blockPos1 -> {
                NonNullList<ItemStack> itemStacks = NonNullList.create();
                IBlockState blockState = this.getWorld().getBlockState(blockPos1);
                blockState.getBlock().getDrops(itemStacks, world, blockPos1, blockState, 0);
                if (addItemsToItemHandler(exportItems, true, itemStacks)) {
                    addItemsToItemHandler(exportItems, false, itemStacks);
                    world.destroyBlock(blockPos1, false);
                }
            });

            if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
                pushItemsIntoNearbyHandlers(getFrontFacing());
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("xPos", new NBTTagLong(x.get()));
        data.setTag("yPos", new NBTTagLong(y.get()));
        data.setTag("zPos", new NBTTagLong(z.get()));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        x.set(data.getLong("xPos"));
        y.set(data.getLong("yPos"));
        z.set(data.getLong("zPos"));
    }

    @Override
    public int getTicksPerOperation() {
        return Math.max((int) (8 / Math.pow(2, this.getTier())), 1);
    }

    @Override
    public int getChunkRange() {
        return 1;
    }

    @Override
    public int getFortune() {
        return 0;
    }

    @Override
    public int getDrillingFluidConsumedPerTick() {
        return (int) Math.pow(2, this.getTier());
    }
}
