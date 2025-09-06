package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/books")
public class BookController {

    @GetMapping
    public String listBooks() {
        return "book/list";
    }

    @GetMapping("/create")
    public String createBookForm() {
        return "book/create";
    }

    @GetMapping("/edit/{id}")
    public String editBookForm() {
        return "book/edit";
    }

    @GetMapping("/view/{id}")
    public String viewBook() {
        return "book/view";
    }
}