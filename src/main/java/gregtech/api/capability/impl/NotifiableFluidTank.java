package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredHandler;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NotifiableFluidTank extends FluidTank implements INotifiableHandler, IFilteredHandler.FluidHandler {

    List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;
    private IFilter<FluidStack> filter;

    public NotifiableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity);
        this.notifiableEntities.add(entityToNotify);
        this.isExport = isExport;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                addToNotifiedList(metaTileEntity, this, isExport);
            }
        }
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    @Nullable
    @Override
    public IFilter<FluidStack> getFilter() {
        return this.filter;
    }

    /**
     * Set filter instance. If {@code null} is given, then the filter is set to be
     *
     * @param filter new filter instance
     * @return this
     */
    @NotNull
    public NotifiableFluidTank setFilter(@Nullable IFilter<FluidStack> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return canFill() && (this.filter == null || this.filter.test(fluid));
    }
}
