package com.kaboot.community.domain.board.service.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kaboot.community.domain.board.dto.response.BoardsResponse.BoardSimpleInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Profile("test")
@ExtendWith(MockitoExtension.class)
class BoardQueryServiceImplTest {
    
    @Mock
    private BoardRepository boardRepository;
    
    @InjectMocks
    private BoardQueryServiceImpl boardQueryService;
    
    @Test
    @DisplayName("게시글 조회성공 - 게시글이 존재하는 경우 nextCursor값 존재")
    void getBoardsNotEmpty() {
        //given
        int DEFAULT_PAGE_SIZE = 3;
        List<BoardSimpleInfo> boardSimpleInfos = createBoardSimpleInfos();

        when(boardRepository.getBoardSimpleInfo(null, DEFAULT_PAGE_SIZE)).thenReturn(boardSimpleInfos.subList(0, 3));

        //when
        BoardsResponse boards = boardQueryService.getBoards(null);

        //then
        assertThat(boards.nextCursor()).isEqualTo(boardSimpleInfos.get(2).boardId());
        assertThat(boards.boardSimpleInfos()).hasSize(DEFAULT_PAGE_SIZE);
    }

    @Test
    @DisplayName("게시글 조회성공 - 게시글이 없는 경우 nextCursor는 null")
    void getBoardsIsEmpty() {
        //given
        int DEFAULT_PAGE_SIZE = 3;
        Long lastCursor = 5L;

        when(boardRepository.getBoardSimpleInfo(lastCursor, DEFAULT_PAGE_SIZE)).thenReturn(List.of());

        //when
        BoardsResponse boards = boardQueryService.getBoards(lastCursor);

        //then
        assertThat(boards.nextCursor()).isNull();
    }

    @Test
    @DisplayName("게시글 상세 조회 성공시 게시글 상세 정보 조회 및 조회수기 증가한다.")
    void getBoardDetailSuccess() {
        //given
        Long validBoardId = 1L;
        BoardDetailResponse boardDetailDto = createBoardDetailDto();
        Board board = createBoard();
        int prevViewCount = board.getViewCount();
        when(boardRepository.findById(anyLong())).thenReturn(Optional.ofNullable(board));
        when(boardRepository.getBoardDetailInfoById(anyLong())).thenReturn(boardDetailDto);

        //when
        boardQueryService.getBoardDetail(validBoardId);

        //then
        verify(boardRepository, times(1)).findById(validBoardId);
        verify(boardRepository, times(1)).getBoardDetailInfoById(validBoardId);
        assertThat(board.getViewCount()).isEqualTo(prevViewCount + 1);
    }

    @Test
    @DisplayName("존재하지 않는 board id가 들어오면 예외가 발생한다.")
    void getBoardDetailFailNotExistBoard() {
        // given
        Long notExistBoardId = 100L;

        when(boardRepository.findById(notExistBoardId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> boardQueryService.getBoardDetail(notExistBoardId))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.BOARD_NOT_EXIST.getMessage());
    }

    private BoardDetailResponse createBoardDetailDto() {
        return BoardDetailResponse.builder()
                .build();
    }

    private Board createBoard() {
        return Board.builder()
                .id(1L)
                .member(null)
                .title("title")
                .content("content")
                .imageOriginalName("image name")
                .imgUrl("url")
                .viewCount(0)
                .build();
    }

    private List<BoardSimpleInfo> createBoardSimpleInfos() {
        List<BoardSimpleInfo> infos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 10; i++) {
            infos.add(
                    BoardSimpleInfo.builder()
                            .boardId((long) (100 - i))
                            .title("테스트 게시글 " + (i + 1))
                            .createdAt(now.minusDays(i))
                            .likeCnt(10 - i)
                            .commentCnt(5 - (i % 5))
                            .viewCount(20 - (i * 2))
                            .authorNickname("사용자" + (i + 1))
                            .authorProfileImg("profile_" + (i + 1) + ".jpg")
                            .build()
            );
        }

        return infos;
    }

}