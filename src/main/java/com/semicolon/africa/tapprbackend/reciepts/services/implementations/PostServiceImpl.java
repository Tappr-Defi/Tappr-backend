package com.semicolon.africa.tapprbackend.reciepts.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vomzersocials.user.data.models.Media;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.exceptions.OwnershipException;
import org.vomzersocials.user.exceptions.PostNotFoundException;
import org.vomzersocials.user.services.interfaces.PostService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaServiceImpl mediaService;

    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           MediaServiceImpl mediaService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
    }

    @Override
    public Mono<CreatePostResponse> createPost(CreatePostRequest request, String userName) {
        return Mono.fromCallable(() -> {
            if (request.getContent() == null || request.getContent().trim().isEmpty())
                throw new IllegalArgumentException("Post content cannot be empty");
            if (request.getAuthor() == null || !userName.equals(request.getAuthor().getUserName()))
                throw new IllegalArgumentException("Author username does not match authenticated user");
            User user = userRepository.findUserByUserName(userName)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!user.getIsLoggedIn())
                throw new SecurityException("User must be logged in to create posts");
            Post post = new Post();
            post.setAuthor(user);
            post.setContent(request.getContent());
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());

            if (request.getMediaIds() != null && !request.getMediaIds().isEmpty()) {
                List<Media> mediaList = mediaService.getMediaByIds(request.getMediaIds());
                post.setMediaList(mediaList);
            }
            Post savedPost = postRepository.save(post);
            return CreatePostResponse.builder()
                    .id(savedPost.getId())
                    .content(savedPost.getContent())
                    .authorId(userName)
                    .timestamp(savedPost.getCreatedAt())
                    .build();
        });
    }

    @Override
    @Transactional
    public Mono<DeletePostResponse> deletePost(DeletePostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!user.getIsLoggedIn())
                throw new SecurityException("User authentication required");
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(request.getPostId()));
            if (!post.getAuthor().getId().equals(userId))
                throw new OwnershipException(userId, request.getPostId());
            post.getMediaList().forEach(media ->
                    mediaService.deleteMediaById(media.getId()));
            postRepository.delete(post);

            return DeletePostResponse.builder()
                    .postId(post.getId())
                    .message("Post deleted successfully")
                    .build();
        });
    }

    @Override
    public Mono<EditPostResponse> editPost(EditPostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(request.getPostId()));
            if (!post.getAuthor().getId().equals(userId))
                throw new OwnershipException(userId, request.getPostId());
            post.setContent(request.getContent());
            post.setUpdatedAt(LocalDateTime.now());
            Post updatedPost = postRepository.save(post);

            return EditPostResponse.builder()
                    .id(updatedPost.getId())
                    .content(updatedPost.getContent())
                    .timestamp(updatedPost.getUpdatedAt())
                    .isEdited(true)
                    .author(updatedPost.getAuthor())
                    .build();
        });
    }

    @Override
    public Mono<RepostResponse> repost(RepostRequest request, String userId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!user.getIsLoggedIn())
                throw new SecurityException("User must be logged in to repost");
            Post originalPost = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(request.getPostId()));
            if (originalPost.getAuthor().getId().equals(userId))
                throw new IllegalArgumentException("Cannot repost your own content");
            if (postRepository.existsByAuthorAndRepostedPost(Optional.of(user), originalPost))
                throw new IllegalArgumentException("Already reposted this content");

            Post repost = new Post();
            repost.setAuthor(user);
            repost.setRepostedPost(originalPost);
            repost.setCreatedAt(LocalDateTime.now());
            postRepository.save(repost);

            originalPost.setRepostCount(originalPost.getRepostCount() + 1);
            postRepository.save(originalPost);
            return RepostResponse.builder()
                    .repostedPostId(originalPost.getId())
                    .foundUserId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
        });
    }

    public void deletePostWithMedia(UUID postId) {
        Post post = postRepository.findById(postId.toString())
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
        post.getMediaList().forEach(media ->
                mediaService.deleteMediaById(media.getId()));
        postRepository.delete(post);
    }
}