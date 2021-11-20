
package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
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
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements IMiner, IControllable {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY};

    private final Material material;
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
    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputInventory;
    private boolean isActive = true;
    private boolean done = false;
    private boolean silkTouch = false;
    private boolean chunkMode = false;

    private final LinkedList<BlockPos> blockPos = new LinkedList<>();
    private int currentRadius;
    private int pipeLength = 0;
    private boolean invFull = false;
    private final int tier;
    private int overclockAmount;
    private final int chunkRadius;
    private final int tick;
    private final int drillingFluidConsumePerTick;
    private final int fortune;
    private final String romanNumeralString;

    private static final Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);


    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, int tier, Material material, int tick, int chunkRadius, int drillingFluidConsumePerTick, int fortune) {
        super(metaTileEntityId);
        this.tick = tick;
        this.material = material;
        this.tier = tier;
        this.chunkRadius = chunkRadius;
        this.currentRadius = chunkRadius * 16;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
        this.fortune = fortune;
        this.romanNumeralString = GTUtility.romanNumeralString(fortune);
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
        this.inputFluidInventory = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.overclockAmount = Math.max(1, GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier);
    }

    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    public boolean drainFluid(boolean simulate) {
        FluidStack drillingFluid = DrillingFluid.getFluid(this.drillingFluidConsumePerTick * overclockAmount);
        FluidStack canDrain = inputFluidInventory.drain(drillingFluid, false);
        if (canDrain != null && canDrain.amount == this.drillingFluidConsumePerTick) {
            if (!simulate)
                inputFluidInventory.drain(drillingFluid, true);
            return true;
        }
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (!isActive())
                return;

            if (done || testForMax() || !drainEnergy(true) || !drainFluid(true)) {
                if (!done && testForMax())
                    initPos();
                resetInv();
                return;
            }

            if (!invFull) {
                drainEnergy(false);
                drainFluid(false);
            }

            WorldServer world = (WorldServer) this.getWorld();
            if (mineY.get() < tempY.get()) {
                world.destroyBlock(new BlockPos(getPos().getX(), tempY.get(), getPos().getZ()), false);
                tempY.decrementAndGet();
                this.pipeLength++;
                writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeInt(pipeLength));
                markDirty();
            }

            if (blockPos.isEmpty())
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, currentRadius, IMiner.getMeanTickTime(world)));

            if (getOffsetTimer() % getTick() == 0 && !blockPos.isEmpty()) {
                int a = 0;
                while (a < overclockAmount && !blockPos.isEmpty()) {
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
                        if (!silkTouch)
                            IMiner.applyTieredHammerNoRandomDrops(world.rand, blockState, itemStacks, this.fortune, null, RecipeMaps.MACERATOR_RECIPES, getVoltageTier());
                        else
                            itemStacks.add(new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState)));
                        if (addItemsToItemHandler(outputInventory, true, itemStacks)) {
                            addItemsToItemHandler(outputInventory, false, itemStacks);
                            world.setBlockState(tempPos, Blocks.COBBLESTONE.getDefaultState());
                            mineX.set(tempPos.getX());
                            mineZ.set(tempPos.getZ());
                            mineY.set(tempPos.getY());
                            a++;
                            blockPos.removeFirst();

                            if (invFull)
                                invFull = false;
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
                blockPos.addAll(IMiner.getBlocksToMine(this, x, y, z, startX, startZ, currentRadius, IMiner.getMeanTickTime(world)));
                if (blockPos.isEmpty()) {
                    done = true;
                }
            }
        }

    }

    @Override
    protected BlockPattern createStructurePattern() {
        return material == null ? null : FactoryBlockPattern.start()
                .aisle("CCC", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("CCC", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("CSC", "#F#", "#F#", "#F#", "###", "###", "###")
                .setAmountAtLeast('L', 3)
                .where('S', selfPredicate())
                .where('L', statePredicate(getCasingState()))
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(getMaterial()).getDefaultState()))
                .where('#', blockWorldState -> true)
                .build();
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        int itemOutputsCount = abilities.getOrDefault(MultiblockAbility.EXPORT_ITEMS, Collections.emptyList()).size();
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        return itemOutputsCount >= 1 && fluidInputsCount >= 1 && abilities.containsKey(MultiblockAbility.INPUT_ENERGY);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.miner.multi.modes"));
        tooltip.add(I18n.format("gregtech.machine.miner.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.miner.multi.tooltip", getCurrentRadius() / 16, getCurrentRadius() / 16));
        tooltip.add(I18n.format("gregtech.machine.miner.multi.production"));
        //small ore: tooltip.add(I18n.format("gregtech.machine.miner.multi.production", getRomanNumeralString()));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", getDrillingFluidConsumePerTick(), I18n.format(DrillingFluid.getFluid().getUnlocalizedName())));
        tooltip.add(I18n.format("gregtech.machine.miner.overclock", GTValues.VN[getTier()], GTValues.VN[getTier() < GTValues.LuV ? getTier() + 1 : GTValues.ZPM]));
    }


    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (this.isStructureFormed()) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = energyContainer.getInputVoltage();
                String voltageName = GTValues.VN[GTUtility.getTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            textList.add(new TextComponentString(String.format("sX: %d      mX: %d", x.get() == Integer.MAX_VALUE ? 0 : x.get(), mineX.get())));
            textList.add(new TextComponentString(String.format("sY: %d      mY: %d", y.get() == Integer.MAX_VALUE ? 0 : y.get(), mineY.get())));
            textList.add(new TextComponentString(String.format("sZ: %d      mZ: %d", z.get() == Integer.MAX_VALUE ? 0 : z.get(), mineZ.get())));
            textList.add(new TextComponentString(String.format("Chunk Radius: %d", currentRadius / 16)));
            if (done)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.done").setStyle(new Style().setColor(TextFormatting.GREEN)));
            else if (isActive)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.working").setStyle(new Style().setColor(TextFormatting.GOLD)));
            else
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            if (invFull)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.invfull").setStyle(new Style().setColor(TextFormatting.RED)));
            if (!drainFluid(true))
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.needsfluid").setStyle(new Style().setColor(TextFormatting.RED)));
            if (!drainEnergy(true))
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

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, getTier(), getMaterial(), getTick(), getMaxChunkRadius(), getDrillingFluidConsumePerTick(), getFortune());
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
        data.setTag("radius", new NBTTagInt(currentRadius));
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
        pipeLength = data.getInteger("pipeLength");
        currentRadius = data.getInteger("radius");
        done = data.getInteger("done") != 0;
        isActive = data.getInteger("isActive") != 0;
        chunkMode = data.getInteger("chunkMode") != 0;
        silkTouch = data.getInteger("silkTouch") != 0;
    }


    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeInt(pipeLength);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.pipeLength = buf.readInt();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().render(renderState, translation, pipeline, getFrontFacing(), this.isActive());
        for (int i = 0; i < pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline, PIPE_CUBOID);
        }
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        if (getTier() == 5)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED;
        if (getTier() == 6)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED_2;
        return Textures.LARGE_MINER_OVERLAY_BASIC;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
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
        if (dataId == GregtechDataCodes.IS_WORKING) {
            this.isActive = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
        if (dataId == GregtechDataCodes.PUMP_HEAD_LEVEL) {
            this.pipeLength = buf.readInt();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    public long getMaxVoltage() {
        return GTValues.V[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())];
    }

    public int getVoltageTier() {
        int voltageCap = getTier() + 1;
        int inputVoltage = GTUtility.getTierByVoltage(energyContainer.getInputVoltage());

        if (inputVoltage < getTier())
            return getTier();
        else if (inputVoltage > voltageCap)
            return voltageCap;
        return inputVoltage;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        ModularUI.Builder builder = ModularUI.extendedBuilder();
        builder.image(7, 4, 145, 128, GuiTextures.DISPLAY);
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
            if (currentRadius - 16 == 0)
                currentRadius = getMaxChunkRadius() * 16;
            else
                currentRadius -= 16;

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
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public boolean testForMax() {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
    }

    public void initPos() {
        if (!chunkMode) {
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
        } else {
            WorldServer world = (WorldServer) this.getWorld();
            Chunk origin = world.getChunk(getPos());
            ChunkPos startPos = (world.getChunk(origin.x - currentRadius / 16, origin.z - currentRadius / 16)).getPos();
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

    void resetInv() {
        if (notifiedItemOutputList.size() > 0) {
            invFull = false;
            notifiedItemOutputList.clear();
        }
    }

    public Material getMaterial() {
        return material;
    }

    public int getTier() {
        return this.tier;
    }

    public int getTick() {
        return this.tick;
    }

    public int getMaxChunkRadius() {
        return this.chunkRadius;
    }

    public int getDrillingFluidConsumePerTick() {
        return this.drillingFluidConsumePerTick;
    }

    public int getFortune() {
        return this.fortune;
    }

    public int getCurrentRadius() {
        return this.currentRadius;
    }

    public String getRomanNumeralString() {
        return this.romanNumeralString;
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
