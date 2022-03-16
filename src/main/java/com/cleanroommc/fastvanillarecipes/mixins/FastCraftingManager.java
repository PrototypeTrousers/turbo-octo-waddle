package com.cleanroommc.fastvanillarecipes.mixins;

import com.cleanroommc.fastvanillarecipes.maps.FastRecipeMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

import static com.cleanroommc.fastvanillarecipes.maps.FastRecipeMap.RECIPE_MAP_REGISTRY;

@Mixin(CraftingManager.class)
public class FastCraftingManager {

    @Final
    @Shadow
    public static final RegistryNamespaced<ResourceLocation, IRecipe> REGISTRY = net.minecraftforge.registries.GameData.getWrapper(IRecipe.class);

    /**
     * @author
     */
    @Overwrite
    @Nullable
    public static IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn)
    {
        ItemStack[] itemStacks = new ItemStack[craftMatrix.getSizeInventory()];
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            itemStacks[i] = craftMatrix.getStackInSlot(i);
        }
        for (FastRecipeMap fastRecipeMap : RECIPE_MAP_REGISTRY.values()) {
            IRecipe irecipe = fastRecipeMap.find(itemStacks);
            if (irecipe != null) {
                return irecipe;
            }
        }
        for (IRecipe irecipe1 : REGISTRY) {
            if (irecipe1.getClass() == ShapedRecipes.class || irecipe1.getClass() == ShapelessRecipes.class || irecipe1.getClass() == ShapedOreRecipe.class || irecipe1.getClass() ==ShapelessOreRecipe.class) {
                continue;
            }
            if (irecipe1.matches(craftMatrix, worldIn)) {
                return irecipe1;
            }
        }
        return null;
    }
}
