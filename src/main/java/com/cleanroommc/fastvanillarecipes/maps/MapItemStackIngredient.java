package com.cleanroommc.fastvanillarecipes.maps;

import net.minecraft.item.ItemStack;

import java.util.Objects;

// TODO: NBT flexibility
public class MapItemStackIngredient extends AbstractMapIngredient {

    public static final MapItemStackIngredient EMPTY = new MapItemStackIngredient(ItemStack.EMPTY);
    public final ItemStack stack;

    public MapItemStackIngredient(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            return ItemStack.areItemsEqual(stack, other.stack) && ItemStack.areItemStackTagsEqual(stack, other.stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        // TODO: can be improved
        return Objects.hash(stack.getItem(), stack.getItemDamage());
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" +
                "item=" + stack.getItem().getRegistryName() +
                '}';
    }

}