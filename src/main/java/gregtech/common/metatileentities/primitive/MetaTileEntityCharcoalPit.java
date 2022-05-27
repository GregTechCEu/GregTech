package gregtech.common.metatileentities.primitive;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IWorkable;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.behaviors.LighterBehaviour;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MetaTileEntityCharcoalPit extends MultiblockControllerBase implements IWorkable {

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;

    private static final Set<Block> ROOF_BLOCKS = new ObjectOpenHashSet<>();

    static {
        ROOF_BLOCKS.add(Blocks.DIRT);
        ROOF_BLOCKS.add(Blocks.GRASS);
        ROOF_BLOCKS.add(Blocks.GRASS_PATH);
        ROOF_BLOCKS.add(Blocks.SAND);
        ROOF_BLOCKS.add(Blocks.GRAVEL);
    }

    private int lDist = 0;
    private int rDist = 0;
    private int hDist = 0;

    private boolean isActive = false;
    private int progressTime = 0;

    public MetaTileEntityCharcoalPit(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCharcoalPit(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROCK_BREAKER_OVERLAY.renderOrientedState(renderState, translation, pipeline, EnumFacing.UP, isActive, true);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        setActive(false);
        this.progressTime = 0;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        // sometimes these can get reset
        if (lDist < 1) lDist = MIN_RADIUS;
        if (rDist < 1) rDist = MIN_RADIUS;
        if (hDist < 2) hDist = MIN_DEPTH;

        StringBuilder wallBuilder = new StringBuilder();       // " XXX "
        StringBuilder cornerBuilder = new StringBuilder();     // "     "
        StringBuilder roofBuilder = new StringBuilder();       // " DDD "
        StringBuilder ctrlBuilder = new StringBuilder();       // " DSD "
        StringBuilder woodBuilder = new StringBuilder();       // "XCCCX"

        wallBuilder.append(" ");
        roofBuilder.append(" ");
        ctrlBuilder.append(" ");
        woodBuilder.append("X");

        for (int i = 0; i < lDist; i++) {
            cornerBuilder.append(" ");
            if (i > 0) {
                wallBuilder.append("X");
                roofBuilder.append("D");
                ctrlBuilder.append("D");
                woodBuilder.append("C");
            }
        }

        wallBuilder.append("X");
        cornerBuilder.append(" ");
        roofBuilder.append("D");
        ctrlBuilder.append("S");
        woodBuilder.append("C");

        for (int i = 0; i < rDist; i++) {
            cornerBuilder.append(" ");
            if (i < rDist - 1) {
                wallBuilder.append("X");
                roofBuilder.append("D");
                ctrlBuilder.append("D");
                woodBuilder.append("C");
            }
        }

        wallBuilder.append(" ");
        roofBuilder.append(" ");
        ctrlBuilder.append(" ");
        woodBuilder.append("X");

        String[] wall = new String[hDist + 1]; // "     ", " XXX ", "     "
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = cornerBuilder.toString();
        wall[wall.length - 1] = cornerBuilder.toString();

        String[] slice = new String[hDist + 1]; // " XXX ", "XCCCX", " DDD "
        Arrays.fill(slice, woodBuilder.toString());
        slice[0] = wallBuilder.toString();

        String[] center = Arrays.copyOf(slice, slice.length); // " XXX ", "XCCCX", " DSD "
        center[center.length - 1] = ctrlBuilder.toString();

        slice[slice.length - 1] = roofBuilder.toString();

        return FactoryBlockPattern.start()
                .aisle(wall)
                .aisle(slice).setRepeatable(0, 4)
                .aisle(center)
                .aisle(slice).setRepeatable(0, 4)
                .aisle(wall)
                .where('S', selfPredicate())
                .where('X', blocks(Blocks.BRICK_BLOCK))
                .where('D', blocks(ROOF_BLOCKS.toArray(new Block[0])))
                .where('C', logPredicate())
                .where(' ', any())
                .build();
    }

    @Nonnull
    private static TraceabilityPredicate logPredicate() {
        return new TraceabilityPredicate(blockWorldState -> blockWorldState.getBlockState().getBlock().isWood(blockWorldState.getWorld(), blockWorldState.getPos()));
    }

    @Override
    public boolean onRightClick(@Nonnull EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            if (isStructureFormed()) {
                ItemStack stack = playerIn.getHeldItem(hand);
                // see if the item is able to ignite blocks
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof MetaItem) {
                        // handle lighters, matches, etc
                        for (IItemBehaviour behaviour : ((MetaItem<?>) stack.getItem()).getBehaviours(stack)) {
                            if (behaviour instanceof LighterBehaviour) {
                                if (!((LighterBehaviour) behaviour).consumeFuel(playerIn, stack)) return false;
                            }
                        }
                    } else if (stack.getItem() instanceof ItemFlintAndSteel || stack.getItem() instanceof ItemFireball) {
                        // handle flint and steel items or fire charge items
                        if (stack.getItem().isDamageable()) stack.damageItem(1, playerIn);
                        else stack.setCount(Math.max(0, stack.getCount() - 1));
                    } else {
                        // cannot ignite things to our knowledge, so do nothing charcoal pit related
                        return super.onRightClick(playerIn, hand, facing, hitResult);
                    }
                    // successfully ignited the charcoal pit, so start running
                    calculateTime();
                    setActive(true);
                }
            } else {
                updateStructureDimensions();
                reinitializeStructurePattern();
                checkStructurePattern();
            }
            playerIn.swingArm(hand);
        }

        return true;
    }

    private void updateStructureDimensions() {
        World world = getWorld();
        EnumFacing right = getFrontFacing().rotateY();
        EnumFacing left = getFrontFacing().rotateYCCW();

        // l and r move down 1 block because the top layer has no bricks
        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos()).move(EnumFacing.DOWN);
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos()).move(EnumFacing.DOWN);
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the brick blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int hDist = 0;

        // find the left, right, height distances for the structure pattern
        // maximum size is 11x11x6, so check 5 block radius around the controller for blocks
        for (int i = 1; i < 6; i++) {
            if (lDist != 0 && rDist != 0 && hDist != 0) break;

            lPos.move(left);
            if (lDist == 0 && world.getBlockState(lPos).getBlock() == Blocks.BRICK_BLOCK) lDist = i;

            rPos.move(right);
            if (rDist == 0 && world.getBlockState(rPos).getBlock() == Blocks.BRICK_BLOCK) rDist = i;

            hPos.move(EnumFacing.DOWN);
            if (hDist == 0 && world.getBlockState(hPos).getBlock() == Blocks.BRICK_BLOCK) hDist = i;
        }
        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || hDist < MIN_DEPTH) invalidateStructure();

        this.lDist = lDist;
        this.rDist = rDist;
        this.hDist = hDist;
    }

    private void setActive(boolean active) {
        this.isActive = active;
    }

    private void calculateTime() {
        //TODO
    }

    @Override
    protected void updateFormedValid() {
        if (isActive) {

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
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder()
                .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                .aisle(" XXX ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                .aisle(" XXX ", "XCCCX", "XCCCX", "XCCCX", " DSD ")
                .aisle(" XXX ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                .where('S', MetaTileEntities.CHARCOAL_PIT, EnumFacing.NORTH)
                .where('X', Blocks.BRICK_BLOCK.getDefaultState())
                .where('D', Blocks.GRASS.getDefaultState())
                .where('C', Blocks.LOG.getDefaultState())
                .build());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", lDist);
        data.setInteger("rDist", rDist);
        data.setInteger("hDist", hDist);
        data.setBoolean("isActive", isActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        lDist = data.getInteger("lDist");
        rDist = data.getInteger("rDist");
        hDist = data.getInteger("hDist");
        isActive = data.getBoolean("isActive");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(lDist);
        buf.writeInt(rDist);
        buf.writeInt(hDist);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        lDist = buf.readInt();
        rDist = buf.readInt();
        hDist = buf.readInt();
        isActive = buf.readBoolean();
    }

    /**
     * Add a block to the valid Charcoal Pit roof blocks
     * @param block the block to add
     */
    @SuppressWarnings("unused")
    public static void addRoofBlock(@Nonnull Block block) {
        ROOF_BLOCKS.add(block);
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) { }

    @Override
    public int getProgress() {
        return progressTime;
    }

    @Override
    public int getMaxProgress() {
        return 1; //TODO
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }
}
