package com.linh.snackgo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Product data model with multi-image support.
 */
public class Product {
    private long id;
    private String name;
    private String description;
    private String category;
    private long price;
    private long originalPrice;
    private String tag;
    private String image;
    private boolean isActive;
    private String createdAt;
    private List<String> images = new ArrayList<>();

    // Rich product fields
    private String origin;
    private String madeIn;
    private String sizeInfo;
    private String ingredients;
    private String features;
    private String usageGuide;
    private String storageGuide;
    private String shelfLife;
    private double rating;
    private int soldCount;

    public Product() {
        this.isActive = true;
    }

    public Product(long id, String name, String description, String category, long price,
                   long originalPrice, String tag, String image, boolean isActive, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.originalPrice = originalPrice;
        this.tag = tag;
        this.image = image;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Product(String name, String description, String category, long price,
                   long originalPrice, String tag, String image, boolean isActive) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.originalPrice = originalPrice;
        this.tag = tag;
        this.image = image;
        this.isActive = isActive;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(long originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    public String getCoverImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return image;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getMadeIn() {
        return madeIn;
    }

    public void setMadeIn(String madeIn) {
        this.madeIn = madeIn;
    }

    public String getSizeInfo() {
        return sizeInfo;
    }

    public void setSizeInfo(String sizeInfo) {
        this.sizeInfo = sizeInfo;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getUsageGuide() {
        return usageGuide;
    }

    public void setUsageGuide(String usageGuide) {
        this.usageGuide = usageGuide;
    }

    public String getStorageGuide() {
        return storageGuide;
    }

    public void setStorageGuide(String storageGuide) {
        this.storageGuide = storageGuide;
    }

    public String getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(String shelfLife) {
        this.shelfLife = shelfLife;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
}
