package io.github.racoondog.recipedl.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.racoondog.recipedl.util.JsonSerializable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(Recipe.class)
public interface RecipeMixin extends JsonSerializable {
    @Shadow String getGroup();
    @Shadow DefaultedList<Ingredient> getIngredients();

    @Shadow RecipeSerializer<?> getSerializer();

    @Shadow ItemStack getOutput(DynamicRegistryManager registryManager);

    @Override
    default JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", Registries.RECIPE_SERIALIZER.getId(getSerializer()).toString());
        if (!getGroup().isEmpty()) object.addProperty("group", getGroup());
        DefaultedList<Ingredient> ingredients = getIngredients();
        if (!ingredients.isEmpty()) {
            JsonArray ingredientsArray = new JsonArray(ingredients.size());
            for (var ingredient : ingredients) ingredientsArray.add(ingredient.toJson());
            object.add("ingredients", ingredientsArray);
        }
        try {
            JsonObject result = new JsonObject();
            result.addProperty("item", Registries.ITEM.getId(getOutput(null).getItem()).toString());
            object.add("result", result);
        } catch (NullPointerException ignored) {}
        return object;
    }
}
