package gregtech.common.covers.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;

import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class FluidFilter implements Filter<FluidStack> {

    private IDirtyNotifiable dirtyNotifiable;
    boolean showTip;

    private BaseFluidFilterReader filterReader;

    protected void setFilterReader(BaseFluidFilterReader filterReader) {
        this.filterReader = filterReader;
    }

    private OnMatch<FluidStack> onMatch = null;

    public abstract void match(FluidStack toMatch);

    public abstract boolean test(FluidStack fluidStack);

    @Override
    public final void setOnMatched(OnMatch<FluidStack> onMatch) {
        this.onMatch = onMatch;
    }

    protected final void onMatch(boolean matched, FluidStack stack, int index) {
        if (this.onMatch != null) this.onMatch.onMatch(matched, stack, index);
    }

    public abstract int getFluidTransferLimit(FluidStack fluidStack);

    @Deprecated
    public abstract void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup);

    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
        var blacklist = new BooleanSyncValue(this.filterReader::isBlacklistFilter, this.filterReader::setBlacklistFilter);
        return new ParentWidget<>().coverChildren()
                .child(new CycleButtonWidget()
                        .value(blacklist)
                        .textureGetter(state -> GTGuiTextures.BUTTON_BLACKLIST[state])
                        .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                        .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")));
    }

    public abstract ItemStack getContainerStack();

    public abstract void readFromNBT(NBTTagCompound tagCompound);

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public abstract void configureFilterTanks(int amount);

    public abstract void setMaxConfigurableFluidSize(int maxStackSize);

    public boolean isBlacklist() {
        return this.filterReader.isBlacklistFilter();
    }

    public void setBlacklistFilter(boolean blacklist) {
        this.filterReader.setBlacklistFilter(blacklist);
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }

    protected abstract static class BaseFluidFilterReader extends BaseFilterReader {

        protected static final String KEY_FLUIDS = "FluidTank";

        public BaseFluidFilterReader(ItemStack container, int slots) {
            super(container, slots);
        }

        @Override
        public NBTTagList getItemsNbt() {
            NBTTagCompound nbt = getStackTag();
            if (!nbt.hasKey(KEY_FLUIDS)) {
                NBTTagList list = new NBTTagList();
                for (int i = 0; i < getSlots(); i++) {
                    list.appendTag(new NBTTagCompound());
                }
                nbt.setTag(KEY_FLUIDS, list);
            }
            return nbt.getTagList(KEY_FLUIDS, Constants.NBT.TAG_COMPOUND);
        }

        public FluidStack getFluidStack(int i) {
            return getFluidTank(i).getFluid();
        }

        public abstract IFluidTank getFluidTank(int i);

        @Override
        public void onMaxStackSizeChange() {

        }
    }
}
