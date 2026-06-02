package gregtech.common.mui.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;

public class DrawableNoHoverWidget extends IDrawable.DrawableWidget {

    public DrawableNoHoverWidget(IDrawable drawable) {
        super(drawable);
    }

    @Override
    public boolean canHover() {
        return false;
    }
}
