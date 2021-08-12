package gregtech.api.terminal.app.recipegraph;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.app.recipegraph.widget.RGContainer;
import gregtech.api.terminal.app.recipegraph.widget.RGNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

public class RecipeGraphApp extends AbstractApplication {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/recipe_graph/icon.png");

    private RGContainer container;

    public RecipeGraphApp() {
        super("Recipe Graph", ICON);
    }

    @Override
    public AbstractApplication createApp(boolean isClient, NBTTagCompound nbt) {
        RecipeGraphApp app = new RecipeGraphApp();
        if (isClient) {
            app.container = new RGContainer(0,0,333, 232);
            app.addWidget(app.container);
            app.container.addWidget(new RGNode(10,10));
        }
        return app;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {

    }
}
