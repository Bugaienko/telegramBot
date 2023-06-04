package ua.bugaienko.telegrambot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Sergii Bugaienko
 */

@Entity
@Table(name = "anketa")
@Setter
@Getter
@NoArgsConstructor
public class Anketa {

    @Id
    @Column(name = "id")
    private Long chatId;

    private int currentState;

    //    @OneToMany(mappedBy = "anketa")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "anketa_answers", joinColumns = @JoinColumn(name = "anketa_id"))
    @MapKeyColumn(name = "answer_number")
    @Column(name = "answer")
    private Map<Integer, String> answers = new HashMap<>();

    private boolean isWaitingForAnswer = false;

    @Column(name = "total_questions")
    private int lastIndex = 1;


}
