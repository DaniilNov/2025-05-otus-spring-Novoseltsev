package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @GetMapping("/view/{id}")
    public String viewComment() {
        return "comment/view";
    }

    @GetMapping("/create")
    public String createCommentForm(@RequestParam("bookId") String bookId) {
        return "comment/create";
    }

    @GetMapping("/edit/{id}")
    public String editCommentForm() {
        return "comment/edit";
    }
}