package com.exposure.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "missions")
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String title;
    @Column(length = 250)
    private String description;

    @Column(length = 1000)
    private String history_description;

    @Min(2)
    @Max(5)
    @Column
    private int roles_amount;

    @Min(2)
    private int initialQuestionsAmount;

    public Mission(String title, String description, String history_description, int roles_amount, int initialQuestionsAmount) {
        this.title = title;
        this.description = description;
        this.history_description = history_description;
        this.roles_amount = roles_amount;
        this.initialQuestionsAmount = initialQuestionsAmount;
    }
}
