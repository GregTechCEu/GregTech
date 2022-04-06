package gregtech.common.covers.newFilter;

import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.nbt.NBTTagCompound;

public abstract class Filter<T> {

    private IDirtyNotifiable dirtyNotifiable;
    private boolean inverted = false;

    public abstract boolean matches(T t, boolean ignoreInverted);

    public boolean matches(T t) {
        return matches(t, false);
    }

    /**
     * Determines how much can transferred based on t.
     *
     * @param object              by default an instance of T, but can be different fe. in {@link gregtech.common.covers.newFilter.item.ItemFilter}
     * @param globalTransferLimit transferLimit of the holder
     * @return transfer limit
     */
    public abstract int getTransferLimit(Object object, int globalTransferLimit);

    public abstract Widget createFilterUI(UIBuildContext buildContext);

    public Widget createBlacklistButton(UIBuildContext buildContext) {
        return new CycleButtonWidget()
                .setToggle(this::isInverted, this::setInverted)
                .setTexture(GuiTextures.BUTTON_BLACKLIST)
                .setBackground(GuiTextures.BASE_BUTTON)
                .setSize(18, 18)
                .setPos(126, 0);
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("Inverted", this.inverted);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.inverted = nbt.getBoolean("Inverted");
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }
}
