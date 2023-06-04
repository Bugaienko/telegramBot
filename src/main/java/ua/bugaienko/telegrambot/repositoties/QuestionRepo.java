package ua.bugaienko.telegrambot.repositoties;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.bugaienko.telegrambot.model.MyQuestion;

@Repository
public interface QuestionRepo extends JpaRepository<MyQuestion, Long> {
}
