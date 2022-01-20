package gregtech.integration.jei.utils;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public abstract class AdvancedRecipeWrapper implements IRecipeWrapper {

    protected final List<JeiButton> buttons = new ArrayList<>();

    public AdvancedRecipeWrapper() {
        initExtras();
    }

    public abstract void initExtras();

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        for (JeiButton button : buttons) {
            button.render(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        }
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        for (JeiButton button : buttons) {
            if (button.isHovering(mouseX, mouseY) && button.getClickAction().click(minecraft, mouseX, mouseY, mouseButton)) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return false;
    }
}
