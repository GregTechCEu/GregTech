package gregtech.integration;

import com.cleanroommc.groovyscript.event.GsHandEvent;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.CTRecipeHelper;
import gregtech.api.util.ClipboardUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GroovyHandCommand {

    @SubscribeEvent
    public static void onHandCommand(GsHandEvent event) {
        ItemStack stackInHand = event.stack;

        String id = CTRecipeHelper.getMetaItemId(stackInHand);
        if (id != null) {
            String ctId = "metaitem('" + id + "')";
            ClipboardUtil.copyToClipboard((EntityPlayerMP) event.player, ctId);
            event.messages.add(new TextComponentTranslation("gregtech.command.hand.meta_item", id).appendSibling(new TextComponentString(" " + id).setStyle(new Style().setColor(TextFormatting.GREEN)))
                    .setStyle(getCopyStyle(ctId, true)));
        }

        // tool info
        if (stackInHand.getItem() instanceof IGTTool) {
            IGTTool tool = (IGTTool) stackInHand.getItem();
            event.messages.add(new TextComponentTranslation("gregtech.command.hand.tool_stats", tool.getToolClasses(stackInHand)));
        }

        // material info
        MaterialStack material = OreDictUnifier.getMaterial(stackInHand);
        if (material != null) {
            event.messages.add(new TextComponentTranslation("gregtech.command.hand.material").appendSibling(new TextComponentString(" " + material.material).setStyle(new Style().setColor(TextFormatting.GREEN)))
                    .setStyle(getCopyStyle("material('" + material.material + "')", false)));
        }
        // ore prefix info
        OrePrefix orePrefix = OreDictUnifier.getPrefix(stackInHand);
        if (orePrefix != null) {
            String ctId = "oreprefix('" + orePrefix.name + "')";
            ClipboardUtil.copyToClipboard((EntityPlayerMP) event.player, ctId);
            event.messages.add(new TextComponentTranslation("gregtech.command.hand.ore_prefix").appendSibling(new TextComponentString(" " + orePrefix.name).setStyle(new Style().setColor(TextFormatting.GREEN)))
                    .setStyle(getCopyStyle(ctId, false)));
        }
    }

    public static Style getCopyStyle(String copyMessage, boolean alreadyCopied) {
        Style style = new Style();
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gt copy " + copyMessage);
        style.setClickEvent(click);

        ITextComponent text = alreadyCopied ?
                new TextComponentString("").appendSibling(new TextComponentString(copyMessage + " ").setStyle(new Style().setColor(TextFormatting.GOLD)))
                        .appendSibling(new TextComponentTranslation("gregtech.command.copy.copied_and_click")) :
                new TextComponentTranslation("gregtech.command.copy.click_to_copy")
                        .appendSibling(new TextComponentString(" " + copyMessage).setStyle(new Style().setColor(TextFormatting.GOLD)));

        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text));
        return style;
    }
}
