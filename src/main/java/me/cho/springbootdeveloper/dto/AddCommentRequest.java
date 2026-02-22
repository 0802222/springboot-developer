package me.cho.springbootdeveloper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.cho.springbootdeveloper.domain.Article;
import me.cho.springbootdeveloper.domain.Comment;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddCommentRequest {

    private Long articleId;
    private String content;

    public Comment toEntity(String author, Article article) {
        return Comment.builder()
            .article(article)
            .content(content)
            .author(author)
            .build();
    }

}
