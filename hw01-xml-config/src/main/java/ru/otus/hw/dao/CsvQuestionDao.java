package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String filename = getFileName();
        try (InputStream inputStream = getResourceStream(filename)) {
            List<QuestionDto> questionDtos = parseCsv(inputStream);
            return convertToDomain(questionDtos);
        } catch (Exception e) {
            throw new QuestionReadException("Error reading questions from file: " + filename, e);
        }
    }

    private String getFileName() {
        return fileNameProvider.getTestFileName();
    }

    private InputStream getResourceStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    private List<QuestionDto> parseCsv(InputStream inputStream) {
        return new CsvToBeanBuilder<QuestionDto>(new InputStreamReader(inputStream))
                .withType(QuestionDto.class)
                .withSeparator(';')
                .withSkipLines(1)
                .build()
                .parse();
    }

    private List<Question> convertToDomain(List<QuestionDto> questionDtos) {
        List<Question> questions = new ArrayList<>();
        for (QuestionDto dto : questionDtos) {
            questions.add(dto.toDomainObject());
        }
        return questions;
    }
}
