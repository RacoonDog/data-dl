package io.github.racoondog.recipedl.mixin;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin implements Recipe<Inventory> {
    @Shadow @Final private CookingRecipeCategory category;
    @Shadow @Final protected Ingredient input;
    @Shadow @Final protected ItemStack output;
    @Shadow @Final protected float experience;
    @Shadow @Final protected int cookTime;

    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", Registries.RECIPE_SERIALIZER.getId(getSerializer()).toString());
        if (!getGroup().isEmpty()) object.addProperty("group", getGroup());
        if (category != CookingRecipeCategory.MISC) object.addProperty("category", category.asString());
        object.add("ingredient", input.toJson());
        object.addProperty("result", Registries.ITEM.getId(output.getItem()).toString());
        if (experience != 0.0f) object.addProperty("experience", experience);
        if (cookTime != getDefaultCookTime()) object.addProperty("cookingtime", cookTime);
        return object;
    }

    @SuppressWarnings("ConstantConditions")
    @Unique
    private int getDefaultCookTime() {
        return (Object) this instanceof SmeltingRecipe ? 200 : 100;
    }
}
