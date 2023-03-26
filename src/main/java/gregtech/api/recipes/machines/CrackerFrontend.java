package gregtech.api.recipes.machines;

import com.google.common.util.concurrent.AtomicDouble;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.recipes.RecipeMapFrontend;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;

public class CrackerFrontend extends RecipeMapFrontend {

    public CrackerFrontend(@Nonnull String unlocalizedName, @Nonnull Byte2ObjectMap<TextureArea> slotOverlays,
                           @Nonnull TextureArea progressBarTexture, @Nonnull ProgressWidget.MoveType progressBarMovetype,
                           @Nullable TextureArea specialTexture, @Nullable int[] specialTexturePosition,
                           @Nullable SoundEvent sound, boolean isVisible) {
        super(unlocalizedName, slotOverlays, progressBarTexture, progressBarMovetype, specialTexture,
                specialTexturePosition, sound, isVisible);
    }

    @Override
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        this.addSlot(builder, 52, 24 + yOffset, 0, importItems, importFluids, false, false);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        this.addSlot(builder, 52, 24 + yOffset + 19 + 18, 0, importItems, importFluids, true, false);
        this.addSlot(builder, 34, 24 + yOffset + 19 + 18, 1, importItems, importFluids, true, false);

        Pair<DoubleSupplier, DoubleSupplier> suppliers = createPairedSupplier(200, 41);
        builder.widget(new RecipeProgressWidget(suppliers.getLeft(), 42, 24 + yOffset + 18, 21, 19, GuiTextures.PROGRESS_BAR_CRACKING_INPUT, ProgressWidget.MoveType.VERTICAL, this.unlocalizedName));
        builder.widget(new RecipeProgressWidget(suppliers.getRight(), 78, 23 + yOffset, 20, 20, this.getProgressBarTexture(), this.getProgressBarMovetype(), this.unlocalizedName));
        return builder;
    }

    @Nonnull
    public static Pair<DoubleSupplier, DoubleSupplier> createPairedSupplier(int ticksPerCycle, int width) {
        AtomicDouble tracker = new AtomicDouble(0.0);
        DoubleSupplier supplier1 = new ProgressWidget.TimedProgressSupplier(ticksPerCycle, width, false) {
            @Override
            public double getAsDouble() {
                double val = super.getAsDouble();
                tracker.set(val);
                return val >= 0.5 ? 1.0 : val * 2;
            }
        };
        DoubleSupplier supplier2 = () -> tracker.get() >= 0.5 ? (tracker.get() - 0.5) * 2 : 0;
        return Pair.of(supplier1, supplier2);
    }
}
