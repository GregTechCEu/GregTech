package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.Widget;

public interface IGuideWidget {
    Widget createStreamWidget(int x, int y, int pageWidth, JsonObject config);
    Widget createFixedWidget(int x, int y, int width, int height, JsonObject config);
    void setPage(GuidePageWidget page);
}
