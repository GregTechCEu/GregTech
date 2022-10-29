package gregtech.api.recipes.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.mc1120.item.MCItemStack;
import crafttweaker.zenscript.IBracketHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.material.Material;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@BracketHandler
@ZenRegister
public class MetaItemBracketHandler implements IBracketHandler {
    private static final Map<String, ItemStack> metaItemNames = new HashMap<>();
    private static final Map<String, ItemStack> metaBlockNames = new HashMap<>();

    private final IJavaMethod method;

    public MetaItemBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(MetaItemBracketHandler.class, "getCtMetaItem", String.class);
    }

    @SuppressWarnings("ConstantConditions")
    public static void rebuildComponentRegistry() {
        metaItemNames.clear();
        for (MetaItem<?> item : MetaItem.getMetaItems()) {
            for (MetaValueItem entry : item.getAllItems()) {
                if (!entry.unlocalizedName.equals("meta_item")) {
                    metaItemNames.put(entry.unlocalizedName, entry.getStackForm());
                }
            }
        }

        for (Map.Entry<Material, BlockCompressed> entry : MetaBlocks.COMPRESSED.entrySet()) {
            metaBlockNames.put("block" + entry.getKey().toCamelCaseString(), entry.getValue().getItem(entry.getKey()));
        }
        for (Map.Entry<Material, BlockFrame> entry : MetaBlocks.FRAMES.entrySet()) {
            metaBlockNames.put("frame" + entry.getKey().toCamelCaseString(), entry.getValue().getItem(entry.getKey()));
        }

        for (BlockCable cable : MetaBlocks.CABLES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaBlockNames.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
        for (BlockItemPipe cable : MetaBlocks.ITEM_PIPES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaBlockNames.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
        for (BlockFluidPipe cable : MetaBlocks.FLUID_PIPES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaBlockNames.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
    }

    public static ItemStack getMetaItem(String name) {
        ItemStack item;
        if ((item = metaItemNames.get(name)) != null) {
            return item.copy();
        }
        if ((item = metaBlockNames.get(name)) != null) {
            return item.copy();
        }
        if ((item = MetaTileEntityBracketHandler.getMetaTileEntityItem(name)) != null) {
            return item.copy();
        }
        throw new NoSuchElementException("No meta item found for " + name);
    }

    public static IItemStack getCtMetaItem(String name) {
        try {
            return new MCItemStack(getMetaItem(name));
        } catch (Exception e) {
            CraftTweakerAPI.logError(e.getMessage());
        }
        return MCItemStack.EMPTY;
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 3)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("metaitem")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 2; i < tokens.size(); i++) {
            nameBuilder.append(tokens.get(i).getValue());
        }
        return position -> new ExpressionCallStatic(position, environment, method,
                new ExpressionString(position, nameBuilder.toString()));
    }

}
