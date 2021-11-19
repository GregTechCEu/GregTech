
package gregtech.common.metatileentities.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IMiner;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.SimpleSidedCubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SteamMiner extends MetaTileEntity implements IMiner, IControllable {

    private final int inventorySize;
    private final ItemStackHandler fluidContainerInventory;
    private boolean needsVenting;
    private boolean ventingStuck;
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
    private int pipeY = 0;
    private final int tick;
    private final int steam;
    private final int fortune;
    private boolean isActive = true;

    public SteamMiner(ResourceLocation metaTileEntityId, int tick, int radius, int steam, int fortune) {
        super(metaTileEntityId);
        this.inventorySize = 4;
        this.fluidContainerInventory = new ItemStackHandler(1);
        this.needsVenting = false;
        this.tick = tick;
        this.currentRadius = radius;
        this.maximumRadius = radius;
        this.steam = steam;
        this.fortune = fortune;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamMiner(metaTileEntityId, getTick(), getCurrentRadius(), getSteam(), getFortune());
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000)
                .setFillPredicate(ModHandler::isSteam));
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
        ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline, multiplier);
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, coloredPipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.STEAM_MINER_OVERLAY.renderSided(renderSide, renderState, translation, coloredPipeline);
        }
        Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < pipeY; i++) {
            translation.translate(0.0, -1.0, 0.0);
            Textures.STEAM_CASING_BRONZE.render(renderState, translation, pipeline, PIPE_CUBOID);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BRONZE_BACKGROUND, 175, 180);
        builder.bindPlayerInventory(entityPlayer.inventory, 94);

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.BRONZE_SLOT));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.BRONZE_SLOT, 10);

        builder.image(7, 16, 105, 75, GuiTextures.BRONZE_DISPLAY)
                .label(10, 5, getMetaFullName());
        builder.widget(new AdvancedTextWidget(10, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new AdvancedTextWidget(70, 19, this::addDisplayText2, 0xFFFFFF)
                .setMaxWidthLimit(84));


        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.steam_miner.description", getWorkingArea(), getWorkingArea(), getTick() / 20));
    }

    public boolean drainEnergy(boolean simulate) {
        int resultSteam = importFluids.getTankAt(0).getFluidAmount() - steam;
        if (!ventingStuck && resultSteam >= 0L && resultSteam <= importFluids.getTankAt(0).getCapacity()) {
            if (!simulate)
                importFluids.getTankAt(0).drain(steam, true);
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
                if (needsVenting) {
                    tryDoVenting();
                    if (ventingStuck)
                        return;
                }
                return;
            }

            if (!invFull)
                drainEnergy(false);

            WorldServer world = (WorldServer) this.getWorld();
            if (mineY.get() < tempY.get()) {
                world.destroyBlock(new BlockPos(getPos().getX(), tempY.get(), getPos().getZ()), false);
                tempY.decrementAndGet();
                this.pipeY++;
                writeCustomData(-200, b -> b.writeInt(pipeY));
                markDirty();
            }

            if(blockPos.isEmpty()) {
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, currentRadius, IMiner.getMeanTickTime(world)));
            }

            if (getOffsetTimer() % tick == 0 && !blockPos.isEmpty()) {
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
                    blockState.getBlock().getDrops(itemStacks, world, tempPos, blockState, 0);
                    if (addItemsToItemHandler(exportItems, true, itemStacks)) {
                        addItemsToItemHandler(exportItems, false, itemStacks);
                        world.setBlockState(tempPos, Blocks.COBBLESTONE.getDefaultState());
                        mineX.set(tempPos.getX());
                        mineZ.set(tempPos.getZ());
                        mineY.set(tempPos.getY());
                        setNeedsVenting(true);
                        blockPos.removeFirst();
                    } else {
                        invFull = true;
                    }
                } else
                    blockPos.removeFirst();
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
        data.setTag("pipeY", new NBTTagInt(pipeY));
        data.setTag("radius", new NBTTagInt(currentRadius));
        data.setTag("done", new NBTTagInt(done ? 1 : 0));
        data.setTag("fluidContainer", fluidContainerInventory.serializeNBT());
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
        pipeY = data.getInteger("pipeY");
        currentRadius = data.getInteger("radius");
        done = data.getInteger("done") != 0;
        fluidContainerInventory.deserializeNBT(data.getCompoundTag("fluidContainer"));
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
        else if (ventingStuck)
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.vent").setStyle(new Style().setColor(TextFormatting.RED)));
        if (!drainEnergy(true))
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.steam").setStyle(new Style().setColor(TextFormatting.RED)));
    }

    void addDisplayText2(List<ITextComponent> textList) {
        textList.add(new TextComponentString(String.format("mX: %d", mineX.get())));
        textList.add(new TextComponentString(String.format("mY: %d", mineY.get())));
        textList.add(new TextComponentString(String.format("mZ: %d", mineZ.get())));
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
            writeCustomData(1, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(pipeY);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.pipeY = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == -200) {
            this.pipeY = buf.readInt();
        }
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP), getPaintingColor());
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(4, (buf) -> buf.writeBoolean(ventingStuck));
        }
    }

    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && this.ventingStuck) {
            this.setVentingStuck(false);
        }

        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(2, (buf) -> buf.writeBoolean(needsVenting));
        }
    }

    protected void tryDoVenting() {
        BlockPos machinePos = this.getPos();
        EnumFacing ventingSide = EnumFacing.UP;
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = this.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(this.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(ventingBlockPos), EntitySelectors.CAN_AI_TARGET).forEach((entity) -> entity.attackEntityFrom(DamageSources.getHeatDamage(), 6.0F));
            WorldServer world = (WorldServer) this.getWorld();
            double posX = (double) machinePos.getX() + 0.5D + (double) ventingSide.getXOffset() * 0.6D;
            double posY = (double) machinePos.getY() + 0.5D + (double) ventingSide.getYOffset() * 0.6D;
            double posZ = (double) machinePos.getZ() + 0.5D + (double) ventingSide.getZOffset() * 0.6D;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 7 + world.rand.nextInt(3), (double) ventingSide.getXOffset() / 2.0D, (double) ventingSide.getYOffset() / 2.0D, (double) ventingSide.getZOffset() / 2.0D, 0.1D);
            world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.setNeedsVenting(false);
        } else if (!this.ventingStuck) {
            this.setVentingStuck(true);
        }
    }

    void resetInv() {
        if (invFull && getOffsetTimer() % 20 == 0) {
            pushItemsIntoNearbyHandlers(getFrontFacing());
            NonNullList<ItemStack> testSpace = NonNullList.create();
            testSpace.add(new ItemStack(Blocks.STONE));
            if (addItemsToItemHandler(exportItems, true, testSpace)) {
                invFull = false;
            }
        }
    }

    public boolean testForMax() {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
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

    public int getTick() {
        return this.tick;
    }

    public int getMaximumRadius() {
        return this.maximumRadius;
    }

    private int getWorkingArea() {
        return this.maximumRadius * 2 + 1;
    }

    public int getCurrentRadius() {
        return this.currentRadius;
    }

    public int getSteam() {
        return this.steam;
    }

    public int getFortune() {
        return this.fortune;
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
