package gtqt.common.VillagerHandler;

import gtqt.common.items.GTQTMetaItems;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@Mod.EventBusSubscriber
public class VillagerHandler {
    //交易列表
    private static final List<GregtechTradeList> GREGTECH_TRADE_LISTS_MAP=new ArrayList<>();
    // 定义职业常量
    public static final String FOLK_SCIENTIST_ID = "gregtech:folk_scientist";
    public static VillagerRegistry.VillagerProfession FOLK_SCIENTIST_PROFESSION;
    public static void Init_TradeList()
    {
        GREGTECH_TRADE_LISTS_MAP.add(new GregtechTradeList(GTQTMetaItems.GENERAL_CIRCUIT_LV.getStackForm()).ListItemForEmeralds( GTQTMetaItems.GENERAL_CIRCUIT_MV.getStackForm(),new EntityVillager.PriceInfo(2,8)));
        GREGTECH_TRADE_LISTS_MAP.add(new GregtechTradeList(GTQTMetaItems.GENERAL_CIRCUIT_MV.getStackForm()).ListItemForEmeralds( GTQTMetaItems.GENERAL_CIRCUIT_HV.getStackForm(),new EntityVillager.PriceInfo(2,8)));
        GREGTECH_TRADE_LISTS_MAP.add(new GregtechTradeList(GTQTMetaItems.GENERAL_CIRCUIT_HV.getStackForm()).ListItemForEmeralds( GTQTMetaItems.GENERAL_CIRCUIT_EV.getStackForm(),new EntityVillager.PriceInfo(2,8)));
    }
    @SubscribeEvent
    public static void onVillagerProfessionRegistration(RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
        Init_TradeList();
        //先注册一个村民的职业参数如下：
        // VillagerRegistry.VillagerProfession 的构造器接受三个 ResourceLocation 作为参数：
        // 第一个 ResourceLocation 是注册名。因为某些原因，它的构造器会调用 setRegistryName。
        // 第二个 ResourceLocation 指定了有这个职业的村民的纹理。必须指明 png 后缀。
        // 第三个 ResourceLocation 指定了有这个职业的僵尸村民的纹理。必须指明 png 后缀。
        //民间科学家职业
        // 创建并注册职业
        FOLK_SCIENTIST_PROFESSION = new VillagerRegistry.VillagerProfession(
                FOLK_SCIENTIST_ID,
                "gregtech:textures/entity/villagers/folk_scientist.png",
                "minecraft:textures/entity/zombie_villager/zombie_villager.png"
        );

        event.getRegistry().register(FOLK_SCIENTIST_PROFESSION);
        //子职业 lv
        VillagerRegistry.VillagerCareer newCareerLv = new VillagerRegistry.VillagerCareer(FOLK_SCIENTIST_PROFESSION, "level_lv");
        VillagerRegistry.VillagerCareer newCareerMv = new VillagerRegistry.VillagerCareer(FOLK_SCIENTIST_PROFESSION, "level_mv");
        VillagerRegistry.VillagerCareer newCareerHv = new VillagerRegistry.VillagerCareer(FOLK_SCIENTIST_PROFESSION, "level_hv");
        //增加交换数据
        newCareerLv.addTrade(1,GREGTECH_TRADE_LISTS_MAP.get(0));
        newCareerMv.addTrade(1,GREGTECH_TRADE_LISTS_MAP.get(1));
        newCareerHv.addTrade(1,GREGTECH_TRADE_LISTS_MAP.get(2));

    }


    public static class GregtechTradeList implements EntityVillager.ITradeList{
        //复制原版的绿宝石交易
        public ItemStack money;
        public ItemStack itemToBuy;
        public EntityVillager.PriceInfo priceInfo;

        public GregtechTradeList(){
            money = new ItemStack(Items.EMERALD);
        }
        public GregtechTradeList(ItemStack money){
            this.money = money;
        }
        public GregtechTradeList  ListItemForEmeralds(Item par1Item, EntityVillager.PriceInfo priceInfo)
        {
            this.itemToBuy = new ItemStack(par1Item);
            this.priceInfo = priceInfo;
            return this;
        }
        public GregtechTradeList ListItemForEmeralds(ItemStack stack, EntityVillager.PriceInfo priceInfo)
        {
            this.itemToBuy = stack;
            this.priceInfo = priceInfo;
            return this;
        }
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int i = 1;

            if (this.priceInfo != null)
            {
                i = this.priceInfo.getPrice(random);
            }

            ItemStack itemstack1;

            if (i < 0)
            {
                money.setCount(i);
                itemstack1 = new ItemStack(this.itemToBuy.getItem(), -i, this.itemToBuy.getMetadata());
            }
            else
            {
                money.setCount(i);
                itemstack1 = new ItemStack(this.itemToBuy.getItem(), 1, this.itemToBuy.getMetadata());
            }

            recipeList.add(new MerchantRecipe(money, itemstack1));
        }

    }
}
