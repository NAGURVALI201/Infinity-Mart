package com.ecommerce.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name="categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    /* If it is null or empty or not whitespaces. raises exception and
    * @RestControllerAdvice will catch it and send 500 internal server error.
    * */
    // Because we are performing validations at the DTO level.
//    @NotBlank
//    @Size(min=5,message = "category name should at least contain 5 or more letters.")

    private String categoryName;
//
    @OneToMany(mappedBy="category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Product> products;
}
