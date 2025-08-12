package gregtech.mixins.minecraft;

import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.common.Loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.swing.*;
import java.net.URL;

import static gregtech.api.util.Mods.Names.GTQT_CORE;

@Mixin(Minecraft.class)
public class QuitMixin {


    @Unique
    private boolean gtqt$isCloseRequested;

    @Unique
    private boolean gtqt$waitingDialogQuit;

    @Shadow
    volatile boolean running;

    /**
     * @author MeowmelMuku
     * @reason Quit
     */
    @Overwrite
    public void shutdown()
    {
        if (ConfigHolder.worldgen.allUniqueStoneTypes)this.running = false;
        if (gtqt$isCloseRequested)
            return;
        if (!gtqt$waitingDialogQuit)
        {
            gtqt$waitingDialogQuit = true;
            new Thread(() -> {
                final JFrame frame = new JFrame();
                frame.setAlwaysOnTop(true);

                final URL resourceURL;
                if (Loader.isModLoaded(GTQT_CORE))resourceURL = Minecraft.class.getClassLoader().getResource("assets/gtqtcore/icons/icon_32.png");
                else resourceURL = Minecraft.class.getClassLoader().getResource("assets/gregtech/textures/gui/icon/gregtech_logo.png");

                final ImageIcon imageIcon = resourceURL == null ? null : new ImageIcon(resourceURL);

                final int result = JOptionPane.showConfirmDialog(frame,
                        I18n.format("gtqt.tooltip.quit_message", "Are you sure you want to exit the game?"),
                        I18n.format("gtqt.tooltip.modpack_name", "GregTech QuantumTransition"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, imageIcon);
                if (result == JOptionPane.YES_OPTION)
                {
                    gtqt$isCloseRequested = true;
                    running = false;
                }
                this.gtqt$waitingDialogQuit = false;
            }).start();
        }
    }
}
