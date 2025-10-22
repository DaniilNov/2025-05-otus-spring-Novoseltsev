package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final ResilientExecutor resilientExecutor;

    @Override
    public List<Author> findAll() {
        return resilientExecutor.executeOrFallback(authorRepository::findAll, Collections::emptyList);
    }
}