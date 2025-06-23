package com.semicolon.africa.tapprbackend.user.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.Like;
import org.vomzersocials.user.data.models.Post;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.LikeRepository;
import org.vomzersocials.user.data.repositories.PostRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LikeRequest;
import org.vomzersocials.user.dtos.responses.LikeResponse;
import org.vomzersocials.user.services.interfaces.LikeService;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public LikeResponse likeOrUnLike(LikeRequest likeRequest) {
        User user = userRepository.findById(likeRequest.getUserId()).orElse(null);
        Post post = postRepository.findById(likeRequest.getPostId()).orElseThrow(() ->
                new IllegalArgumentException("Post not found"));

        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        LikeResponse likeResponse = new LikeResponse();

        if (existingLike.isPresent()) {
            Like like = existingLike.get();
            like.setIsLiked(false);
            likeRepository.save(like);
            likeResponse.setMessage(like.getIsLiked() ? "Liked" : "Unliked");
            likeResponse.setLiked(like.getIsLiked());
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setPost(post);
            like.setIsLiked(true);
            likeRepository.save(like);
            likeResponse.setMessage("Liked");
            likeResponse.setLiked(true);
        }
        return likeResponse;
    }
}
