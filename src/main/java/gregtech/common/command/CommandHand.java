package gregtech.common.command;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.items.toolitem.ToolMetaItem.MetaToolValueItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
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
import java.util.List;
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
        return "gregtech.command.util.hand.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            ItemStack stackInHand = player.inventory.getCurrentItem();
            if (stackInHand.isEmpty()) {
                throw new CommandException("gregtech.command.util.hand.no_item");
            }
            String registryName = stackInHand.getItem().getRegistryName().toString();
            ClickEvent itemNameEvent = new ClickEvent(Action.OPEN_URL, registryName);
            player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.item_id", registryName, stackInHand.getItemDamage())
                    .setStyle(new Style().setClickEvent(itemNameEvent)));

            IElectricItem electricItem = stackInHand.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            IFluidHandlerItem fluidHandlerItem = stackInHand.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (electricItem != null) {
                player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.electric",
                        electricItem.getCharge(),
                        electricItem.getMaxCharge(),
                        electricItem.getTier(),
                        Boolean.toString(electricItem.canProvideChargeExternally())));
            }

            if (fluidHandlerItem != null) {
                for (IFluidTankProperties properties : fluidHandlerItem.getTankProperties()) {
                    FluidStack contents = properties.getContents();
                    String fluidName = contents == null ? "empty" : contents.getFluid().getName();
                    ClickEvent fluidClickEvent = new ClickEvent(Action.OPEN_URL, fluidName);
                    player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.fluid",
                            fluidName,
                            contents == null ? 0 : contents.amount,
                            properties.getCapacity(),
                            Boolean.toString(properties.canFill()), Boolean.toString(properties.canDrain()))
                            .setStyle(new Style().setClickEvent(fluidClickEvent)));
                }
            }

            if (stackInHand.getItem() instanceof MetaItem) {
                MetaItem<?> metaItem = (MetaItem<?>) stackInHand.getItem();
                MetaValueItem metaValueItem = metaItem.getItem(stackInHand);
                if (metaValueItem != null) {
                    if (metaValueItem instanceof ToolMetaItem.MetaToolValueItem) {
                        IToolStats toolStats = ((MetaToolValueItem) metaValueItem).getToolStats();
                        player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.tool_stats", toolStats.getClass().getName()));
                    }
                    String id = metaValueItem.unlocalizedName;
                    String ctId = "<metaitem:" + metaValueItem.unlocalizedName + ">";
                    ClipboardUtil.copyToClipboard(player, ctId);
                    player.sendMessage(new TextComponentString("MetaItem ID: ").appendSibling(new TextComponentString(id).setStyle(new Style().setColor(TextFormatting.GREEN)))
                            .setStyle(getCopyStyle(ctId)));

                }
            }
            if(stackInHand.getItem() instanceof MachineItemBlock) {
                MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(stackInHand);
                if(mte != null) {
                    String id = mte.metaTileEntityId.toString();
                    if(mte.metaTileEntityId.getNamespace().equals("gregtech"))
                        id = mte.metaTileEntityId.getPath();
                    String ctId = "<meta_tile_entity:" + id + ">";
                    ClipboardUtil.copyToClipboard(player, ctId);
                    player.sendMessage(new TextComponentString("MetaTileEntity ID: ").appendSibling(new TextComponentString(id).setStyle(new Style().setColor(TextFormatting.GREEN)))
                            .setStyle(getCopyInfoStyle(ctId)));
                }
            }

            Set<String> oreDicts = OreDictUnifier.getOreDictionaryNames(stackInHand);
            if(!oreDicts.isEmpty()) {
                sender.sendMessage(new TextComponentString("\u00A73OreDict Entries:"));
                for(String oreName : oreDicts) {
                    player.sendMessage(new TextComponentString("    \u00A7e- \u00A7b" + oreName)
                            .setStyle(getCopyStyle("<ore:" + oreName + ">")));
                }
            }
        } else {
            throw new CommandException("gregtech.command.util.hand.not_a_player");
        }
    }

    public static Style getCopyStyle(String copyMessage) {
        Style style = new Style();
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gt copy " + copyMessage);
        style.setClickEvent(click);

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to copy [\u00A76" + copyMessage + "\u00A7r]"));
        style.setHoverEvent(hoverEvent);

        return style;
    }

    public static Style getCopyInfoStyle(String copyMessage) {
        Style style = new Style();

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("\u00A76" + copyMessage + "\u00A7r was copied to clipboard"));
        style.setHoverEvent(hoverEvent);

        return style;
    }
}
