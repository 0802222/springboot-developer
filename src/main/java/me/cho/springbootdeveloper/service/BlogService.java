package me.cho.springbootdeveloper.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.cho.springbootdeveloper.config.error.exception.ArticleNotFoundException;
import me.cho.springbootdeveloper.domain.Article;
import me.cho.springbootdeveloper.domain.Comment;
import me.cho.springbootdeveloper.dto.AddArticleRequest;
import me.cho.springbootdeveloper.dto.AddCommentRequest;
import me.cho.springbootdeveloper.dto.UpdateArticleRequest;
import me.cho.springbootdeveloper.repository.BlogRepository;
import me.cho.springbootdeveloper.repository.CommentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;

    public Article save(AddArticleRequest request, String username) {
        return blogRepository.save(request.toEntity(username));
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(Long id) {
        return blogRepository.findById(id)
            .orElseThrow(ArticleNotFoundException::new);
    }

    public void deleteById(Long id) {
        Article article = blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    @Transactional
    public Article update(Long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    public Comment addComment(AddCommentRequest request, String userName) {
        Article article = blogRepository.findById(request.getArticleId())
            .orElseThrow(() -> new IllegalArgumentException("not found: " + request.getArticleId()));

        return commentRepository.save(request.toEntity(userName, article));
    }

}
