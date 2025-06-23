package com.semicolon.africa.tapprbackend.reciepts.services.implementations;

import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.models.UserFollowing;
import org.vomzersocials.user.data.repositories.FollowRepository;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.FollowUserRequest;
import org.vomzersocials.user.services.interfaces.FollowerService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FollowerServiceImpl implements FollowerService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowerServiceImpl(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void followUser(FollowUserRequest followUserRequest) {
        User follower = userRepository.findById(followUserRequest.getFollowerId())
                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));

        User following = userRepository.findById(followUserRequest.getFollowingId())
                .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));

        if (follower.getId().equals(following.getId())) throw new IllegalArgumentException("You cannot follow yourself");

        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId());

        if (alreadyFollowing) throw new IllegalStateException("You are already following this user");

        setUserFollowingAnotherUser(follower, following);
        updateUserFollowCounts_andSaveUsers(follower, following, true);
    }

    @Override
    public void unfollowUser(FollowUserRequest followUserRequest) {
        String followerId = followUserRequest.getFollowerId();
        String followingId = followUserRequest.getFollowingId();

        UserFollowing userFollowing = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new IllegalArgumentException("You are not following this user"));

        followRepository.delete(userFollowing);

        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();

        updateUserFollowCounts_andSaveUsers(follower, following, false);
    }

    @Override
    public boolean isFollowing(FollowUserRequest followUserRequest) {
        return followRepository.findByFollowerIdAndFollowingId(
                followUserRequest.getFollowerId(), followUserRequest.getFollowingId()
        ).map(UserFollowing::getIsFollowing).orElse(false);
    }

    @Override
    public void toggleFollow(FollowUserRequest request) {
        String followerId = request.getFollowerId();
        String followingId = request.getFollowingId();

        if (followerId.equals(followingId)) throw new IllegalArgumentException("You cannot follow yourself");

        var existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        if (existingFollow.isPresent()) {
            // unfollow
            followRepository.delete(existingFollow.get());
            User follower = userRepository.findById(followerId).orElseThrow();
            User following = userRepository.findById(followingId).orElseThrow();
            updateUserFollowCounts_andSaveUsers(follower, following, false);
        } else {
            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
            User following = userRepository.findById(followingId)
                    .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));

            setUserFollowingAnotherUser(follower, following);
            updateUserFollowCounts_andSaveUsers(follower, following, true);
        }
    }

    private void updateUserFollowCounts_andSaveUsers(User follower, User following, boolean isIncrement) {
        if (isIncrement) {
            follower.setFollowingCount(follower.getFollowingCount() + 1);
            following.setFollowerCount(following.getFollowerCount() + 1);
        } else {
            follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
            following.setFollowerCount(Math.max(0, following.getFollowerCount() - 1));
        }
        userRepository.saveAll(List.of(follower, following));
    }

    private void setUserFollowingAnotherUser(User follower, User following) {
        UserFollowing userFollowing = new UserFollowing();
        userFollowing.setFollowedAt(LocalDateTime.now());
        userFollowing.setIsFollowing(true);
        userFollowing.setFollower(follower);
        userFollowing.setFollowing(following);
        followRepository.save(userFollowing);
    }
}
