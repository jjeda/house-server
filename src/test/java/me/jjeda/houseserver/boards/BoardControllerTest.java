package me.jjeda.houseserver.boards;

import me.jjeda.houseserver.accounts.Account;
import me.jjeda.houseserver.accounts.AccountRepository;
import me.jjeda.houseserver.accounts.AccountRole;
import me.jjeda.houseserver.accounts.AccountService;
import me.jjeda.houseserver.common.AppProperties;
import me.jjeda.houseserver.common.BaseControllerTest;
import me.jjeda.houseserver.common.TestDescription;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;


public class BoardControllerTest extends BaseControllerTest {

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp() {
        this.boardRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

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
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
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
                                fieldWithPath("modifiedDateTime").description("date time of modified board"),
                                fieldWithPath("boardType").description("boardType of new board"),
                                fieldWithPath("files").description("files URL of new board"),
                                fieldWithPath("manager").description("manager of new board")
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
                                fieldWithPath("boardType").description("boardType of new board"),
                                fieldWithPath("manager").description("manager of new board"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-boards.href").description("link to query board list"),
                                fieldWithPath("_links.update-board.href").description("link to update existing board"),
                                fieldWithPath("_links.profile.href").description("link to profile")

                        )
                ));

    }

    private String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "bearer " + getAccessToken(needToCreateAccount);
    }

    private String getAccessToken(boolean needToCreateAccount) throws Exception {
        // Given
        if (needToCreateAccount) {
            createAccount();
        }

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));

        var responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    private Account createAccount() {
        Account jjeda = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        return this.accountService.saveAccount(jjeda);
    }

    @Test
    @TestDescription("입력 값이 유효하지 않는 경우에 에러가 발생하는 테스트")
    public void createBoard_Bad_Request_Wrong_Input() throws Exception {
        Board board = Board.builder().build();

        this.mockMvc.perform(post("/api/boards")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(board)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-board"));
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
                .andExpect(jsonPath("_embedded.boardList[0]._links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("query-boards"));
    }

    @Test
    @TestDescription("로그인 후 30개의 게시물을 10개씩 2번째 페이지 조회하기")
    public void queryBoardsWithAuthentiation() throws Exception {
        //Given
        IntStream.range(0, 30).forEach(this::generateBoard);

        //When & Then
        this.mockMvc.perform(get("/api/boards")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(true))
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.boardList[0]._links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-board").exists())
                .andDo(document("query-boards"));
    }


    @Test
    @TestDescription("기존의 게시물을 하나 조회하기")
    public void getBoard() throws Exception {

        Account account = this.createAccount();
        Board board = this.generateBoard(100,account);

        this.mockMvc.perform(get("/api/boards/{id}", board.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("contents").exists())
                .andExpect(jsonPath("boardType").exists())
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
        Account account = this.createAccount();
        Board tempBoard = this.generateBoard(200, account);

        Board board = this.modelMapper.map(tempBoard, Board.class);
        String BoardTitle = "Updated Board";
        board.setTitle(BoardTitle);

        //When & Them
        this.mockMvc.perform(put("/api/boards/{id}", tempBoard.getId())
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
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
        Account account = this.createAccount();
        Board tempBoard = this.generateBoard(200, account);

        Board board = Board.builder().build();

        //When & Them
        this.mockMvc.perform(put("/api/boards/{id}", tempBoard.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(board)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateBoard404() throws Exception {
        //Given
        Account account = this.createAccount();
        Board tempBoard = this.generateBoard(200, account);

        Board board = this.modelMapper.map(tempBoard, Board.class);

        //When & Them
        this.mockMvc.perform(put("/api/boards/1234124")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(board)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Board generateBoard(int index, Account account) {
        Board board = buildBoard(index);
        board.setManager(account);
        return this.boardRepository.save(board);
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