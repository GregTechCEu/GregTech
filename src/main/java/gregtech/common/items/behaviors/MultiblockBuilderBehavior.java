package gregtech.common.items.behaviors;

import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.pattern.PatternError;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.SortableListWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiblockBuilderBehavior implements IItemBehaviour, ItemUIFactory {

    public static final int MAX_KEYS = 16;

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        initNBT(heldItem);
        if (!world.isRemote) {
            MetaItemGuiFactory.open(player, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    public static Object2IntMap<String> getMap(ItemStack target) {
        if (!target.hasTagCompound()) return new Object2IntOpenHashMap<>();

        NBTTagCompound tag = target.getTagCompound().getCompoundTag("MultiblockBuilder");
        Object2IntMap<String> result = new Object2IntOpenHashMap<>();

        for (String str : tag.getKeySet()) {
            NBTTagCompound entry = tag.getCompoundTag(str);
            String key = entry.getString("Key");
            String val = entry.getString("Value");
            if (key.isEmpty() || val.isEmpty()) continue;

            result.put(key, Integer.parseInt(val));
        }

        return result;
    }

    protected static void initNBT(ItemStack target) {
        if (target.hasTagCompound()) return;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("MultiblockBuilder", new NBTTagCompound());
        target.setTagCompound(tag);
    }

    protected static NBTData loadKeys(ItemStack target) {
        NBTTagCompound tag = target.getTagCompound().getCompoundTag("MultiblockBuilder");

        String[] keys = new String[MAX_KEYS];
        String[] values = new String[MAX_KEYS];

        for (int i = 0; i < MAX_KEYS; i++) {
            String str = Integer.toString(i);
            if (!tag.hasKey(str)) {
                keys[i] = values[i] = "";
            }
            keys[i] = tag.getCompoundTag(str).getString("Key");
            values[i] = tag.getCompoundTag(str).getString("Value");
        }

        return new NBTData(keys, values);
    }

    protected static void setKey(int id, String key, String value, ItemStack target) {
        NBTTagCompound baseTag = target.getTagCompound().getCompoundTag("MultiblockBuilder");

        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Key", key);
        tag.setString("Value", value);
        baseTag.setTag(Integer.toString(id), tag);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        initNBT(guiData.getUsedItemStack());

        StringSyncValue[] keyValues = new StringSyncValue[MAX_KEYS];
        StringSyncValue[] valueValues = new StringSyncValue[MAX_KEYS];

        NBTData data = loadKeys(guiData.getUsedItemStack());
        String[] keys = data.keys;
        String[] values = data.values;

        List<Integer> total = IntStream.range(0, MAX_KEYS).boxed().collect(Collectors.toList());
        List<Integer> present = IntStream.range(0, MAX_KEYS).boxed().collect(Collectors.toList());

        for (int i = 0; i < MAX_KEYS; i++) {
            int finalI = i;
            keyValues[i] = new StringSyncValue(() -> keys[finalI], s -> {
                keys[finalI] = s;
                setKey(finalI, s, values[finalI], guiData.getUsedItemStack());
            });

            valueValues[i] = new StringSyncValue(() -> values[finalI], s -> {
                values[finalI] = s;
                setKey(finalI, keys[finalI], s, guiData.getUsedItemStack());
            });
        }

        SortableListWidget<Integer, SortableListWidget.Item<Integer>> list = SortableListWidget
                .sortableBuilder(total, present,
                        s -> new SortableListWidget.Item<>(s, new Row()
                                .size(8 * 18, 18)
                                .child(new TextFieldWidget()
                                        .left(0).width(4 * 18)
                                        .setValidator(str -> str.replaceAll("\\W", ""))
                                        .value(keyValues[s])
                                        .background(GTGuiTextures.DISPLAY))
                                .child(new TextFieldWidget()
                                        .left(4 * 18).width(4 * 18)
                                        .setValidator(str -> str.replaceAll("\\D", ""))
                                        .value(valueValues[s])
                                        .background(GTGuiTextures.DISPLAY))));

        return GTGuis.createPanel(guiData.getUsedItemStack(), 8 * 18 + 2 * 7 + 4, 8 * 18 + 8)
                .child(IKey.str("Test").asWidget().pos(5, 5))
                .child(new Row()
                        .pos(7, 18).coverChildren()
                        .child(IKey.str("Key").asWidget().pos(0, 0).size(4 * 18, 18))
                        .child(IKey.str("Value").asWidget().pos(4 * 18, 0).size(4 * 18, 18)))
                .child(list.pos(7, 36).height(6 * 18).width(8 * 18));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        // Initial checks
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IGregTechTileEntity)) return EnumActionResult.PASS;
        MetaTileEntity mte = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
        if (!(mte instanceof MultiblockControllerBase multiblock)) return EnumActionResult.PASS;
        if (!player.canPlayerEdit(pos, side, player.getHeldItem(hand))) return EnumActionResult.FAIL;
        if (world.isRemote) return EnumActionResult.SUCCESS;

        if (player.isSneaking()) {
            // If sneaking, try to build the multiblock.
            // Only try to auto-build if the structure is not already formed
            if (!multiblock.isStructureFormed("MAIN")) {
                multiblock.getBuildableShapes("MAIN", getMap(player.getHeldItem(hand))).get(0).getMap(multiblock, new BlockPos(0, 128, 0), new HashMap<>());
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        } else {
            // todo full substructure debug and autobuild support
            // If not sneaking, try to show structure debug info (if any) in chat.
            if (!multiblock.isStructureFormed("MAIN")) {
                PatternError error = multiblock.getSubstructure("MAIN").getPatternState().getError();
                if (error != null) {
                    player.sendMessage(
                            new TextComponentTranslation("gregtech.multiblock.pattern.error_message_header"));
                    player.sendMessage(new TextComponentString(error.getErrorInfo()));
                    return EnumActionResult.SUCCESS;
                }
            }
            player.sendMessage(new TextComponentTranslation("gregtech.multiblock.pattern.no_errors")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public void addPropertyOverride(@NotNull Item item) {
        item.addPropertyOverride(GTUtility.gregtechId("auto_mode"),
                (stack, world, entity) -> (entity != null && entity.isSneaking()) ? 1.0F : 0.0F);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.tool.multiblock_builder.tooltip2"));
    }

    @Desugar
    public record NBTData(String[] keys, String[] values) {}
}
