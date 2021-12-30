package gregtech.common.metatileentities.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.metaitem.DefaultSubItemHandler;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.client.renderer.texture.custom.TankRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.unification.material.Material;
import gregtech.api.util.ByteBufUtils;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.WatchedFluidTank;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static gregtech.api.capability.GregtechDataCodes.*;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;
import static gregtech.api.util.GTUtility.convertOpaqueRGBA_CLtoRGB;

public class MetaTileEntityTank extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private static final int FLUID_SYNC_THROTTLE = 0;
    private final int tankSize;
    private final int maxSizeVertical;
    private final int maxSizeHorizontal;
    private final Material material;
    private boolean addedToMultiblock = false;
    private boolean needsCoversUpdate = false;
    private boolean isRemoved = false;
    private int blockedSides = 0;
    private int connectionMask = 0;

    private BlockPos controllerPos = null;
    private WeakReference<MetaTileEntityTank> controllerCache = new WeakReference<>(null);

    private final FluidTank multiblockFluidTank;
    private List<BlockPos> connectedTanks = new ArrayList<>();
    private Vec3i multiblockSize = new Vec3i(1, 1, 1);
    private boolean recomputeTankSize = false;

    private NetworkStatus networkStatus;
    private FluidStack lastSentFluidStack;
    private int timeSinceLastFluidSync = Integer.MAX_VALUE;
    private boolean needsShapeResync;
    private boolean needsFluidResync;

    public MetaTileEntityTank(ResourceLocation metaTileEntityId, Material material, int tankSize, int maxSizeVertical, int maxSizeHorizontal) {
        super(metaTileEntityId);
        this.tankSize = tankSize;
        this.material = material;
        this.maxSizeVertical = maxSizeVertical;
        this.maxSizeHorizontal = maxSizeHorizontal;
        this.multiblockFluidTank = new WatchedFluidTank(tankSize) {
            @Override
            protected void onFluidChanged(FluidStack newFluidStack, FluidStack oldFluidStack) {
                MetaTileEntityTank.this.onFluidChangedInternal();
            }
        };
        this.fluidInventory = getActualFluidTank();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.recheckBlockedSides();
    }

    @Override
    public void update() {
        this.fluidInventory = getActualFluidTank();
        super.update();
        if (!getWorld().isRemote) {
            if (!addedToMultiblock) {
                this.addedToMultiblock = true;
                checkScanMultiblock();
            }
            if (recomputeTankSize) {
                this.recomputeTankSize = false;
                recomputeTankSizeNow(true);
            }
            handleTankSyncing();
        }
        if (needsCoversUpdate) {
            this.needsCoversUpdate = false;
            recheckBlockedSides();
            if (!getWorld().isRemote) {
                checkScanMultiblock();
            }
        }
        if (getWorld().isRemote) {
            int newConnectionMask = getConnectionMaskForTank(getPos(), blockedSides);
            if (connectionMask != newConnectionMask) {
                this.connectionMask = newConnectionMask;
                getHolder().scheduleChunkForRenderUpdate();
            }
        }
    }

    private void recheckBlockedSides() {
        this.blockedSides = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = getCoverAtSide(side);
            if (coverBehavior == null) continue;
            if (coverBehavior.canPipePassThrough()) continue;
            this.blockedSides |= 1 << side.getIndex();
        }
    }

    @Override
    protected void onCoverPlacementUpdate() {
        super.onCoverPlacementUpdate();
        this.needsCoversUpdate = true;
        setTankController(null);
    }

    @Override
    public void onAttached(Object... data) {
        super.onAttached(data);
        recomputeTankSizeNow(true);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        this.isRemoved = true;
        setTankController(null);
        checkScanMultiblock();
    }

    private void checkScanMultiblock() {
        HashSet<BlockPos> visitedSet = new HashSet<>();
        for (EnumFacing side : EnumFacing.VALUES) {
            MetaTileEntityTank tank = getTankTile(getPos().offset(side));
            if (tank != null) tank.checkScanMultiblockSelf(visitedSet);
        }
    }

    private void checkScanMultiblockSelf(Set<BlockPos> visitedSet) {
        Map<BlockPos, MetaTileEntityTank> tankMap = findConnectedTanks(this, visitedSet);
        if (tankMap.isEmpty()) return;
        Comparator<BlockPos> comparator = Comparator.comparing(it -> it.getX() + it.getY() + it.getZ());
        BlockPos lowestCorner = tankMap.keySet().stream().min(comparator).get();
        BlockPos highestCorner = tankMap.keySet().stream().max(comparator).get();
        int expectedSizeX = Math.min(highestCorner.getX() - lowestCorner.getX() + 1, maxSizeHorizontal);
        int expectedSizeY = Math.min(highestCorner.getY() - lowestCorner.getY() + 1, maxSizeVertical);
        int expectedSizeZ = Math.min(highestCorner.getZ() - lowestCorner.getZ() + 1, maxSizeHorizontal);
        boolean multiblockAssembled = checkMultiblockValid(tankMap, lowestCorner, expectedSizeX, expectedSizeY, expectedSizeZ);
        MetaTileEntityTank newController = multiblockAssembled ? tankMap.get(lowestCorner) : null;
        if (newController != null) {
            Vec3i multiblockSize = new Vec3i(expectedSizeX, expectedSizeY, expectedSizeZ);
            newController.setTankController(null);
            newController.updateControllerSize(multiblockSize);
        }
        tankMap.values().forEach(it -> it.setTankController(newController));
    }

    private double getFluidLevelForTank(BlockPos tankPos) {
        if (!isTankController()) {
            MetaTileEntityTank controller = getControllerEntity();
            return controller == null ? 0.0 : controller.getFluidLevelForTank(tankPos);
        }
        FluidStack fluidStack = multiblockFluidTank.getFluid();
        if (fluidStack == null) {
            return 0.0;
        }
        double fluidLevel = multiblockFluidTank.getFluidAmount() / (1.0 * multiblockFluidTank.getCapacity());
        double resultLevel;
        if (fluidStack.getFluid().isGaseous(fluidStack)) {
            resultLevel = fluidLevel;
        } else {
            int tankOffset = (tankPos.getY() - getPos().getY());
            resultLevel = fluidLevel * multiblockSize.getY() - tankOffset;
        }
        return MathHelper.clamp(resultLevel, 0.0, 1.0);
    }

    private int getConnectionMaskForTank(BlockPos blockPos, int excludeMask) {
        if (!isTankController()) {
            MetaTileEntityTank controller = getControllerEntity();
            return controller == null ? 0 : controller.getConnectionMaskForTank(blockPos, excludeMask);
        }
        int resultConnectionMask = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            if ((excludeMask & 1 << side.getIndex()) > 0) continue;
            BlockPos offsetPos = blockPos.offset(side);
            if (!connectedTanks.contains(offsetPos) && !getPos().equals(offsetPos)) continue;
            resultConnectionMask |= 1 << side.getIndex();
        }
        return resultConnectionMask;
    }

    private int getFluidStoredInTank(BlockPos blockPos) {
        double fluidLevel = getFluidLevelForTank(blockPos);
        return MathHelper.floor(fluidLevel * tankSize);
    }

    private MetaTileEntityTank getTankTile(BlockPos blockPos) {
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(getWorld(), blockPos);
        if (!(metaTileEntity instanceof MetaTileEntityTank)) {
            return null;
        }
        MetaTileEntityTank metaTileEntityTank = (MetaTileEntityTank) metaTileEntity;
        if (metaTileEntityTank.isRemoved ||
                metaTileEntityTank.material != material ||
                metaTileEntityTank.tankSize != tankSize ||
                !isTankFluidEqual(metaTileEntityTank, this)) {
            return null;
        }
        return metaTileEntityTank;
    }

    private boolean isTankController() {
        return controllerPos == null;
    }

    private FluidTank getActualFluidTank() {
        if (controllerPos != null) {
            MetaTileEntityTank metaTileEntityTank = getControllerEntity();
            return metaTileEntityTank == null ? new FluidTank(0) :
                    metaTileEntityTank.getActualFluidTank();
        } else {
            return multiblockFluidTank;
        }
    }

    private FluidStack getActualTankFluid() {
        FluidTank fluidTank = getActualFluidTank();
        return fluidTank == null ? null : fluidTank.getFluid();
    }

    private void removeTankFromMultiblock(MetaTileEntityTank removedTank) {
        int fluidInTank = getFluidStoredInTank(removedTank.getPos());
        this.needsShapeResync = true;
        removedTank.setTankControllerInternal(null);
        FluidStack fluidStack = multiblockFluidTank.getFluid();
        //do not retain fluid to disconnected tank if it has less than 0 bucket
        if (fluidStack != null && fluidInTank >= Fluid.BUCKET_VOLUME) {
            FluidStack fillStack = GTUtility.copyAmount(fluidInTank, fluidStack);
            removedTank.multiblockFluidTank.fill(fillStack, true);
            this.multiblockFluidTank.drain(fillStack, true);
        }
        this.connectedTanks.remove(removedTank.getPos());
        recomputeTankSize();
    }

    private void addTankToMultiblock(MetaTileEntityTank addedTank) {
        this.connectedTanks.add(addedTank.getPos());
        this.needsShapeResync = true;
        FluidStack tankFluid = addedTank.setTankControllerInternal(this);
        recomputeTankSize();
        if (tankFluid != null) {
            multiblockFluidTank.fill(tankFluid, true);
        }
    }

    private void recomputeTankSize() {
        this.recomputeTankSize = true;
        recomputeTankSizeNow(false);
    }

    private void recomputeTankSizeNow(boolean allowShrinking) {
        int newCapacity = (connectedTanks.size() + 1) * tankSize;
        this.multiblockFluidTank.setCapacity(newCapacity);
        if (allowShrinking && multiblockFluidTank.getFluid() != null &&
                multiblockFluidTank.getFluidAmount() > multiblockFluidTank.getCapacity()) {
            multiblockFluidTank.getFluid().amount = multiblockFluidTank.getCapacity();
        }
    }

    private MetaTileEntityTank getControllerEntity() {
        if (controllerPos == null) {
            return null;
        }
        MetaTileEntityTank cachedController = controllerCache.get();
        if (cachedController != null) {
            if (cachedController.isValid()) {
                return cachedController;
            } else {
                controllerCache.clear();
            }
        }
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(getWorld(), controllerPos);
        if (metaTileEntity instanceof MetaTileEntityTank) {
            this.controllerCache = new WeakReference<>((MetaTileEntityTank) metaTileEntity);
            return (MetaTileEntityTank) metaTileEntity;
        }
        return null;
    }

    private FluidStack setTankControllerInternal(MetaTileEntityTank controller) {
        this.controllerPos = controller == null ? null : controller.getPos();
        this.controllerCache = new WeakReference<>(controller);
        markDirty();
        if (controller == null) {
            this.networkStatus = NetworkStatus.DETACHED_FROM_MULTIBLOCK;
            return null;
        } else {
            this.networkStatus = NetworkStatus.ATTACHED_TO_MULTIBLOCK;
            FluidStack fluidStack = multiblockFluidTank.drain(Integer.MAX_VALUE, true);
            this.multiblockFluidTank.setFluid(null);
            this.lastSentFluidStack = null;
            this.needsFluidResync = false;
            return fluidStack;
        }
    }

    private void onFluidChangedInternal() {
        //also mark ourselves dirty by the way
        markDirty();
        //controller fluid update - sent in update() after network status
        if (getWorld() != null && !getWorld().isRemote && isTankController()) {
            this.needsFluidResync = true;
        }
    }

    @Override
    public void addDebugInfo(List<String> debugInfo) {
        debugInfo.add("IsController: " + isTankController());
        debugInfo.add("ControllerPos: " + controllerPos);
        List<BlockPos> connectedTanks = new ArrayList<>(this.connectedTanks);
        while (!connectedTanks.isEmpty()) {
            int startIndex = Math.min(4, connectedTanks.size());
            debugInfo.add("ConnectedBlocks: " + connectedTanks.subList(0, startIndex));
            if (connectedTanks.size() > startIndex) {
                connectedTanks = connectedTanks.subList(startIndex, connectedTanks.size());
            } else break;
        }
        debugInfo.add("Tank fluid: " + multiblockFluidTank.getFluidAmount() + "/" + multiblockFluidTank.getCapacity() + " #" + multiblockFluidTank.hashCode());
        FluidTank actualTankFluid = getActualFluidTank();
        debugInfo.add("Actual Tank fluid: " + actualTankFluid.getFluidAmount() + "/" + actualTankFluid.getCapacity() + " #" + actualTankFluid.hashCode());
        debugInfo.add("FluidLevel: " + getFluidLevelForTank(getPos()));
        debugInfo.add("FluidInTank: " + getFluidStoredInTank(getPos()));
    }

    private void updateControllerSize(Vec3i controllerSize) {
        Preconditions.checkState(isTankController(), "Can update controller size only on controller tile entity!");
        this.multiblockSize = controllerSize;
    }

    private void setTankController(MetaTileEntityTank controller) {
        if (controller == this) {
            return;
        }
        MetaTileEntityTank oldController = getControllerEntity();
        if (oldController == controller) {
            return; //do not pointlessly update controller
        }
        if (oldController != null) {
            oldController.removeTankFromMultiblock(this);
        }
        if (controller != null) {
            //we are controlled by somebody now, remove our own controlled tanks
            clearConnectedTanks();
            controller.addTankToMultiblock(this);
        } else {
            //we are controller ourselves now
            setTankControllerInternal(null);
            //reset multiblock size anyway
            this.multiblockSize = new Vec3i(1, 1, 1);
        }
    }

    private void clearConnectedTanks() {
        for (BlockPos tankPos : ImmutableList.copyOf(connectedTanks)) {
            MetaTileEntityTank metaTileEntityTank = getTankTile(tankPos);
            if (metaTileEntityTank == null) continue;
            removeTankFromMultiblock(metaTileEntityTank);
        }
        this.connectedTanks.clear();
        this.multiblockSize = new Vec3i(1, 1, 1);
    }

    private static boolean isTankFluidEqual(MetaTileEntityTank tank1, MetaTileEntityTank tank2) {
        FluidStack fluidStack1 = tank1.getActualTankFluid();
        FluidStack fluidStack2 = tank2.getActualTankFluid();
        return fluidStack1 == null || fluidStack2 == null || fluidStack1.isFluidEqual(fluidStack2);
    }

    private static boolean canTanksConnect(MetaTileEntityTank tank1, MetaTileEntityTank tank2, EnumFacing side) {
        return side == null || ((tank1.blockedSides & 1 << side.getIndex()) <= 0 &&
                (tank2.blockedSides & 1 << side.getOpposite().getIndex()) <= 0);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTank(metaTileEntityId, material, tankSize, maxSizeVertical, maxSizeHorizontal);
    }

    @Override
    public int getLightOpacity() {
        return 1; //let light pass trough us entirely
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(material) ? "axe" : "pickaxe";
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(itemStack.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
            this.multiblockFluidTank.fill(fluidStack, true);
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        FluidStack fluidStack = multiblockFluidTank.drain(Integer.MAX_VALUE, false);
        if (fluidStack != null && fluidStack.amount > 0) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            itemStack.setTag(FluidHandlerItemStack.FLUID_NBT_KEY, tagCompound);
        }
    }

    @Override
    public String getItemSubTypeId(ItemStack itemStack) {
        return DefaultSubItemHandler.getFluidContainerSubType(itemStack);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        super.getSubItems(creativeTab, subItems);
        if (creativeTab == CreativeTabs.SEARCH && !ConfigHolder.compat.hideFilledTanksInJEI) {
            DefaultSubItemHandler.addFluidContainerVariants(getStackForm(), subItems);
        }
    }

    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new FluidHandlerItemStack(itemStack, tankSize) {
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                FluidStack drained = super.drain(resource, doDrain);
                this.removeTagWhenEmptied(doDrain);
                return drained;
            }

            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                FluidStack drained = super.drain(maxDrain, doDrain);
                this.removeTagWhenEmptied(doDrain);
                return drained;
            }

            private void removeTagWhenEmptied(boolean doDrain) {
                if (doDrain && this.getFluid() == null) {
                    this.container.setTagCompound(null);
                }
            }

            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return MetaTileEntityTank.this.canFillFluidType(fluid);
            }
        };
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (isTankController()) {
            buf.writeBoolean(true);
            ByteBufUtils.writeRelativeBlockList(buf, getPos(), connectedTanks);
            buf.writeBlockPos(new BlockPos(multiblockSize));
            FluidStack fluidStack = multiblockFluidTank.getFluid();
            ByteBufUtils.writeFluidStack(buf, fluidStack);
        } else {
            buf.writeBoolean(false);
            buf.writeBlockPos(controllerPos);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            this.connectedTanks = ByteBufUtils.readRelativeBlockList(buf, getPos());
            this.multiblockSize = buf.readBlockPos();
            recomputeTankSizeNow(true);
            FluidStack fluidStack = ByteBufUtils.readFluidStack(buf);
            this.multiblockFluidTank.setFluid(fluidStack);
        } else {
            this.controllerPos = buf.readBlockPos();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_CONTROLLER_POSITION) {
            this.controllerPos = buf.readBlockPos();
            this.controllerCache = new WeakReference<>(null);
            this.multiblockFluidTank.setFluid(null);
        } else if (dataId == DEFORM_TANK) {
            this.controllerPos = null;
            this.controllerCache = new WeakReference<>(null);
            this.multiblockFluidTank.setFluid(null);
            this.connectedTanks.clear();
            this.multiblockSize = new Vec3i(1, 1, 1);
        } else if (dataId == SYNC_FLUID_CHANGE) {
            if (controllerPos == null) {
                FluidStack fluidStack = ByteBufUtils.readFluidStackDelta(buf, multiblockFluidTank.getFluid());
                this.multiblockFluidTank.setFluid(fluidStack);
            }
        } else if (dataId == SYNC_TANK_SHAPE) {
            this.connectedTanks = ByteBufUtils.readRelativeBlockList(buf, getPos());
            this.multiblockSize = buf.readBlockPos();
            recomputeTankSizeNow(true);
        }
    }

    private IFluidTankProperties getTankProperties() {
        IFluidTankProperties[] properties = fluidInventory.getTankProperties();
        return properties.length == 0 ? null : properties[0];
    }

    @Override
    public int getActualComparatorValue() {
        IFluidTankProperties properties = getTankProperties();
        if (properties == null) {
            return 0;
        }
        FluidStack fluidStack = properties.getContents();
        int fluidAmount = fluidStack == null ? 0 : fluidStack.amount;
        int maxCapacity = properties.getCapacity();
        float f = fluidAmount / (maxCapacity * 1.0f);
        return MathHelper.floor(f * 14.0f) + (fluidAmount > 0 ? 1 : 0);
    }

    @Override
    public int getActualLightValue() {
        IFluidTankProperties properties = getTankProperties();
        if (properties == null) {
            return 0;
        }
        FluidStack fluidStack = properties.getContents();
        if (fluidStack == null) {
            return 0;
        }
        return fluidStack.getFluid().getLuminosity(fluidStack);
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return getWorld().isRemote || (FluidUtil.interactWithFluidHandler(playerIn, hand, fluidInventory) && !playerIn.isSneaking());
    }

    @SideOnly(Side.CLIENT)
    private TankRenderer getTankRenderer() {
        if (ModHandler.isMaterialWood(material)) {
            return Textures.WOODEN_TANK;
        } else return Textures.METAL_TANK;
    }

    @SideOnly(Side.CLIENT)
    private int getActualPaintingColor() {
        int color = ColourRGBA.multiply(
                GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())
        );
        color = convertOpaqueRGBA_CLtoRGB(color);
        return color;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getTankRenderer().getParticleTexture(), getActualPaintingColor());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        int baseColor = GTUtility.convertRGBtoOpaqueRGBA_CL(getActualPaintingColor());
        getTankRenderer().render(renderState, translation, ArrayUtils.add(pipeline, new ColourMultiplier(baseColor)), connectionMask);
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        double fillPercent;
        FluidStack fluidStack = getFluidForRendering();
        if (getWorld() == null) {
            fillPercent = fluidStack == null ? 0 : fluidStack.amount / (tankSize * 1.0);
        } else {
            fillPercent = getFluidLevelForTank(getPos());
        }
        if (fillPercent > 0.0) {
            getTankRenderer().renderFluid(renderState, translation, connectionMask, fillPercent, fluidStack);
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == RENDER_PASS_TRANSLUCENT; //fluids should be rendered in translucent layer
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos());
    }

    @SideOnly(Side.CLIENT)
    private FluidStack getFluidForRendering() {
        if (getWorld() == null && renderContextStack != null) {
            NBTTagCompound tagCompound = renderContextStack.getTagCompound();
            if (tagCompound != null && tagCompound.hasKey("Fluid", NBT.TAG_COMPOUND)) {
                return FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            }
            return null;
        }
        return getActualTankFluid();
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", tankSize));
        tooltip.add(I18n.format("gregtech.machine.fluid_tank.max_multiblock", maxSizeHorizontal, maxSizeVertical, maxSizeHorizontal));
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack != null) {
                tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", fluidStack.amount, fluidStack.getLocalizedName()));
            }
            String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
            if (formula != null)
                tooltip.add(TextFormatting.GRAY + formula);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (controllerPos != null) {
            data.setTag("ControllerPos", NBTUtil.createPosTag(controllerPos));
        } else {
            data.setTag("FluidInventory", multiblockFluidTank.writeToNBT(new NBTTagCompound()));
            NBTTagList connectedTanks = new NBTTagList();
            this.connectedTanks.forEach(pos -> connectedTanks.appendTag(NBTUtil.createPosTag(pos)));
            data.setTag("ConnectedTanks", connectedTanks);
            data.setTag("MultiblockSize", NBTUtil.createPosTag(new BlockPos(multiblockSize)));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("ControllerPos")) {
            this.controllerPos = NBTUtil.getPosFromTag(data.getCompoundTag("ControllerPos"));
        } else {
            NBTTagList connectedTanks = data.getTagList("ConnectedTanks", NBT.TAG_COMPOUND);
            connectedTanks.forEach(pos -> this.connectedTanks.add(NBTUtil.getPosFromTag((NBTTagCompound) pos)));
            recomputeTankSizeNow(true);
            this.multiblockFluidTank.readFromNBT(data.getCompoundTag("FluidInventory"));
            this.lastSentFluidStack = this.multiblockFluidTank.getFluid();
            if (data.hasKey("MultiblockSize")) {
                this.multiblockSize = NBTUtil.getPosFromTag(data.getCompoundTag("MultiblockSize"));
            }
        }
    }

    protected boolean canFillFluidType(FluidStack fluid) {
        return !ModHandler.isMaterialWood(material) &&
                !material.hasFlag(FLAMMABLE) ||
                fluid.getFluid().getTemperature(fluid) <= 325;
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false; //handled manually
    }

    private enum NetworkStatus {
        //retain contained fluid, reset controller pos
        DETACHED_FROM_MULTIBLOCK,
        //clear contained fluid, update controller pos
        ATTACHED_TO_MULTIBLOCK
    }

    private void handleTankSyncing() {
        if (networkStatus == NetworkStatus.ATTACHED_TO_MULTIBLOCK) {
            writeCustomData(UPDATE_CONTROLLER_POSITION, buf -> buf.writeBlockPos(controllerPos));
            this.needsFluidResync = false;
            this.networkStatus = null;
            this.needsShapeResync = false;
        }
        if (networkStatus == NetworkStatus.DETACHED_FROM_MULTIBLOCK) {
            writeCustomData(DEFORM_TANK, buf -> {
            });
            this.networkStatus = null;
        }
        if (isTankController() && needsShapeResync) {
            writeCustomData(SYNC_TANK_SHAPE, buf -> {
                ByteBufUtils.writeRelativeBlockList(buf, getPos(), connectedTanks);
                buf.writeBlockPos(new BlockPos(multiblockSize));
            });
            this.needsShapeResync = false;
        }
        if (isTankController() && needsFluidResync) {
            if (timeSinceLastFluidSync++ >= FLUID_SYNC_THROTTLE) {
                FluidStack fluidStack = multiblockFluidTank.getFluid();
                writeCustomData(SYNC_FLUID_CHANGE, buf -> ByteBufUtils.writeFluidStackDelta(buf, lastSentFluidStack, fluidStack));
                this.lastSentFluidStack = fluidStack == null ? null : fluidStack.copy();
                this.timeSinceLastFluidSync = 0;
                this.needsFluidResync = false;
            }
        }
    }

    private static Map<BlockPos, MetaTileEntityTank> findConnectedTanks(MetaTileEntityTank tank, Set<BlockPos> visitedSet) {
        BlockPos startPos = tank.getPos();
        HashMap<BlockPos, MetaTileEntityTank> observedSet = new HashMap<>();
        if (visitedSet.contains(startPos)) {
            return observedSet;
        }
        observedSet.put(tank.getPos(), tank);
        visitedSet.add(startPos);
        MetaTileEntityTank firstNode = observedSet.get(startPos);
        MutableBlockPos currentPos = new MutableBlockPos(startPos);
        Stack<EnumFacing> moveStack = new Stack<>();
        int currentAmount = 0;
        int scanSizeLimit = tank.maxSizeHorizontal * tank.maxSizeHorizontal * tank.maxSizeVertical * 4;
        main:
        while (true) {
            if (currentAmount >= scanSizeLimit) {
                break;
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                currentPos.move(facing);
                MetaTileEntityTank metaTileEntity;
                if (!visitedSet.contains(currentPos) &&
                        !observedSet.containsKey(currentPos) &&
                        (metaTileEntity = tank.getTankTile(currentPos)) != null &&
                        canTanksConnect(firstNode, metaTileEntity, facing)) {
                    observedSet.put(metaTileEntity.getPos(), metaTileEntity);
                    visitedSet.add(metaTileEntity.getPos());
                    firstNode = metaTileEntity;
                    moveStack.push(facing.getOpposite());
                    currentAmount++;
                    continue main;
                } else currentPos.move(facing.getOpposite());
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
                firstNode = observedSet.get(currentPos);
            } else break;
        }
        return observedSet;
    }

    private boolean checkMultiblockValid(Map<BlockPos, MetaTileEntityTank> tankMap, BlockPos lowestCorner, int sizeX, int sizeY, int sizeZ) {
        //iterate each block to ensure that we have tanks in valid positions
        HashSet<BlockPos> visitedPositions = new HashSet<>();
        MutableBlockPos blockPos = new MutableBlockPos();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                for (int k = 0; k < sizeZ; k++) {
                    blockPos.setPos(lowestCorner.getX() + i, lowestCorner.getY() + j, lowestCorner.getZ() + k);
                    MetaTileEntityTank tankHere = tankMap.get(blockPos);
                    if (tankHere == null) {
                        //tank is not here, so multiblock is not formed
                        return false;
                    }
                    //add found tank into the visited position list
                    visitedPositions.add(tankHere.getPos());
                }
            }
        }
        //multiblock is assembled only if every tank neighbour is in the multiblock
        return visitedPositions.containsAll(tankMap.keySet());
    }
}
