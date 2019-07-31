package me.jjeda.houseserver.boards;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class BoardResource extends Resource<Board> {

    public BoardResource(Board board, Link... links) {
        super(board, links);
        add(linkTo(BoardController.class).slash(board.getId()).withSelfRel());
    }
}
