
package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.IMiner;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.tools.ToolUtility;
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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements IMiner { //todo implement maintenance and soft hammering

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY};

    public final IMiner.Type type;
    private Material material;
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
    private IEnergyContainer energyContainer;
    private IMultipleTankHandler importFluidHandler;
    protected IItemHandlerModifiable outputInventory;
    private boolean isActive = false;
    private boolean done = false;
    private boolean silkTouch = false;
    protected boolean wasActiveAndNeedsUpdate;
    private boolean chunkMode = false;

    private LinkedList<BlockPos> blockPos = new LinkedList<>();
    private int aRadius;
    private int pipeY = 0;
    private boolean invFull = false;
    private final int tier;
    private int overclockAmount;

    private static final Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);


    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, IMiner.Type type, int tier, Material material) {
        super(metaTileEntityId);
        this.type = type;
        this.material = material;
        this.tier = tier;
        aRadius = getType().radius * 16;
        reinitializeStructurePattern();
    }


    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (isActive)
            setActive(false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.importFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.overclockAmount = (int) Math.pow(2, getVoltageTier() - this.tier);
    }

    private void resetTileAbilities() {
        this.importFluidHandler = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public boolean drainEnergy() {
        if (energyContainer.getInputVoltage() < getMaxVoltage())
            return false;
        FluidStack drillingFluid = DrillingFluid.getFluid(type.drillingFluidConsumePerTick);
        FluidStack canDrain = importFluidHandler.drain(drillingFluid, false);
        if (energyContainer.getEnergyStored() >= getMaxVoltage() && canDrain != null && canDrain.amount == type.drillingFluidConsumePerTick && !invFull && !testForMax()) {
            energyContainer.removeEnergy(energyContainer.getInputVoltage());
            importFluidHandler.drain(drillingFluid, true);
            return true;
        }
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (done || !drainEnergy()) {
                if (isActive)
                    setActive(false);
                if (!done && testForMax()) {
                    initPos();
                }
                if (invFull && getOffsetTimer() % 20 == 0) {
                    pushItemsIntoNearbyHandlers(getFrontFacing());
                    NonNullList<ItemStack> testSpace = NonNullList.create();
                    testSpace.add(new ItemStack(Blocks.STONE));
                    if (addItemsToItemHandler(outputInventory, true, testSpace)) {
                        invFull = false;
                    }
                }
                return;
            }

            if (!isActive)
                setActive(true);

            WorldServer world = (WorldServer) this.getWorld();

            if (mineY.get() < tempY.get()) {
                world.destroyBlock(new BlockPos(getPos().getX(), tempY.get(), getPos().getZ()), false);
                tempY.decrementAndGet();
                this.pipeY++;
                writeCustomData(-200, b -> b.writeInt(pipeY));
                markDirty();
            }

            if(y.get() > 0) {
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, startY, aRadius, IMiner.getTPS(world)));
            }

            //MAINTENANCE IMPLEMENTATION:
            // if (getOffsetTimer() % (this.getNumProblems() > 0 ? type.tick * 2L : type.tick) == 0 && !blockPos.isEmpty()) {
            //    for (int x = 0; x < (this.getNumProblems() > 0 ? 1 : overclockAmount); ) {

            if (getOffsetTimer() % type.tick == 0 && !blockPos.isEmpty()) {
                int a = 0;
                while (a < overclockAmount && !blockPos.isEmpty()) {
                    BlockPos tempPos = blockPos.getFirst();
                    NonNullList<ItemStack> itemStacks = NonNullList.create();
                    IBlockState blockState = this.getWorld().getBlockState(tempPos);
                    if (blockState != Blocks.AIR.getDefaultState()) {
                        if (!silkTouch) {
                            ToolUtility.applyHammerDrops(world.rand, blockState, itemStacks, type.fortune, null, RecipeMaps.MACERATOR_RECIPES);
                        } else {
                            itemStacks.add(new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState)));
                        }
                        if (addItemsToItemHandler(outputInventory, true, itemStacks)) {
                            addItemsToItemHandler(outputInventory, false, itemStacks);
                            world.setBlockState(tempPos, Blocks.AIR.getDefaultState());
                            mineX.set(tempPos.getX());
                            mineZ.set(tempPos.getZ());
                            mineY.set(tempPos.getY());
                            a++;
                            blockPos.removeFirst();
                        } else {
                            invFull = true;
                            break;
                        }
                    } else {
                        a++;
                        blockPos.removeFirst();
                    }
                }
            } else if (blockPos.isEmpty()) {
                x.set(mineX.get());
                y.set(mineY.get());
                z.set(mineZ.get());
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, startY, aRadius, IMiner.getTPS(world)));
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
    protected BlockPattern createStructurePattern() {
        return material == null || type == null ? null : FactoryBlockPattern.start()
                .aisle("CCC", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("CPC", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("CSC", "#F#", "#F#", "#F#", "###", "###", "###")
                .setAmountAtLeast('L', 3)
                .where('S', selfPredicate())
                .where('L', statePredicate(getCasingState()))
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(getCasingState()))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(getMaterial()).getDefaultState()))
                .where('#', blockWorldState -> true)
                .build();
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        int itemOutputsCount = abilities.getOrDefault(MultiblockAbility.EXPORT_ITEMS, Collections.emptyList())
                .stream().map(it -> (IItemHandler) it).mapToInt(IItemHandler::getSlots).sum();
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        return itemOutputsCount >= 1 &&
                fluidInputsCount >= 1 &&
                abilities.containsKey(MultiblockAbility.INPUT_ENERGY);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.miner.multi.description", type.radius, type.radius, type.fortuneString));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", type.drillingFluidConsumePerTick, I18n.format(DrillingFluid.getFluid(0).getUnlocalizedName())));
        tooltip.add(I18n.format("gregtech.machine.miner.overclock", GTValues.VN[getTier() < 6 ? getTier() + 1 : 14]));
    }


    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (this.isStructureFormed()) {

            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = energyContainer.getInputVoltage();
                String voltageName = GTValues.VN[GTUtility.getTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            textList.add(new TextComponentString(String.format("sX: %d", x.get() == Integer.MAX_VALUE ? 0 : x.get())));
            textList.add(new TextComponentString(String.format("sY: %d", y.get() == Integer.MAX_VALUE ? 0 : y.get())));
            textList.add(new TextComponentString(String.format("sZ: %d", z.get() == Integer.MAX_VALUE ? 0 : z.get())));
            textList.add(new TextComponentString(String.format("mX: %d", mineX.get())));
            textList.add(new TextComponentString(String.format("mY: %d", mineY.get())));
            textList.add(new TextComponentString(String.format("mZ: %d", mineZ.get())));
            textList.add(new TextComponentString(String.format("Chunk Radius: %d", aRadius / 16)));
            if (done)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.done").setStyle(new Style().setColor(TextFormatting.GREEN)));
            else if (isActive)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.working").setStyle(new Style().setColor(TextFormatting.GOLD)));
            else if (invFull)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.invfull").setStyle(new Style().setColor(TextFormatting.RED)));
            else if (!(importFluidHandler.drain(DrillingFluid.getFluid(type.drillingFluidConsumePerTick), false) != null && (Objects.requireNonNull(importFluidHandler.drain(DrillingFluid.getFluid(type.drillingFluidConsumePerTick), false))).amount == type.drillingFluidConsumePerTick))
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.needsfluid").setStyle(new Style().setColor(TextFormatting.RED)));
            else
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.needspower").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        super.addDisplayText(textList);
    }

    public IBlockState getCasingState() {
        switch (material.getUnlocalizedName()) {
            default:
                return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
            case "material.titanium":
                return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
            case "material.tungsten_steel":
                return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        }
    }


    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        switch (material.getUnlocalizedName()) {
            default:
                return Textures.SOLID_STEEL_CASING;
            case "material.titanium":
                return Textures.STABLE_TITANIUM_CASING;
            case "material.tungsten_steel":
                return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        }
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, getType(), getTier(), getMaterial());
    }


    public int getTier() {
        return this.tier;
    }

    @Override
    public Type getType() {
        return type;
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
        data.setTag("radius", new NBTTagInt(aRadius));
        data.setTag("isActive", new NBTTagInt(isActive ? 1 : 0));
        data.setTag("done", new NBTTagInt(done ? 1 : 0));
        data.setTag("chunkMode", new NBTTagInt(chunkMode ? 1 : 0));
        data.setTag("silkTouch", new NBTTagInt(silkTouch ? 1 : 0));
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
        aRadius = data.getInteger("radius");
        done = data.getInteger("done") != 0;
        isActive = data.getInteger("isActive") != 0;
        chunkMode = data.getInteger("chunkMode") != 0;
        silkTouch = data.getInteger("silkTouch") != 0;
    }


    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeInt(pipeY);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.pipeY = buf.readInt();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive);
        for (int i = 0; i < pipeY; i++) {
            translation.translate(0.0, -1.0, 0.0);
            Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline, PIPE_CUBOID);
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
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        } else if (dataId == -200) {
            this.pipeY = buf.readInt();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    public long getMaxVoltage() {
        return GTValues.V[getVoltageTier()];
    }

    public int getVoltageTier() {
        int voltageCap = getType() == Type.BASIC ? 5 : getType() == Type.LARGE ? 6 : 9;
        int inputVoltage = GTUtility.getTierByVoltage(energyContainer.getInputVoltage());

        if (inputVoltage < this.tier)
            return this.tier;
        else if (inputVoltage > voltageCap)
            return voltageCap;
        return inputVoltage;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        ModularUI.Builder builder = ModularUI.extendedBuilder();
        builder.image(7, 4, 145, 121, GuiTextures.DISPLAY);
        builder.label(11, 9, this.getMetaFullName(), 16777215);
        builder.widget((new AdvancedTextWidget(11, 19, this::addDisplayText,
                16777215)).setMaxWidthLimit(139).setClickHandler(this::handleDisplayClick));
        builder.bindPlayerInventory(entityPlayer.inventory, 134);

        builder.widget(new ToggleButtonWidget(154, 4, 16, 16,
                this::getChunkMode, this::setChunkMode).setTooltipText("gregtech.gui.chunkmode"));
        builder.widget(new ToggleButtonWidget(154, 22, 16, 16,
                this::getSilkTouch, this::setSilkTouch).setTooltipText("gregtech.gui.silktouch"));

        return builder.build(getHolder(), entityPlayer);
    }

    public void setChunkMode(boolean chunkMode) {
        if (!isActive) {
            x.set(Integer.MAX_VALUE);
            y.set(Integer.MAX_VALUE);
            z.set(Integer.MAX_VALUE);
            this.chunkMode = chunkMode;
        }
    }

    public boolean getChunkMode() {
        return this.chunkMode;
    }

    public void setSilkTouch(boolean silkTouch) {
        this.silkTouch = silkTouch;
    }

    public boolean getSilkTouch() {
        return this.silkTouch;
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

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!isActive) {

            if (aRadius - 16 == 0)
                aRadius = getType().radius * 16;
            else
                aRadius -= 16;

            x.set(Integer.MAX_VALUE);
            y.set(Integer.MAX_VALUE);
            z.set(Integer.MAX_VALUE);
            playerIn.sendStatusMessage(new TextComponentTranslation(String.format("gregtech.multiblock.large_miner.radius", aRadius)), false);
        } else {
            playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.multiblock.large_miner.errorradius"), false);
        }
        return true;
    }

    public boolean testForMax() {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
    }

    public void initPos() {
        if (!chunkMode) {
            x.set(getPos().getX() - aRadius);
            z.set(getPos().getZ() - aRadius);
            y.set(getPos().getY() - 1);
            startX.set(getPos().getX() - aRadius);
            startZ.set(getPos().getZ() - aRadius);
            startY.set(getPos().getY());
            tempY.set(getPos().getY() - 1);
            mineX.set(getPos().getX() - aRadius);
            mineZ.set(getPos().getZ() - aRadius);
            mineY.set(getPos().getY() - 1);
        } else {
            WorldServer world = (WorldServer) this.getWorld();
            Chunk origin = world.getChunk(getPos());
            ChunkPos startPos = (world.getChunk(origin.x - aRadius / 16, origin.z - aRadius / 16)).getPos();
            x.set(startPos.getXStart());
            z.set(startPos.getZStart());
            y.set(getPos().getY() - 1);
            startX.set(startPos.getXStart());
            startZ.set(startPos.getZStart());
            mineX.set(startPos.getXStart());
            mineZ.set(startPos.getZStart());
            mineY.set(getPos().getY() - 1);
            startY.set(getPos().getY());
            tempY.set(getPos().getY() - 1);
        }
    }
}
