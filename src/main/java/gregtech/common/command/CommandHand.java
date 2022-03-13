package gregtech.common.command;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.CTRecipeHelper;
import gregtech.api.util.ClipboardUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.Set;

public class CommandHand extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "hand";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.hand.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            ItemStack stackInHand = player.getHeldItemMainhand();
            if (stackInHand.isEmpty()) {
                stackInHand = player.getHeldItemOffhand();
                if (stackInHand.isEmpty()) {
                    throw new CommandException("gregtech.command.hand.no_item");
                }
            }
            String registryName = stackInHand.getItem().getRegistryName().toString();
            ClickEvent itemNameEvent = new ClickEvent(Action.OPEN_URL, registryName);
            player.sendMessage(new TextComponentTranslation("gregtech.command.hand.item_id", registryName, stackInHand.getItemDamage())
                    .setStyle(new Style().setClickEvent(itemNameEvent)));

            IElectricItem electricItem = stackInHand.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            IFluidHandlerItem fluidHandlerItem = stackInHand.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (electricItem != null) {
                player.sendMessage(new TextComponentTranslation("gregtech.command.hand.electric",
                        electricItem.getCharge(),
                        electricItem.getMaxCharge(),
                        electricItem.getTier(),
                        Boolean.toString(electricItem.canProvideChargeExternally())));
            }

            if (fluidHandlerItem != null) {
                for (IFluidTankProperties properties : fluidHandlerItem.getTankProperties()) {
                    FluidStack contents = properties.getContents();
                    String fluidName = contents == null ? "empty" : contents.getFluid().getName();
                    player.sendMessage(new TextComponentTranslation("gregtech.command.hand.fluid",
                            contents == null ? 0 : contents.amount,
                            properties.getCapacity(),
                            Boolean.toString(properties.canFill()), Boolean.toString(properties.canDrain())));
                    if (contents != null) {
                        player.sendMessage(new TextComponentTranslation("gregtech.command.hand.fluid2", fluidName).appendSibling(new TextComponentString(" " + fluidName).setStyle(new Style().setColor(TextFormatting.GREEN)))
                                .setStyle(getCopyStyle("<liquid:" + fluidName + ">", false)));
                    }
                }
            }

            String id = CTRecipeHelper.getMetaItemId(stackInHand);
            if (id != null) {
                String ctId = "<metaitem:" + id + ">";
                ClipboardUtil.copyToClipboard(player, ctId);
                player.sendMessage(new TextComponentTranslation("gregtech.command.hand.meta_item", id).appendSibling(new TextComponentString(" " + id).setStyle(new Style().setColor(TextFormatting.GREEN)))
                        .setStyle(getCopyStyle(ctId, true)));
            }

            if (stackInHand.getItem() instanceof MetaItem) {
                MetaItem<?> metaItem = (MetaItem<?>) stackInHand.getItem();
                MetaValueItem metaValueItem = metaItem.getItem(stackInHand);
                if (metaValueItem != null) {
                    // tool info
                    if (metaValueItem instanceof ToolMetaItem.MetaToolValueItem) {
                        IToolStats toolStats = ((MetaToolValueItem) metaValueItem).getToolStats();
                        player.sendMessage(new TextComponentTranslation("gregtech.command.hand.tool_stats", toolStats.getClass().getName()));
                    }
                }
            }

            // material info
            MaterialStack material = OreDictUnifier.getMaterial(stackInHand);
            if (material != null) {
                player.sendMessage(new TextComponentTranslation("gregtech.command.hand.material").appendSibling(new TextComponentString(" " + material.material).setStyle(new Style().setColor(TextFormatting.GREEN)))
                        .setStyle(getCopyStyle("<material:" + material.material + ">", false)));
            }
            // ore prefix info
            OrePrefix orePrefix = OreDictUnifier.getPrefix(stackInHand);
            if (orePrefix != null) {
                player.sendMessage(new TextComponentTranslation("gregtech.command.hand.ore_prefix").appendSibling(new TextComponentString(" " + orePrefix.name).setStyle(new Style().setColor(TextFormatting.GREEN)))
                        .setStyle(getCopyStyle(orePrefix.name, false)));
            }

            Set<String> oreDicts = OreDictUnifier.getOreDictionaryNames(stackInHand);
            if (!oreDicts.isEmpty()) {
                sender.sendMessage(new TextComponentTranslation("gregtech.command.hand.ore_dict_entries"));
                for (String oreName : oreDicts) {
                    player.sendMessage(new TextComponentString("    \u00A7e- \u00A7b" + oreName)
                            .setStyle(getCopyStyle("<ore:" + oreName + ">", false)));
                }
            }
        } else {
            throw new CommandException("gregtech.command.hand.not_a_player");
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
