package ua.bugaienko.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.bugaienko.telegrambot.model.Anketa;
import ua.bugaienko.telegrambot.model.Answer;
import ua.bugaienko.telegrambot.repositoties.AnswerRepo;

import java.util.List;

/**
 * @author Sergii Bugaienko
 */

@Service
public class AnswersService {

    private final AnswerRepo answerRepo;
    private final AnketaService anketaService;

    @Autowired
    public AnswersService(AnswerRepo answerRepo, AnketaService anketaService) {
        this.answerRepo = answerRepo;
        this.anketaService = anketaService;
    }

    public List<Answer> getAllAnswerByChatId(Long chatId) {
        Anketa anketa = anketaService.getAnketaByChatId(chatId);
        return answerRepo.findAllByAnketa(anketa);
    }

    public List<Answer> getAllAnswerByAnketa(Anketa anketa) {
        return answerRepo.findAllByAnketa(anketa, Sort.by("answerNumber"));
    }
}
