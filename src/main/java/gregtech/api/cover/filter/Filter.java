package gregtech.api.cover.filter;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import gregtech.api.newgui.GuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

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
     * Creates the filter ui.
     * Ideally you manually set the size of the resulting widgets, so that covers can auto size themselves properly.
     *
     * @return the non-null filter ui widget
     */
    @NotNull
    public abstract IWidget createFilterUI(ModularPanel mainPanel, GuiSyncManager syncManager);

    public IWidget createBlacklistButton(ModularPanel mainPanel, GuiSyncManager syncManager) {
        return new CycleButtonWidget()
                .value(new BooleanSyncValue(this::isInverted, this::setInverted))
                .texture(GuiTextures.BUTTON_BLACKLIST)
                .background(GuiTextures.BUTTON)
                .size(18, 18)
                .pos(121, 0);
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
