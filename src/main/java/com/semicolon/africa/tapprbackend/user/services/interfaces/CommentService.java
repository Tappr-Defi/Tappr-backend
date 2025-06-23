package com.semicolon.africa.tapprbackend.user.services.interfaces;

import org.vomzersocials.user.data.models.Comment;
import org.vomzersocials.user.dtos.requests.CreateCommentRequest;
import org.vomzersocials.user.dtos.responses.CreateCommentResponse;

import java.util.List;

public interface CommentService {
    CreateCommentResponse createComment(CreateCommentRequest request);
    List<CreateCommentResponse> getCommentsByPost(String postId);
    List<Comment> getCommentsForPost(String postId);
    List<Comment> getReplies(String commentId);
    void deleteComment(String commentId);
}
