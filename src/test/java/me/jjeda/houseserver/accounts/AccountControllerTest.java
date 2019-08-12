package me.jjeda.houseserver.accounts;

import me.jjeda.houseserver.common.BaseControllerTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerTest extends BaseControllerTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;


    @Test
    public void 권한수정하는테스트() throws Exception {

        //Given
        String password = "jjeda";
        String username = "jjeda@email.com";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.TEAM_USER))
                .build();
        System.out.println(account.getId());

        //When & Them
        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/accounts/{id}", account.getId()))
                .andExpect(status().isOk());

        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}