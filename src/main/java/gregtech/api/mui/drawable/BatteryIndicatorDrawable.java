package gregtech.api.mui.drawable;

import gregtech.api.util.GTUtility;
import gregtech.api.util.function.FloatSupplier;
import gregtech.client.utils.RenderUtil;

import net.minecraft.util.math.MathHelper;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import org.jetbrains.annotations.NotNull;

public class BatteryIndicatorDrawable implements IDrawable {

    private static final int red = Color.withAlpha(Color.RED.main, 0.3f);
    private static final int yellow = Color.withAlpha(Color.YELLOW.main, 0.3f);
    private static final int green = Color.withAlpha(Color.GREEN.main, 0.4f);

    @NotNull
    private final FloatSupplier chargeLevelProvider;
    private final float lowCharge;

    public BatteryIndicatorDrawable(@NotNull FloatSupplier chargeLevelProvider, float lowCharge) {
        this.chargeLevelProvider = chargeLevelProvider;
        this.lowCharge = lowCharge;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        float charge = MathHelper.clamp(chargeLevelProvider.getAsFloat(), 0.0f, 1.0f);
        float newHeight = height * charge;
        int color;
        if (charge < lowCharge) {
            color = GTUtility.argbLerp(red, yellow, charge / lowCharge);
        } else {
            color = GTUtility.argbLerp(yellow, green, (charge - lowCharge) / (1.0f - lowCharge));
        }

        RenderUtil.renderRect(1, (height - newHeight) + 1, width - 2, newHeight - 2, 1.0f, color);
    }
}
