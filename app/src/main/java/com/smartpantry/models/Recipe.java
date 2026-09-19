package com.smartpantry.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Recipe implements Parcelable {

    private long id;
    private String name;
    private String steps;
    private String category;
    private List<RecipeIngredient> requiredIngredients;

    public Recipe(long id, String name, String steps, String category) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.category = category;
        this.requiredIngredients = new ArrayList<>();
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }
    public String getCategory() { return category; }

    public List<RecipeIngredient> getRequiredIngredients() { return requiredIngredients; }
    public void setRequiredIngredients(List<RecipeIngredient> list) { this.requiredIngredients = list; }

    protected Recipe(Parcel in) {
        id = in.readLong();
        name = in.readString();
        steps = in.readString();
        category = in.readString();
        requiredIngredients = new ArrayList<>();
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) { return new Recipe(in); }

        @Override
        public Recipe[] newArray(int size) { return new Recipe[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(steps);
        dest.writeString(category);
    }
}