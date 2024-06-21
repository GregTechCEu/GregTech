package gregtech.common.covers.ender;

import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.virtualregistry.VirtualEntry;
import gregtech.common.covers.CoverPump;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.layout.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.regex.Pattern;

public abstract class CoverAbstractEnderLink<T extends VirtualEntry> extends CoverBase
                                            implements CoverWithUI, ITickable, IControllable {

    private static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("[0-9a-fA-F]*");

    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.IMPORT;

    private UUID playerUUID = null;
    private boolean isPrivate = false;
    private boolean workingEnabled = true;
    private boolean ioEnabled = false;

    public CoverAbstractEnderLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                  @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    protected abstract T getEntry();

    protected abstract void setEntry(T entry);

    protected abstract void updateLink();

    protected final String createName() {
        return identifier() + getEntry().getColor();
    }

    protected abstract String identifier();

    protected final UUID getOwner() {
        return isPrivate ? playerUUID : null;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        if (player != null) {
            this.playerUUID = player.getUniqueID();
        }
    }

    public void updateColor(String str) {
        if (str.length() == 8) {
            getEntry().setColor(str);
        } else {
            getEntry().setColor(null);
        }
        updateLink();
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176, 192);

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createWidgets(panel, guiSyncManager))
                .bindPlayerInventory();
    }

    protected abstract Column createWidgets(ModularPanel panel, GuiSyncManager guiSyncManager);

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workingEnabled = isActivationAllowed;
    }

    private boolean isIoEnabled() {
        return ioEnabled;
    }

    private void setIoEnabled(boolean ioEnabled) {
        this.ioEnabled = ioEnabled;
    }

    private boolean isPrivate() {
        return isPrivate;
    }

    private void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateLink();
    }
}
