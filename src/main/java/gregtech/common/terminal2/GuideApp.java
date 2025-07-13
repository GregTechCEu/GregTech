package gregtech.common.terminal2;

import gregtech.api.terminal2.ITerminalApp;
import gregtech.api.terminal2.Terminal2;
import gregtech.api.util.GTStringUtils;
import gregtech.common.items.MetaItems;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.HoverableIcon;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.RichTextWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuideApp implements ITerminalApp {
    private static final String GUIDE_PATH = "guides";
    private static final String FALLBACK_LOCALE = "en_us";

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel) {
        return new RichTextWidget()
                .sizeRel(1.0F)
                .add("here is some Riched Text")
                .addStringLines(findGuides().stream().map(File::getPath).collect(Collectors.toList()))
                .add(stackIcon(MetaItems.TERMINAL.getStackForm()));

    }

    private static IIcon stackIcon(ItemStack stack) {
        return new HoverableIcon(new ItemDrawable(stack).asIcon())
                .addTooltipStringLines(GTStringUtils.itemStackTooltip(stack));
    }

    private Collection<File> findGuides() {
        String locale = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        File guideRootFolder = new File(Terminal2.TERMINAL_PATH, GUIDE_PATH);
        File guideLocalizedFolder = new File(guideRootFolder, locale);
        File guideFallbackFolder = new File(guideRootFolder, FALLBACK_LOCALE);
        if (!guideLocalizedFolder.isDirectory()) {
            guideLocalizedFolder = guideFallbackFolder;
        }
        if (!guideFallbackFolder.isDirectory()) {
            return new ArrayList<>();
        }

        //noinspection DataFlowIssue
        List<File> guides = new ArrayList<>(Arrays.asList(guideLocalizedFolder.listFiles()));

        // add all guides from the fallback folder that do not share a filename with a guide from the localized folder
        if (!guideLocalizedFolder.equals(guideFallbackFolder)) {
            Set<String> extantGuideNames = guides.stream().map(File::getName).collect(Collectors.toSet());
            //noinspection DataFlowIssue
            for (File fallbackGuide : guideFallbackFolder.listFiles()) {
                if (!extantGuideNames.contains(fallbackGuide.getName())) {
                    guides.add(fallbackGuide);
                    extantGuideNames.add(fallbackGuide.getName());
                }
            }
        }

        return guides;
    }
}
