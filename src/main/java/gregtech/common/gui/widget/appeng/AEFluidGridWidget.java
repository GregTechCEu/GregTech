package gregtech.common.gui.widget.appeng;

import gregtech.api.gui.Widget;
import gregtech.common.gui.widget.appeng.slot.AEFluidDisplayWidget;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.FluidList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @Author GlodBlock
 * @Description Display fluid list
 * @Date 2023/4/19-0:28
 */
public class AEFluidGridWidget extends AEListGridWidget<IAEFluidStack> {

    private final Object2LongMap<IAEFluidStack> changeMap = new Object2LongOpenHashMap<>();
    protected final IItemList<IAEFluidStack> cached = new FluidList();
    protected final IItemList<IAEFluidStack> displayList = new FluidList();

    public AEFluidGridWidget(int x, int y, int slotsY, IItemList<IAEFluidStack> internalList) {
        super(x, y, slotsY, internalList);
    }

    @Override
    public IAEFluidStack getAt(int index) {
        int cnt = 0;
        for (IAEFluidStack fluid : this.displayList) {
            if (cnt == index) {
                return fluid;
            }
            cnt++;
        }
        return null;
    }

    @Override
    protected void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = this.widgets.size();
            Widget widget = new AEFluidDisplayWidget(0, 0, this, widgetAmount);
            this.addWidget(widget);
        }
    }

    @Override
    protected void writeListChange() {
        this.changeMap.clear();
        // Remove fluid
        for (IAEFluidStack fluid : this.cached) {
            if (this.list.findPrecise(fluid) == null || this.list.findPrecise(fluid).getStackSize() == 0) {
                this.changeMap.put(fluid.copy(), -fluid.getStackSize());
                fluid.reset();
            }
        }
        // Change/Add fluid
        for (IAEFluidStack fluid : this.list) {
            IAEFluidStack cachedFluid = this.cached.findPrecise(fluid);
            if (cachedFluid == null || cachedFluid.getStackSize() == 0) {
                this.changeMap.put(fluid.copy(), fluid.getStackSize());
                this.cached.add(fluid.copy());
            } else {
                if (cachedFluid.getStackSize() != fluid.getStackSize()) {
                    this.changeMap.put(fluid.copy(), fluid.getStackSize() - cachedFluid.getStackSize());
                    this.cached.add(fluid.copy().setStackSize(fluid.getStackSize() - cachedFluid.getStackSize()));
                }
            }
        }
        this.writeUpdateInfo(CONTENT_CHANGE_ID, buf -> {
            buf.writeVarInt(this.changeMap.size());
            for (IAEFluidStack fluid : this.changeMap.keySet()) {
                buf.writeString(fluid.getFluid().getName());
                buf.writeVarLong(this.changeMap.get(fluid));
            }
        });
    }

    @Override
    protected void readListChange(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            FluidStack fluid = FluidRegistry.getFluidStack(buffer.readString(Integer.MAX_VALUE / 16), 1);
            long delta = buffer.readVarLong();
            if (fluid != null) {
                this.displayList.add(AEFluidStack.fromFluidStack(fluid).setStackSize(delta));
            }
        }
    }
}
