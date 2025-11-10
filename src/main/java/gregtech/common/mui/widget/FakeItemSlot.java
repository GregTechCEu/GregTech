package gregtech.common.mui.widget;

import gregtech.client.utils.RenderUtil;
import gregtech.integration.jei.JustEnoughItemsModule;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FakeItemSlot extends Widget<FakeItemSlot>
                          implements Interactable, RecipeViewerGhostIngredientSlot<ItemStack>,
                          RecipeViewerIngredientProvider {

    private final boolean receiveFromRecipeViewer;

    private FakeItemSlotSyncHandler syncHandler;
    private BooleanSupplier showTooltip = () -> true;
    private BooleanSupplier showAmount = () -> true;

    public FakeItemSlot(boolean receiveFromRecipeViewer) {
        this.receiveFromRecipeViewer = receiveFromRecipeViewer;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof FakeItemSlotSyncHandler;
    }

    public FakeItemSlot item(@NotNull ItemStack itemStack) {
        return item(() -> itemStack);
    }

    public FakeItemSlot item(@NotNull Supplier<@NotNull ItemStack> itemStackSupplier) {
        if (this.syncHandler == null) {
            this.syncHandler = new FakeItemSlotSyncHandler();
            setSyncHandler(syncHandler);
        }

        this.syncHandler.setItemStackSupplier(itemStackSupplier);
        return this;
    }

    public FakeItemSlot slot(@NotNull IItemHandler itemHandler, int index) {
        return item(() -> itemHandler.getStackInSlot(index));
    }

    public FakeItemSlot receiveItemFromClient(@NotNull Consumer<@NotNull ItemStack> itemStackConsumer) {
        if (this.syncHandler == null) {
            this.syncHandler = new FakeItemSlotSyncHandler();
            setSyncHandler(syncHandler);
        }

        this.syncHandler.setItemStackConsumer(itemStackConsumer);
        return this;
    }

    @Override
    public @NotNull FakeItemSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }

        return this.syncHandler;
    }

    @Override
    public void onInit() {
        tooltip().setAutoUpdate(true);
        tooltip().tooltipBuilder(rt -> {
            if (!isSynced()) return;
            ItemStack stack = getSyncHandler().getStack();
            if (stack.isEmpty()) return;
            rt.addFromItem(stack);
        });

        if (receiveFromRecipeViewer) {
            getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
        }
    }

    public FakeItemSlot showTooltip(boolean showTooltip) {
        return showTooltip(() -> showTooltip);
    }

    public FakeItemSlot showTooltip(BooleanSupplier showTooltip) {
        this.showTooltip = showTooltip;
        return getThis();
    }

    public FakeItemSlot showAmount(boolean showAmount) {
        return showAmount(() -> showAmount);
    }

    public FakeItemSlot showAmount(BooleanSupplier showAmount) {
        this.showAmount = showAmount;
        return getThis();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.syncHandler == null) return;

        RenderUtil.drawItemStack(getSyncHandler().getStack(), 1, 1, showAmount.getAsBoolean());
        RenderUtil.handleJEIGhostSlotOverlay(this, widgetTheme);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (showTooltip.getAsBoolean() && tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), getSyncHandler().getStack());
        }
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.ITEM_SLOT);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        getSyncHandler().setStackFromCursor();
        return Result.ACCEPT;
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        if (receiveFromRecipeViewer) {
            getSyncHandler().setStack(ingredient);
        } else {
            throw new IllegalStateException(
                    "setGhostIngredient was called on a FakeItemSlot that had receiveFromRecipeViewer false");
        }
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return receiveFromRecipeViewer ? JustEnoughItemsModule.ingredientRegistry
                .getIngredientHelper(ingredient)
                .getCheatItemStack(ingredient) : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return getSyncHandler().getStack();
    }

    public static class FakeItemSlotSyncHandler extends SyncHandler {

        private static final int ITEM_CHANGED = 0;
        private static final int ITEM_SET_FROM_CLIENT = 1;
        private static final int ITEM_SET_TO_CURSOR = 2;

        @Nullable
        private Supplier<@NotNull ItemStack> itemStackSupplier;
        @Nullable
        private Consumer<@NotNull ItemStack> itemStackConsumer;

        @NotNull
        private ItemStack cachedStack = ItemStack.EMPTY;

        public void setItemStackSupplier(@Nullable Supplier<@NotNull ItemStack> itemStackSupplier) {
            this.itemStackSupplier = itemStackSupplier;
        }

        public void setItemStackConsumer(@Nullable Consumer<@NotNull ItemStack> itemStackConsumer) {
            this.itemStackConsumer = itemStackConsumer;
        }

        public @NotNull ItemStack getStack() {
            return cachedStack;
        }

        public void setStack(@NotNull ItemStack itemStack) {
            syncToServer(ITEM_SET_FROM_CLIENT, buf -> buf.writeItemStack(itemStack));
        }

        public void setStackFromCursor() {
            syncToServer(ITEM_SET_TO_CURSOR);
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            if (itemStackSupplier == null) {
                return;
            }

            ItemStack sourceStack = itemStackSupplier.get();
            boolean itemsEqual = cachedStack.isItemEqual(sourceStack);
            if (itemsEqual) {
                NBTTagCompound sourceStackNBT = sourceStack.getTagCompound();
                NBTTagCompound cachedStackNBT = cachedStack.getTagCompound();
                if (sourceStackNBT == null) {
                    itemsEqual = cachedStackNBT == null;
                } else {
                    if (cachedStackNBT != null) {
                        itemsEqual = cachedStackNBT.equals(sourceStack.getTagCompound());
                    } else {
                        itemsEqual = false;
                    }
                }
            }

            if (init || !itemsEqual) {
                cachedStack = sourceStack.copy();
                syncToClient(ITEM_CHANGED, buf -> buf.writeItemStack(cachedStack));
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {
            if (id == ITEM_CHANGED) {
                cachedStack = buf.readItemStack();
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {
            ItemStack itemStack = switch (id) {
                case ITEM_SET_FROM_CLIENT -> buf.readItemStack();
                case ITEM_SET_TO_CURSOR -> getSyncManager().getCursorItem();
                default -> ItemStack.EMPTY;
            };

            if (itemStackConsumer != null) {
                itemStackConsumer.accept(itemStack);
            }
        }
    }
}
