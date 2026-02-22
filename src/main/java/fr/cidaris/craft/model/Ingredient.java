package fr.cidaris.craft.model;

import fr.cidaris.craft.model.enums.IngredientType;

public class Ingredient {
    private final IngredientType type;
    private final String id;

    public Ingredient(IngredientType type, String id) {
        this.type = type;
        this.id = id;
    }

    public IngredientType getType() { return type; }
    public String getId() { return id; }
}