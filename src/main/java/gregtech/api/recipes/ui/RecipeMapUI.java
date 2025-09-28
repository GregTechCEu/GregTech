package gregtech.api.recipes.ui;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.widget.RecipeProgressWidget;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.mui.widget.GTFluidSlot;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

@ApiStatus.Experimental
public class RecipeMapUI<R extends RecipeMap<?>> {

    private final R recipeMap;
    private final boolean modifyItemInputs;
    private final boolean modifyItemOutputs;
    private final boolean modifyFluidInputs;
    private final boolean modifyFluidOutputs;

    private final boolean isGenerator;

    private @NotNull Area specialTexturePosition = new Area();
    private boolean isJEIVisible = true;

    /* *********************** MUI 1 *********************** */

    @Deprecated
    private final Byte2ObjectMap<TextureArea> slotOverlays = new Byte2ObjectOpenHashMap<>();

    @Deprecated
    private TextureArea progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
    @Deprecated
    private gregtech.api.gui.widgets.ProgressWidget.MoveType moveType = gregtech.api.gui.widgets.ProgressWidget.MoveType.HORIZONTAL;
    @Deprecated
    private @Nullable TextureArea specialTexture;

    /* *********************** MUI 2 *********************** */

    private final Byte2ObjectMap<Int2ObjectMap<IDrawable>> overlays = new Byte2ObjectArrayMap<>(4);

    @ApiStatus.Experimental
    private boolean usesMui2 = false;
    private UITexture progressTexture = GTGuiTextures.PROGRESS_BAR_ARROW;
    private ProgressWidget.Direction progressDirection = ProgressWidget.Direction.RIGHT;
    private @Nullable IDrawable specialDrawableTexture;

    /**
     * @param recipeMap          the recipemap corresponding to this ui
     * @param modifyItemInputs   if item input amounts can be modified
     * @param modifyItemOutputs  if item output amounts can be modified
     * @param modifyFluidInputs  if fluid input amounts can be modified
     * @param modifyFluidOutputs if fluid output amounts can be modified
     */
    public RecipeMapUI(@NotNull R recipeMap, boolean modifyItemInputs, boolean modifyItemOutputs,
                       boolean modifyFluidInputs, boolean modifyFluidOutputs, boolean isGenerator) {
        this.recipeMap = recipeMap;
        this.modifyItemInputs = modifyItemInputs;
        this.modifyItemOutputs = modifyItemOutputs;
        this.modifyFluidInputs = modifyFluidInputs;
        this.modifyFluidOutputs = modifyFluidOutputs;
        this.isGenerator = isGenerator;
    }

    /**
     * Compute the storage key for slot overlays.
     *
     * @param isOutput if the slot is an output slot
     * @param isFluid  if the slot is a fluid slot
     * @param isLast   if the slot is the last slot of its type
     * @return the key
     */
    @ApiStatus.Internal
    public static byte computeOverlayKey(boolean isOutput, boolean isFluid, boolean isLast) {
        return (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
    }

    /**
     * Determines the slot grid for an item input amount
     *
     * @param itemInputsCount the item input amount
     * @return [slots to the left, slots downwards]
     */
    @Contract("_ -> new")
    public static int @NotNull [] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        // if the number of input has an integer root
        // return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = (int) sqrt;
            itemSlotsToDown = itemSlotsToLeft;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            // if we couldn't fit all into a perfect square,
            // increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            // if we still can't fit all the slots in a grid,
            // increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[] { itemSlotsToLeft, itemSlotsToDown };
    }

    /**
     * Determines the slot grid sizes for the item and fluid counts
     *
     * @param itemInputsCount  the amount of item slots
     * @param fluidInputsCount the amount of fluid tanks
     * @return [item grid width, item grid height, fluid grid width, fluid grid height]
     */
    @Contract("_, _ -> new")
    public static int @NotNull [] determineSlotsGrid(int itemInputsCount, int fluidInputsCount) {
        int[] arr = determineSlotsGrid(itemInputsCount);
        int[] sizes = { arr[0], arr[1], 0, 0 };
        arr = determineSlotsGrid(fluidInputsCount);
        sizes[2] = arr[0];
        sizes[3] = arr[1];
        return sizes;
    }

    /* *********************** MUI 1 *********************** */

    /**
     * Create a JEI UI Template
     *
     * @param importItems  the input item inventory
     * @param exportItems  the output item inventory
     * @param importFluids the input fluid inventory
     * @param exportFluids the output fluid inventory
     * @param yOffset      the y offset for the gui
     * @return the populated builder
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new gregtech.api.gui.widgets.RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20,
                progressBarTexture, moveType, recipeMap));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (specialTexture != null) {
            addSpecialTexture(builder);
        }
        return builder;
    }

    /**
     * This DOES NOT include machine control widgets or binds player inventory
     *
     * @param progressSupplier a supplier for the progress bar
     * @param importItems      the input item inventory
     * @param exportItems      the output item inventory
     * @param importFluids     the input fluid inventory
     * @param exportFluids     the output fluid inventory
     * @param yOffset          the y offset for the gui
     * @return the populated builder
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems,
                                              IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                              FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(
                new gregtech.api.gui.widgets.RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20,
                        progressBarTexture, moveType, recipeMap));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (specialTexture != null) {
            addSpecialTexture(builder);
        }
        return builder;
    }

    /**
     * This DOES NOT include machine control widgets or binds player inventory
     *
     * @param progressSupplier a supplier for the progress bar
     * @param importItems      the input item inventory
     * @param exportItems      the output item inventory
     * @param importFluids     the input fluid inventory
     * @param exportFluids     the output fluid inventory
     * @param yOffset          the y offset for the gui
     * @return the populated builder
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier,
                                                       IItemHandlerModifiable importItems,
                                                       IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                                       FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(
                new gregtech.api.gui.widgets.RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20,
                        progressBarTexture, moveType, recipeMap));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (specialTexture != null) {
            addSpecialTexture(builder);
        }
        return builder;
    }

    /**
     * @param builder      the builder to add to
     * @param itemHandler  the item handler to use
     * @param fluidHandler the fluid handler to use
     * @param isOutputs    if slots should be output slots
     * @param yOffset      the y offset for the gui
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    protected void addInventorySlotGroup(@NotNull ModularUI.Builder builder,
                                         @NotNull IItemHandlerModifiable itemHandler,
                                         @NotNull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int) (itemSlotsToDown / 2.0 * 18) + yOffset;
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.getTanks() == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemHandler.getSlots() >= 6 && fluidHandler.getTanks() >= 2 && !isOutputs) startInputsY -= 9;
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                if (slotIndex >= itemInputsCount) break;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
            }
        }
        if (wasGroup) startInputsY += 2;
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, true, isOutputs);
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) :
                            startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, true, isOutputs);
                }
            }
        }
    }

    /**
     * Add a slot to this ui
     *
     * @param builder      the builder to add to
     * @param x            the x coordinate of the slot
     * @param y            the y coordinate of the slot
     * @param slotIndex    the slot index of the slot
     * @param itemHandler  the item handler to use
     * @param fluidHandler the fluid handler to use
     * @param isFluid      if the slot is a fluid slot
     * @param isOutputs    if slots should be output slots
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler,
                           FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isFluid) {
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs).setBackgroundTexture(
                    getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true)
                    .setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.getTanks() - 1))
                    .setContainerClicking(true, !isOutputs));
        }
    }

    /**
     * @return the height used to determine size of background texture in JEI
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public int getPropertyHeightShift() {
        int maxPropertyCount = 0;
        if (shouldShiftWidgets()) {
            for (Recipe recipe : recipeMap.getRecipeList()) {
                int count = recipe.propertyStorage().size();
                if (count > maxPropertyCount) {
                    maxPropertyCount = count;
                }
            }
        }
        return maxPropertyCount * 10; // GTRecipeWrapper#LINE_HEIGHT
    }

    /**
     * @return widgets should be shifted
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    private boolean shouldShiftWidgets() {
        return recipeMap.getMaxInputs() + recipeMap.getMaxOutputs() >= 6 ||
                recipeMap.getMaxFluidInputs() + recipeMap.getMaxFluidOutputs() >= 6;
    }

    /**
     * @param isOutput if the slot is an output slot
     * @param isFluid  if the slot is a fluid slot
     * @param isLast   if the slot is the last slot of its type
     * @return the overlays for a slot
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    protected TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast) {
        TextureArea base = isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT;
        byte overlayKey = computeOverlayKey(isOutput, isFluid, isLast);
        if (slotOverlays.containsKey(overlayKey)) {
            return new TextureArea[] { base, slotOverlays.get(overlayKey) };
        }
        return new TextureArea[] { base };
    }

    /**
     * @return the progress bar's move type
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull gregtech.api.gui.widgets.ProgressWidget.MoveType progressBarMoveType() {
        return moveType;
    }

    /**
     * @param moveType the new progress bar move type
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setProgressBarMoveType(@NotNull gregtech.api.gui.widgets.ProgressWidget.MoveType moveType) {
        this.moveType = moveType;
    }

    /**
     * @return the texture of the progress bar
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull TextureArea progressBarTexture() {
        return progressBarTexture;
    }

    /**
     * @param progressBarTexture the new progress bar texture
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setProgressBarTexture(@NotNull TextureArea progressBarTexture) {
        this.progressBarTexture = progressBarTexture;
    }

    /**
     * @param progressBarTexture the new progress bar texture
     * @param moveType           the new progress bar move type
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setProgressBar(@NotNull TextureArea progressBarTexture,
                               @NotNull gregtech.api.gui.widgets.ProgressWidget.MoveType moveType) {
        this.progressBarTexture = progressBarTexture;
        this.moveType = moveType;
    }

    /**
     * @param specialTexture the special texture to set
     * @param x              the x coordinate of the texture
     * @param y              the y coordinate of the texture
     * @param width          the width of the texture
     * @param height         the height of the texture
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setSpecialTexture(@NotNull TextureArea specialTexture, int x, int y, int width, int height) {
        setSpecialTexture(specialTexture, new int[] { x, y, width, height });
    }

    /**
     * @param specialTexture the special texture to set
     * @param position       the position of the texture: [x, y, width, height]
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setSpecialTexture(@NotNull TextureArea specialTexture, int @NotNull [] position) {
        this.specialTexture = specialTexture;
        this.specialTexturePosition.set(
                position[0],
                position[1],
                position[2],
                position[3]);
    }

    /**
     * @return the special texture
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @Nullable TextureArea specialTexture() {
        return this.specialTexture;
    }

    /**
     * @return the special texture's position
     */
    public Area specialTexturePosition() {
        return this.specialTexturePosition;
    }

    /**
     * Add a special texture to a builder
     *
     * @param builder the builder to add to
     * @return the updated builder
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull ModularUI.Builder addSpecialTexture(@NotNull ModularUI.Builder builder) {
        builder.image(specialTexturePosition.x(), specialTexturePosition.y(),
                specialTexturePosition.w(),
                specialTexturePosition.h(), specialTexture);
        return builder;
    }

    /**
     * @return if this UI represents an energy generating recipemap
     */
    public boolean isGenerator() {
        return isGenerator;
    }

    /**
     * @param texture  the texture to set
     * @param isOutput if the slot is an output slot
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setItemSlotOverlay(@NotNull TextureArea texture, boolean isOutput) {
        this.slotOverlays.put(computeOverlayKey(isOutput, false, false), texture);
        this.slotOverlays.put(computeOverlayKey(isOutput, false, true), texture);
    }

    /**
     * @param texture    the texture to set
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setItemSlotOverlay(@NotNull TextureArea texture, boolean isOutput, boolean isLastSlot) {
        this.slotOverlays.put(computeOverlayKey(isOutput, false, isLastSlot), texture);
    }

    /**
     * @param texture  the texture to set
     * @param isOutput if the slot is an output slot
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setFluidSlotOverlay(@NotNull TextureArea texture, boolean isOutput) {
        this.slotOverlays.put(computeOverlayKey(isOutput, true, false), texture);
        this.slotOverlays.put(computeOverlayKey(isOutput, true, true), texture);
    }

    /**
     * @param texture    the texture to set
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public void setFluidSlotOverlay(@NotNull TextureArea texture, boolean isOutput, boolean isLastSlot) {
        this.slotOverlays.put(computeOverlayKey(isOutput, true, isLastSlot), texture);
    }

    /**
     * @param key     the key to store the slot's texture with
     * @param texture the texture to store
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @ApiStatus.Internal
    public void setSlotOverlay(byte key, @NotNull TextureArea texture) {
        this.slotOverlays.put(key, texture);
    }

    /* *********************** MUI 2 *********************** */

    public ModularPanel constructPanel(ModularPanel panel, DoubleSupplier progressSupplier,
                                       IItemHandlerModifiable importItems,
                                       IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                       FluidTankList exportFluids, int yOffset, PanelSyncManager syncManager) {
        DoubleSyncValue progressValue = new DoubleSyncValue(progressSupplier);

        Flow row = Flow.row()
                .height(3 * 18 + 9)
                .debugName("recipemapui.parent")
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .top(23 - 7);

        // this isn't great but it works for now
        // panel size is hardcoded because you can't get the panel size from the panel
        int m = calculateCenter(importItems.getSlots(), importFluids.getTanks(), 176 + 20);

        row.child(makeInventorySlotGroup(importItems, importFluids, false)
                .marginLeft(m - 4));
        row.child(new RecipeProgressWidget()
                .recipeMap(recipeMap)
                .debugName("recipe.progress")
                .size(20)
                .margin(4, 0)
                .value(progressValue)
                .texture(progressTexture, 20)
                .direction(progressDirection));
        row.child(makeInventorySlotGroup(exportItems, exportFluids, true));
        panel.child(row);
        if (specialDrawableTexture != null) {
            panel.child(specialDrawableTexture.asWidget()
                    .debugName("special_texture")
                    .pos(specialTexturePosition.x(), specialTexturePosition.y())
                    .size(specialTexturePosition.w(), specialTexturePosition.h()));
        }
        return panel;
    }

    private int calculateCenter(int inputItems, int inputFluids, int panelSize) {
        int[] ints = determineSlotsGrid(inputItems, inputFluids);
        int leftSize = ints[1] >= inputFluids && ints[0] < 3 ?
                (ints[0] + ints[2]) * 18 :
                Math.max(ints[0] * 18, ints[2] * 18);
        int p = panelSize / 2;
        p -= 10;
        p -= leftSize;
        return p;
    }

    private Widget<?> makeItemGroup(int width, IItemHandlerModifiable handler, boolean isOutputs) {
        Flow col = Flow.column().mainAxisAlignment(Alignment.MainAxis.END)
                .coverChildren().debugName("item.col");
        int c = handler.getSlots();
        int h = (int) Math.ceil((double) c / width);
        SlotGroup slotGroup = new SlotGroup(isOutputs ? "output_items" : "input_items",
                width, 1, !isOutputs);
        for (int i = 0; i < h; i++) {
            Flow row = Flow.row().mainAxisAlignment(isOutputs ? Alignment.MainAxis.START : Alignment.MainAxis.END)
                    .coverChildren().debugName("item.row." + i);
            for (int j = 0; j < width; j++) {
                row.child(makeItemSlot(slotGroup, (i * h) + j, handler, isOutputs));
            }
            col.child(row);
        }
        return col;
    }

    private Widget<?> makeFluidGroup(int width, FluidTankList handler, boolean isOutputs) {
        Flow col = Flow.column().mainAxisAlignment(Alignment.MainAxis.START)
                .coverChildren().debugName("fluid.col");
        int c = handler.getTanks();
        int h = (int) Math.ceil((double) c / width);
        for (int i = 0; i < h; i++) {
            Flow row = Flow.row().mainAxisAlignment(isOutputs ? Alignment.MainAxis.START : Alignment.MainAxis.END)
                    .coverChildren().debugName("fluid.row");
            for (int j = 0; j < width; j++) {
                row.child(makeFluidSlot((i * h) + j, handler, isOutputs));
            }
            col.child(row);
        }
        return col;
    }

    protected Widget<?> makeInventorySlotGroup(@NotNull IItemHandlerModifiable itemHandler,
                                               @NotNull FluidTankList fluidHandler, boolean isOutputs) {
        final int itemInputsCount = itemHandler.getSlots();
        boolean onlyFluids = itemInputsCount == 0;
        final int fluidInputsCount = fluidHandler.getTanks();
        if (fluidInputsCount == 0 && onlyFluids)
            return null; // nothing to do here

        int[] slotGridSizes = determineSlotsGrid(itemInputsCount, fluidInputsCount);
        int itemGridWidth = slotGridSizes[onlyFluids ? 2 : 0];
        int itemGridHeight = slotGridSizes[onlyFluids ? 3 : 1];

        int fluidGridWidth = slotGridSizes[2];
        int fluidGridHeight = slotGridSizes[3];
        boolean singleRow = itemGridHeight >= fluidInputsCount && itemGridWidth < 3;

        Flow flow = (singleRow ? Flow.row() : Flow.column()).coverChildren()
                .debugName(singleRow ? "parent.row" : "parent.col");
        flow.crossAxisAlignment(isOutputs ? Alignment.CrossAxis.START : Alignment.CrossAxis.END);

        if (!onlyFluids && fluidGridHeight > 1) {
            // 1 should be 18, 2 should be 0, 3 should be -18, 4 should be -36
            // this is to make the first item row align with progress widget
            flow.top((2 - itemGridHeight) * 18);
        }

        if (itemInputsCount > 6) {
            flow.top(0);
        }

        if (onlyFluids) {
            flow.childIf(fluidInputsCount > 0, () -> makeFluidGroup(fluidGridWidth, fluidHandler, isOutputs));
        } else {
            flow.childIf(!singleRow || isOutputs, () -> makeItemGroup(itemGridWidth, itemHandler, isOutputs));
            flow.childIf(fluidInputsCount > 0, () -> makeFluidGroup(fluidGridWidth, fluidHandler, isOutputs));
            flow.childIf(singleRow && !isOutputs, () -> makeItemGroup(itemGridWidth, itemHandler, isOutputs));
        }

        return flow;
    }

    protected ItemSlot makeItemSlot(SlotGroup group, int slotIndex, IItemHandlerModifiable itemHandler,
                                    boolean isOutputs) {
        return new ItemSlot()
                .debugName("item.slot." + slotIndex + ":" + group.getName())
                .slot(SyncHandlers.itemSlot(itemHandler, slotIndex)
                        .slotGroup(group)
                        .accessibility(!isOutputs, true))
                .background(getDrawableOverlaysForSlot(isOutputs, false, slotIndex));
    }

    protected GTFluidSlot makeFluidSlot(int slotIndex, FluidTankList fluidHandler, boolean isOutputs) {
        return new GTFluidSlot()
                .debugName("fluid.slot." + slotIndex)
                .syncHandler(GTFluidSlot.sync(fluidHandler.getTankAt(slotIndex))
                        .accessibility(true, !isOutputs)
                        .drawAlwaysFull(true))
                .background(getDrawableOverlaysForSlot(isOutputs, true, slotIndex));
    }

    @ApiStatus.Experimental
    protected IDrawable getDrawableOverlaysForSlot(boolean isOutput, boolean isFluid, int index) {
        UITexture base = isFluid ? GTGuiTextures.FLUID_SLOT : GTGuiTextures.SLOT;
        Int2ObjectMap<IDrawable> overlays = getOverlayMap(isOutput, isFluid);
        if (overlays.containsKey(index)) {
            return IDrawable.of(base, overlays.get(index));
        }
        return IDrawable.of(base);
    }

    protected Int2ObjectMap<IDrawable> getOverlayMap(boolean isOutput, boolean isFluid) {
        return this.overlays.computeIfAbsent(computeKey(isOutput, isFluid), k -> new Int2ObjectArrayMap<>());
    }

    protected static byte computeKey(boolean isOutput, boolean isFluid) {
        byte k = 0b00;
        if (isOutput) k |= 0b10;
        if (isFluid) k |= 0b01;
        return k;
    }

    /** Marked experimental as this method will be removed when all GTCEu UIs are ported to MUI2. */
    @ApiStatus.Experimental
    @ApiStatus.Internal
    public void setUsesMui2() {
        this.usesMui2 = true;
    }

    /** Marked experimental as this method will be removed when all GTCEu UIs are ported to MUI2. */
    @ApiStatus.Experimental
    public boolean usesMui2() {
        return usesMui2;
    }

    // todo this is a quick and dirty method, find a better way
    /** Marked experimental as this method will be removed when all GTCEu UIs are ported to MUI2. */
    @ApiStatus.Experimental
    public RecipeMapUI<R> buildMui2(@NotNull Consumer<RecipeMapUIBuilder> builderConsumer) {
        builderConsumer.accept(new RecipeMapUIBuilder(this));
        return this;
    }

    /**
     * @param progressTexture the new progress bar texture
     */
    public void setProgressBarTexture(@NotNull UITexture progressTexture) {
        this.progressTexture = progressTexture;
    }

    /**
     * @param direction the new progress bar move type
     */
    public void setProgressBarDirection(@NotNull ProgressWidget.Direction direction) {
        this.progressDirection = direction;
    }

    /**
     * @param specialTexture the special texture to set
     * @param position       the position of the texture: [x, y, width, height]
     */
    public void setSpecialTexture(@NotNull IDrawable specialTexture, @NotNull Area position) {
        this.specialDrawableTexture = specialTexture;
        this.specialTexturePosition = position;
    }

    /**
     * @param texture  the texture to store
     * @param index    the key to store the slot's texture with
     * @param isFluid  if the slot is fluid
     * @param isOutput if the slot is an output
     */
    @ApiStatus.Internal
    public void setSlotOverlay(@NotNull IDrawable texture, int index, boolean isFluid, boolean isOutput) {
        getOverlayMap(isOutput, isFluid).put(index, texture);
    }

    /**
     * @return if this ui should be visible in JEI
     */
    public boolean isJEIVisible() {
        return isJEIVisible;
    }

    /**
     * @param isJEIVisible if the ui should be visible in JEI
     */
    public void setJEIVisible(boolean isJEIVisible) {
        this.isJEIVisible = isJEIVisible;
    }

    /**
     * @return if item input slot amounts can be modified
     */
    public boolean canModifyItemInputs() {
        return modifyItemInputs;
    }

    /**
     * @return if item output slot amounts can be modified
     */
    public boolean canModifyItemOutputs() {
        return modifyItemOutputs;
    }

    /**
     * @return if fluid input slot amounts can be modified
     */
    public boolean canModifyFluidInputs() {
        return modifyFluidInputs;
    }

    /**
     * @return if fluid output slot amounts can be modified
     */
    public boolean canModifyFluidOutputs() {
        return modifyFluidOutputs;
    }

    /**
     * @return the UI's recipemap
     */
    public @NotNull R recipeMap() {
        return recipeMap;
    }
}
