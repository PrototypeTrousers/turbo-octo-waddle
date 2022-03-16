package com.cleanroommc.fastvanillarecipes;

import com.cleanroommc.fastvanillarecipes.maps.FastRecipeMap;
import com.cleanroommc.fastvanillarecipes.mixins.FastCraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static com.cleanroommc.fastvanillarecipes.maps.FastRecipeMap.RECIPE_MAP_REGISTRY;
import static net.minecraft.item.crafting.CraftingManager.REGISTRY;

@Mod(modid = FastVanillaRecipes.MOD_ID)
public class FastVanillaRecipes {
    public static final String MOD_ID = "fastvanillarecipes";

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent ev) {
        init();
    }

    public void init() {
        for (IRecipe irecipe : REGISTRY) {
            if (irecipe instanceof ShapedRecipes || irecipe instanceof ShapedOreRecipe) {
                if (RECIPE_MAP_REGISTRY.get(irecipe.getClass().getName()) == null) {
                    new FastRecipeMap(irecipe.getClass().getName());
                }
                RECIPE_MAP_REGISTRY.get(irecipe.getClass().getName()).compileRecipe(irecipe, true);
            }
            if (irecipe instanceof ShapelessRecipes || irecipe instanceof ShapelessOreRecipe) {
                if (RECIPE_MAP_REGISTRY.get(irecipe.getClass().getName()) == null) {
                    new FastRecipeMap(irecipe.getClass().getName());
                }
                RECIPE_MAP_REGISTRY.get(irecipe.getClass().getName()).compileRecipe(irecipe, false);
            }
        }
    }
}