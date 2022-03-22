package com.cleanroommc.fastvanillarecipes.mixins;

import com.cleanroommc.fastvanillarecipes.FastVanillaRecipes;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(CraftingManager.class)
public class FastCraftingManager {

    @Final
    @Shadow
    public static final RegistryNamespaced<ResourceLocation, IRecipe> REGISTRY = GameData.getWrapper(IRecipe.class);

    /**
     * @author
     */
    @Overwrite
    @Nullable
    public static IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn)
    {
        for (Reference2ReferenceMap.Entry<IRecipe, IRecipe> cachedRecipe : FastVanillaRecipes.getCache().reference2ReferenceEntrySet()) {
            if (cachedRecipe.getKey() == null) continue;
            if (cachedRecipe.getKey().matches(craftMatrix, worldIn)) {
                FastVanillaRecipes.getCache().getAndMoveToFirst(cachedRecipe.getKey());
                return cachedRecipe.getKey();
            }
        }
        for (IRecipe irecipe1 : REGISTRY) {
            if (FastVanillaRecipes.getCache().containsKey(irecipe1)) continue;
            if (irecipe1.matches(craftMatrix, worldIn)) {
                FastVanillaRecipes.getCache().putIfAbsent(irecipe1,irecipe1);
                return irecipe1;
            }
        }
        return null;
    }

    /**
     * @author
     */
    @Overwrite
    public static NonNullList<ItemStack> getRemainingItems(InventoryCrafting craftMatrix, World worldIn) {

        for (Reference2ReferenceMap.Entry<IRecipe, IRecipe> cachedRecipe : FastVanillaRecipes.getCache().reference2ReferenceEntrySet()) {
            if (cachedRecipe.getKey() == null) continue;
            if (cachedRecipe.getKey().matches(craftMatrix, worldIn)) {
                FastVanillaRecipes.getCache().getAndMoveToFirst(cachedRecipe.getKey());
                return cachedRecipe.getKey().getRemainingItems(craftMatrix);
            }
        }
        for (IRecipe irecipe1 : REGISTRY) {
            if (FastVanillaRecipes.getCache().containsKey(irecipe1)) continue;
            if (irecipe1.matches(craftMatrix, worldIn)) {
                FastVanillaRecipes.getCache().putIfAbsent(irecipe1,irecipe1);
                return irecipe1.getRemainingItems(craftMatrix);
            }
        }

        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftMatrix.getSizeInventory(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            nonnulllist.set(i, craftMatrix.getStackInSlot(i));
        }

        return nonnulllist;
    }

    /**
     * @author
     */
    @Overwrite
    public static ItemStack findMatchingResult(InventoryCrafting craftMatrix, World worldIn) {
        for (Reference2ReferenceMap.Entry<IRecipe, IRecipe> cachedRecipe : FastVanillaRecipes.getCache().reference2ReferenceEntrySet()) {
            if (cachedRecipe.getKey() == null) continue;
            if (cachedRecipe.getKey().matches(craftMatrix, worldIn)) {
                FastVanillaRecipes.getCache().getAndMoveToFirst(cachedRecipe.getKey());
                return cachedRecipe.getKey().getCraftingResult(craftMatrix);
            }
        }
        for (IRecipe irecipe1 : REGISTRY) {
            if (FastVanillaRecipes.getCache().containsKey(irecipe1)) continue;
            if (irecipe1.matches(craftMatrix, worldIn)) {
                FastVanillaRecipes.getCache().putIfAbsent(irecipe1,irecipe1);
                return irecipe1.getCraftingResult(craftMatrix);
            }
        }

        return ItemStack.EMPTY;
    }
}
