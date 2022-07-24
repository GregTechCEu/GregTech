package gregtech.common.covers.filter;

import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

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
     * @param object              by default an instance of T, but can be different fe. in {@link gregtech.common.covers.filter.item.ItemFilter}
     * @param globalTransferLimit transferLimit of the holder
     * @return transfer limit
     */
    public abstract int getTransferLimit(Object object, int globalTransferLimit);

    /**
     * Creates the filter ui. Typically a {@link com.cleanroommc.modularui.common.widget.MultiChildWidget}. <br>
     * Ideally you manually set the size of the resulting widgets, so that covers can auto size themselves properly.
     *
     * @param player player who opened the ui
     * @return the non null filter ui widget
     */
    @Nonnull
    public abstract Widget createFilterUI(EntityPlayer player);

    public Widget createBlacklistButton(EntityPlayer player) {
        return new CycleButtonWidget()
                .setToggle(this::isInverted, this::setInverted)
                .setTexture(GuiTextures.BUTTON_BLACKLIST)
                .setBackground(GuiTextures.BASE_BUTTON)
                .setSize(18, 18)
                .setPos(121, 0);
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
