package me.jjeda.houseserver.configs;


import me.jjeda.houseserver.accounts.Account;
import me.jjeda.houseserver.accounts.AccountRole;
import me.jjeda.houseserver.accounts.AccountService;
import me.jjeda.houseserver.boards.Board;
import me.jjeda.houseserver.boards.BoardRepository;
import me.jjeda.houseserver.boards.BoardType;
import me.jjeda.houseserver.common.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

@Configuration
public class AppConfig {



    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
      /*  return new PasswordEncoder() {
            public String encode (CharSequence charSequence) {
                return charSequence.toString();
            }
            public boolean matches(CharSequence charSequence, String s) {
                return true;
            }
        };*/
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Autowired
            AppProperties appProperties;

            @Autowired
            BoardRepository boardRepository;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account admin = Account.builder()
                        .email(appProperties.getAdminUsername())
                        .password(appProperties.getAdminPassword())
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.TEAM_USER,AccountRole.USER))
                        .build();
                accountService.saveAccount(admin);

                Account teamUser = Account.builder()
                        .email("team@email.com")
                        .password("team")
                        .roles(Set.of(AccountRole.USER,AccountRole.TEAM_USER))
                        .build();
                accountService.saveAccount(teamUser);

                Account user = Account.builder()
                        .email(appProperties.getUserUsername())
                        .password(appProperties.getUserPassword())
                        .roles(Set.of(AccountRole.USER))
                        .build();
                accountService.saveAccount(user);
                IntStream.range(0, 30).forEach(this::generateBoard);
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
                        .files(null)
                        .createdDateTime(LocalDateTime.now())
                        .boardType(BoardType.PORTFOLIO)
                        .build();
            }

        };
    }



}
