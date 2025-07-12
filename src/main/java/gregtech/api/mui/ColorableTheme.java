package gregtech.api.mui;

import gregtech.api.util.GTUtility;

import com.cleanroommc.modularui.theme.WidgetTheme;

public class ColorableTheme extends WidgetTheme {

    protected int color = 0;

    public ColorableTheme() {
        super(null, null, 0, 0, false);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setColor(int a, int r, int g, int b) {
        setColor(GTUtility.combineRGB(a, r, g, b));
    }

    @Override
    public int getColor() {
        return color;
    }
}
