package me.jjeda.houseserver.index;

import me.jjeda.houseserver.boards.Board;
import me.jjeda.houseserver.boards.BoardController;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public ResourceSupport index() {
        var index = new ResourceSupport();
        index.add(linkTo(BoardController.class).withRel("boards"));
        // TODO: index.add(linkTo(Post.class).withRel("posts"));
        return index;

    }
}
