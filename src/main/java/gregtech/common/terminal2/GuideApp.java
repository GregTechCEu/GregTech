package gregtech.common.terminal2;

import gregtech.api.terminal2.ITerminalApp;
import gregtech.api.terminal2.Terminal2;
import gregtech.api.terminal2.Terminal2Theme;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTStringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.HoverableIcon;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.layout.Row;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuideApp implements ITerminalApp {

    private static final String GUIDE_PATH = "guides";
    private static final String FALLBACK_LOCALE = "en_us";

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel) {
        // return new RichTextWidget()
        // .sizeRel(1.0F)
        // .add("here is some Riched Text")
        // .addStringLines(findGuides().stream().map(File::getPath).collect(Collectors.toList()))
        // .add(stackIcon(MetaItems.TERMINAL.getStackForm()));

        List<Guide> guides = findGuides();
        var pages = new PagedWidget<>()
                .addPage(IDrawable.NONE.asWidget())
                .sizeRel(1.0F)
                .background(Terminal2Theme.COLOR_BACKGROUND_1);

        for (Guide g : guides) {
            pages.addPage(new RichTextWidget().sizeRel(0.9F)
                    .anchor(Alignment.CenterLeft)
                    .alignment(Alignment.TopLeft)
                    .addStringLines(g.rawText));
        }

        var list = new ListWidget<>().children(guides.size(), (i) -> new ButtonWidget<>()
                .overlay(IKey.str(guides.get(i).filename))
                .size(80, 10)
                .onMousePressed(_b -> {
                    pages.setPage(i + 1);
                    return true;
                }))
                .width(90)
                .heightRel(1.0F)
                .background(Terminal2Theme.COLOR_BACKGROUND_1);

        return new Row()
                .sizeRel(1.0F)
                .child(list)
                .child(pages)
                .childPadding(5);
    }

    private static IIcon stackIcon(ItemStack stack) {
        return new HoverableIcon(new ItemDrawable(stack).asIcon())
                .addTooltipStringLines(GTStringUtils.itemStackTooltip(stack));
    }

    private List<Guide> findGuides() {
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

        // noinspection DataFlowIssue
        List<File> guideFiles = new ArrayList<>(Arrays.asList(guideLocalizedFolder.listFiles()));

        // add all guides from the fallback folder that do not share a filename with a guide from the localized folder
        if (!guideLocalizedFolder.equals(guideFallbackFolder)) {
            Set<String> extantGuideNames = guideFiles.stream().map(File::getName).collect(Collectors.toSet());
            // noinspection DataFlowIssue
            for (File fallbackGuide : guideFallbackFolder.listFiles()) {
                if (!extantGuideNames.contains(fallbackGuide.getName())) {
                    guideFiles.add(fallbackGuide);
                    extantGuideNames.add(fallbackGuide.getName());
                }
            }
        }

        guideFiles.sort(Comparator.comparing(File::getName));

        List<Guide> guides = new ArrayList<>(guideFiles.size());
        for (File guideFile : guideFiles) {
            try {
                guides.add(new Guide(
                        Files.readAllLines(guideFile.toPath()),
                        !guideFile.toPath().startsWith(guideLocalizedFolder.toPath()),
                        guideFile.getName(),
                        guideFile.toPath().relativize(guideLocalizedFolder.toPath()).toString() // todo fix this
                ));
            } catch (IOException e) {
                GTLog.logger.error("Could not read guide file", e);
            }
        }
        return guides;
    }

    private static class Guide {

        private final List<String> rawText;
        private final boolean isFallbackLocale;
        private final String filename;
        private final String path;

        public Guide(List<String> rawText, boolean isFallbackLocale, String filename, String path) {
            this.rawText = rawText;
            this.isFallbackLocale = isFallbackLocale;
            this.filename = filename;
            this.path = path;
        }
    }
}
