package ua.bugaienko.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.bugaienko.telegrambot.model.MyQuestion;
import ua.bugaienko.telegrambot.repositoties.QuestionRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergii Bugaienko
 */

@Service
public class QuestionsService {

    private final QuestionRepo questionRepo;

    @Autowired
    public QuestionsService(QuestionRepo questionRepo) {
        this.questionRepo = questionRepo;
    }

    public Map<Integer, String> getQuestionsMap() {
        List<MyQuestion> myQuestionList = questionRepo.findAll();
        Map<Integer, String> map = new HashMap<>();

        for (MyQuestion myQuestion: myQuestionList) {
            map.put(myQuestion.getId(), myQuestion.getQuestion());
        }

        return map;

    }
}
