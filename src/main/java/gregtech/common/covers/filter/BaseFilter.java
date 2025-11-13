package gregtech.common.covers.filter;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import org.jetbrains.annotations.NotNull;

public abstract class BaseFilter implements IFilter {

    public static final BaseFilter ERROR_FILTER = new BaseFilter() {

        private final BaseFilterReader filterReader = new BaseFilterReader(ItemStack.EMPTY, 0);

        @Override
        public BaseFilterReader getFilterReader() {
            return this.filterReader;
        }

        @Override
        public @NotNull ModularPanel createPopupPanel(PanelSyncManager syncManager) {
            return GTGuis.createPopupPanel("error", 100, 100)
                    .child(createWidgets(syncManager));
        }

        @Override
        public @NotNull ModularPanel createPanel(PanelSyncManager syncManager) {
            return GTGuis.createPanel("error", 100, 100)
                    .child(createWidgets(syncManager));
        }

        @Override
        public @NotNull Widget<?> createWidgets(PanelSyncManager syncManager) {
            return IKey.lang("INVALID FILTER").alignment(Alignment.Center).asWidget();
        }

        @Override
        public FilterType getType() {
            return FilterType.ITEM;
        }
    };
    protected IDirtyNotifiable dirtyNotifiable;

    public abstract BaseFilterReader getFilterReader();

    public final ItemStack getContainerStack() {
        return this.getFilterReader().getContainer();
    }

    public static @NotNull BaseFilter getFilterFromStack(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem<?>metaItem) {
            var metaValueItem = metaItem.getItem(stack);
            var factory = metaValueItem == null ? null : metaValueItem.getFilterFactory();
            if (factory != null)
                return factory.create(stack);
        }
        return ERROR_FILTER;
    }

    public final void setBlacklistFilter(boolean blacklistFilter) {
        this.getFilterReader().setBlacklistFilter(blacklistFilter);
        markDirty();
    }

    @Override
    public final MatchResult match(Object toMatch) {
        if (toMatch instanceof ItemStack stack) {
            return matchItem(stack);
        } else if (toMatch instanceof FluidStack stack) {
            return matchFluid(stack);
        }
        return MatchResult.NONE;
    }

    public MatchResult matchFluid(FluidStack fluidStack) {
        return MatchResult.NONE;
    }

    public MatchResult matchItem(ItemStack itemStack) {
        return MatchResult.NONE;
    }

    @Override
    public final boolean test(Object toTest) {
        boolean b = false;
        if (toTest instanceof ItemStack stack) {
            b = testItem(stack);
        } else if (toTest instanceof FluidStack stack) {
            b = testFluid(stack);
        }
        return b != isBlacklistFilter();
    }

    public boolean testFluid(FluidStack toTest) {
        return false;
    }

    public boolean testItem(ItemStack toTest) {
        return false;
    }

    @Override
    public final int getTransferLimit(Object o, int transferSize) {
        if (o instanceof ItemStack stack) {
            return getTransferLimit(stack, transferSize);
        } else if (o instanceof FluidStack stack) {
            return getTransferLimit(stack, transferSize);
        }
        return 0;
    }

    public int getTransferLimit(FluidStack stack, int transferSize) {
        return 0;
    }

    public int getTransferLimit(ItemStack stack, int transferSize) {
        return 0;
    }

    public final boolean isBlacklistFilter() {
        return getFilterReader().isBlacklistFilter();
    }

    public IWidget createBlacklistUI() {
        return new ParentWidget<>().coverChildren()
                .child(new CycleButtonWidget()
                        .value(new BooleanSyncValue(
                                this::isBlacklistFilter,
                                this::setBlacklistFilter))
                        .stateBackground(0, GTGuiTextures.BUTTON_BLACKLIST[0])
                        .stateBackground(1, GTGuiTextures.BUTTON_BLACKLIST[1])
                        .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                        .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")));
    }

    public final int getMaxTransferSize() {
        return this.getFilterReader().getMaxTransferRate();
    }

    public final void setMaxTransferSize(int maxStackSize) {
        this.getFilterReader().setMaxTransferRate(maxStackSize);
    }

    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter();
    }

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
        this.getFilterReader().setDirtyNotifiable(dirtyNotifiable);
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }

    public void readFromNBT(NBTTagCompound tag) {
        this.getFilterReader().deserializeNBT(tag);
        markDirty();
    }

    public void writeInitialSyncData(PacketBuffer packetBuffer) {}

    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {}
}
