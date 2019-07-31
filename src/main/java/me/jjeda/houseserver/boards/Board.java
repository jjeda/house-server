package me.jjeda.houseserver.boards;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.jjeda.houseserver.accounts.Account;
import me.jjeda.houseserver.accounts.AccountSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Board {

    @Id @GeneratedValue
    private Integer id;
    @NotEmpty
    private String title;
    @NotEmpty
    private String contents;
    @NotNull
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

    @Enumerated(EnumType.STRING)
    @NotNull
    private BoardType boardType;

    @ManyToOne
    @JsonSerialize(using = AccountSerializer.class)
    private Account manager;

}
