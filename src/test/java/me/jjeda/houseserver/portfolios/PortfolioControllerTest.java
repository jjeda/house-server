package me.jjeda.houseserver.portfolios;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jjeda.houseserver.common.BaseControllerTest;
import me.jjeda.houseserver.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PortfolioControllerTest extends BaseControllerTest {

    @Autowired
    PortfolioRepository portfolioRepository;





    @Test
    @TestDescription("정상적으로 포트폴리오를 생성하는 테스트")
    public void createPortfolio() throws Exception {
        Portfolio portfolio = Portfolio.builder()
                .title("Portfolio Test")
                .contents("Rest API Development with Spring")
                .createdDateTime(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/portfolios")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(portfolio)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists());

    }

    @Test
    @TestDescription("30개의 포트폴리오를 10개씩 2번째 페이지 조회하기")
    public void queryPortfolios() throws Exception {
        //Given
        IntStream.range(0,30).forEach(this::generatePortfolio);

        //When & Then
        this.mockMvc.perform(get("/api/portfolios")
                .param("page","1")
                .param("size","10")
                .param("sort","id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.portfolioList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists());
    }


    @Test
    @TestDescription("기존의 포트폴리오를 하나 조회하기")
    public void getPortfolio() throws Exception {

        Portfolio portfolio = this.generatePortfolio(100);

        this.mockMvc.perform(get("/api/portfolios/{id}",portfolio.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("contents").exists())
                .andExpect(jsonPath("_links.self").exists());


    }

    @Test
    @TestDescription("없는 포트폴리오를 조회했을 때 404 응답받기")
    public void getPortfolio404() throws Exception {
        this.mockMvc.perform(get("/api/portfolio/1234123"))
                .andExpect(status().isNotFound());

    }

    private Portfolio generatePortfolio(int index) {
        Portfolio portfolio = buildPortfolio(index);
        return this.portfolioRepository.save(portfolio);
    }

    private Portfolio buildPortfolio(int index) {
        return Portfolio.builder()
                .title("Portfolio Test"+ index)
                .contents("Rest API Development with Spring")
                .createdDateTime(LocalDateTime.now())
                .build();
    }
}