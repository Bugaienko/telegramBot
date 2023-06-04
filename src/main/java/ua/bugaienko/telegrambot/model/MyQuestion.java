package ua.bugaienko.telegrambot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Sergii Bugaienko
 */

@Entity
@Table(name = "questions")
@Setter
@Getter
@NoArgsConstructor
public class MyQuestion {

    @Id
    private Integer id;

    private String question;

    private boolean isActive = true;
}
