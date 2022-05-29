package gregtech.common.metatileentities.primitive;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.*;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MetaTileEntityCharcoalPileIgniter extends MultiblockControllerBase implements IWorkable {

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;

    private static final Set<Block> WALL_BLOCKS = new ObjectOpenHashSet<>();

    private final Set<BlockPos> logPositions = new ObjectOpenHashSet<>();

    static {
        WALL_BLOCKS.add(Blocks.DIRT);
        WALL_BLOCKS.add(Blocks.GRASS);
        WALL_BLOCKS.add(Blocks.GRASS_PATH);
        WALL_BLOCKS.add(Blocks.SAND);
    }

    private int lDist = 0;
    private int rDist = 0;
    private int hDist = 0;

    private boolean isActive;
    private int progressTime = 0;
    private int maxProgress = 0;

    public MetaTileEntityCharcoalPileIgniter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCharcoalPileIgniter(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.CHARCOAL_PILE_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive, true);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        setActive(false);
        this.progressTime = 0;
        this.maxProgress = 0;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        // start the machine on formation
        updateMaxProgressTime();
        setActive(true);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        // update the structure's dimensions just before we create it
        if (getWorld() != null) updateStructureDimensions();

        // sometimes these can get reset
        if (lDist < 1) lDist = MIN_RADIUS;
        if (rDist < 1) rDist = MIN_RADIUS;
        if (hDist < 2) hDist = MIN_DEPTH;

        StringBuilder wallBuilder = new StringBuilder();       // " XXX "
        StringBuilder floorBuilder = new StringBuilder();      // " BBB "
        StringBuilder cornerBuilder = new StringBuilder();     // "     "
        StringBuilder ctrlBuilder = new StringBuilder();       // " XSX "
        StringBuilder woodBuilder = new StringBuilder();       // "XCCCX"

        // everything to the left of the controller
        wallBuilder.append(" ");
        floorBuilder.append(" ");
        ctrlBuilder.append(" ");
        woodBuilder.append("X");

        for (int i = 0; i < lDist; i++) {
            cornerBuilder.append(" ");
            if (i > 0) {
                wallBuilder.append("X");
                floorBuilder.append("B");
                ctrlBuilder.append("X");
                woodBuilder.append("C");
            }
        }

        // everything in-line with the controller
        wallBuilder.append("X");
        floorBuilder.append("B");
        cornerBuilder.append(" ");
        ctrlBuilder.append("S");
        woodBuilder.append("C");

        // everything to the right of the controller
        for (int i = 0; i < rDist; i++) {
            cornerBuilder.append(" ");
            if (i < rDist - 1) {
                wallBuilder.append("X");
                floorBuilder.append("B");
                ctrlBuilder.append("X");
                woodBuilder.append("C");
            }
        }

        wallBuilder.append(" ");
        floorBuilder.append(" ");
        ctrlBuilder.append(" ");
        woodBuilder.append("X");

        String[] wall = new String[hDist + 1]; // "     ", " XXX ", "     "
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = cornerBuilder.toString();
        wall[wall.length - 1] = cornerBuilder.toString();

        String[] slice = new String[hDist + 1]; // " BBB ", "XCCCX", " XXX "
        Arrays.fill(slice, woodBuilder.toString());
        slice[0] = floorBuilder.toString();

        String[] center = Arrays.copyOf(slice, slice.length); // " BBB ", "XCCCX", " XSX "
        center[center.length - 1] = ctrlBuilder.toString();

        // slice is finished after center, so we can re-use it a bit more
        slice[slice.length - 1] = wallBuilder.toString();

        return FactoryBlockPattern.start()
                .aisle(wall)
                .aisle(slice).setRepeatable(0, 4)
                .aisle(center)
                .aisle(slice).setRepeatable(0, 4)
                .aisle(wall)
                .where('S', selfPredicate())
                .where('B', blocks(Blocks.BRICK_BLOCK))
                .where('X', blocks(WALL_BLOCKS.toArray(new Block[0])))
                .where('C', logPredicate())
                .where(' ', any())
                .build();
    }

    @Nonnull
    private TraceabilityPredicate logPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState().getBlock().isWood(blockWorldState.getWorld(), blockWorldState.getPos())) {
                // store the position of every log, so we can easily turn them into charcoal
                logPositions.add(blockWorldState.getPos());
                return true;
            }
            return false;
        });
    }

    private void updateStructureDimensions() {
        World world = getWorld();
        EnumFacing left = getFrontFacing().getOpposite().rotateYCCW();
        EnumFacing right = left.getOpposite();

        // l, r move down 1 block because the top layer has no bricks
        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos()).move(EnumFacing.DOWN);
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos()).move(EnumFacing.DOWN);
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the brick blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int hDist = 0;

        // find the left, right, height distances for the structure pattern
        // maximum size is 11x11x6 including walls, so check 5 block radius around the controller for blocks
        for (int i = 1; i < 6; i++) {
            if (lDist != 0 && rDist != 0 && hDist != 0) break;
            if (lDist == 0 && isBlockWall(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockWall(world, rPos, right)) rDist = i;
            if (hDist == 0 && isBlockFloor(world, hPos)) hDist = i;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            invalidateStructure();
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.hDist = hDist;
    }

    private static boolean isBlockWall(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction) {
        return WALL_BLOCKS.contains(world.getBlockState(pos.move(direction)).getBlock());
    }

    private static boolean isBlockFloor(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos) {
        return world.getBlockState(pos.move(EnumFacing.DOWN)).getBlock() == Blocks.BRICK_BLOCK;
    }

    @Override
    public boolean onRightClick(@Nonnull EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        // update the structure on right click, since finding edges happens to late after initial placement
        if (!getWorld().isRemote && !playerIn.isSneaking() && !isStructureFormed()) reinitializeStructurePattern();
        return false;
    }

    private void setActive(boolean active) {
        this.isActive = active;
        writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(this.isActive));
    }

    private void updateMaxProgressTime() {
        this.maxProgress = Math.max(1, (int) Math.sqrt(logPositions.size() * 240_000));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld() != null && getWorld().isRemote && isActive) {
            BlockPos pos = getPos();
            EnumFacing facing = EnumFacing.UP;
            float xPos = facing.getXOffset() * 0.76F + pos.getX() + 0.5F;
            float yPos = facing.getYOffset() * 0.76F + pos.getY() + 0.25F;
            float zPos = facing.getZOffset() * 0.76F + pos.getZ() + 0.5F;
            float ySpd = facing.getYOffset() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();

            getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, 0, ySpd, 0);
        }
    }

    @Override
    protected void updateFormedValid() {
        if (isActive && maxProgress > 0) {
            if (++progressTime == maxProgress) {
                progressTime = 0;
                maxProgress = 0;
                convertLogBlocks();
                setActive(false);
            }
        }
    }

    private void convertLogBlocks() {
        World world = getWorld();
        for (BlockPos pos : logPositions) {
            world.setBlockState(pos, MetaBlocks.BRITTLE_CHARCOAL.getDefaultState());
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.2"));
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.3"));
            tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.4"));
            tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.5"));
            tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.6"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.hold_shift"));
        }
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder()
                .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DSD ")
                .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                .where('S', MetaTileEntities.CHARCOAL_PILE_IGNITER, EnumFacing.NORTH)
                .where('B', Blocks.BRICK_BLOCK.getDefaultState())
                .where('X', Blocks.DIRT.getDefaultState())
                .where('D', Blocks.GRASS.getDefaultState())
                .where('C', Blocks.LOG.getDefaultState())
                .build());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", this.lDist);
        data.setInteger("rDist", this.rDist);
        data.setInteger("hDist", this.hDist);
        data.setInteger("progressTime", this.progressTime);
        data.setInteger("maxProgress", this.maxProgress);
        data.setBoolean("isActive", this.isActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.lDist = data.getInteger("lDist");
        this.rDist = data.getInteger("rDist");
        this.hDist = data.getInteger("hDist");
        this.progressTime = data.getInteger("progressTime");
        this.maxProgress = data.getInteger("maxProgress");
        this.isActive = data.getBoolean("isActive");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.lDist);
        buf.writeInt(this.rDist);
        buf.writeInt(this.hDist);
        buf.writeInt(this.progressTime);
        buf.writeInt(this.maxProgress);
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.hDist = buf.readInt();
        this.progressTime = buf.readInt();
        this.maxProgress = buf.readInt();
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    /**
     * Add a block to the valid Charcoal Pile valid wall/roof blocks
     * @param block the block to add
     */
    @SuppressWarnings("unused")
    public static void addWallBlock(@Nonnull Block block) {
        WALL_BLOCKS.add(block);
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {

    }

    @Override
    public int getProgress() {
        return progressTime;
    }

    @Override
    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        }

        return super.getCapability(capability, side);
    }
}
