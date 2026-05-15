package com.app.shecare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plan name (PREMIUM, PRO)
    @Column(nullable = false)
    private String name;

    // Price in rupees (e.g., 199, 499)
    @Column(nullable = false)
    private int price;

    // Duration: MONTHLY / YEARLY
    @Column(nullable = false)
    private String duration;

    // Razorpay Plan ID (VERY IMPORTANT)
    private String razorpayPlanId;

    // Optional: description (for UI display)
    private String description;

    // Optional: active/inactive plan
    private boolean active = true;

    // Constructors
    public Plan() {}

    public Plan(String name, int price, String duration, String description) {
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.description = description;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRazorpayPlanId() {
        return razorpayPlanId;
    }

    public void setRazorpayPlanId(String razorpayPlanId) {
        this.razorpayPlanId = razorpayPlanId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}