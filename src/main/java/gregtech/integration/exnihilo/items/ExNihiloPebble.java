/*
    Copyright 2019, TheLimePixel, dan, Irgendwer01
    GregBlock Utilities

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gregtech.integration.exnihilo.items;

import exnihilocreatio.ExNihiloCreatio;
import exnihilocreatio.entities.ProjectileStone;
import exnihilocreatio.util.Data;
import exnihilocreatio.util.IHasModel;
import gregtech.integration.exnihilo.ExNihiloModule;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ExNihiloPebble extends Item implements IHasModel {
    public ExNihiloPebble() {
        setTranslationKey("gtPebble");
        setRegistryName("gtPebble");
        setCreativeTab(ExNihiloCreatio.tabExNihilo);
        setHasSubtypes(true);
        Data.ITEMS.add(this);
    }

    @Override
    @Nonnull
    public String getTranslationKey(@Nonnull ItemStack stack) {
        return String.format("%s.%s", getTranslationKey(), GTPebbles.VALUES[stack.getItemDamage()].getName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nullable CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        if (tab != null && isInCreativeTab(tab)) {
            for (GTPebbles pebble : GTPebbles.VALUES) {
                list.add(new ItemStack(ExNihiloModule.GTPebbles, 1, pebble.ordinal()));
            }
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!stack.isEmpty()) {
            world.playSound(player, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!world.isRemote) {
                ItemStack thrown = stack.copy();
                thrown.setCount(1);

                ProjectileStone projectile = new ProjectileStone(world, player);
                projectile.setStack(thrown);
                projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 0.5F);
                world.spawnEntity(projectile);
            }

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel(ModelRegistryEvent e) {
        ModelResourceLocation[] locations = new ModelResourceLocation[GTPebbles.VALUES.length];
        for (GTPebbles pebble : GTPebbles.VALUES) {
            locations[pebble.ordinal()] = new ModelResourceLocation(getRegistryName(), String.format("type=%s", pebble.getName()));
        }

        ModelBakery.registerItemVariants(this, locations);
        ModelLoader.setCustomMeshDefinition(this, stack -> locations[stack.getMetadata()]);
    }

    private enum GTPebbles implements IStringSerializable {
        BASALT("basalt"),
        BLACK_GRANITE("black_granite"),
        MARBLE("marble"),
        RED_GRANITE("red_granite");

        private final String name;
        public static final GTPebbles[] VALUES = values();

        GTPebbles(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}
