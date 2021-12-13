package gregtech.common.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ItemStackTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;

public class TutorialGuideApp extends GuideApp<String> {

    public TutorialGuideApp() {
        super("tutorials", new ItemStackTexture(Items.PAPER));
    }

    @Override
    protected String itemName(String item) {
        return I18n.format(item);
    }

    @Override
    protected String rawItemName(String item) {
        return item;
    }

    @Override
    public String ofJson(JsonObject json) {
        if (json.has("tutorial"))
            return json.get("tutorial").getAsString();
        return json.get("title").getAsString();
    }
}
