package gregtech.api.items.metaitem;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.api.util.SmallDigits;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaOreDictItem extends StandardMetaItem {

    public final Map<String, String> OREDICT_TO_FORMULA = new HashMap<>();
    private final Map<Short, OreDictValueItem> ITEMS = new HashMap<>();
    private static final List<MaterialIconType> DISALLOWED_TYPES = ImmutableList.of(
            MaterialIconType.block, MaterialIconType.foilBlock, MaterialIconType.wire,
            MaterialIconType.ore, MaterialIconType.frameGt, MaterialIconType.pipeHuge,
            MaterialIconType.pipeLarge, MaterialIconType.pipeSide, MaterialIconType.pipeSmall,
            MaterialIconType.pipeMedium, MaterialIconType.pipeTiny);
    private static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing", "inventory");

    public MetaOreDictItem(short metaItemOffset) {
        super(metaItemOffset);
    }

    @Override
    public void registerSubItems() {
        for (OreDictValueItem item : ITEMS.values()) {
            addItem(item.id, item.getName());
            OreDictUnifier.registerOre(new ItemStack(this, 1, item.id), item.getOre());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            OreDictValueItem item = ITEMS.get((short) stack.getItemDamage());
            return item == null ? 0xFFFFFF : item.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        TIntObjectHashMap<ModelResourceLocation> alreadyRegistered = new TIntObjectHashMap<>();
        for (Map.Entry<Short, OreDictValueItem> metaItem : ITEMS.entrySet()) {
            OrePrefix prefix = metaItem.getValue().orePrefix;
            MaterialIconSet materialIconSet = metaItem.getValue().materialIconSet;
            if (prefix.materialIconType == null || DISALLOWED_TYPES.contains(prefix.materialIconType))
                continue;
            int registrationKey = prefix.id * 1000 + materialIconSet.id;
            if (!alreadyRegistered.containsKey(registrationKey)) {
                prefix.materialIconType.getItemModelPath(materialIconSet);
                ResourceLocation resourceLocation = prefix.materialIconType.getItemModelPath(materialIconSet);
                ModelBakery.registerItemVariants(this, resourceLocation);
                alreadyRegistered.put(registrationKey, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem.getKey(), resourceLocation);
        }
    }

    @SuppressWarnings("unused")
    public OreDictValueItem addOreDictItem(int id, String materialName, int rgb, MaterialIconSet materialIconSet, OrePrefix orePrefix) {
        return this.addOreDictItem(id, materialName, rgb, materialIconSet, orePrefix, null);
    }

    public OreDictValueItem addOreDictItem(int id, String materialName, int materialRGB, MaterialIconSet materialIconSet, OrePrefix orePrefix, String chemicalFormula) {
        return new OreDictValueItem((short) id, materialName, materialRGB, materialIconSet, orePrefix, chemicalFormula);
    }

    public class OreDictValueItem {

        private final String materialName;
        private final int materialRGB;
        private final MaterialIconSet materialIconSet;
        private final short id;
        private final OrePrefix orePrefix;

        protected String chemicalFormula;

        private OreDictValueItem(short id, String materialName, int materialRGB, MaterialIconSet materialIconSet, OrePrefix orePrefix, String chemicalFormula) {
            this.id = id;
            this.materialName = materialName;
            this.materialRGB = materialRGB;
            this.materialIconSet = materialIconSet;
            this.orePrefix = orePrefix;
            this.chemicalFormula = chemicalFormula;
            MetaOreDictItem.this.ITEMS.put(this.id, this);
            MetaOreDictItem.this.OREDICT_TO_FORMULA.put(this.getOre(), calculateChemicalFormula(chemicalFormula));
        }

        public String getOre() {
            return orePrefix.name() + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, materialName);
        }

        public ItemStack getItemStack(int amount) {
            ItemStack stack = OreDictUnifier.get(getOre());
            stack.setCount(amount);
            return stack;
        }

        public ItemStack getItemStack() {
            return getItemStack(1);
        }

        public String getName() {
            return materialName + '_' + GTUtility.toLowerCaseUnderscore(orePrefix.name());
        }

        protected String calculateChemicalFormula(String unformattedFormula) {
            StringBuilder sb = new StringBuilder();
            if (unformattedFormula != null && !unformattedFormula.isEmpty()) {
                for (char c : unformattedFormula.toCharArray()) {
                    if (Character.isDigit(c))
                        sb.append(SmallDigits.toSmallDownNumbers(Character.toString(c)));
                    else
                        sb.append(c);
                }
            }
            return sb.toString(); // returns "" if no formula, like other method
        }

        public String getFormula() {
            return chemicalFormula;
        }

        public int getMaterialRGB() {
            return materialRGB;
        }
    }

}
