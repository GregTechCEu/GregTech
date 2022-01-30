package gregtech.integration.jei.utils;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AdvancedRecipeWrapper implements IRecipeWrapper {

    protected final List<JeiButton> buttons = new ArrayList<>();

    public AdvancedRecipeWrapper() {
        initExtras();
    }

    public abstract void initExtras();

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        for (JeiButton button : buttons) {
            button.render(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
            if (button.isHovering(mouseX, mouseY)) {
                List<String> lines = new ArrayList<>();
                button.buildTooltip(lines);
                if (lines.isEmpty())
                    continue;
                Minecraft mc = Minecraft.getMinecraft();
                int width = (int) (mc.displayWidth / 2f + recipeWidth / 2f);
                int maxWidth = Math.min(200, width - mouseX - 5);
                GuiUtils.drawHoveringText(ItemStack.EMPTY, lines, mouseX, mouseY,
                        width,
                        mc.displayHeight, maxWidth, mc.fontRenderer);
                GlStateManager.disableLighting();
            }
        }
    }

    @Override
    public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        for (JeiButton button : buttons) {
            if (button.isHovering(mouseX, mouseY) && button.getClickAction().click(minecraft, mouseX, mouseY, mouseButton)) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return false;
    }
}
