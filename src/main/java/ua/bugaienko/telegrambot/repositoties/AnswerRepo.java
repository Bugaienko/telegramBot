package ua.bugaienko.telegrambot.repositoties;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.bugaienko.telegrambot.model.Anketa;
import ua.bugaienko.telegrambot.model.Answer;

import java.util.List;

/**
 * @author Sergii Bugaienko
 */

@Repository
public interface AnswerRepo extends JpaRepository<Answer, Long> {

    public List<Answer> findAllByAnketa(Anketa anketa, Sort answerNumber);

    public List<Answer> findAllByAnketa(Anketa anketa);
}
