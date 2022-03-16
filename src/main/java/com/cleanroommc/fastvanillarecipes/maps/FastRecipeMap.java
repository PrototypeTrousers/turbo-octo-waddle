package com.cleanroommc.fastvanillarecipes.maps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class FastRecipeMap {

    public static final Map<String, FastRecipeMap> RECIPE_MAP_REGISTRY = new Object2ReferenceOpenHashMap<>();

    private final Branch lookup = new Branch();
    private final Set<AbstractMapIngredient> root = new ObjectOpenHashSet<>();

    public FastRecipeMap(String unlocalizedName) {
        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);
    }

    public void compileRecipe(IRecipe recipe, boolean isShaped) {
        if (recipe == null) {
            return;
        }
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe, isShaped);
        if (recurseItemTreeAdd(recipe, items, lookup, 0, 0, isShaped)) {
            items.forEach(root::addAll);
        }
    }

    @Nullable
    public IRecipe find(@Nonnull ItemStack[] items) {

        // First, check if items and fluids are valid.
        if (items.length> Long.SIZE) {
            return null;
        }
        if (items.length == 0 )
            return null;
        // Filter out empty fluids.

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.length);
        buildFromItemStacks(list, items);

        if (list.size() == 0)
            return null;

        return recurseItemTreeFind(list, lookup);
    }

    /**
     * Recursively finds a recipe, top level. call this to find a recipe
     *
     * @param items the items part
     * @param map   the root branch to search from.
     * @return a recipe
     */
    IRecipe recurseItemTreeFind(@Nonnull List<List<AbstractMapIngredient>> items, @Nonnull Branch map) {
        // Try each ingredient as a starting point, adding it to the skiplist.
        return recurseItemTreeFind(items, map, 0);
    }

    /**
     * Recursively finds a recipe
     *
     * @param items     the items part
     * @param map       the current branch of the tree
     * @param index     the index of the wrapper to get
     *                  recursion.
     * @return a recipe
     */
    IRecipe recurseItemTreeFind(@Nonnull List<List<AbstractMapIngredient>> items, @Nonnull Branch map, int index) {
        List<AbstractMapIngredient> wr = items.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient t : wr) {
            Either<IRecipe, FastRecipeMap.Branch> result = map.nodes.get(t);
            if (result != null) {
                // Either return recipe or continue branch.
                IRecipe r = result.map(recipe -> recipe , right -> callback(items, right, index + 1));
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private IRecipe callback(@Nonnull List<List<AbstractMapIngredient>> items, @Nonnull Branch map, int index) {
        // Recursive call.
        return recurseItemTreeFind(items, map, index);
    }

    /**
     * Adds a recipe to the map. (recursive part)
     *
     * @param recipe      the recipe to add.
     * @param ingredients list of input ingredients.
     * @param map         the current place in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many added already.
     */
    boolean recurseItemTreeAdd(@Nonnull IRecipe recipe, @Nonnull List<List<AbstractMapIngredient>> ingredients,
                               @Nonnull Branch map, int index, int count, boolean isShaped) {
        if (count >= ingredients.size())
            return true;
        if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        }
        // Loop through NUMBER_OF_INGREDIENTS times.
        List<AbstractMapIngredient> current = ingredients.get(index);
        if (current.isEmpty()) {
            current.add(MapItemStackIngredient.EMPTY);
        }
        Either<IRecipe, Branch> r;
            for (AbstractMapIngredient obj : current) {
                // Either add the recipe or create a branch.
                r = map.nodes.compute(obj, (k, v) -> {
                    if (count == ingredients.size() - 1) {
                        if (v == null) {
                            v = Either.left(recipe);
                        }
                        return v;
                    } else if (v == null) {
                        Branch traverse = new Branch();
                        v = Either.right(traverse);
                    }
                    return v;
                });
                // At the end, return.
                if (count == ingredients.size() - 1) {
                    continue;
                }
                // If left was present before. Shouldn't be needed?
                /*
                 * if (r.left().isPresent()) { Utils.onInvalidData("COLLISION DETECTED!");
                 * current.forEach(map.NODES::remove); return false; }
                 */
                // should always be present but this gives no warning.
                if (r.right().map(m -> !recurseItemTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(), count + 1, isShaped)).orElse(false)) {
                    current.forEach(map.nodes::remove);
                    return false;
                }
        }
        return true;
    }

    protected List<List<AbstractMapIngredient>> fromRecipe(IRecipe r, boolean isShaped) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>((r.getIngredients().size()));
        buildFromItems(list, r.getIngredients(), isShaped);
        return list;
    }

    protected void buildFromItems(List<List<AbstractMapIngredient>> list, NonNullList<Ingredient> ingredients, boolean isShaped) {
        if (ingredients.isEmpty()){
            List<AbstractMapIngredient> inner = new ObjectArrayList<>(1);
            inner.add(MapItemStackIngredient.EMPTY);
            list.add(inner);
        }
        for (Ingredient t : ingredients) {
            List<AbstractMapIngredient> inner = new ObjectArrayList<>(t.getMatchingStacks().length);
            for (ItemStack stack : t.getMatchingStacks()) {
                inner.add(new MapItemStackIngredient(stack));
            }
            list.add(inner);
        }
    }

    protected void buildFromItemStacks(List<List<AbstractMapIngredient>> list, ItemStack[] ingredients) {
        if (ingredients.length == 0){
            List<AbstractMapIngredient> ls = new ObjectArrayList<>(2);
            ls.add(MapItemStackIngredient.EMPTY);
            list.add(ls);
        }
        for (ItemStack t : ingredients) {
            List<AbstractMapIngredient> ls = new ObjectArrayList<>(2);
            ls.add(new MapItemStackIngredient(t));
            list.add(ls);
        }
    }

    protected static class Branch {

        private Map<AbstractMapIngredient, Either<IRecipe, Branch>> nodes = new Object2ObjectOpenHashMap<>();

        public Stream<IRecipe> getRecipes() {
            return nodes.values().stream().flatMap(t -> t.map(Stream::of, Branch::getRecipes));
        }

        public boolean removeRecipe(IRecipe recipe) {
            for (Map.Entry<AbstractMapIngredient, Either<IRecipe, Branch>> entry : nodes.entrySet()) {
                if (entry.getValue().left().map(check -> check.equals(recipe)).orElse(false)) {
                    return true;
                } else if (entry.getValue().right().map(branch -> branch.removeRecipe(recipe)).orElse(false)) {
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            nodes = new Object2ObjectOpenHashMap<>();
        }

    }


    public abstract static class Either<L, R> {

        private static final class Left<L, R> extends Either<L, R> {
            private final L value;

            public Left(final L value) {
                this.value = value;
            }

            @Override
            public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
                return new Left<>(f1.apply(value));
            }

            @Override
            public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
                return l.apply(value);
            }

            @Override
            public Either<L, R> ifLeft(Consumer<? super L> consumer) {
                consumer.accept(value);
                return this;
            }

            @Override
            public Either<L, R> ifRight(Consumer<? super R> consumer) {
                return this;
            }

            @Override
            public java.util.Optional<L> left() {
                return java.util.Optional.of(value);
            }

            @Override
            public java.util.Optional<R> right() {
                return java.util.Optional.empty();
            }

            @Override
            public String toString() {
                return "Left[" + value + "]";
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                final Left<?, ?> left = (Left<?, ?>) o;
                return Objects.equals(value, left.value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(value);
            }
        }

        private static final class Right<L, R> extends Either<L, R> {
            private final R value;

            public Right(final R value) {
                this.value = value;
            }

            @Override
            public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
                return new Right<>(f2.apply(value));
            }

            @Override
            public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
                return r.apply(value);
            }

            @Override
            public Either<L, R> ifLeft(Consumer<? super L> consumer) {
                return this;
            }

            @Override
            public Either<L, R> ifRight(Consumer<? super R> consumer) {
                consumer.accept(value);
                return this;
            }

            @Override
            public java.util.Optional<L> left() {
                return java.util.Optional.empty();
            }

            @Override
            public java.util.Optional<R> right() {
                return java.util.Optional.of(value);
            }

            @Override
            public String toString() {
                return "Right[" + value + "]";
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                final Right<?, ?> right = (Right<?, ?>) o;
                return Objects.equals(value, right.value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(value);
            }
        }

        private Either() {
        }

        public abstract <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2);

        public abstract <T> T map(final Function<? super L, ? extends T> l, Function<? super R, ? extends T> r);

        public abstract Either<L, R> ifLeft(final Consumer<? super L> consumer);

        public abstract Either<L, R> ifRight(final Consumer<? super R> consumer);

        public abstract java.util.Optional<L> left();

        public abstract java.util.Optional<R> right();

        public <T> Either<T, R> mapLeft(final Function<? super L, ? extends T> l) {
            return map(t -> left(l.apply(t)), Either::right);
        }

        public <T> Either<L, T> mapRight(final Function<? super R, ? extends T> l) {
            return map(Either::left, t -> right(l.apply(t)));
        }

        public static <L, R> Either<L, R> left(final L value) {
            return new Left<>(value);
        }

        public static <L, R> Either<L, R> right(final R value) {
            return new Right<>(value);
        }

        public L orThrow() {
            return map(l -> l, r -> {
                if (r instanceof Throwable) {
                    throw new RuntimeException((Throwable) r);
                }
                throw new RuntimeException(r.toString());
            });
        }

        public Either<R, L> swap() {
            return map(Either::right, Either::left);
        }

        public <L2> Either<L2, R> flatMap(final Function<L, Either<L2, R>> function) {
            return map(function, Either::right);
        }
    }
}
