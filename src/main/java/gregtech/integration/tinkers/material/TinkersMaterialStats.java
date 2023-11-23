package gregtech.integration.tinkers.material;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.integration.tinkers.TinkersConfig;
import gregtech.integration.tinkers.TinkersUtil;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.ArrowShaftMaterialStats;
import slimeknights.tconstruct.library.materials.BowStringMaterialStats;
import slimeknights.tconstruct.library.materials.BowMaterialStats;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.FletchingMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.materials.ProjectileMaterialStats;
import slimeknights.tconstruct.library.traits.ITrait;

import java.util.*;

import static slimeknights.tconstruct.library.materials.Material.UNKNOWN;

public class TinkersMaterialStats {

    private final Material gtMaterial;

    private final Map<String, IMaterialStats> allStats = new HashMap<>();
    private final Map<ITrait, List<String>> allTraits = new HashMap<>();

    private final Map<UnificationEntry, Integer> additionalItems = new HashMap<>();

    private Boolean castOverride;
    private Boolean craftOverride;
    private UnificationEntry representativeItemOverride;
    private boolean ignoreFluid;

    private TinkersMaterialStats(Material gtMaterial) {
        this.gtMaterial = gtMaterial;
    }

    private void setStat(String type, IMaterialStats stat) {
        allStats.put(type, stat);
    }

    public void register() {
        TMaterial tconMaterial = new TMaterial(gtMaterial.toString(), gtMaterial.getMaterialRGB());
        String formattedName = TinkersUtil.getFormattedName(gtMaterial);

        if (!ignoreFluid && gtMaterial.hasProperty(PropertyKey.FLUID)) {
            tconMaterial.setFluid(gtMaterial.getFluid());
            TinkersUtil.registerMelting(tconMaterial, gtMaterial);
        }

        for (Map.Entry<UnificationEntry, Integer> entry : additionalItems.entrySet()) {
            if (entry.getValue() != 0) {
                tconMaterial.addItem(entry.getKey().toString(), 1, entry.getValue());
            } else tconMaterial.addItem(entry.getKey().toString());
        }

        if (gtMaterial.hasProperty(PropertyKey.INGOT)) {
            tconMaterial.setCastable(castOverride == null || castOverride); // default true, unless overridden
            tconMaterial.setCraftable(craftOverride != null && craftOverride); // default false, unless overridden
            tconMaterial.addCommonItems(formattedName);
            tconMaterial.addItemIngot(new UnificationEntry(OrePrefix.ingot, gtMaterial).toString());
            tconMaterial.setRepresentativeItem(new UnificationEntry(OrePrefix.ingot, gtMaterial).toString());
        } else if (gtMaterial.hasProperty(PropertyKey.GEM)) {
            tconMaterial.setCastable(false); // always false, since no fluid should exist
            // todo is this how we want to make gem tool parts?
            tconMaterial.setCraftable(craftOverride == null || craftOverride); // default true, unless overridden
            tconMaterial.addItem(new UnificationEntry(OrePrefix.gem, gtMaterial).toString(), 1, 144);
            tconMaterial.addItem(new UnificationEntry(OrePrefix.gemFlawless, gtMaterial).toString(), 1, 288);
            tconMaterial.addItem(new UnificationEntry(OrePrefix.gemExquisite, gtMaterial).toString(), 1, 576);
            if (ConfigHolder.recipes.generateLowQualityGems) {
                tconMaterial.addItem(new UnificationEntry(OrePrefix.gemChipped, gtMaterial).toString(), 1, 36);
                tconMaterial.addItem(new UnificationEntry(OrePrefix.gemFlawed, gtMaterial).toString(), 1, 72);
            }
            tconMaterial.setRepresentativeItem(new UnificationEntry(OrePrefix.gem, gtMaterial).toString());
        }

        if (representativeItemOverride != null) {
            tconMaterial.setRepresentativeItem(representativeItemOverride.toString());
        }

        if (TinkerRegistry.getMaterial(tconMaterial.identifier) == UNKNOWN) {
            TinkerRegistry.addMaterial(tconMaterial);
            registerStatsTraits(tconMaterial);
            if (!ignoreFluid && gtMaterial.hasProperty(PropertyKey.FLUID)) {
                TinkerRegistry.integrate(tconMaterial, tconMaterial.getFluid(), formattedName);
            }
        } else { // try to add these stats to an existing material TODO might cause issues?
            registerStatsTraits(TinkerRegistry.getMaterial(tconMaterial.identifier));
        }
    }

    private void registerStatsTraits(slimeknights.tconstruct.library.materials.Material material) {
        for (IMaterialStats stat : allStats.values()) {
            material.addStats(stat);
        }
        for (var entry : allTraits.entrySet()) {
            ITrait trait = entry.getKey();
            if (entry.getValue() == null) {
                material.addTrait(trait);
            } else for (String part : entry.getValue()) {
                material.addTrait(trait, part);
            }
        }
    }

    public static Builder builder(Material gtMaterial) {
        return new Builder(gtMaterial);
    }

    /** Requires the GT Material provided to have a Tool Property */
    public static Builder createIngotTemplate(Material gtMaterial) {
        ToolProperty prop = gtMaterial.getProperty(PropertyKey.TOOL);
        if (prop == null) {
            throw new IllegalArgumentException("Cannot auto-initialize tinkers tool stats from material with no tool property!");
        }

        int durability = prop.getToolDurability();
        float speed = prop.getToolSpeed();
        float damage = prop.getToolAttackDamage();
        int harvestLevel = gtMaterial.getToolHarvestLevel();

        // TODO bow, arrow shaft
        return builder(gtMaterial)
                .setHead((int) (durability * 0.8), speed, damage, harvestLevel)
                .setHandle((harvestLevel - 0.5f) / 2, durability / 3)
                .setExtra(durability / 4)
                .setProjectile();
        //.setBow()
        //.setArrowShaft();
    }

    /** Requires the GT Material provided to have a Tool Property */
    public static Builder createGemTemplate(Material gtMaterial) {
        ToolProperty prop = gtMaterial.getProperty(PropertyKey.TOOL);
        if (prop == null) {
            throw new IllegalArgumentException("Cannot auto-initialize tinkers tool stats from material with no tool property!");
        }

        int durability = prop.getToolDurability();
        float speed = prop.getToolSpeed();
        float damage = prop.getToolAttackDamage();
        int harvestLevel = gtMaterial.getToolHarvestLevel();

        // TODO bow, arrow shaft
        return builder(gtMaterial)
                .setHead(durability, speed, damage, harvestLevel)
                .setHandle(harvestLevel - 0.5f, durability / 4)
                .setExtra(durability / 100)
                .setProjectile();
        //.setBow()
        //.setArrowShaft();
    }

    public static Builder createPolymerTemplate(Material gtMaterial) {
        return builder(gtMaterial)
                .setIgnoreFluid()
                .addItems(new UnificationEntry(OrePrefix.plate, gtMaterial), 144)
                .setRepresentativeItem(new UnificationEntry(OrePrefix.plate, gtMaterial))
                .setCraftOverride(true);
    }

    public static class Builder {

        private final TinkersMaterialStats stats;
        private boolean setCancelled;

        private Builder (Material gtMaterial) {
            stats = new TinkersMaterialStats(gtMaterial);
        }

        /**
         * @param durability   Base value for durability calculations
         * @param miningSpeed  How fast a tool head of this material can break blocks
         * @param attackDamage Base value for attack calculations
         * @param harvestLevel What range of blocks at tool with a tool head of this material can mine
         */
        public Builder setHead(int durability, float miningSpeed, float attackDamage, int harvestLevel) {
            stats.setStat(MaterialTypes.HEAD, new HeadMaterialStats(
                    (int) (durability * TinkersConfig.toolStats.durabilityModifier),
                    miningSpeed * (float) TinkersConfig.toolStats.miningSpeedModifier,
                    attackDamage * (float) TinkersConfig.toolStats.attackDamageModifier,
                    harvestLevel));
            return this;
        }

        /**
         * @param modifier   The total durability of the tool will be multiplied by this
         * @param durability Tool durability will be changed by this amount
         */
        public Builder setHandle(float modifier, int durability) {
            stats.setStat(MaterialTypes.HANDLE, new HandleMaterialStats(
                    modifier * (float) TinkersConfig.toolStats.handleModifier,
                    durability));
            return this;
        }

        /**
         * @param extraDurability How much durability this part contributes when used as an accessory
         */
        public Builder setExtra(int extraDurability) {
            stats.setStat(MaterialTypes.EXTRA, new ExtraMaterialStats(extraDurability));
            return this;
        }

        /**
         * @param drawSpeed   How fast you can draw the bow
         * @param range       How far the projectile can be propelled (multiplier)
         * @param bonusDamage Bonus damage dealt on hit. The force of the arrow
         */
        public Builder setBow(float drawSpeed, float range, float bonusDamage) {
            stats.setStat(MaterialTypes.BOW, new BowMaterialStats(
                    drawSpeed * (float) TinkersConfig.toolStats.bowDrawSpeedModifier,
                    range * (float) TinkersConfig.toolStats.bowFlightSpeedModifier,
                    bonusDamage * (float) TinkersConfig.toolStats.arrowMassModifier));
            return this;
        }

        /**
         * @param modifier The tool durability will be multiplied by this
         */
        public Builder setBowString(float modifier) {
            stats.setStat(MaterialTypes.BOWSTRING, new BowStringMaterialStats(modifier));
            return this;
        }

        /**
         * @param accuracy How stable the flight path will be using this fletching
         * @param modifier How many arrows you can craft with this. Projectile ammo will be multiplied by this
         */
        public Builder setFletching(float accuracy, float modifier) {
            stats.setStat(MaterialTypes.FLETCHING, new FletchingMaterialStats(accuracy, modifier));
            return this;
        }

        // TODO is this useful?
        public Builder setProjectile() {
            stats.setStat(MaterialTypes.PROJECTILE, new ProjectileMaterialStats());
            return this;
        }

        /**
         * @param modifier  The total ammo count of the tool will be multiplied by this
         * @param bonusAmmo This much flat ammo will be added
         */
        public Builder setArrowShaft(float modifier, int bonusAmmo) {
            stats.setStat(MaterialTypes.SHAFT, new ArrowShaftMaterialStats(
                    modifier * (float) TinkersConfig.toolStats.arrowAmmoModifier,
                    bonusAmmo));
            return this;
        }

        public Builder addTrait(ITrait trait, String... parts) {
            List<String> traitParts = stats.allTraits.computeIfAbsent(trait, k -> new ArrayList<>());
            traitParts.addAll(Arrays.asList(parts));
            return this;
        }

        public Builder setCastOverride(boolean canCast) {
            stats.castOverride = canCast;
            return this;
        }

        public Builder setCraftOverride(boolean canCraft) {
            stats.craftOverride = canCraft;
            return this;
        }

        public Builder addItems(UnificationEntry item, int amount) {
            stats.additionalItems.put(item, amount);
            return this;
        }

        public Builder setRepresentativeItem(UnificationEntry item) {
            stats.representativeItemOverride = item;
            return this;
        }

        public Builder setIgnoreFluid() {
            stats.ignoreFluid = true;
            return this;
        }

        public Builder cancel() {
            setCancelled = true;
            return this;
        }

        public void build() {
            if (!setCancelled) {
                stats.register();
            }
        }
    }
}
