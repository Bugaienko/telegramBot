package ua.bugaienko.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.bugaienko.telegrambot.model.Anketa;
import ua.bugaienko.telegrambot.model.Answer;
import ua.bugaienko.telegrambot.repositoties.AnketaRepo;
import ua.bugaienko.telegrambot.repositoties.AnswerRepo;

import java.util.Map;
import java.util.Optional;

/**
 * @author Sergii Bugaienko
 */

@Service
public class AnketaService {

    private final AnketaRepo anketaRepo;
    private final AnswerRepo answerRepo;
    private final QuestionsService questionsService;

    @Autowired
    public AnketaService(AnketaRepo anketaRepo, AnswerRepo answerRepo, QuestionsService questionsService) {
        this.anketaRepo = anketaRepo;
        this.answerRepo = answerRepo;
        this.questionsService = questionsService;
    }

    public Anketa saveAnswer(Long chatId, String answer, int answerNumber) {
        Optional<Anketa> anketaOpt = anketaRepo.findById(chatId);

        if (anketaOpt.isPresent()) {
            Anketa anketa = anketaOpt.get();
            Map<Integer, String> questionsMap = questionsService.getQuestionsMap();


            Answer answer1 = new Answer();
            answer1.setAnswer(answer);
            answer1.setAnketa(anketa);
            answer1.setQuestion(questionsMap.get(answerNumber));
            answer1.setAnswerNumber(answerNumber);
            answer1 = answerRepo.save(answer1);


            Map<Integer, String> answerMap = anketa.getAnswers();
            answerMap.put(answerNumber, answer1.getAnswer());
            answerNumber++;
            System.out.println("answerNumber " + answerNumber);
            anketa.setCurrentState(answerNumber);

            if (anketa.getCurrentState()  > anketa.getLastIndex()) {
                anketa.setWaitingForAnswer(false);
            }

            return anketaRepo.save(anketa);
        }
        return null;
    }

    public Anketa getAnketaByChatId(Long chatId) {
        Optional<Anketa> optionalAnketa = anketaRepo.getAnketaByChatId(chatId);
        if (optionalAnketa.isPresent()) {
            return optionalAnketa.get();
        } else {
            Anketa anketa = new Anketa();
            anketa.setChatId(chatId);
            return anketaRepo.save(anketa);
        }
    }

    public Anketa save(Anketa anketa) {
        return anketaRepo.save(anketa);
    }
}
