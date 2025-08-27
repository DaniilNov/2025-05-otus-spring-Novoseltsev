package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/view/{id}")
    public String viewComment(@PathVariable String id, Model model) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        model.addAttribute("comment", comment);
        return "comment/view";
    }

    @GetMapping("/create")
    public String createCommentForm(@RequestParam("bookId") String bookId, Model model) {
        bookService.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));

        model.addAttribute("comment", new Comment());
        model.addAttribute("bookId", bookId);
        return "comment/create";
    }

    @PostMapping("/create")
    public String createComment(@RequestParam("text") String text,
                                @RequestParam("bookId") String bookId) {
        Comment savedComment = commentService.create(text, bookId);
        return "redirect:/books/view/" + bookId;
    }

    @GetMapping("/edit/{id}")
    public String editCommentForm(@PathVariable String id, Model model) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        model.addAttribute("comment", comment);
        return "comment/edit";
    }

    @PostMapping("/edit/{id}")
    public String editComment(@PathVariable String id,
                              @RequestParam("text") String text) {
        Comment updatedComment = commentService.update(id, text);
        String bookId = updatedComment.getBook().getId();
        return "redirect:/books/view/" + bookId;
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable String id) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        String bookId = comment.getBook().getId();

        commentService.deleteById(id);
        return "redirect:/books/view/" + bookId;
    }
}