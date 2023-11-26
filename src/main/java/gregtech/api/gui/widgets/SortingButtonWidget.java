package gregtech.api.gui.widgets;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Loader;

import java.util.function.Consumer;

public class SortingButtonWidget extends ClickButtonWidget {

    private static boolean inventoryTweaksChecked;
    private static boolean inventoryTweaksPresent;
    private static KeyBinding sortKeyBinding;

    public SortingButtonWidget(int xPosition, int yPosition, int width, int height, String displayText,
                               Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height, displayText, onPressed);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            int sortButton = getInvTweaksSortCode();
            if (sortButton < 0 && button == 100 + sortButton) {
                triggerButton();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        if (!super.keyTyped(charTyped, keyCode)) {
            int sortButton = getInvTweaksSortCode();
            if (sortButton > 0 && keyCode == sortButton) {
                triggerButton();
                return true;
            }
            return false;
        }
        return true;
    }

    private static int getInvTweaksSortCode() {
        if (!inventoryTweaksChecked) {
            inventoryTweaksChecked = true;
            inventoryTweaksPresent = Loader.isModLoaded("inventorytweaks");
        }
        if (!inventoryTweaksPresent) {
            return 0;
        }
        try {
            if (sortKeyBinding == null) {
                Class<?> proxyClass = Class.forName("invtweaks.forge.ClientProxy");
                sortKeyBinding = (KeyBinding) proxyClass.getField("KEYBINDING_SORT").get(null);
            }
            return sortKeyBinding.getKeyCode();
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }
}
