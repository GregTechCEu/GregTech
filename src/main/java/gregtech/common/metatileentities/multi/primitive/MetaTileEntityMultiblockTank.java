package gregtech.common.metatileentities.multi.primitive;

import codechicken.lib.vec.Cuboid6;

import codechicken.lib.vec.Vector3;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockSteamCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.block.BlockGlass;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Console;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityMultiblockTank extends MultiblockWithDisplayBase {

    private final int tier;
    private final int volumePerBlock;
    private final int startingVolume;

    private FilteredFluidHandler tank;
    private PropertyFluidFilter filter;
    private int volume;
    private int lDist = 0;
    private int rDist = 0;
    private int bDist = 0;
    private int hDist = 0;

    public static final int MIN_RADIUS = 1;
    public static final int MIN_HEIGHT = 1;

    @Nullable
    protected FluidStack previousFluid;

    public MetaTileEntityMultiblockTank(ResourceLocation metaTileEntityId, int startingVolume, int tier, int volumePerBlock) {
        super(metaTileEntityId);
        this.tier = tier;
        this.startingVolume = startingVolume;
        this.volumePerBlock = volumePerBlock;
        initializeInventory();
    }

    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        volume = (lDist + rDist - 1) * bDist * hDist;
        tank.setCapacity(startingVolume + volume * volumePerBlock);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();

        tank = new FilteredFluidHandler(startingVolume);

        if (tier == 0) filter = new PropertyFluidFilter(340, false, false, false, false);
        if (tier == 1) filter = new PropertyFluidFilter(1855, true, false, false, false);
        if (tier == 2) filter = new PropertyFluidFilter(1166, true, true, false, false);
        if (tier == 3) filter = new PropertyFluidFilter(2428, true, true, true, false);
        if (tier == 4) filter = new PropertyFluidFilter(2426, true, true, true, false);
        if (tier == 5) filter = new PropertyFluidFilter(3587, true, true, true, false);

        tank.setFilter(filter);

        this.exportFluids = this.importFluids = new FluidTankList(true, tank);

        this.fluidInventory = tank;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMultiblockTank(metaTileEntityId, startingVolume, tier, volumePerBlock);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank();
            fillInternalTankFromFluidContainer();

            FluidStack currentFluid = tank.getFluid();
            if (previousFluid == null) {
                // tank was empty, but now is not
                if (currentFluid != null) {
                    updatePreviousFluid(currentFluid);
                }
            } else {
                if (currentFluid == null) {
                    // tank had fluid, but now is empty
                    updatePreviousFluid(null);
                } else if (previousFluid.getFluid().equals(currentFluid.getFluid()) &&
                        previousFluid.amount != currentFluid.amount) {
                    // tank has fluid with changed amount
                    previousFluid.amount = currentFluid.amount;
                    writeCustomData(GregtechDataCodes.UPDATE_FLUID_AMOUNT, buf -> buf.writeInt(currentFluid.amount));
                } else
                if (!previousFluid.equals(currentFluid)) {
                    // tank has a different fluid from before
                    updatePreviousFluid(currentFluid);
                }
            }
        }
    }

    // should only be called on the server
    protected void updatePreviousFluid(FluidStack currentFluid) {
        previousFluid = currentFluid == null ? null : currentFluid.copy();
        writeCustomData(GregtechDataCodes.UPDATE_FLUID, buf -> buf
                .writeCompoundTag(currentFluid == null ? null : currentFluid.writeToNBT(new NBTTagCompound())));
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public boolean updateStructureDimensions() {
        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = back.rotateYCCW();
        EnumFacing right = left.getOpposite();

        //The distance of the edges is calculated from a position inside the container
        BlockPos.MutableBlockPos innerPos = new BlockPos.MutableBlockPos(getPos());
        innerPos.move(back);
        innerPos.move(EnumFacing.UP);

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(innerPos);
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(innerPos);
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(innerPos);
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(innerPos);

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int hDist = 0;

        // find the left, right, and back distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < 8; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (lDist != 0 && rDist != 0) break;
        }

        //Back and height are done separately since the controller is at the edge of those 2 axes
        for (int i = 1; i < 15; i++) {
            if (isBlockEdge(world, bPos, back)) bDist = i;
            if (bDist != 0) break;
        }

        for (int i = 1; i < 15; i++) {
            if (isBlockEdge(world, hPos, EnumFacing.UP)) hDist = i;
            if (hDist != 0) break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || hDist < MIN_HEIGHT) {
            invalidateStructure();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.hDist = hDist;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.bDist);
            buf.writeInt(this.hDist);
        });

        return true;
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                               @NotNull EnumFacing direction) {
        IBlockState blockState = world.getBlockState(pos.move(direction));
        return blockState == getCasingState() || blockState == getValve() || blockState == getGlass();
    }

    @Override
    @NotNull
    protected BlockPattern createStructurePattern() {
        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI
        if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
        if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
        if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
        if (hDist < MIN_HEIGHT) hDist = MIN_HEIGHT;

        if (this.frontFacing == EnumFacing.EAST || this.frontFacing == EnumFacing.WEST) {
            int tmp = lDist;
            lDist = rDist;
            rDist = tmp;
        }

        // build each row of the structure
        StringBuilder borderBuilder = new StringBuilder();
        StringBuilder controllerBuilder = new StringBuilder();
        StringBuilder wallBuilder = new StringBuilder();
        StringBuilder insideBuilder = new StringBuilder();
        StringBuilder roofBuilder = new StringBuilder();

        // everything to the left of the controller
        for (int i = 0; i < lDist; i++) {
            borderBuilder.append("E");
            controllerBuilder.append("E");
            if (i == 0) {
                wallBuilder.append("E");
                insideBuilder.append("W");
                roofBuilder.append("E");
            } else {
                wallBuilder.append("W");
                insideBuilder.append(" ");
                roofBuilder.append("W");
            }
        }

        // everything in-line with the controller
        borderBuilder.append("E");
        controllerBuilder.append("S");

        wallBuilder.append("W");
        insideBuilder.append(" ");
        roofBuilder.append("W");

        // everything to the right of the controller
        for (int i = 0; i < rDist; i++) {
            borderBuilder.append("E");
            controllerBuilder.append("E");
            if (i == rDist - 1) {
                wallBuilder.append("E");
                insideBuilder.append("W");
                roofBuilder.append("E");
            } else {
                wallBuilder.append("W");
                insideBuilder.append(" ");
                roofBuilder.append("W");
            }
        }

        // build each slice of the structure
        String[] frontWall = new String[hDist + 2]; // "EESEE", "EWWWE", "EWWWE", "EWWWE", "EEEEE"
        Arrays.fill(frontWall, wallBuilder.toString());
        frontWall[0] = controllerBuilder.toString();
        frontWall[frontWall.length - 1] = borderBuilder.toString();

        String[] backWall = new String[hDist + 2]; // "EEEEE", "EWWWE", "EWWWE", "EWWWE", "EEEEE"
        Arrays.fill(backWall, wallBuilder.toString());
        backWall[0] = borderBuilder.toString();
        backWall[backWall.length - 1] = borderBuilder.toString();

        String[] slice = new String[hDist + 2]; // "EEEEE", "W   W", "W   W", "W   W", "EWWWE"
        Arrays.fill(slice, insideBuilder.toString());
        slice[0] = wallBuilder.toString();
        slice[slice.length - 1] = roofBuilder.toString();

        TraceabilityPredicate wallPredicate = states(getCasingState(), getGlass()).or(metaTileEntities(getValve()));
        TraceabilityPredicate edgePredicate = states(getCasingState()).or(metaTileEntities(getValve()));

        // layer the slices one behind the next
        return FactoryBlockPattern.start()
                .aisle(backWall)
                .aisle(slice).setRepeatable(bDist)
                .aisle(frontWall)
                .where('S', selfPredicate())
                .where('W', wallPredicate)
                .where('E', edgePredicate)
                .where(' ', air())
                .build();
    }

    private IBlockState getCasingState() {
        if (tier == 0) return MetaBlocks.STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.WOOD_WALL);
        if (tier == 1) return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
        if (tier == 2) return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.MAGNALIUM_FROSTPROOF);
        if (tier == 3) return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
        if (tier == 4) return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (tier == 5) return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);

        return MetaBlocks.STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.WOOD_WALL);
    }

    private MetaTileEntity getValve() {
        if (tier == 0) return MetaTileEntities.WOODEN_TANK_VALVE;
        if (tier == 1) return MetaTileEntities.STEEL_TANK_VALVE;
        if (tier == 2) return MetaTileEntities.ALUMINIUM_TANK_VALVE;
        if (tier == 3) return MetaTileEntities.STAINLESS_STEEL_TANK_VALVE;
        if (tier == 4) return MetaTileEntities.TITANIUM_TANK_VALVE;
        if (tier == 5) return MetaTileEntities.TUNGSTENSTEEL_TANK_VALVE;

        return MetaTileEntities.WOODEN_TANK_VALVE;
    }

    private IBlockState getGlass() {
        if (tier == 0) return MetaBlocks.STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.WOOD_WALL);
        if (tier == 1) return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
        if (tier == 2) return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
        if (tier == 3) return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
        if (tier == 4) return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS);
        if (tier == 5) return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS);

        return (IBlockState) Blocks.GLASS;
    }

    @SideOnly(Side.CLIENT)
    @Override
    @NotNull
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (tier == 0) return Textures.WOOD_WALL;
        if (tier == 1) return Textures.SOLID_STEEL_CASING;
        if (tier == 2) return Textures.FROST_PROOF_CASING;
        if (tier == 3) return Textures.CLEAN_STAINLESS_STEEL_CASING;
        if (tier == 4) return Textures.STABLE_TITANIUM_CASING;
        if (tier == 5) return Textures.ROBUST_TUNGSTENSTEEL_CASING;

        return Textures.WOOD_WALL;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!isStructureFormed())
            return false;
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return isStructureFormed();
    }

    @Override
    protected ModularUI.Builder createUITemplate(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .widget(new LabelWidget(6, 6, getMetaFullName()))
                .widget(new TankWidget(importFluids.getTankAt(0), 52, 18, 72, 61)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setContainerClicking(true, true))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 0);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        getFrontOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);

        renderTankFluid(renderState, translation, pipeline, tank, getWorld(), getPos(), getFrontFacing());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_TANK_OVERLAY;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.tank.tooltip"));
        tooltip.add(I18n.format("gregtech.multiblock.tank.tooltip_1"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_per_block_storage_capacity", startingVolume, volumePerBlock));
        tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", filter.getMaxFluidTemperature()));
        if (filter.isGasProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
        else tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof"));
        if (filter.isPlasmaProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
        if (filter.isCryoProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
        if (filter.isAcidProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.bDist = buf.readInt();
            this.hDist = buf.readInt();
            this.volume = (lDist + rDist - 1) * bDist * hDist;
            this.tank.setCapacity(volume * volumePerBlock);
        } else if (dataId == GregtechDataCodes.UPDATE_FLUID) {
            try {
                this.tank.setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
            } catch (IOException ignored) {
                GTLog.logger.warn("Failed to load fluid from NBT in a multiblock tank at " + this.getPos() +
                        " on a routine fluid update");
            }
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_FLUID_AMOUNT) {
            FluidStack stack = tank.getFluid();
            if (stack != null) {
                stack.amount = Math.min(buf.readInt(), tank.getCapacity());
                scheduleRenderUpdate();
            }
        }
    }

    public void renderTankFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                       FluidTank tank, IBlockAccess world, BlockPos pos, EnumFacing frontFacing) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        if (world != null) {
            renderState.setBrightness(world, pos);
        }
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount == 0)
            return;

        Cuboid6 partialFluidBox = new Cuboid6(0.01, 0.01, 0.01, 0.98,
                0.98, 0.98);

        double fillFraction = (double) stack.amount / tank.getCapacity();

        //Gases will appear to occupy the entire multiblock
        if (tank.getFluid().getFluid().isGaseous()) {
            fillFraction = 1;
        }

        partialFluidBox.max.y = Math.min((16 * fillFraction) + 0, 15.99) / 16.0;

        //Translate fluid to correct location
        if (frontFacing == EnumFacing.NORTH) {
            translation.translate(-(rDist - 1), 1, 1);
        }
        if (frontFacing == EnumFacing.SOUTH) {
            translation.translate(-(lDist - 1), 1, -bDist);
        }
        if (frontFacing == EnumFacing.WEST) {
            translation.translate(1, 1, -(lDist - 1));
        }
        if (frontFacing == EnumFacing.EAST) {
            translation.translate(-bDist, 1, -(rDist - 1));
        }

        //"Rotate" the fluid
        if (frontFacing == EnumFacing.WEST || frontFacing == EnumFacing.EAST) {
            translation.scale(bDist, hDist, lDist + rDist - 1);
        } else {
            translation.scale(lDist + rDist - 1, hDist, bDist);
        }

        renderState.setFluidColour(stack);
        ResourceLocation fluidStill = stack.getFluid().getStill(stack);
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(fluidStill.toString());
        for (EnumFacing facing : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, pipeline, facing, partialFluidBox, fluidStillSprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
        GlStateManager.resetColor();

        renderState.reset();
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", this.lDist);
        data.setInteger("rDist", this.rDist);
        data.setInteger("bDist", this.bDist);
        data.setInteger("hDist", this.hDist);
        data.setTag("FluidInventory", tank.writeToNBT(new NBTTagCompound()));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.lDist = data.hasKey("lDist") ? data.getInteger("lDist") : this.lDist;
        this.rDist = data.hasKey("rDist") ? data.getInteger("rDist") : this.rDist;
        this.bDist = data.hasKey("bDist") ? data.getInteger("bDist") : this.bDist;
        this.hDist = data.hasKey("hDist") ? data.getInteger("hDist") : this.hDist;
        this.tank.readFromNBT(data.getCompoundTag("FluidInventory"));
        reinitializeStructurePattern();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.lDist);
        buf.writeInt(this.rDist);
        buf.writeInt(this.bDist);
        buf.writeInt(this.hDist);
        buf.writeCompoundTag(tank.getFluid() == null ? null : tank.getFluid().writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.bDist = buf.readInt();
        this.hDist = buf.readInt();
        try {
            this.tank.setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
        } catch (IOException e) {
            GTLog.logger.warn("Failed to load fluid from NBT in a multiblock tank at " + this.getPos() +
                    " on initial server/client sync");
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (isStructureFormed()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidInventory);
            } else {
                return null;
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X X", "XXX")
                .aisle("XSX", "XGX", "XXX")
                .where('X', getCasingState())
                .where('G', getGlass())
                .where('S', this, EnumFacing.SOUTH)
                .where(' ', Blocks.AIR.getDefaultState()).build());
    }
}
