package gregtech.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.running.RecipeProgressOperation;
import gregtech.api.recipes.logic.workable.RecipeSteamWorkable;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public abstract class SteamMetaTileEntity extends MetaTileEntity implements IControllable,
                                          RecipeSteamWorkable.ISupportsRecipeSteamWorkable {

    protected static final int STEAM_CAPACITY = 16000;

    protected final boolean isHighPressure;
    protected final ICubeRenderer renderer;
    protected final RecipeMap<?> recipeMap;
    protected RecipeSteamWorkable workable;
    protected FluidTank steamFluidTank;
    protected @NotNull EnumFacing ventingSide = EnumFacing.UP;

    protected Deque<ItemStack> bufferedItemOutputs;
    protected Deque<FluidStack> bufferedFluidOutputs;

    public SteamMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer,
                               boolean isHighPressure) {
        super(metaTileEntityId);
        this.isHighPressure = isHighPressure;
        this.recipeMap = recipeMap;
        this.bufferedItemOutputs = new ArrayDeque<>();
        this.bufferedFluidOutputs = new ArrayDeque<>();
        RecipeStandardStateMachineBuilder std = new RecipeStandardStateMachineBuilder(this.recipeMap::getLookup);
        modifyRecipeLogicStandardBuilder(std);
        workable = new RecipeSteamWorkable(this, std);
        this.renderer = renderer;
    }

    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        builder.setOffthreadSearchAndSetup(true)
                .setItemInput(this::getImportItems)
                .setFluidInput(this::getImportFluids)
                .setProperties(this::computePropertySet)
                .setFluidOutput(bufferedFluidOutputs::addAll)
                .setFluidTrim(this::getFluidOutputLimit)
                .setItemOutput(bufferedItemOutputs::addAll)
                .setItemTrim(this::getItemOutputLimit)
                .setNotifiedFluidInputs(this::getNotifiedFluidInputList)
                .setNotifiedItemInputs(this::getNotifiedItemInputList)
                .setItemOutAmountLimit(() -> getExportItems().getSlots() * 64)
                .setItemOutStackLimit(() -> getExportItems().getSlots())
                .setFluidOutAmountLimit(() -> {
                    int sum = 0;
                    for (var e : getExportFluids().getFluidTanks()) {
                        int capacity = e.getCapacity();
                        sum += capacity;
                    }
                    return sum;
                })
                .setFluidOutStackLimit(() -> getExportFluids().getTanks())
                .setPerTickRecipeCheck((recipe) -> {
                    double progress = recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY);
                    double maxProgress = recipe.getDouble("Duration");
                    long voltage = recipe.getLong("Voltage");
                    long amperage = recipe.getLong("Amperage");
                    long eut = (long) (Math.min(1, maxProgress - progress) * voltage * amperage);
                    boolean generating = recipe.getBoolean("Generating");
                    if (!generating) {
                        FluidStack drain = steamFluidTank.drain((int) eut, true);
                        return (drain == null ? 0 : drain.amount) >= eut;
                    } else {
                        return true;
                    }
                });
        if (isHighPressure()) {
            builder.setVoltageDiscount(() -> 2);
        } else {
            builder.setDurationDiscount(() -> 2);
        }
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.supply(GTValues.V[GTValues.LV], 1);
        return set;
    }

    protected boolean insufficientSteam() {
        // if the steam container has less than a tenth of its capacity, we're probably low on steam.
        return isActive() && steamFluidTank.getFluidAmount() <= steamFluidTank.getCapacity() * 0.1;
    }

    @Override
    public boolean isActive() {
        return workable.isRunning();
    }

    @Override
    public boolean isWorkingEnabled() {
        return workable.getProgressAndComplete().isLogicEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (isWorkingAllowed != workable.getProgressAndComplete().isLogicEnabled()) {
            workable.getProgressAndComplete().setLogicEnabled(isWorkingAllowed);
            workable.getLookupAndSetup().setLogicEnabled(isWorkingAllowed);
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, b -> b.writeBoolean(isWorkingAllowed));
        }
    }

    @Override
    public boolean shouldRecipeWorkableUpdate() {
        return true;
    }

    public void setVentingSide(EnumFacing ventingSide) {
        if (this.ventingSide != ventingSide) {
            this.ventingSide = ventingSide;
            if (!getWorld().isRemote) {
                markDirty();
                writeCustomData(GregtechDataCodes.VENTING_SIDE, buf -> buf.writeByte(ventingSide.getIndex()));
            } else {
                scheduleRenderUpdate();
            }
        }
    }

    public @NotNull EnumFacing getVentingSide() {
        return ventingSide;
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.VENTING_SIDE) {
            setVentingSide(EnumFacing.VALUES[buf.readByte()]);
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            setWorkingEnabled(buf.readBoolean());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("VentingSide", getVentingSide().getIndex());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.ventingSide = EnumFacing.VALUES[data.getInteger("VentingSide")];
    }

    @Override
    public void update() {
        super.update();
        updateBufferedOutputs();
    }

    @Override
    public boolean areOutputsClogged() {
        updateBufferedOutputs();
        return !bufferedItemOutputs.isEmpty() || !bufferedFluidOutputs.isEmpty();
    }

    protected void updateBufferedOutputs() {
        while (!bufferedItemOutputs.isEmpty()) {
            ItemStack first = bufferedItemOutputs.removeFirst();
            ItemStack remainder = GTTransferUtils.insertItem(getExportItems(), first, false);
            if (!remainder.isEmpty() && !canVoidRecipeItemOutputs()) {
                bufferedItemOutputs.addFirst(remainder);
                break;
            }
        }
        while (!bufferedFluidOutputs.isEmpty()) {
            FluidStack first = bufferedFluidOutputs.peekFirst();
            first.amount -= getExportFluids().fill(first, true);
            if (first.amount <= 0 || canVoidRecipeFluidOutputs()) {
                bufferedFluidOutputs.removeFirst();
            } else {
                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        if (isHighPressure) {
            if (isBrickedCasing()) {
                return Textures.STEAM_BRICKED_CASING_STEEL;
            } else {
                return Textures.STEAM_CASING_STEEL;
            }
        } else {
            if (isBrickedCasing()) {
                return Textures.STEAM_BRICKED_CASING_BRONZE;
            } else {
                return Textures.STEAM_CASING_BRONZE;
            }
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            EnumFacing currentVentingSide = getVentingSide();
            if (currentVentingSide == facing ||
                    getFrontFacing() == facing)
                return false;
            setVentingSide(facing);
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getBaseRenderer().render(renderState, translation, colouredPipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                isWorkingEnabled());
        Textures.STEAM_VENT_OVERLAY.renderSided(getVentingSide(), renderState,
                RenderUtil.adjustTrans(translation, getVentingSide(), 2), pipeline);
    }

    protected boolean isBrickedCasing() {
        return false;
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        this.steamFluidTank = new FilteredFluidHandler(STEAM_CAPACITY).setFilter(CommonFluidFilters.STEAM);
        return new FluidTankList(false, steamFluidTank);
    }

    public ModularUI.Builder createUITemplate(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(isHighPressure), 176, 166)
                .label(6, 6, getMetaFullName()).shouldColor(false)
                .widget(new ImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_STEAM.get(isHighPressure))
                        .setPredicate(this::insufficientSteam))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT_STEAM.get(isHighPressure), 0);
    }

    public RecipeMap<?> getRecipeMap() {
        return recipeMap;
    }

    @Override
    public SoundEvent getSound() {
        return Objects.requireNonNull(getRecipeMap()).getSound();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            EnumParticleTypes smokeParticle = isHighPressure ? EnumParticleTypes.SMOKE_LARGE :
                    EnumParticleTypes.SMOKE_NORMAL;
            VanillaParticleEffects.defaultFrontEffect(this, smokeParticle, EnumParticleTypes.FLAME);

            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public @NotNull Collection<FacingPos> getVentingBlockFacings() {
        return Collections.singleton(new FacingPos(getPos(), getVentingSide()));
    }

    public boolean isHighPressure() {
        return isHighPressure;
    }

    @Override
    public float getVentingDamage() {
        return isHighPressure() ? 12.0f : 6.0f;
    }
}
