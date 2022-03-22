package com.cleanroommc.fastvanillarecipes;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

@Mod.EventBusSubscriber
@Mod(modid = FastVanillaRecipes.MOD_ID)
public class FastVanillaRecipes {
    public static final String MOD_ID = "fastvanillarecipes";

    private static final Reference2ReferenceLinkedOpenHashMap<IRecipe,IRecipe> recipesCache = new Reference2ReferenceLinkedOpenHashMap<IRecipe,IRecipe>(){
        @Override
        public IRecipe put(IRecipe key, IRecipe value) {
            IRecipe o = super.put(key, value);
            if (size() > 2000) {
                removeLast();
            }
            return o;
        }
    };

    public static Reference2ReferenceLinkedOpenHashMap<IRecipe, IRecipe> getCache(){
        return recipesCache;
    }

    @Mod.EventHandler
    public void onPostInit(FMLServerStartedEvent event) {
        recipesCache.clear();
    }

}