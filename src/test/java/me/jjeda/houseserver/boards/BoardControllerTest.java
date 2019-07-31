package me.jjeda.houseserver.boards;

import me.jjeda.houseserver.common.BaseControllerTest;
import me.jjeda.houseserver.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;


public class BoardControllerTest extends BaseControllerTest {

    @Autowired
    BoardRepository boardRepository;

    @Test
    @TestDescription("정상적으로 게시물을 생성하는 테스트")
    public void createBoard() throws Exception {
        Board board = Board.builder()
                .title("Board Test")
                .contents("Rest API Development with Spring")
                .createdDateTime(LocalDateTime.now())
                .boardType(BoardType.PORTFOLIO)
                .build();

        mockMvc.perform(post("/api/boards")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(board)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(document("create-board",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-boards").description("link to query boards"),
                                linkWithRel("update-board").description("link to update an existing board"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("id").description("identifier of new board"),
                                fieldWithPath("title").description("Name of new board"),
                                fieldWithPath("contents").description("contents of new board"),
                                fieldWithPath("createdDateTime").description("date time of begin of new board"),
                                fieldWithPath("modifiedDateTime").description("date time of modified board")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("identifier of new board"),
                                fieldWithPath("title").description("Name of new board"),
                                fieldWithPath("contents").description("contents of new board"),
                                fieldWithPath("createdDateTime").description("date time of begin of new board"),
                                fieldWithPath("modifiedDateTime").description("date time of modified board"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-boards.href").description("link to query board list"),
                                fieldWithPath("_links.update-board.href").description("link to update existing board"),
                                fieldWithPath("_links.profile.href").description("link to profile")

                        )
                ));

    }

    @Test
    @TestDescription("입력 값이 유효하지 않는 경우에 에러가 발생하는 테스트")
    public void createBoard_Bad_Request_Wrong_Input() throws Exception {
        Board board = Board.builder().build();

        this.mockMvc.perform(post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(board)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @TestDescription("30개의 게시물을 10개씩 2번째 페이지 조회하기")
    public void queryBoards() throws Exception {
        //Given
        IntStream.range(0, 30).forEach(this::generateBoard);

        //When & Then
        this.mockMvc.perform(get("/api/boards")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.BoardList[0]._links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("query-boards"));
    }


    @Test
    @TestDescription("기존의 게시물을 하나 조회하기")
    public void getBoard() throws Exception {

        Board board = this.generateBoard(100);

        this.mockMvc.perform(get("/api/boards/{id}", board.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("contents").exists())
                .andExpect(jsonPath("BoardType").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-board"));


    }

    @Test
    @TestDescription("없는 게시물을 조회했을 때 404 응답받기")
    public void getBoard404() throws Exception {
        this.mockMvc.perform(get("/api/boards/1234123"))
                .andExpect(status().isNotFound());

    }

    @Test
    @TestDescription("게시물을 정상적으로 수정하기")
    public void updateBoard() throws Exception {
        //Given
        Board tempBoard = this.generateBoard(200);

        Board board = this.modelMapper.map(tempBoard, Board.class);
        String BoardTitle = "Updated Board";
        board.setTitle(BoardTitle);

        //When & Them
        this.mockMvc.perform(put("/api/boards/{id}", tempBoard.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(board)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value(BoardTitle))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-board"));

    }

    @Test
    @TestDescription("입력값이 유효하지 않는 경우에 이벤트 수정 실패")
    public void updateBoard400_Wrong() throws Exception {
        //Given
        Board tempBoard = this.generateBoard(200);

        Board board = Board.builder().build();

        //When & Them
        this.mockMvc.perform(put("/api/boards/{id}", tempBoard.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(board)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateBoard404() throws Exception {
        //Given
        Board tempBoard = this.generateBoard(200);

        Board board = this.modelMapper.map(tempBoard, Board.class);

        //When & Them
        this.mockMvc.perform(put("/api/boards/1234124")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(board)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Board generateBoard(int index) {
        Board board = buildBoard(index);
        return this.boardRepository.save(board);
    }

    private Board buildBoard(int index) {
        return Board.builder()
                .title("Board Test" + index)
                .contents("Rest API Development with Spring")
                .createdDateTime(LocalDateTime.now())
                .boardType(BoardType.PORTFOLIO)
                .build();
    }


}