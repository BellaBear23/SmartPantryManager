package com.smartpantry.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model representing a single item in the user's pantry.
 * Implements Parcelable so instances can be passed between Activities via Intent extras.
 */
public class Ingredient implements Parcelable {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // nullable, stored as "YYYY-MM-DD" string

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    /** Constructor for new ingredients before they are assigned a database ID. */
    public Ingredient(String name, double quantity, String unit, String expiryDate) {
        this(-1, name, quantity, unit, expiryDate);
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ")";
    }

    // --- Parcelable implementation ---

    protected Ingredient(Parcel in) {
        id = in.readLong();
        name = in.readString();
        quantity = in.readDouble();
        unit = in.readString();
        expiryDate = in.readString();
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) { return new Ingredient(in); }

        @Override
        public Ingredient[] newArray(int size) { return new Ingredient[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeDouble(quantity);
        dest.writeString(unit);
        dest.writeString(expiryDate);
    }
}
