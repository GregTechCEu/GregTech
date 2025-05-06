package gregtech.common.metatileentities.multi.fission;

import gregtech.api.capability.*;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IFissionReactor;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.category.ICategoryOverride;
import gregtech.api.recipes.properties.impl.FissionCoolantProperty;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityFissionCoolantHatch extends MetaTileEntityMultiblockPart
                                               implements IMultiblockAbilityPart<IFissionRodPort>, IFissionRodPort,
                                               ICategoryOverride, IControllable {

    protected final FluidTank in;
    protected final FluidTank out;

    protected final IFissionRodPort.RodType type;

    protected boolean dry;

    protected boolean workingEnabled = true;

    public MetaTileEntityFissionCoolantHatch(ResourceLocation metaTileEntityId, int tier,
                                             IFissionRodPort.RodType type) {
        super(metaTileEntityId, tier);
        in = new FluidTank(type.getCoolingParallelsPer1000K() * 1000);
        out = new FluidTank(type.getCoolingParallelsPer1000K() * 1000);
        this.type = type;
        initializeInventory();
    }

    @Override
    public @NotNull RodType getRodType() {
        return type;
    }

    public static boolean onReactorTick(@NotNull IFissionReactor reactor, @NotNull IFissionRodPort port,
                                        @NotNull IFissionRodPort opposingPort) {
        if (port instanceof MetaTileEntityFissionCoolantHatch a &&
                opposingPort instanceof MetaTileEntityFissionCoolantHatch b) {
            if (a.doCooling(reactor, b) || b.doCooling(reactor, a)) {
                a.dry = false;
                b.dry = false;
                return true;
            }
            a.dry = true;
            b.dry = true;
        }
        return false;
    }

    private boolean doCooling(@NotNull IFissionReactor reactor, @NotNull MetaTileEntityFissionCoolantHatch other) {
        FluidStack drain = this.in.drain(Integer.MAX_VALUE, false);
        if (drain == null) return false;
        Recipe r = RecipeMaps.FISSION_COOLANT_RECIPES.findRecipe(1, Collections.emptyList(),
                Collections.singletonList(drain));
        if (r == null) return false;
        FissionCoolantProperty.FissionCoolantValues values = r.getProperty(FissionCoolantProperty.getInstance(), null);
        if (values == null) return false;
        int temp = reactor.getTemperature();
        if (temp < values.getMinimumTemperature() || (temp > values.getCutoffTemperature() && this.dry)) return false;
        int parallel = 1 + (type.getCoolingParallelsPer1000K() * (temp - values.getMinimumTemperature()) / 1000);
        int i = r.getFluidInputs().get(0).getAmount();
        if (drain.amount / i < parallel) return false;
        FluidStack out = null;
        if (!r.getFluidOutputs().isEmpty()) {
            out = r.getFluidOutputs().get(0);
            i = out.amount;
            out = out.copy();
            out.amount *= parallel;
            if (other.out.fill(out, false) / i < parallel) return false;
            out.amount = i;
        }
        reactor.applyHeat(-values.getHeatEquivalentPerOperation());
        this.in.drain(i * parallel, true);
        if (out != null) {
            out.amount *= parallel;
            other.out.fill(out, true);
        }
        return true;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionCoolantHatch(metaTileEntityId, getTier(), type);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation,
                    pipeline);
            Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, in);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, out);
    }

    @Override
    public @Nullable MultiblockAbility<IFissionRodPort> getAbility() {
        return MultiblockAbility.FISSION_ROD_PORT;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        type.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public @NotNull RecipeMap<?> @NotNull [] getJEIRecipeMapCategoryOverrides() {
        return new RecipeMap[] { RecipeMaps.FISSION_COOLANT_RECIPES };
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (workingEnabled) {
                pushFluidsIntoNearbyHandlers(getFrontFacing().getOpposite());
                pullFluidsFromNearbyHandlers(getFrontFacing());
            }
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("workingEnabled", workingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("workingEnabled")) {
            this.workingEnabled = data.getBoolean("workingEnabled");
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }
}
