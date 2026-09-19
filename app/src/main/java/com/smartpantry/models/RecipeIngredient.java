package com.smartpantry.models;

public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public long getRecipeId() { return recipeId; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    public String getDisplayText() {
        String qty = (quantity == Math.floor(quantity))
                ? String.valueOf((int) quantity)
                : String.valueOf(quantity);
        String unitPart = unit.equals("whole") ? "" : unit + " ";
        return qty + " " + unitPart + name;
    }
}