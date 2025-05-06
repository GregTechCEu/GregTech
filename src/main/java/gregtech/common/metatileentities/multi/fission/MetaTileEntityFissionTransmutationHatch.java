package gregtech.common.metatileentities.multi.fission;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IFissionRodPort;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IFissionReactor;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
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

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class MetaTileEntityFissionTransmutationHatch extends MetaTileEntityMultiblockPart
                                      implements IMultiblockAbilityPart<IFissionRodPort>, IFissionRodPort,
                                                 IControllable {

    public static final int AVERAGE_TICKS_PER_TRANSMUTATION = 20*60*5; // 5 minutes

    protected final @Nullable GTItemStackHandler inItem;
    protected final @Nullable FluidTank inFluid;
    protected final @Nullable FluidTank out;

    protected final @Nullable Supplier<ItemStack> moderatorItem;
    protected final @Nullable FluidStack moderatorFluid;
    protected final @Nullable FluidStack transmute;

    protected boolean operational;
    protected final RodType type;

    protected boolean workingEnabled = true;

    public MetaTileEntityFissionTransmutationHatch(ResourceLocation metaTileEntityId, int tier, RodType type,
                                                   @Nullable Supplier<ItemStack> moderatorItem,
                                                   @Nullable FluidStack moderatorFluid,
                                                   @Nullable FluidStack transmute) {
        super(metaTileEntityId, tier);
        if ((moderatorItem == null) == (moderatorFluid == null))
            throw new IllegalArgumentException("Transmutation hatches must consume either an item or a fluid to operate.");
        if (moderatorItem != null) {
            inItem = new GTItemStackHandler(this, 1) {

                @Override
                public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                    return ItemStack.areItemStacksEqual(stack, moderatorItem.get());
                }
            };
        } else inItem = null;
        if (moderatorFluid != null) {
            inFluid = new FluidTank(moderatorFluid.amount * 1000) {

                @Override
                public boolean canFillFluidType(FluidStack fluid) {
                    return fluid.isFluidEqual(moderatorFluid);
                }
            };
        } else inFluid = null;
        if (transmute != null) {
            out = new FluidTank(transmute.amount * 1000);
        } else out = null;
        this.moderatorItem = moderatorItem;
        this.moderatorFluid = moderatorFluid;
        this.transmute = transmute;
        this.type = type;
        initializeInventory();
    }

    @Override
    public @NotNull RodType getRodType() {
        return type;
    }

    public static void onReactorTick(@NotNull IFissionReactor reactor, @NotNull IFissionRodPort port,
                                     @NotNull IFissionRodPort opposingPort) {
        if (port instanceof MetaTileEntityFissionTransmutationHatch a &&
                opposingPort instanceof MetaTileEntityFissionTransmutationHatch b) {
            if (reactor.isActive() && Math.random() * AVERAGE_TICKS_PER_TRANSMUTATION < 1) {
                if (!a.doTransmutation(reactor, b)) {
                    b.doTransmutation(reactor, a);
                }
            } else if (Math.random() * 20 > 1) return;
            boolean val = a.operational;
            if (isOperational(reactor, port, opposingPort) != val) {
                reactor.recomputeRodStats();
            }
        }
    }

    public static boolean isOperational(@NotNull IFissionReactor reactor, @NotNull IFissionRodPort port,
                                        @NotNull IFissionRodPort opposingPort) {
        if (port instanceof MetaTileEntityFissionTransmutationHatch a && opposingPort instanceof MetaTileEntityFissionTransmutationHatch b) {
            boolean ret = a.isOperational() || b.isOperational();
            a.operational = ret;
            b.operational = ret;
            return ret;
        }
        return false;
    }

    private boolean isOperational() {
        if (moderatorFluid != null) {
            assert inFluid != null;
            FluidStack f = inFluid.drain(moderatorFluid, false);
            if (f != null && f.amount == moderatorFluid.amount) return true;
        }
        if (moderatorItem != null) {
            assert inItem != null;
            ItemStack stack = moderatorItem.get();
            if (inItem.extractItem(0, stack.getCount(), true).getCount() == stack.getCount()) return true;
        }
        return false;
    }

    private boolean doTransmutation(@NotNull IFissionReactor reactor, @NotNull MetaTileEntityFissionTransmutationHatch other) {
        if (moderatorFluid != null) {
            assert inFluid != null;
            FluidStack drain = inFluid.drain(moderatorFluid, false);
            if (drain != null && drain.amount == moderatorFluid.amount) {
                inFluid.drain(moderatorFluid, true);
                if (transmute != null) {
                    assert other.out != null;
                    other.out.fill(transmute, true);
                }
                return true;
            }
        }
        if (moderatorItem != null) {
            assert inItem != null;
            ItemStack stack = moderatorItem.get();
            if (inItem.extractItem(0, stack.getCount(), true).getCount() == stack.getCount()) {
                inItem.extractItem(0, stack.getCount(), false);
                if (transmute != null) {
                    assert other.out != null;
                    other.out.fill(transmute, true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionTransmutationHatch(metaTileEntityId, getTier(), type, moderatorItem, moderatorFluid, transmute);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
            if (inFluid != null) {
                Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
            if (inItem != null) {
                Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return inItem == null ? super.createImportItemHandler() : inItem;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, inFluid);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (out == null) return super.createExportFluidHandler();
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
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (workingEnabled) {
                pushFluidsIntoNearbyHandlers(getFrontFacing().getOpposite());
                if (moderatorFluid != null) pullFluidsFromNearbyHandlers(getFrontFacing());
                if (moderatorItem != null) pullItemsFromNearbyHandlers(getFrontFacing());
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
