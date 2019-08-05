package me.jjeda.houseserver.accounts;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;

@Controller
@AllArgsConstructor
public class AccountController {

    private AccountService accountService;

    @PostMapping(value = "/oauth/register", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
    public ResponseEntity signUp(@RequestBody Account account) {
        Account newAccount = Account.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .roles(Set.of(AccountRole.USER,AccountRole.ADMIN))
                .build();
        accountService.saveAccount(newAccount);

        return ResponseEntity.ok().build();
    }
}
