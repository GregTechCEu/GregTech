package gregtech.integration.crafttweaker.recipe;

import gregtech.api.GregTechAPI;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PipeNetProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.block.cable.CableBlock;
import gregtech.common.pipelike.handlers.properties.MaterialEnergyProperties;
import gregtech.common.pipelike.handlers.properties.MaterialFluidProperties;
import gregtech.common.pipelike.handlers.properties.MaterialItemProperties;
import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraft.item.ItemStack;

import com.cleanroommc.groovyscript.api.GroovyLog;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.mc1120.item.MCItemStack;
import crafttweaker.zenscript.IBracketHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@BracketHandler
@ZenRegister
public class MetaItemBracketHandler implements IBracketHandler {

    private static final Map<String, Map<String, ItemStack>> metaItemNames = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Map<String, ItemStack>> metaBlockNames = new Object2ObjectOpenHashMap<>();

    private final IJavaMethod method;

    public MetaItemBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(MetaItemBracketHandler.class, "getCtMetaItem", String.class);
    }

    public static void rebuildComponentRegistry() {
        metaItemNames.clear();
        for (MetaItem<?> item : MetaItem.getMetaItems()) {
            String namespace = Objects.requireNonNull(item.getRegistryName()).getNamespace();
            Map<String, ItemStack> map = metaItemNames.computeIfAbsent(namespace,
                    k -> new Object2ObjectOpenHashMap<>());

            for (MetaValueItem entry : item.getAllItems()) {
                if (!"meta_item".equals(entry.unlocalizedName)) {
                    map.put(entry.unlocalizedName, entry.getStackForm());
                }
            }
        }

        for (Map.Entry<Material, BlockCompressed> entry : MetaBlocks.COMPRESSED.entrySet()) {
            String modid = entry.getKey().getModid();
            Map<String, ItemStack> map = metaBlockNames.computeIfAbsent(modid, (k) -> new Object2ObjectOpenHashMap<>());
            String name = "block" + entry.getKey().toCamelCaseString();
            ItemStack stack = entry.getValue().getItem(entry.getKey());
            map.put(name, stack);
        }
        for (Map.Entry<Material, BlockFrame> entry : MetaBlocks.FRAMES.entrySet()) {
            String modid = entry.getKey().getModid();
            Map<String, ItemStack> map = metaBlockNames.computeIfAbsent(modid, (k) -> new Object2ObjectOpenHashMap<>());
            String name = "frame" + entry.getKey().toCamelCaseString();
            ItemStack stack = entry.getValue().getItem(entry.getKey());
            map.put(name, stack);
        }

        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            String modid = registry.getModid();
            Map<String, ItemStack> map = metaBlockNames.computeIfAbsent(modid, (k) -> new Object2ObjectOpenHashMap<>());

            for (Material material : registry) {
                PipeNetProperties prop = material.getProperty(PropertyKey.PIPENET_PROPERTIES);
                if (prop == null) continue;

                for (PipeNetProperties.IPipeNetMaterialProperty property : prop.getRegisteredProperties()) {
                    // TODO this is awkward, surely there's a better solution?
                    if (property instanceof MaterialEnergyProperties) {
                        for (CableBlock cable : MetaBlocks.CABLES.get(modid)) {
                            String name = cable.getStructure().getOrePrefix().name + material.toCamelCaseString();
                            ItemStack stack = cable.getItem(material);
                            map.put(name, stack);
                        }
                    } else
                        if (property instanceof MaterialItemProperties || property instanceof MaterialFluidProperties) {
                            for (PipeMaterialBlock pipe : MetaBlocks.MATERIAL_PIPES.get(modid)) {
                                String name = pipe.getStructure().getOrePrefix().name + material.toCamelCaseString();
                                ItemStack stack = pipe.getItem(material);
                                map.put(name, stack);
                            }
                        }
                }
            }
            metaBlockNames.put(modid, map);
        }
    }

    public static void clearComponentRegistry() {
        metaItemNames.clear();
        metaBlockNames.clear();
    }

    public static ItemStack getMetaItem(String name) {
        String[] split = MetaTileEntityBracketHandler.splitObjectName(name);
        String modid = split[0];
        String itemName = split[1];

        Map<String, ItemStack> itemMap = metaItemNames.get(modid);
        Map<String, ItemStack> blockMap = metaBlockNames.get(modid);

        ItemStack item;
        if (itemMap != null) {
            item = itemMap.get(itemName);
            if (item != null) return item.copy();
        }

        if (blockMap != null) {
            item = blockMap.get(itemName);
            if (item != null) return item.copy();
        }

        item = MetaTileEntityBracketHandler.getMetaTileEntityItem(split);
        if (item != null) return item.copy();

        if (GroovyScriptModule.isCurrentlyRunning()) {
            GroovyLog.get().error("Could not resolve metaitem('{}')", name);
        }
        return ItemStack.EMPTY;
    }

    // referenced via this class's constructor
    @SuppressWarnings("unused")
    public static IItemStack getCtMetaItem(String name) {
        ItemStack itemStack = getMetaItem(name);
        if (itemStack.isEmpty()) {
            CraftTweakerAPI.logError("Could not resolve <metaitem:" + name + ">");
            return MCItemStack.EMPTY;
        }
        return new MCItemStack(itemStack);
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
