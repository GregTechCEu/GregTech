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

    public abstract boolean matches(T t);

    public abstract Widget createFilterUI(UIBuildContext buildContext);

    public Widget createBlacklistButton(UIBuildContext buildContext) {
        return new CycleButtonWidget()
                .setToggle(this::isInverted, this::setInverted)
                .setTexture(GuiTextures.BUTTON_BLACKLIST)
                .setBackground(GuiTextures.BASE_BUTTON)
                .setSize(18, 18)
                .setPos(126, 0);
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setBoolean("Inverted", this.inverted);
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        this.inverted = tagCompound.getBoolean("Inverted");
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
