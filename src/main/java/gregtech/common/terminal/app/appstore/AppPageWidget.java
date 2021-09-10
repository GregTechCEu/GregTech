package gregtech.common.terminal.app.appstore;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class AppPageWidget extends TerminalDialogWidget {
    private final AbstractApplication application;
    private final AppStoreApp store;
    private final CircleButtonWidget[] buttons;
    private int lineWidth;
    private boolean back;

    public AppPageWidget(AbstractApplication application, AppStoreApp store) { // 323 222
        super(store.getOs(), 5, 5, 333 - 10, 232 - 10);
        this.application = application;
        this.store = store;
        String name = this.application.getRegistryName();
        int stage = application.getMaxTier() + 1;
        int color = application.getThemeColor();
        int lightColor = color & 0x00ffffff | ((0x6f) << 24);
        int dur = 323 / (stage + 1);
        buttons = new CircleButtonWidget[stage];
        for (int i = 0; i < stage; i++) {
            int tier = i;
            // upgrade button
            buttons[i] = new CircleButtonWidget(dur + dur * i, 110, 6, 2, 0)
                    .setColors(0, lightColor, color)
                    .setHoverText("Tier " + (i + 1))
                    .setClickListener(cd->buttonClicked(tier));
            this.addWidget(buttons[i]);
        }
        if (store.getOs().isRemote()) {
            // profile
            IGuiTexture profile;
            if (ResourceHelper.isResourceExist("textures/gui/terminal/" + application.getRegistryName() + "/profile.png")) {
                profile = TextureArea.fullImage("textures/gui/terminal/" + application.getRegistryName() + "/profile.png");
            } else {
                profile = application.getIcon();
            }
            this.addWidget(new ImageWidget(10, 15, 80, 80, profile));


            for (int i = 0; i < stage; i++) {
                List<ItemStack> conditions = TerminalRegistry.getAppHardwareUpgradeConditions(name, i);
                // line
                if (conditions.size() > 0) {
                    this.addWidget(new ImageWidget(dur + dur * i, 115, 1, -18 + (conditions.size() >= 4 ? 4 * 25 : conditions.size() * 25),
                            new ColorRectTexture(0xafffffff)));
                }
                // conditions
                for (int j = 0; j < conditions.size(); j++) {
                    this.addWidget(new SlotWidget(new SingleItemStackHandler(conditions.get(j)), 0,
                            dur + dur * i + 25 * (j / 4)- 9, 120 + 25 * (j % 4), false, false));
                }
            }
        }
    }

    private void buttonClicked(int tier) {
        int lastTier;
        TerminalOSWidget os = store.getOs();
        if (!os.installedApps.contains(application)) {
            lastTier = -1;
        } else if (TerminalBehaviour.isCreative(os.itemStack)) {
            lastTier = application.getMaxTier();
        } else {
            lastTier = Math.min(os.tabletNBT.getCompoundTag(application.getRegistryName()).getInteger("_tier"), application.getMaxTier());
        }
        if (lastTier != tier) {
            if (lastTier == -1) {
                os.installApplication(application);
            }
            NBTTagCompound tag = os.tabletNBT.getCompoundTag(application.getRegistryName());
            tag.setInteger("_tier", tier);
            os.tabletNBT.setTag(application.getRegistryName(), tag);
        }
    }

    @Override
    public void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;

        GlStateManager.disableDepth();

        drawSolidRect(x, y, width, height, store.darkMode ? 0xcf000000 : 0xcfdddddd);
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        int stage;
        TerminalOSWidget os = store.getOs();
        if (!os.installedApps.contains(application)) {
            stage = 0;
        } else if (TerminalBehaviour.isCreative(os.itemStack)) {
            stage = application.getMaxTier() + 1;
        } else {
            stage = Math.min(os.tabletNBT.getCompoundTag(application.getRegistryName()).getInteger("_tier"), application.getMaxTier()) + 1;
        }
        int maxStage = application.getMaxTier() + 1;
        int color = application.getThemeColor();
        int lightColor = color & 0x00ffffff | ((0x6f) << 24);
        int dur = 323 / (maxStage + 1);

        int hover = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isMouseOverElement(mouseX, mouseY)) {
                hover = i;
            }
        }

        // draw current tier
        drawSolidRect(x, y + 110 - 2, dur * stage, 4, color);
        if (stage == maxStage) {
            drawSolidRect(x + stage * dur, y + 110 - 2, dur, 4, color);
        } else {
            drawSolidRect(x + stage * dur, y + 110 - 2, dur, 4, lightColor);
        }

        int end = dur * (hover + 1 - stage);

        if (hover + 1 > stage) {
            if (lineWidth != end && (interpolator == null || back)) {
                back = false;
                interpolator = new Interpolator(lineWidth, end, (end - lineWidth) / 15, Eases.EaseLinear,
                        value-> lineWidth = value.intValue(),
                        value-> interpolator = null);
                interpolator.start();
            }
        } else {
            if (lineWidth != 0 && (interpolator == null || !back)) {
                back = true;
                interpolator = new Interpolator(lineWidth, 0, lineWidth / 15, Eases.EaseLinear,
                        value-> lineWidth = value.intValue(),
                        value-> interpolator = null);
                interpolator.start();
            }
        }

        if (lineWidth != 0) {
            int smoothWidth = lineWidth;
            if (hover + 1 > stage) {
                if (lineWidth != end) {
                    smoothWidth += partialTicks * end / 10;
                }
            } else {
                smoothWidth -= partialTicks * end / 10;
            }

            drawSolidRect(x + stage * dur, y + 110 - 2, smoothWidth, 4, color);
        }

        // description
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        List<String> description = fr.listFormattedStringToWidth(application.getDescription(), 210);
        int fColor = store.darkMode ? -1 : 0xff333333;
        fr.drawString(I18n.format(application.getUnlocalizedName()), x + 100, y + 14, fColor, store.darkMode);
        for (int i = 0; i < description.size(); i++) {
            fr.drawString(description.get(i), x + 100, y + 25 + i * fr.FONT_HEIGHT, fColor, store.darkMode);
        }

        drawBorder(x + 10, y + 15, 80, 80, store.darkMode ? -1 : 0xff333333, 2);

        GlStateManager.enableDepth();
    }

}
