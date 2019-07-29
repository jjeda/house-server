package me.jjeda.houseserver.portfolios;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jjeda.houseserver.common.BaseControllerTest;
import me.jjeda.houseserver.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

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





}