package gtqt.common.VillagerHandler;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;

import gregtech.common.items.MetaItems;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.common.items.MetaItems.*;

@Mod.EventBusSubscriber
public class VillagerHandler {

    // 常量定义
    public static final String FOLK_SCIENTIST_ID = "gregtech:folk_scientist";
    private static final String FOLK_SCIENTIST_TEXTURE = "gregtech:textures/entity/villagers/folk_scientist.png";
    private static final String ZOMBIE_VILLAGER_TEXTURE = "minecraft:textures/entity/zombie_villager/zombie_villager.png";

    // 职业等级常量
    private static final String CAREER_LEVEL_LV = "level_lv";
    private static final String CAREER_LEVEL_MV = "level_mv";
    private static final String CAREER_LEVEL_HV = "level_hv";

    // 交易列表
    private static final List<GregtechTradeList> GREGTECH_TRADE_LISTS = new ArrayList<>();

    public static VillagerRegistry.VillagerProfession FOLK_SCIENTIST_PROFESSION;

    /**
     * 初始化交易列表
     */
    private static void initTradeLists() {
        //自定义交易列表参数详解   构造参数传递的货币，然后调用的ListItemForEmeralds ,这个方法俩参数  第一个 传递货物(ItemStack) 和货物的数量范围 PriceInfo

        //第一位表示货币
        //第二位表示等价货币可购买的商品
        ItemStack copperIngot = OreDictUnifier.get(OrePrefix.ingot, Copper);
        if (copperIngot.isEmpty()) {
            System.out.println("Copper ingot is empty!");
        }
        //材料
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_COPPER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Copper), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_COPPER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Tin), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_COPPER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Lead), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_COPPER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.gem, Coal), new EntityVillager.PriceInfo(1, 4)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_CUPRONICKEL.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Steel), new EntityVillager.PriceInfo(1, 2)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_CUPRONICKEL.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Gold), new EntityVillager.PriceInfo(1, 2)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_CUPRONICKEL.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Invar), new EntityVillager.PriceInfo(1, 2)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_CUPRONICKEL.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Bronze), new EntityVillager.PriceInfo(1, 2)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Aluminium), new EntityVillager.PriceInfo(1, 2)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.ingot, Gold), new EntityVillager.PriceInfo(1, 2)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(OreDictUnifier.get(OrePrefix.gem, Diamond), new EntityVillager.PriceInfo(1, 2)));

        //杂项
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_CUPRONICKEL.getStackForm())
                .setItemForCurrency(new ItemStack(Blocks.GLASS), new EntityVillager.PriceInfo(1, 2)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(new ItemStack(Blocks.PISTON), new EntityVillager.PriceInfo(1, 2)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(new ItemStack(Blocks.STICKY_PISTON), new EntityVillager.PriceInfo(1, 2)));

        //SMD
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(RESISTOR.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(DIODE.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(CAPACITOR.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(TRANSISTOR.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(INDUCTOR.getStackForm(), new EntityVillager.PriceInfo(1, 4)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(SMD_CAPACITOR.getStackForm(), new EntityVillager.PriceInfo(4, 16)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(SMD_DIODE.getStackForm(), new EntityVillager.PriceInfo(4, 16)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(SMD_RESISTOR.getStackForm(), new EntityVillager.PriceInfo(4, 16)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(SMD_TRANSISTOR.getStackForm(), new EntityVillager.PriceInfo(4, 16)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(SMD_INDUCTOR.getStackForm(), new EntityVillager.PriceInfo(4, 16)));

        //电路
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_CUPRONICKEL.getStackForm())
                .setItemForCurrency(VACUUM_TUBE.getStackForm(), new EntityVillager.PriceInfo(1, 2)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(ELECTRONIC_CIRCUIT_LV.getStackForm(), new EntityVillager.PriceInfo(2, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(ELECTRONIC_CIRCUIT_MV.getStackForm(), new EntityVillager.PriceInfo(1, 2)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(INTEGRATED_CIRCUIT_LV.getStackForm(), new EntityVillager.PriceInfo(4, 8)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(INTEGRATED_CIRCUIT_MV.getStackForm(), new EntityVillager.PriceInfo(2, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(INTEGRATED_CIRCUIT_HV.getStackForm(), new EntityVillager.PriceInfo(1, 2)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(PROCESSOR_MV.getStackForm(), new EntityVillager.PriceInfo(8, 16)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(PROCESSOR_ASSEMBLY_HV.getStackForm(), new EntityVillager.PriceInfo(4, 8)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(WORKSTATION_EV.getStackForm(), new EntityVillager.PriceInfo(2, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(MAINFRAME_IV.getStackForm(), new EntityVillager.PriceInfo(1, 2)));

        //部件
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(ELECTRIC_PISTON_LV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(ELECTRIC_MOTOR_LV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(CONVEYOR_MODULE_LV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_SILVER.getStackForm())
                .setItemForCurrency(ELECTRIC_PUMP_LV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(ELECTRIC_PISTON_MV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(ELECTRIC_MOTOR_MV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(CONVEYOR_MODULE_MV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_GOLD.getStackForm())
                .setItemForCurrency(ELECTRIC_PUMP_MV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));

        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(ELECTRIC_PISTON_HV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(ELECTRIC_MOTOR_HV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(CONVEYOR_MODULE_HV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
        GREGTECH_TRADE_LISTS.add(new GregtechTradeList(CREDIT_PLATINUM.getStackForm())
                .setItemForCurrency(ELECTRIC_PUMP_HV.getStackForm(), new EntityVillager.PriceInfo(1, 4)));
    }

    @SubscribeEvent
    public static void onVillagerProfessionRegistration(
            RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {


        // 创建并注册民间科学家职业
        FOLK_SCIENTIST_PROFESSION = new VillagerRegistry.VillagerProfession(
                FOLK_SCIENTIST_ID,
                FOLK_SCIENTIST_TEXTURE,
                ZOMBIE_VILLAGER_TEXTURE
        );

        event.getRegistry().register(FOLK_SCIENTIST_PROFESSION);

    }

    public static void registerTrade(){
        initTradeLists();
        // 创建三个子职业等级
        VillagerRegistry.VillagerCareer careerLv = new VillagerRegistry.VillagerCareer(FOLK_SCIENTIST_PROFESSION,
                CAREER_LEVEL_LV);
        VillagerRegistry.VillagerCareer careerMv = new VillagerRegistry.VillagerCareer(FOLK_SCIENTIST_PROFESSION,
                CAREER_LEVEL_MV);
        VillagerRegistry.VillagerCareer careerHv = new VillagerRegistry.VillagerCareer(FOLK_SCIENTIST_PROFESSION,
                CAREER_LEVEL_HV);

        // 为每个职业等级添加对应的交易
        for (GregtechTradeList gregtechTradeList : GREGTECH_TRADE_LISTS) {
            if (isItemEqual(gregtechTradeList.currencyStack, CREDIT_PLATINUM)) {
                careerHv.addTrade(3, gregtechTradeList);
            } else if (isItemEqual(gregtechTradeList.currencyStack, CREDIT_GOLD)) {
                careerHv.addTrade(2, gregtechTradeList);
                careerMv.addTrade(3, gregtechTradeList);
            } else if (isItemEqual(gregtechTradeList.currencyStack, CREDIT_SILVER)) {
                careerHv.addTrade(1, gregtechTradeList);
                careerMv.addTrade(2, gregtechTradeList);
                careerLv.addTrade(3, gregtechTradeList);
            } else if (isItemEqual(gregtechTradeList.currencyStack, CREDIT_CUPRONICKEL)) {
                careerHv.addTrade(1, gregtechTradeList);
                careerMv.addTrade(1, gregtechTradeList);
                careerLv.addTrade(2, gregtechTradeList);
            } else if (isItemEqual(gregtechTradeList.currencyStack, CREDIT_COPPER)) {
                careerHv.addTrade(1, gregtechTradeList);
                careerMv.addTrade(1, gregtechTradeList);
                careerLv.addTrade(1, gregtechTradeList);
            }
        }
    }

    public static boolean isItemEqual(ItemStack item1, MetaItem<?>.MetaValueItem item2) {
        return item1.getItem() == MetaItems.GT_META_ITEM && item1.getMetadata() == item2.getMetaValue();
    }
    /**
     * 自定义GregTech交易列表实现
     */
    public static class GregtechTradeList implements EntityVillager.ITradeList {

        public ItemStack currencyStack;
        public ItemStack goodsStack;
        private EntityVillager.PriceInfo priceInfo;

        public GregtechTradeList() {
            this.currencyStack = new ItemStack(Items.EMERALD);
        }

        public GregtechTradeList(ItemStack currencyStack) {
            this.currencyStack = currencyStack.copy();
        }

        /**
         * 设置用货币购买物品的交易
         *
         * @param item      要购买的物品
         * @param priceInfo 价格信息
         * @return 当前交易列表实例，用于链式调用
         */
        public GregtechTradeList setItemForCurrency(ItemStack item, EntityVillager.PriceInfo priceInfo) {
            this.goodsStack = item.copy();
            this.priceInfo = priceInfo;
            return this;
        }

        @Override
        public void addMerchantRecipe(@NotNull IMerchant merchant, @NotNull MerchantRecipeList recipeList,
                                      @NotNull Random random) {
            if (goodsStack == null || priceInfo == null) {
                return; // 未正确初始化的交易不添加
            }

            int price = priceInfo.getPrice(random);
            ItemStack priceStack = currencyStack.copy();

            if (price < 0) {
                // 负数价格表示玩家获得货币，村民获得物品
                priceStack.setCount(-price);
                ItemStack goodsCopy = goodsStack.copy();
                goodsCopy.setCount(1);
                recipeList.add(new MerchantRecipe(goodsCopy, priceStack));
            } else {
                // 正数价格表示玩家支付货币获得物品
                priceStack.setCount(price);
                ItemStack goodsCopy = goodsStack.copy();
                goodsCopy.setCount(1);
                recipeList.add(new MerchantRecipe(priceStack, goodsCopy));
            }
        }
    }
}
