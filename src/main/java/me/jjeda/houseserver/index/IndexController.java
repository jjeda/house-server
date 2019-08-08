package me.jjeda.houseserver.index;

import me.jjeda.houseserver.accounts.AccountController;
import me.jjeda.houseserver.boards.Board;
import me.jjeda.houseserver.boards.BoardController;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@CrossOrigin
public class IndexController {

    @GetMapping("/api")
    public ResourceSupport index() {
        var index = new ResourceSupport();
        index.add(linkTo(BoardController.class).withRel("boards"));
        index.add(linkTo(AccountController.class).slash("api/accounts").withRel("accounts"));
        // TODO: index.add(linkTo(Post.class).withRel("posts"));
        return index;

    }
}
