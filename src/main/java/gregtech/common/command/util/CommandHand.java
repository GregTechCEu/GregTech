package gregtech.common.command.util;

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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;

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
                if (metaValueItem == null) {
                    if (metaItem instanceof MetaPrefixItem) {
                        Material material = ((MetaPrefixItem) metaItem).getMaterial(stackInHand);
                        OrePrefix orePrefix = ((MetaPrefixItem) metaItem).getOrePrefix();
                        String oreDictName = new UnificationEntry(orePrefix, material).toString();
                        player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.material_meta_item", orePrefix.name(), material)
                                .setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, oreDictName))));
                    }
                } else {
                    if (metaValueItem instanceof ToolMetaItem.MetaToolValueItem) {
                        IToolStats toolStats = ((MetaToolValueItem) metaValueItem).getToolStats();
                        player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.tool_stats", toolStats.getClass().getName()));
                    }
                    String id = "<metaitem:" + metaValueItem.unlocalizedName + ">";
                    ClipboardUtil.copyToClipboard(player, id);
                    ClickEvent metaItemEvent = new ClickEvent(Action.OPEN_URL, id);
                    player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.meta_item", id)
                            .setStyle(new Style().setColor(TextFormatting.AQUA)
                                    .setClickEvent(metaItemEvent)));

                }
            }
            if(stackInHand.getItem() instanceof MachineItemBlock) {
                MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(stackInHand);
                if(mte != null) {
                    String id = mte.metaTileEntityId.toString();
                    if(mte.metaTileEntityId.getNamespace().equals("gregtech"))
                        id = mte.metaTileEntityId.getPath();
                    id = "<meta_tile_entity:" + id + ">";
                    ClipboardUtil.copyToClipboard(player, id);
                    ClickEvent metaItemEvent = new ClickEvent(Action.OPEN_FILE, id);
                    player.sendMessage(new TextComponentTranslation("gregtech.command.util.hand.meta_item", id)
                            .setStyle(new Style().setColor(TextFormatting.AQUA)
                                    .setClickEvent(metaItemEvent)));
                }
            }
        } else {
            throw new CommandException("gregtech.command.util.hand.not_a_player");
        }
    }
}
