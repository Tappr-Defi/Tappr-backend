package com.semicolon.africa.tapprbackend.reciepts.services.interfaces;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.dtos.requests.CreatePostRequest;
import org.vomzersocials.user.dtos.requests.DeletePostRequest;
import org.vomzersocials.user.dtos.requests.EditPostRequest;
import org.vomzersocials.user.dtos.requests.RepostRequest;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.dtos.responses.DeletePostResponse;
import org.vomzersocials.user.dtos.responses.EditPostResponse;
import org.vomzersocials.user.dtos.responses.RepostResponse;
import reactor.core.publisher.Mono;

@Service
public interface PostService {
    Mono<CreatePostResponse> createPost(CreatePostRequest request, String userId);
    Mono<EditPostResponse> editPost(EditPostRequest request, String userId);
    Mono<DeletePostResponse> deletePost(DeletePostRequest request, String userId);
    Mono<RepostResponse> repost(RepostRequest request, String userId);
}