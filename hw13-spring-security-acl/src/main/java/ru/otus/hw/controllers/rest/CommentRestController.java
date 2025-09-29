package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.controllers.rest.dto.CommentCreateDto;
import ru.otus.hw.controllers.rest.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.User;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.UserServiceImpl;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    private final UserServiceImpl userService;

    @GetMapping("/api/v1/comments/book/{bookId}")
    public ResponseEntity<List<Comment>> getCommentsByBookId(@PathVariable("bookId") String bookId) {
        return ResponseEntity.ok(commentService.findByBookId(bookId));
    }

    @GetMapping("/api/v1/comments/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("id") String id) {
        return commentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/comments")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CommentCreateDto commentDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetails userDetails = userService.loadUserByUsername(username);
        User author = (User) userDetails;

        Comment comment = commentService.create(commentDto.getText(), commentDto.getBookId(), author);
        return ResponseEntity.status(201).body(comment);
    }

    @PutMapping("/api/v1/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable("id") String id,
                                                 @Valid @RequestBody CommentUpdateDto commentDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetails userDetails = userService.loadUserByUsername(username);
        User user = (User) userDetails;

        Comment comment;
        try {
            comment = commentService.update(id, commentDto.getText(), user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetails userDetails = userService.loadUserByUsername(username);
        User user = (User) userDetails;

        try {
            commentService.deleteById(id, user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}