package gregtech.api.recipes;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;

public class RecipeMapFrontend {

    private final Byte2ObjectMap<TextureArea> slotOverlays;
    protected final String unlocalizedName;

    protected TextureArea progressBarTexture;
    protected ProgressWidget.MoveType progressBarMovetype;
    protected TextureArea specialTexture;
    protected int[] specialTexturePosition;
    protected SoundEvent sound;

    private boolean isVisible;

    /**
     * @param unlocalizedName the unlocalized name of the recipemap
     * @param slotOverlays the slot overlays
     * @param progressBarTexture the progress bar texture
     * @param progressBarMovetype the progress bar movetype
     * @param specialTexture a special texture
     * @param specialTexturePosition the position of a special texture. index=0 is x, index=1 is y, index=2 is width,
     *                               index=3 is height
     * @param sound the sound for the recipemap
     * @param isVisible if the recipemap is visible in JEI
     */
    public RecipeMapFrontend(@Nonnull String unlocalizedName, @Nonnull Byte2ObjectMap<TextureArea> slotOverlays,
                             @Nonnull TextureArea progressBarTexture, @Nonnull ProgressWidget.MoveType progressBarMovetype,
                             @Nullable TextureArea specialTexture, @Nullable int[] specialTexturePosition,
                             @Nullable SoundEvent sound, boolean isVisible) {
        this.unlocalizedName = unlocalizedName;
        this.slotOverlays = slotOverlays;
        this.progressBarTexture = progressBarTexture;
        this.progressBarMovetype = progressBarMovetype;
        this.specialTexture = specialTexture;
        this.specialTexturePosition = specialTexturePosition;
        this.sound = sound;
        this.isVisible = isVisible;
    }

    @Nonnull
    public static int[] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        //if the number of input has an integer root
        //return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = itemSlotsToDown = (int) sqrt;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            //if we couldn't fit all into a perfect square,
            //increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            //if we still can't fit all the slots in a grid,
            //increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[]{itemSlotsToLeft, itemSlotsToDown};
    }

    @Nonnull
    public TextureArea getProgressBarTexture() {
        return progressBarTexture;
    }

    public void setProgressBarTexture(@Nonnull TextureArea progressBarTexture) {
        this.progressBarTexture = progressBarTexture;
    }

    @Nonnull
    public ProgressWidget.MoveType getProgressBarMovetype() {
        return progressBarMovetype;
    }

    public void setProgressBarMovetype(@Nonnull ProgressWidget.MoveType progressBarMovetype) {
        this.progressBarMovetype = progressBarMovetype;
    }

    public void setProgressBar(@Nonnull TextureArea progressBar, @Nonnull ProgressWidget.MoveType moveType) {
        this.progressBarTexture = progressBar;
        this.progressBarMovetype = moveType;
    }

    public void setSpecialTexture(@Nullable TextureArea specialTexture, @Nullable int[] position) {
        this.specialTexture = specialTexture;
        this.specialTexturePosition = position;
    }

    public void setSlotOverlay(boolean isOutput, boolean isFluid, @Nonnull TextureArea slotOverlay) {
        this.setSlotOverlay(isOutput, isFluid, false, slotOverlay);
        this.setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public void setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, @Nonnull TextureArea slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
    }

    @Nullable
    public SoundEvent getSound() {
        return sound;
    }

    public void setSound(@Nullable SoundEvent sound) {
        this.sound = sound;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, progressBarTexture, progressBarMovetype, unlocalizedName));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, progressBarMovetype, unlocalizedName));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, progressBarMovetype, unlocalizedName));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    protected void addInventorySlotGroup(ModularUI.Builder builder, @Nonnull IItemHandlerModifiable itemHandler, @Nonnull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
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
                this.addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
            }
        }
        if (wasGroup) startInputsY += 2;
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    this.addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    this.addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            }
        }
    }

    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isFluid) {
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs).setBackgroundTexture(getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true).setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.getTanks() - 1)).setContainerClicking(true, !isOutputs));
        }
    }

    @Nonnull
    public TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast) {
        TextureArea base = isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT;
        byte overlayKey = (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
        if (slotOverlays.containsKey(overlayKey)) {
            return new TextureArea[]{base, slotOverlays.get(overlayKey)};
        }
        return new TextureArea[]{base};
    }

    public void setSpecialTexture(int x, int y, int width, int height, TextureArea area) {
        this.specialTexturePosition = new int[]{x, y, width, height};
        this.specialTexture = area;
    }

    public ModularUI.Builder addSpecialTexture(@Nonnull ModularUI.Builder builder) {
        builder.image(specialTexturePosition[0], specialTexturePosition[1], specialTexturePosition[2], specialTexturePosition[3], specialTexture);
        return builder;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    @Override
    public String toString() {
        return "RecipeMapFrontend{unlocalizedName='" + unlocalizedName + "'}";
    }

    @FunctionalInterface
    public interface FrontendCreator {

        /**
         * @see RecipeMapFrontend#RecipeMapFrontend(String, Byte2ObjectMap, TextureArea, ProgressWidget.MoveType,
         * TextureArea, int[], SoundEvent, boolean)
         * @return a new RecipeMapFrontEnd
         */
        @Nonnull
        RecipeMapFrontend apply(@Nonnull String unlocalizedName, @Nonnull Byte2ObjectMap<TextureArea> slotOverlays,
                                @Nonnull TextureArea progressBarTexture, @Nonnull ProgressWidget.MoveType progressBarMovetype,
                                @Nullable TextureArea specialTexture, @Nullable int[] specialTexturePosition,
                                @Nullable SoundEvent sound, boolean isVisible);
    }
}
