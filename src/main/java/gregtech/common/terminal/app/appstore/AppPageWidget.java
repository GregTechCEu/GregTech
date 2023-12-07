package gregtech.common.terminal.app.appstore;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AppPageWidget extends TerminalDialogWidget {

    private final AbstractApplication application;
    private final AppCardWidget appCardWidget;
    private final AppStoreApp store;
    private final CircleButtonWidget[] buttons;
    private int lineWidth;
    private boolean back;

    public AppPageWidget(AbstractApplication application, AppStoreApp store, AppCardWidget appCardWidget) { // 323 222
        super(store.getOs(), 5, 5, 333 - 10, 232 - 10);
        this.appCardWidget = appCardWidget;
        this.application = application;
        this.store = store;
        String name = this.application.getRegistryName();
        int stage = application.getMaxTier() + 1;

        int dur = 323 / (stage + 1);
        buttons = new CircleButtonWidget[stage];
        for (int i = 0; i < stage; i++) {
            int tier = i;
            // upgrade button
            buttons[i] = new CircleButtonWidget(dur + dur * i, 110, 6, 2, 0)
                    .setClickListener(cd -> buttonClicked(tier));
            this.addWidget(buttons[i]);
        }
        this.addWidget(new CircleButtonWidget(310, 10, 6, 1, 8)
                .setColors(0,
                        TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_3.getColor())
                .setIcon(GuiTextures.ICON_REMOVE)
                .setHoverText("terminal.store.close")
                .setClickListener(cd -> close()));
        if (store.getOs().isRemote()) {
            // profile
            int color = application.getThemeColor();
            int lightColor = color & 0x00ffffff | ((0x6f) << 24);
            IGuiTexture profile = application.getProfile();
            this.addWidget(new ImageWidget(10, 15, 80, 80, profile));

            for (int i = 0; i < stage; i++) {
                List<String> demand = TerminalRegistry.getAppHardwareDemand(name, i)
                        .stream()
                        .map(hw -> hw.getLocalizedName() + "(" + hw.addInformation() + ")")
                        .collect(Collectors.toList());
                demand.add(0, application.getTierInformation(i));
                buttons[i].setColors(0, lightColor, color).setHoverText(demand.toArray(new String[0]));
                List<ItemStack> conditions = TerminalRegistry.getAppHardwareUpgradeConditions(name, i);
                // line
                if (conditions.size() > 0) {
                    this.addWidget(new ImageWidget(dur + dur * i, 115, 1,
                            -18 + (conditions.size() >= 4 ? 4 * 25 : conditions.size() * 25),
                            new ColorRectTexture(0xafffffff)));
                }
                // conditions
                for (int j = 0; j < conditions.size(); j++) {
                    this.addWidget(new SlotWidget(new SingleItemStackHandler(conditions.get(j)), 0,
                            dur + dur * i + 25 * (j / 4) - 9, 120 + 25 * (j % 4), false, false));
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
            lastTier = Math.min(os.tabletNBT.getCompoundTag(application.getRegistryName()).getInteger("_tier"),
                    application.getMaxTier());
        }
        String name = application.getRegistryName();
        if (tier > lastTier) {
            boolean match = true; // inventory match
            List<ItemStack> requirements = new ArrayList<>();
            ItemStack missStack = null;
            if (!gui.entityPlayer.isCreative()) {
                for (int i = lastTier + 1; i <= tier; i++) {
                    for (ItemStack condition : TerminalRegistry.getAppHardwareUpgradeConditions(name, i)) {
                        boolean miss = true;
                        for (ItemStack requirement : requirements) {
                            if (requirement.isItemEqual(condition)) {
                                requirement.setCount(requirement.getCount() + condition.getCount());
                                miss = false;
                                break;
                            }
                        }
                        if (miss) {
                            requirements.add(condition.copy());
                        }
                    }
                }
                for (ItemStack requirement : requirements) {
                    int left = requirement.getCount();
                    for (ItemStack hold : gui.entityPlayer.inventory.mainInventory) {
                        if (requirement.isItemEqual(hold)) {
                            if (hold.getCount() < left) {
                                left -= hold.getCount();
                            } else {
                                left = 0;
                            }
                            if (left == 0) {
                                break;
                            }
                        }
                    }
                    if (left > 0) {
                        missStack = requirement.copy();
                        missStack.setCount(left);
                        match = false;
                        break;
                    }
                }
            }

            if (match) {
                if (os.isRemote()) {
                    appCardWidget.updateState(tier == application.getMaxTier() ? 0 : 1);
                }
                if (!gui.entityPlayer.isCreative()) { // cost
                    TerminalDialogWidget
                            .showConfirmDialog(store.getOs(), "terminal.dialog.notice", "terminal.store.match", res -> {
                                if (res) {
                                    for (ItemStack requirement : requirements) {
                                        int left = requirement.getCount();
                                        for (ItemStack hold : gui.entityPlayer.inventory.mainInventory) {
                                            if (requirement.isItemEqual(hold)) {
                                                if (hold.getCount() <= left) {
                                                    hold.setCount(0);
                                                    left -= hold.getCount();
                                                } else {
                                                    hold.setCount(hold.getCount() - left);
                                                    left = 0;
                                                }
                                                if (left == 0) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    updateTerminalAppTier(tier, lastTier);
                                }
                            }).open();
                } else {
                    updateTerminalAppTier(tier, lastTier);
                }
            } else {
                if (isRemote()) {
                    TerminalDialogWidget.showInfoDialog(store.getOs(),
                            "terminal.dialog.notice",
                            I18n.format("terminal.store.miss", missStack.getDisplayName(), missStack.getCount()))
                            .setClientSide().open();
                }
            }
        }
    }

    private void updateTerminalAppTier(int tier, int lastTier) {
        TerminalOSWidget os = store.getOs();
        os.openedApps.stream()
                .filter(app -> app.getRegistryName().equals(this.application.getRegistryName()))
                .findFirst()
                .ifPresent(app -> os.closeApplication(app, os.isRemote()));
        if (lastTier == -1) { // update terminal
            NBTTagList installed = os.tabletNBT.getTagList("_installed", Constants.NBT.TAG_STRING);
            installed.appendTag(new NBTTagString(application.getRegistryName()));
            os.tabletNBT.setTag("_installed", installed);
            os.installApplication(application);
        }
        NBTTagCompound tag = os.tabletNBT.getCompoundTag(application.getRegistryName());
        tag.setInteger("_tier", tier);
        os.tabletNBT.setTag(application.getRegistryName(), tag);
        lineWidth = 0;
    }

    @Override
    public void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;

        GlStateManager.disableDepth();

        drawSolidRect(x, y, width, height, store.darkMode ? 0xcf000000 : 0xdfdddddd);
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        int stage;
        TerminalOSWidget os = store.getOs();
        if (!os.installedApps.contains(application)) {
            stage = 0;
        } else if (TerminalBehaviour.isCreative(os.itemStack)) {
            stage = application.getMaxTier() + 1;
        } else {
            stage = Math.min(os.tabletNBT.getCompoundTag(application.getRegistryName()).getInteger("_tier"),
                    application.getMaxTier()) + 1;
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
                interpolator = new Interpolator(lineWidth, end, (end - lineWidth) / 15, Eases.LINEAR,
                        value -> lineWidth = value.intValue(),
                        value -> interpolator = null);
                interpolator.start();
            }
        } else {
            if (lineWidth != 0 && (interpolator == null || !back)) {
                back = true;
                interpolator = new Interpolator(lineWidth, 0, lineWidth / 15, Eases.LINEAR,
                        value -> lineWidth = value.intValue(),
                        value -> interpolator = null);
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
        String localizedName = I18n.format(application.getUnlocalizedName());
        drawStringSized(localizedName, x + 100, y + 14, fColor, store.darkMode, 2, false);
        if (isMouseOver(x + 100, y + 14, fr.getStringWidth(localizedName) * 2, fr.FONT_HEIGHT * 2, mouseX, mouseY)) {
            drawHoveringText(ItemStack.EMPTY, Collections.singletonList("(" + application.getRegistryName() + ")"), 200,
                    mouseX, mouseY);
        }
        for (int i = 0; i < description.size(); i++) {
            fr.drawString(description.get(i), x + 100, y + 35 + i * fr.FONT_HEIGHT, fColor, store.darkMode);
        }

        drawBorder(x + 10, y + 15, 80, 80, store.darkMode ? -1 : 0xff333333, 2);

        GlStateManager.enableDepth();
    }
}
