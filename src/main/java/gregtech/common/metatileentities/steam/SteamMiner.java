package gregtech.common.metatileentities.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.IMiner;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.SimpleSidedCubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class SteamMiner extends MetaTileEntity implements IMiner {

    private final int inventorySize;
    private AtomicLong x = new AtomicLong(Long.MAX_VALUE), y = new AtomicLong(Long.MAX_VALUE), z = new AtomicLong(Long.MAX_VALUE);
    private final ItemStackHandler fluidContainerInventory;
    private boolean needsVenting;
    private boolean ventingStuck;

    public SteamMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.inventorySize = 4;
        this.fluidContainerInventory = new ItemStackHandler(2);
        this.needsVenting = false;
        initializeInventory();
    }

    @Override
    public int getTicksPerOperation() {
        return 8;
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
        return 1;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamMiner(metaTileEntityId);
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(true,
                new FilteredFluidHandler(16000).setFillPredicate(fluidStack -> Objects.requireNonNull(Materials.DrillingFluid.getFluid(0)).isFluidEqual(fluidStack)),
                new FilteredFluidHandler(16000).setFillPredicate(ModHandler::isSteam)
        );
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(0);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(inventorySize);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline, multiplier);
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, coloredPipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            Textures.STEAM_MINER_OVERLAY.renderSided(renderSide, renderState, translation, coloredPipeline);
        }
        Textures.PIPE_OUT_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP), getPaintingColor());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BRONZE_BACKGROUND, 176, 166);

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 143 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.BRONZE_SLOT));
            }
        }

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.BRONZE_SLOT, 0);

        builder.image(7, 16, 81, 55, GuiTextures.BRONZE_DISPLAY)
                .label(10, 5, getMetaFullName());

        TankWidget tankWidget = new TankWidget(importFluids.getTankAt(0), 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);

        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);

        builder.widget(new FluidContainerSlotWidget(fluidContainerInventory, 0, 90, 18, true)
                .setBackgroundTexture(GuiTextures.BRONZE_SLOT, GuiTextures.BRONZE_IN_SLOT_OVERLAY))

                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.BRONZE_TANK_ICON))

                .widget(new SlotWidget(fluidContainerInventory, 1, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.BRONZE_SLOT, GuiTextures.BRONZE_OUT_SLOT_OVERLAY));


        return builder.build(getHolder(), entityPlayer);
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(4, (buf) -> {
                buf.writeBoolean(ventingStuck);
            });
        }
    }

    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && this.ventingStuck) {
            this.setVentingStuck(false);
        }

        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(2, (buf) -> {
                buf.writeBoolean(needsVenting);
            });
        }
    }

    protected void tryDoVenting() {
        BlockPos machinePos = this.getPos();
        EnumFacing ventingSide = EnumFacing.UP;
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = this.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(this.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(ventingBlockPos), EntitySelectors.CAN_AI_TARGET).forEach((entity) -> {
                entity.attackEntityFrom(DamageSources.getHeatDamage(), 6.0F);
            });
            WorldServer world = (WorldServer)this.getWorld();
            double posX = (double)machinePos.getX() + 0.5D + (double)ventingSide.getXOffset() * 0.6D;
            double posY = (double)machinePos.getY() + 0.5D + (double)ventingSide.getYOffset() * 0.6D;
            double posZ = (double)machinePos.getZ() + 0.5D + (double)ventingSide.getZOffset() * 0.6D;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 7 + world.rand.nextInt(3), (double)ventingSide.getXOffset() / 2.0D, (double)ventingSide.getYOffset() / 2.0D, (double)ventingSide.getZOffset() / 2.0D, 0.1D, new int[0]);
            world.playSound((EntityPlayer)null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.setNeedsVenting(false);
        } else if (!this.ventingStuck) {
            this.setVentingStuck(true);
        }
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillInternalTankFromFluidContainer(fluidContainerInventory, fluidContainerInventory, 0, 1);

            if (needsVenting && this.getOffsetTimer() % 10L == 0L) {
                tryDoVenting();
                if (ventingStuck) {
                    return;
                }
            }

            // if sufficient amounts of steam and drilling fluid aren't present, do nothing
            if ((importFluids.getTankAt(0).getFluidAmount() < 1) || (importFluids.getTankAt(1).getFluidAmount() < 16)) {
                return;
            }

            importFluids.getTankAt(0).drain(this.getDrillingFluidConsumedPerTick(), true);
            importFluids.getTankAt(1).drain(16, true);

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
                    setNeedsVenting(true);
                }
            });
        }
    }

    @Override
    public void onRemoval() {
        for (int slot = 0; slot < fluidContainerInventory.getSlots(); slot++)
            Block.spawnAsEntity(getWorld(), getPos(), fluidContainerInventory.getStackInSlot(slot));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.miner.steam.description"));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", this.getDrillingFluidConsumedPerTick(), I18n.format(Materials.DrillingFluid.getFluid(0).getUnlocalizedName())));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("xPos", new NBTTagLong(x.get()));
        data.setTag("yPos", new NBTTagLong(y.get()));
        data.setTag("zPos", new NBTTagLong(z.get()));
        data.setTag("fluidContainer", fluidContainerInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        x.set(data.getLong("xPos"));
        y.set(data.getLong("yPos"));
        z.set(data.getLong("zPos"));
        fluidContainerInventory.deserializeNBT(data.getCompoundTag("fluidContainer"));
    }

}
