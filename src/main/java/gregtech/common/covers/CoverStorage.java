package gregtech.common.covers;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoverStorage extends CoverBase implements CoverWithUI {

    private final ItemStackHandler storageHandler = new ItemStackHandler(9);
    private static final int MAX_WIDTH = 176;
    private static final int MAX_HEIGHT = 126;
    private static final int SLOT_SIZE = 18;

    public CoverStorage(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                        @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.STORAGE.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(storageHandler);
    }

    @Override
    public @NotNull EnumActionResult onRightClick(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                                  @NotNull CuboidRayTraceResult hitResult) {
        if (!getCoverableView().getWorld().isRemote) {
            openUI((EntityPlayerMP) player);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) player);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        guiSyncManager.registerSlotGroup("item_inv", this.storageHandler.getSlots());

        int rowSize = this.storageHandler.getSlots();
        List<List<IWidget>> widgets = new ArrayList<>();
        widgets.add(new ArrayList<>());
        for (int i = 0; i < rowSize; i++) {
            widgets.get(0)
                    .add(new ItemSlot().slot(SyncHandlers.itemSlot(this.storageHandler, i).slotGroup("item_inv")));
        }
        return GTGuis.createPanel(this, MAX_WIDTH, MAX_HEIGHT)
                .child(IKey.lang("cover.storage.title").asWidget().pos(5, 5))
                .bindPlayerInventory()
                .child(new Grid()
                        .top((MAX_HEIGHT - SLOT_SIZE * 5) / 2).left(7).right(7).height(18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(widgets));
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("Storage", this.storageHandler.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.storageHandler.deserializeNBT(tagCompound.getCompoundTag("Storage"));
    }
}
