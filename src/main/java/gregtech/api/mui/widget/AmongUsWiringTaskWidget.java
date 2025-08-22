package gregtech.api.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.mui.ColorableTheme;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTUtility;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class AmongUsWiringTaskWidget extends Widget<AmongUsWiringTaskWidget> {

    // Due to how UITextures are coded, they apply their own Color.setGlColor() from the theme right before drawing.
    private static final ColorableTheme colorableTheme = new ColorableTheme();

    @NotNull
    private final Runnable onCompletion;
    @NotNull
    private final BooleanSupplier canInteract;

    private static final int wireAmount = 4;
    private final int[] colors = new int[wireAmount];
    private final int[] rightSideIndexesMapping = new int[wireAmount];
    private final boolean[] connected = new boolean[wireAmount];
    private int wireBeingDragged = -1;

    public AmongUsWiringTaskWidget(@NotNull BooleanSupplier canInteract, @NotNull Runnable onCompletion) {
        this.onCompletion = onCompletion;
        this.canInteract = canInteract;
        size(80, 50);
        background(GTGuiTextures.AMONG_US_BACKGROUND);

        for (int index = 0; index < wireAmount; index++) {
            colors[index] = GTValues.RNG.nextInt() | 0xff000000;
        }

        for (int index = 0; index < wireAmount; index++) {
            rightSideIndexesMapping[index] = index;
        }
        GTUtility.shuffle(rightSideIndexesMapping);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        float xScale = 0.15873016f;
        float yScale = 0.09920635f;

        for (int index = 0; index < wireAmount; index++) {
            double y1 = yScale * ((index + 1) * 104 - 10);
            int color = colors[index];
            if (connected[index]) {

            }
        }

        for (int index = 0; index < wireAmount; index++) {
            double y1 = yScale * ((index + 1) * 104 - 10);
            int color = colors[index];

            if (index == wireBeingDragged) {
                // draw connecting line
            }

            colorableTheme.setColor(color);
            GTGuiTextures.AMONG_US_WIRE_BASE.drawSubArea(
                    (xScale * 25),
                    (float) ((y1 - yScale) * 5.0f),
                    (xScale * 38),
                    (yScale * 32),
                    0.0f,
                    0.0f,
                    0.5f,
                    1.0f,
                    colorableTheme);

            GTGuiTextures.AMONG_US_WIRE.drawSubArea(
                    0.0f,
                    (float) y1,
                    (xScale * 50),
                    (yScale * 32),
                    0.0f,
                    0.0f,
                    0.5f,
                    1.0f,
                    colorableTheme);

            colorableTheme.setColor(colors[rightSideIndexesMapping[index]]);
            GTGuiTextures.AMONG_US_WIRE_BASE.drawSubArea(
                    80 - (xScale * (25 + 38)),
                    (float) y1 - yScale * 5,
                    (xScale * 38), (yScale * 32),
                    0.5f,
                    0.0f,
                    1.0f,
                    1.0f,
                    colorableTheme);

            GTGuiTextures.AMONG_US_WIRE.drawSubArea(
                    80 - (xScale * 50),
                    (float) y1, (xScale * 50),
                    (yScale * 32),
                    0.5f,
                    0.0f,
                    1.0f,
                    1.0f,
                    colorableTheme);
        }
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetTheme widgetTheme) {}
}
