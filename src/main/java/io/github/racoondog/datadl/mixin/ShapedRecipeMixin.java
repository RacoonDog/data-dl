package io.github.racoondog.datadl.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.racoondog.datadl.util.MutableChar;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import it.unimi.dsi.fastutil.objects.Object2CharOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin implements CraftingRecipe {
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow @Final CraftingRecipeCategory category;
    @Shadow @Final boolean showNotification;
    @Shadow @Final ItemStack output;

    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", Registries.RECIPE_SERIALIZER.getId(getSerializer()).toString());
        if (!getGroup().isEmpty()) object.addProperty("group", getGroup());
        if (category != CraftingRecipeCategory.MISC) object.addProperty("category", category.asString());
        if (!showNotification) object.addProperty("show_notification", false);

        // Compute data
        DefaultedList<Ingredient> ingredients = getIngredients();
        MutableChar key = MutableChar.of('a');
        Object2CharMap<Ingredient> keyResolver = new Object2CharOpenHashMap<>();
        keyResolver.put(Ingredient.EMPTY, ' ');
        char[] map = new char[getWidth() * getHeight()];
        for (int i = 0; i < map.length; i++) map[i] = keyResolver.computeIfAbsent(ingredients.get(i), o -> key.getAndIncrement());

        // Serialize keys
        JsonObject keys = new JsonObject();
        for (var entry : keyResolver.object2CharEntrySet()) if (entry.getKey() != Ingredient.EMPTY) keys.add(Character.toString(entry.getCharValue()), entry.getKey().toJson());
        object.add("key", keys);

        // Serialize pattern
        JsonArray pattern = new JsonArray();
        for (int h = 0; h < getHeight(); h++) {
            StringBuilder builder = new StringBuilder();
            for (int w = 0; w < getWidth(); w++) {
                builder.append(map[w * getHeight() + h]);
            }
            pattern.add(builder.toString());
        }
        object.add("pattern", pattern);

        JsonObject result = new JsonObject();
        result.addProperty("item", Registries.ITEM.getId(output.getItem()).toString());
        if (output.getCount() != 1) result.addProperty("count", output.getCount());
        object.add("result", result);

        return object;
    }
}
