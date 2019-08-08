package me.jjeda.houseserver.accounts;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@CrossOrigin
public class AccountController {

    private AccountService accountService;

    private AccountRepository accountRepository;

    @PostMapping(value = "/oauth/register", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
    public ResponseEntity signUp(@RequestBody Account account) {
        Account newAccount = Account.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .roles(Set.of(AccountRole.USER))
                .build();
        accountService.saveAccount(newAccount);

        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
    public ResponseEntity list(Pageable pageable,
                               PagedResourcesAssembler<Account> assembler) {
        Page<Account> page = this.accountRepository.findAll(pageable);

        return ResponseEntity.ok(page);
    }

    @PutMapping(value = "/api/accounts/{id}", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
    public ResponseEntity updateRoleSwitch(@PathVariable Integer id, @RequestBody Account account) {

        // TODO : TEAM_USER <-> USER
        // id 값 변경없이
        /*Set roles = account.getRoles();

        if(roles.contains(AccountRole.TEAM_USER)) {
            roles.remove(AccountRole.TEAM_USER);
        } else {
            roles.add(AccountRole.TEAM_USER);
        }
        */
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/accounts/{id}", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
    public ResponseEntity deleteAccount(@PathVariable Integer id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
