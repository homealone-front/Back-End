package com.elice.homealone.module.commentlike.service;

import com.elice.homealone.module.commentlike.Repository.CommentLikeRepository;
import com.elice.homealone.module.commentlike.dto.CommentLikeReqDto;
import com.elice.homealone.module.commentlike.dto.CommentLikeResDto;
import com.elice.homealone.module.commentlike.entity.CommentLike;
import com.elice.homealone.module.comment.entity.Comment;
import com.elice.homealone.module.comment.repository.CommentRepository;
import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.member.service.MemberQueryService;
import com.elice.homealone.module.member.service.MemberService;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeService {

        private final CommentLikeRepository commentLikeRepository;
        private final CommentRepository commentRepository;
        private final MemberService memberService;
        private final MemberQueryService memberQueryService;
        private final AuthService authService;

        @Transactional
        public CommentLikeResDto createAndDeleteCommentLike(CommentLikeReqDto reqDto) {
                try {
                        Member member = authService.getMember();
                        member = memberQueryService.findById(member.getId());
                        Comment comment = commentRepository.findById(reqDto.getCommentId()).get();
                        if(member==null) {
                                return null;
                        }
                        Optional<CommentLike> commentLike = commentLikeRepository.findByMemberIdAndCommentId(member.getId(), comment.getId());
                        if(commentLike.isEmpty()) {
                                CommentLike newCommentLike = reqDto.toEntity(member, comment);
                                member.getCommentLikes().add(newCommentLike);
                                comment.getLikes().add(newCommentLike);
                                commentLikeRepository.save(newCommentLike);
                                CommentLikeResDto resDto = CommentLikeResDto.fromEntity(newCommentLike);
                                resDto.setTotalCount(comment.getLikes().size());
                                return resDto;
                        }
                        commentLikeRepository.delete(commentLike.get());
                        return null;
                } catch (HomealoneException e) {
                        if (e.getErrorCode()== ErrorCode.MEMBER_NOT_FOUND) {
                                return null;
                        } else {
                                throw new HomealoneException(ErrorCode.BAD_REQUEST);
                        }
                }
        }

        // 멤버가 좋아요 한 리스트 조회
        public List<CommentLike> findLikesByMemberAndCommentIn(Member member, List<Comment> comments) {
                return commentLikeRepository.findByMemberAndCommentIn(member, comments);
        }


}
