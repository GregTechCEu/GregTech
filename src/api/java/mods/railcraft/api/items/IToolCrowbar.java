/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2020

 This work (the API) is licensed under the "MIT" License,
 see LICENSE.md for details.
 -----------------------------------------------------------------------------*/
package mods.railcraft.api.items;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IToolCrowbar {
    String ORE_TAG = "toolCrowbar";

    /**
     * Controls non-rotational interactions with blocks. Crowbar specific stuff.
     * <p/>
     * Rotational interaction is handled by the Block.rotateBlock() function,
     * which should be called from the Item.onUseFirst() function of your tool.
     *
     * @param player  the player
     * @param crowbar the crowbar
     * @param pos     the block   @return true if can whack a block
     */
    boolean canWhack(EntityPlayer player, EnumHand hand, ItemStack crowbar, BlockPos pos);

    /**
     * Callback to do damage to the item.
     *
     * @param player  the player
     * @param crowbar the crowbar
     * @param pos     the block
     */
    void onWhack(EntityPlayer player, EnumHand hand, ItemStack crowbar, BlockPos pos);

    /**
     * Controls whether you can link a cart.
     *
     * @param player  the player
     * @param crowbar the crowbar
     * @param cart    the cart   @return true if can link a cart
     */
    boolean canLink(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart);

    /**
     * Callback to do damage.
     *
     * @param player  the player
     * @param crowbar the crowbar
     * @param cart    the cart
     */
    void onLink(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart);

    /**
     * Controls whether you can boost a cart.
     *
     * @param player  the player
     * @param crowbar the crowbar
     * @param cart    the cart   @return true if can boost a cart
     */
    boolean canBoost(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart);

    /**
     * Callback to do damage, boosting a cart usually does more damage than
     * normal usage.
     *
     * @param player  the player
     * @param crowbar the crowbar
     * @param cart    the cart
     */
    void onBoost(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart);
}
