package gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.*;
import gregtech.api.render.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MetaTileEntityMiner extends TieredMetaTileEntity implements IMiner, IControllable {

    private final int inventorySize;
    private final long energyPerTick;
    private final AtomicInteger x = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger y = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger z = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger tempY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineY = new AtomicInteger(Integer.MAX_VALUE);
    private boolean done = false;
    private boolean invFull = false;
    private static final Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    private final LinkedList<BlockPos> blockPos = new LinkedList<>();
    private int currentRadius;
    private final int maximumRadius;
    private int pipeLength = 0;
    private final int tick;
    private boolean isActive = true;
    private final int fortune;

    public MetaTileEntityMiner(ResourceLocation metaTileEntityId, int tier, int tick, int radius, int fortune) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier - 1];
        this.maximumRadius = radius;
        this.currentRadius = radius;
        this.tick = tick;
        this.fortune = fortune;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMiner(metaTileEntityId, getTier(), getTick(), getMaximumRadius(), getFortune());
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if(isActive)
            Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        else
            Textures.BLANK_SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.CHUNK_MINER_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
        }
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline, PIPE_CUBOID);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        ModularUI.Builder builder;

        if (getTier() == 3) {
            builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 195, 176);
            builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 16, 94);

            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(new SlotWidget(exportItems, index, 151 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT));
                }
            }

        } else {
            builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 195, 176);
            builder.bindPlayerInventory(entityPlayer.inventory, 94);

            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT));
                }
            }
        }

        builder.image(7, 16, 105, 75, GuiTextures.DISPLAY)
                .label(10, 5, getMetaFullName());
        builder.widget(new AdvancedTextWidget(10, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new AdvancedTextWidget(70, 19, this::addDisplayText2, 0xFFFFFF)
                .setMaxWidthLimit(84));


        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VN[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.miner.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.miner.usage", getWorkingArea(), getWorkingArea(), getTick() / 20, getEnergyPerTick()));
    }

    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyPerTick);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (!isActive)
                return;

            if (done || testForMax() || !drainEnergy(true)) {
                if (!done && testForMax())
                    initPos();
                resetInv();
                return;
            }

            if (!invFull)
                drainEnergy(false);

            WorldServer world = (WorldServer) this.getWorld();
            if (mineY.get() < tempY.get()) {
                world.destroyBlock(new BlockPos(getPos().getX(), tempY.get(), getPos().getZ()), false);
                tempY.decrementAndGet();
                this.pipeLength++;
                writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeInt(pipeLength));
                markDirty();
            }

            if(blockPos.isEmpty())
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, currentRadius, IMiner.getMeanTickTime(world)));

            if (getOffsetTimer() % this.tick == 0 && !blockPos.isEmpty()) {
                BlockPos tempPos = blockPos.getFirst();
                NonNullList<ItemStack> itemStacks = NonNullList.create();
                IBlockState blockState = this.getWorld().getBlockState(tempPos);
                if (blockState != Blocks.AIR.getDefaultState()) {
                    /*small ores
                        if orePrefix of block in blockPos is small
                            applyTieredHammerNoRandomDrops...
                        else
                            current code...
                    */
                    blockState.getBlock().getDrops(itemStacks, world, tempPos, blockState, this.fortune);
                    if (addItemsToItemHandler(exportItems, true, itemStacks)) {
                        addItemsToItemHandler(exportItems, false, itemStacks);
                        world.setBlockState(tempPos, Blocks.COBBLESTONE.getDefaultState());
                        mineX.set(tempPos.getX());
                        mineZ.set(tempPos.getZ());
                        mineY.set(tempPos.getY());
                        blockPos.removeFirst();

                        if (invFull)
                            invFull = false;
                    } else {
                        invFull = true;
                    }
                } else {
                    blockPos.removeFirst();
                }
            } else if (blockPos.isEmpty()) {
                x.set(mineX.get());
                y.set(mineY.get());
                z.set(mineZ.get());
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, currentRadius, IMiner.getMeanTickTime(world)));
                if (blockPos.isEmpty()) {
                    done = true;
                }
            }

            if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
                pushItemsIntoNearbyHandlers(getFrontFacing());
            }

        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("xPos", new NBTTagInt(x.get()));
        data.setTag("yPos", new NBTTagInt(y.get()));
        data.setTag("zPos", new NBTTagInt(z.get()));
        data.setTag("mxPos", new NBTTagInt(mineX.get()));
        data.setTag("myPos", new NBTTagInt(mineY.get()));
        data.setTag("mzPos", new NBTTagInt(mineZ.get()));
        data.setTag("sxPos", new NBTTagInt(startX.get()));
        data.setTag("syPos", new NBTTagInt(startY.get()));
        data.setTag("szPos", new NBTTagInt(startZ.get()));
        data.setTag("tempY", new NBTTagInt(tempY.get()));
        data.setTag("pipeLength", new NBTTagInt(pipeLength));
        data.setTag("isActive", new NBTTagInt(isActive ? 1 : 0));
        data.setTag("radius", new NBTTagInt(currentRadius));
        data.setTag("done", new NBTTagInt(done ? 1 : 0));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        x.set(data.getInteger("xPos"));
        y.set(data.getInteger("yPos"));
        z.set(data.getInteger("zPos"));
        mineX.set(data.getInteger("mxPos"));
        mineY.set(data.getInteger("myPos"));
        mineZ.set(data.getInteger("mzPos"));
        startX.set(data.getInteger("sxPos"));
        startY.set(data.getInteger("syPos"));
        startZ.set(data.getInteger("szPos"));
        tempY.set(data.getInteger("tempY"));
        pipeLength = data.getInteger("pipeLength");
        currentRadius = data.getInteger("radius");
        isActive = data.getInteger("isActive") != 0;
        done = data.getInteger("done") != 0;
    }

    void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString(String.format("sX: %d", x.get())));
        textList.add(new TextComponentString(String.format("sY: %d", y.get())));
        textList.add(new TextComponentString(String.format("sZ: %d", z.get())));
        textList.add(new TextComponentString(String.format("Radius: %d", currentRadius)));
        if (done)
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.done").setStyle(new Style().setColor(TextFormatting.GREEN)));
        else if (isActive)
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.working").setStyle(new Style().setColor(TextFormatting.GOLD)));
        else
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
        if (invFull)
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.invfull").setStyle(new Style().setColor(TextFormatting.RED)));
        if (!drainEnergy(true))
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.needspower").setStyle(new Style().setColor(TextFormatting.RED)));
    }

    void addDisplayText2(List<ITextComponent> textList) {
        textList.add(new TextComponentString(String.format("mX: %d", mineX.get())));
        textList.add(new TextComponentString(String.format("mY: %d", mineY.get())));
        textList.add(new TextComponentString(String.format("mZ: %d", mineZ.get())));
    }

    public void initPos() {
        x.set(getPos().getX() - currentRadius);
        z.set(getPos().getZ() - currentRadius);
        y.set(getPos().getY() - 1);
        startX.set(getPos().getX() - currentRadius);
        startZ.set(getPos().getZ() - currentRadius);
        startY.set(getPos().getY());
        tempY.set(getPos().getY() - 1);
        mineX.set(getPos().getX() - currentRadius);
        mineZ.set(getPos().getZ() - currentRadius);
        mineY.set(getPos().getY() - 1);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!isActive) {
            if (currentRadius == 1)
                currentRadius = getMaximumRadius();
            else if (playerIn.isSneaking())
                currentRadius = Math.max(1, Math.round(currentRadius / 2.0f));
            else
                currentRadius = Math.max(1, currentRadius - 1);

            if (!getWorld().isRemote)
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, currentRadius, IMiner.getMeanTickTime(getWorld())));

            if (getWorld().isRemote)
                playerIn.sendMessage(new TextComponentTranslation("gregtech.multiblock.large_miner.radius", currentRadius));
        } else {
            playerIn.sendMessage(new TextComponentTranslation("gregtech.multiblock.large_miner.errorradius"));
        }
        return true;
    }


    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(pipeLength);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.pipeLength = buf.readInt();
        this.isActive = buf.readBoolean();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking() && this.openGUIOnRightClick()) {
            if (this.getWorld() != null && !this.getWorld().isRemote) {
                MetaTileEntityUIFactory.INSTANCE.openUI(this.getHolder(), (EntityPlayerMP) playerIn);
            }
            return true;
        } else {
            return false;
        }
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.IS_WORKING, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.PUMP_HEAD_LEVEL) {
            this.pipeLength = buf.readInt();
            getHolder().scheduleChunkForRenderUpdate();
        }
        if (dataId == GregtechDataCodes.IS_WORKING) {
            this.isActive = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    void resetInv() {
        if (notifiedItemOutputList.size() > 0) {
            invFull = false;
            notifiedItemOutputList.clear();
        }
    }

    public boolean testForMax() {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
    }

    public int getTick() {
        return this.tick;
    }

    public int getMaximumRadius() {
        return this.maximumRadius;
    }

    public int getWorkingArea() {
        return this.maximumRadius * 2 + 1;
    }

    public int getCurrentRadius() {
        return this.currentRadius;
    }

    public int getFortune() {
        return this.fortune;
    }

    public long getEnergyPerTick() {
        return this.energyPerTick;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isActive;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        setActive(isActivationAllowed);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
}
