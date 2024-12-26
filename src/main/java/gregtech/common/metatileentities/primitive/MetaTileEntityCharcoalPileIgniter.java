package gregtech.common.metatileentities.primitive;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IWorkable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.PatternError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.pattern.pattern.FactoryExpandablePattern;
import gregtech.api.pattern.pattern.IBlockPattern;
import gregtech.api.util.Mods;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.behaviors.LighterBehaviour;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.minecraft.CraftTweakerMC;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@ZenClass("mods.gregtech.machines.CharcoalPileIgniter")
@ZenRegister
public class MetaTileEntityCharcoalPileIgniter extends MultiblockControllerBase implements IWorkable {

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;
    private static final int MAX_RADIUS = 5;
    private static final int MAX_DEPTH = 5;

    private static final Set<Block> WALL_BLOCKS = new ObjectOpenHashSet<>();

    private final Collection<BlockPos> logPositions = new ObjectOpenHashSet<>();

    static {
        WALL_BLOCKS.add(Blocks.DIRT);
        WALL_BLOCKS.add(Blocks.GRASS);
        WALL_BLOCKS.add(Blocks.GRASS_PATH);
        WALL_BLOCKS.add(Blocks.SAND);
    }

    private final int[] bounds = new int[] { 0, MIN_DEPTH, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS };

    private boolean isActive;
    private int progressTime = 0;
    private int maxProgress = 0;

    /**
     * Reverse map from enum facing -> relative direction, refreshed on every setFrontFacing(...) call
     */
    private final Map<EnumFacing, RelativeDirection> facingMap = new EnumMap<>(EnumFacing.class);

    public MetaTileEntityCharcoalPileIgniter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        MinecraftForge.EVENT_BUS.register(MetaTileEntityCharcoalPileIgniter.class);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCharcoalPileIgniter(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.CHARCOAL_PILE_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isActive, true);
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        setActive(false);
        this.progressTime = 0;
        this.maxProgress = 0;
    }

    @Override
    protected void formStructure(String name) {
        super.formStructure(name);
        // this doesn't iterate over any(), so doesn't count the borders
        forEachFormed("MAIN", (info, pos) -> {
            BlockPos immutable = pos.immutable();

            if (info.getBlockState().getBlock().isWood(getWorld(), immutable)) {
                logPositions.add(immutable);
            }
        });
        // calculate the duration upon formation
        updateMaxProgressTime();
    }

    @NotNull
    @Override
    protected IBlockPattern createStructurePattern() {
        TraceabilityPredicate floorPredicate = blocks(Blocks.BRICK_BLOCK);
        TraceabilityPredicate wallPredicate = blocks("walls", WALL_BLOCKS.toArray(new Block[0]));
        TraceabilityPredicate logPredicate = logPredicate();

        // basically cleanroom code
        return FactoryExpandablePattern.start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                .boundsFunction((w, c, f, u) -> bounds)
                .predicateFunction((c, b) -> {
                    if (c.origin()) return selfPredicate();

                    int intersects = 0;

                    // aisle dir is up, so its bounds[0] and bounds[1]
                    boolean topAisle = c.x() == b[0];
                    boolean botAisle = c.x() == -b[1];

                    if (topAisle || botAisle) intersects++;
                    // negative signs for the LEFT and BACK ordinals
                    // string dir is right, so its bounds[2] and bounds[3]
                    if (c.y() == -b[2] || c.y() == b[3]) intersects++;
                    // char dir is front, so its bounds[4] and bounds[5]
                    if (c.z() == b[4] || c.z() == -b[5]) intersects++;

                    if (intersects >= 2) return any();

                    if (intersects == 1) {
                        if (botAisle) return floorPredicate;
                        return wallPredicate;
                    }

                    return logPredicate;
                })
                .build();
    }

    @NotNull
    @Override
    public Iterator<Map<String, String>> getPreviewBuilds() {
        return IntStream.range(0, WALL_BLOCKS.size())
                .mapToObj(i -> Collections.singletonMap("walls", Integer.toString(i)))
                .iterator();
    }

    @NotNull
    private TraceabilityPredicate logPredicate() {
        return new TraceabilityPredicate(
                worldState -> worldState.getBlockState().getBlock().isWood(worldState.getWorld(),
                        worldState.getPos()) ||
                        worldState.getBlockState().equals(MetaBlocks.BRITTLE_CHARCOAL.getDefaultState()) ? null :
                                PatternError.PLACEHOLDER);
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
        if (getWorld() != null) {
            if (!getWorld().isRemote && !this.isStructureFormed("MAIN") && getOffsetTimer() % 20 == 0) {
                this.reinitializeStructurePattern();
            } else if (isActive) {
                BlockPos pos = getPos();
                EnumFacing facing = EnumFacing.UP;
                float xPos = facing.getXOffset() * 0.76F + pos.getX() + 0.5F;
                float yPos = facing.getYOffset() * 0.76F + pos.getY() + 0.25F;
                float zPos = facing.getZOffset() * 0.76F + pos.getZ() + 0.5F;
                float ySpd = facing.getYOffset() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();

                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xPos, yPos, zPos, 0, ySpd, 0);
            }
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
        logPositions.clear();
    }

    protected void updateFacingMap() {
        // cache relative front, back, left, right
        for (int i = 2; i < 6; i++) {
            EnumFacing abs = RelativeDirection.VALUES[i].getRelativeFacing(frontFacing, upwardsFacing, false);
            facingMap.put(abs, RelativeDirection.VALUES[i]);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getWorld().isRemote) return true;

            RelativeDirection dir = facingMap.getOrDefault(facing, RelativeDirection.DOWN);
            bounds[dir.ordinal()] += 1;
            if (bounds[dir.ordinal()] > (dir == RelativeDirection.DOWN ? MAX_DEPTH : MAX_RADIUS)) {
                bounds[dir.ordinal()] = (dir == RelativeDirection.DOWN ? MIN_DEPTH : MIN_RADIUS);
            }

            playerIn.sendMessage(
                    new TextComponentTranslation("gregtech.direction." + facing.name().toLowerCase(Locale.ROOT))
                            .appendText(" ")
                            .appendSibling(new TextComponentTranslation("gregtech.machine.miner.radius",
                                    bounds[dir.ordinal()])));
            getSubstructure("MAIN").clearCache();
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void setFrontFacing(EnumFacing facing) {
        super.setFrontFacing(facing);
        updateFacingMap();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.2"));
        if (TooltipHelper.isCtrlDown()) {
            tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.3"));
            tooltip.add(I18n.format("gregtech.machine.charcoal_pile.tooltip.4"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.hold_ctrl"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("progressTime", this.progressTime);
        data.setInteger("maxProgress", this.maxProgress);
        data.setBoolean("isActive", this.isActive);
        data.setIntArray("bounds", this.bounds);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.progressTime = data.getInteger("progressTime");
        this.maxProgress = data.getInteger("maxProgress");
        this.isActive = data.getBoolean("isActive");
        if (data.hasKey("bounds")) System.arraycopy(data.getIntArray("bounds"), 0, bounds, 0, 6);
        updateFacingMap();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.progressTime);
        buf.writeInt(this.maxProgress);
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.progressTime = buf.readInt();
        this.maxProgress = buf.readInt();
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {} else
            if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
                this.isActive = buf.readBoolean();
                scheduleRenderUpdate();
            }
    }

    /**
     * Add a block to the valid Charcoal Pile valid wall/roof blocks
     *
     * @param block the block to add
     */
    @SuppressWarnings("unused")
    public static void addWallBlock(@NotNull Block block) {
        WALL_BLOCKS.add(block);
    }

    @ZenMethod("addWallBlock")
    @Optional.Method(modid = Mods.Names.CRAFT_TWEAKER)
    @SuppressWarnings("unused")
    public static void addWallBlockCT(@NotNull IBlock block) {
        WALL_BLOCKS.add(CraftTweakerMC.getBlock(block));
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {}

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
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        }

        return super.getCapability(capability, side);
    }

    @SubscribeEvent
    public static void onItemUse(@NotNull PlayerInteractEvent.RightClickBlock event) {
        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        MetaTileEntity mte = null;
        if (tileEntity instanceof IGregTechTileEntity) {
            mte = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
        }
        if (mte instanceof MetaTileEntityCharcoalPileIgniter &&
                ((IMultiblockController) mte).isStructureFormed("MAIN")) {
            if (event.getSide().isClient()) {
                event.setCanceled(true);
                event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);
            } else if (!mte.isActive()) {
                boolean shouldActivate = false;
                ItemStack stack = event.getItemStack();
                if (stack.getItem() instanceof ItemFlintAndSteel) {
                    // flint and steel
                    stack.damageItem(1, event.getEntityPlayer());

                    // flint and steel sound does not get played when handled like this
                    event.getWorld().playSound(null, event.getPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                            SoundCategory.PLAYERS, 1.0F, 1.0F);

                    shouldActivate = true;
                } else if (stack.getItem() instanceof ItemFireball) {
                    // fire charge
                    stack.shrink(1);

                    // fire charge sound does not get played when handled like this
                    event.getWorld().playSound(null, event.getPos(), SoundEvents.ITEM_FIRECHARGE_USE,
                            SoundCategory.PLAYERS, 1.0F, 1.0F);

                    shouldActivate = true;
                } else if (stack.getItem() instanceof MetaItem) {
                    // lighters
                    MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
                    if (valueItem != null) {
                        for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
                            if (behaviour instanceof LighterBehaviour &&
                                    ((LighterBehaviour) behaviour).consumeFuel(event.getEntityPlayer(), stack)) {
                                // lighter sound does not get played when handled like this
                                event.getWorld().playSound(null, event.getPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                                        SoundCategory.PLAYERS, 1.0F, 1.0F);

                                shouldActivate = true;
                                break;
                            }
                        }
                    }
                }

                if (shouldActivate) {
                    ((MetaTileEntityCharcoalPileIgniter) mte).setActive(true);
                    event.setCancellationResult(EnumActionResult.FAIL);
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public boolean allowsFlip() {
        return false;
    }
}
