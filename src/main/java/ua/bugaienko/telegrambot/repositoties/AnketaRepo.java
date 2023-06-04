package ua.bugaienko.telegrambot.repositoties;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.bugaienko.telegrambot.model.Anketa;

import java.util.Optional;

/**
 * @author Sergii Bugaienko
 */

@Repository
public interface AnketaRepo extends JpaRepository<Anketa, Long> {

    public Optional<Anketa> getAnketaByChatId(Long chatId);

}
