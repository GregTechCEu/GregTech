package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.IMiner;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static gregtech.api.unification.material.Materials.*;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements IMiner { //todo maintenance in miner algorithm overhaul

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY};

    private Material material;
    private AtomicLong x = new AtomicLong(Long.MAX_VALUE), y = new AtomicLong(Long.MAX_VALUE), z = new AtomicLong(Long.MAX_VALUE);
    private AtomicInteger currentChunk = new AtomicInteger(0);
    private IEnergyContainer energyContainer;
    private IMultipleTankHandler importFluidHandler;
    protected IItemHandlerModifiable outputInventory;
    private List<Chunk> chunks = new ArrayList<>();
    private boolean isActive = false;
    private boolean done = false;
    private boolean silktouch = false;
    protected boolean wasActiveAndNeedsUpdate;

    private final int ticksPerOperation;
    private final int chunkRange;
    private final int fortune;
    private final int drillingFluidConsumedPerTick;
    private final String fortuneString;


    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, Material material, int ticksPerOperation, int chunkRange, int fortune, int drillingFluidConsumedPerTick) {
        super(metaTileEntityId);
        this.material = material;
        this.ticksPerOperation = ticksPerOperation;
        this.chunkRange = chunkRange;
        this.fortune = fortune;
        this.drillingFluidConsumedPerTick = drillingFluidConsumedPerTick;

        this.fortuneString = GTUtility.romanNumeralString(this.fortune);
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
    }

    private void resetTileAbilities() {
        this.importFluidHandler = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public boolean drainEnergy() {
        long energyDrain = GTValues.V[Math.max(GTValues.EV, GTUtility.getTierByVoltage(energyContainer.getInputVoltage()))];
        FluidStack drillingFluid = DrillingFluid.getFluid(this.getDrillingFluidConsumedPerTick());
        FluidStack canDrain = importFluidHandler.drain(drillingFluid, false);
        if (energyContainer.getEnergyStored() >= energyDrain && canDrain != null && canDrain.amount == this.getDrillingFluidConsumedPerTick()) {
            energyContainer.removeEnergy(energyContainer.getInputVoltage());
            importFluidHandler.drain(drillingFluid, true);
            return true;
        }
        return false;
    }

    @Override
    public int getTicksPerOperation() {
        return this.ticksPerOperation;
    }

    @Override
    public int getChunkRange() {
        return this.chunkRange;
    }

    @Override
    public int getFortune() {
        return this.fortune;
    }

    @Override
    public int getDrillingFluidConsumedPerTick() {
        return this.drillingFluidConsumedPerTick;
    }

    @Override
    public long getNbBlock() {
        int tierDifference = GTUtility.getTierByVoltage(energyContainer.getInputVoltage()) - GTValues.EV;
        return (long) Math.floor(Math.pow(2, tierDifference));
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (done || !drainEnergy()) {
                if (isActive)
                    setActive(false);
                return;
            }

            if (!isActive)
                setActive(true);

            WorldServer world = (WorldServer) this.getWorld();
            Chunk chunkMiner = world.getChunk(getPos());
            Chunk origin;
            if (chunks.size() == 0 && this.getChunkRange() / 2.0 > 1.0) {
                int tmp = Math.floorDiv(this.getChunkRange(), 2);
                origin = world.getChunk(chunkMiner.x - tmp, chunkMiner.z - tmp);
                for (int i = 0; i < this.getChunkRange(); i++) {
                    for (int j = 0; j < this.getChunkRange(); j++) {
                        chunks.add(world.getChunk(origin.x + i, origin.z + j));
                    }
                }
            }

            if (currentChunk.intValue() == chunks.size()) {
                setActive(false);
                return;
            }

            Chunk chunk = chunks.get(currentChunk.intValue());

            if (x.get() == Long.MAX_VALUE) {
                x.set(chunk.getPos().getXStart());
            }
            if (z.get() == Long.MAX_VALUE) {
                z.set(chunk.getPos().getZStart());
            }
            if (y.get() == Long.MAX_VALUE) {
                y.set(getPos().getY());
            }

            List<BlockPos> blockPos = IMiner.getBlockToMinePerChunk(this, x, y, z, chunk.getPos());
            blockPos.forEach(blockPos1 -> {
                NonNullList<ItemStack> itemStacks = NonNullList.create();
                IBlockState blockState = this.getWorld().getBlockState(blockPos1);
                if (!silktouch) {
                    ToolUtility.applyHammerDrops(world.rand, blockState, itemStacks, this.getFortune(), null, RecipeMaps.MACERATOR_RECIPES);
                } else {
                    itemStacks.add(new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState)));
                }
                if (addItemsToItemHandler(outputInventory, true, itemStacks)) {
                    addItemsToItemHandler(outputInventory, false, itemStacks);
                    world.destroyBlock(blockPos1, false);
                }
            });

            if (y.get() < 0) {
                currentChunk.incrementAndGet();
                if (currentChunk.get() >= chunks.size()) {
                    done = true;
                } else {
                    x.set(chunks.get(currentChunk.intValue()).getPos().getXStart());
                    z.set(chunks.get(currentChunk.intValue()).getPos().getZStart());
                    y.set(getPos().getY());
                }
            }


            if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
                pushItemsIntoNearbyHandlers(getFrontFacing());
            }
        }

    }

    @Override
    protected BlockPattern createStructurePattern() {
        return material == null ? null : FactoryBlockPattern.start()
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
        //basically check minimal requirements for inputs count
        int itemOutputsCount = abilities.getOrDefault(MultiblockAbility.EXPORT_ITEMS, Collections.emptyList())
                .stream().map(it -> (IItemHandler) it).mapToInt(IItemHandler::getSlots).sum();
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        return itemOutputsCount >= 1 &&
                fluidInputsCount >= 1 &&
                abilities.containsKey(MultiblockAbility.INPUT_ENERGY);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.miner.multi.description", this.getChunkRange(), this.getChunkRange(), fortuneString));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", this.getDrillingFluidConsumedPerTick(), I18n.format(DrillingFluid.getFluid(0).getUnlocalizedName())));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (this.isStructureFormed()) {

            textList.add(new TextComponentString(String.format("X: %d", x.get())));
            textList.add(new TextComponentString(String.format("Y: %d", y.get())));
            textList.add(new TextComponentString(String.format("Z: %d", z.get())));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.chunk", currentChunk.get()));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.nb_chunk", chunks.size()));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.block_per_tick", getNbBlock()));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.silktouch", silktouch));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.mode"));
            if (done)
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.done", getNbBlock()).setStyle(new Style().setColor(TextFormatting.GREEN)));
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
        return new MetaTileEntityLargeMiner(metaTileEntityId, getMaterial(), getTicksPerOperation(), getChunkRange(), getFortune(), getDrillingFluidConsumedPerTick());
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        this.silktouch = !silktouch;
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("xPos", new NBTTagLong(x.get()));
        data.setTag("yPos", new NBTTagLong(y.get()));
        data.setTag("zPos", new NBTTagLong(z.get()));
        data.setTag("chunk", new NBTTagInt(currentChunk.get()));
        data.setTag("done", new NBTTagInt(done ? 1 : 0));
        data.setTag("silktouch", new NBTTagInt(silktouch ? 1 : 0));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        x.set(data.getLong("xPos"));
        y.set(data.getLong("yPos"));
        z.set(data.getLong("zPos"));
        currentChunk.set(data.getInteger("chunk"));
        done = data.getInteger("done") != 0;
        silktouch = data.getInteger("silktouch") != 0;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive);
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
        }
    }

}
