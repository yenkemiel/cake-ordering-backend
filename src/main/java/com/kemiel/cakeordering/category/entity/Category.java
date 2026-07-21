package com.kemiel.cakeordering.category.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 商品分類，對應 categories 資料表
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name="sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name="created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    protected Category() {
    }

    public Category(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
