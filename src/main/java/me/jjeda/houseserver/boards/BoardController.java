package me.jjeda.houseserver.boards;

import lombok.AllArgsConstructor;
import me.jjeda.houseserver.accounts.Account;
import me.jjeda.houseserver.accounts.CurrentUser;
import org.apache.tomcat.jni.Local;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@AllArgsConstructor
@CrossOrigin
@RequestMapping(value = "/api/boards", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class BoardController {

    private final BoardRepository boardRepository;

    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity createBoard(@RequestBody @Valid Board requestBoard,
                                          @CurrentUser Account currentUser) {

        Board board = modelMapper.map(requestBoard, Board.class);
        board.setManager(currentUser);
        board.setCreatedDateTime(LocalDateTime.now());
        Board newBoard = this.boardRepository.save(board);
        ControllerLinkBuilder selfLinkBuilder = linkTo(BoardController.class).slash(newBoard.getId());
        URI createdUri = selfLinkBuilder.toUri();
        BoardResource boardResource = new BoardResource(board);
        boardResource.add(linkTo(BoardController.class).withRel("query-boards"));
        boardResource.add(selfLinkBuilder.withRel("update-board"));
        boardResource.add(new Link("/docs/index.html#resources-boards-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(boardResource);
    }

    @GetMapping
    public ResponseEntity queryBoards(Pageable pageable,
                PagedResourcesAssembler<Board> assembler,
                @CurrentUser Account account) {
            Page<Board> page = this.boardRepository.findAll(pageable);
            PagedResources pagedResources = assembler.toResource(page, e -> new BoardResource(e));
            pagedResources.add(new Link("/docs/index.html#resources-boards-list").withRel("profile"));
            if (account != null) {
                pagedResources.add(linkTo(BoardController.class).withRel("create-board"));
            }


            return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getBoards(@PathVariable Integer id,
                                        @CurrentUser Account currentUser) {

        Optional<Board> optionalBoard = this.boardRepository.findById(id);

        if (optionalBoard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Board board = optionalBoard.get();
        BoardResource boardResource = new BoardResource(board);
        boardResource.add(new Link("/docs/index.html#resources-boards-get").withRel("profile"));
        if(board.getManager().equals(currentUser)) {
            boardResource.add(linkTo(BoardController.class).slash(board.getId()).withRel("update-board"));
            boardResource.add(linkTo(BoardController.class).slash(board.getId()).withRel("delete-board"));
        }
        return ResponseEntity.ok(boardResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateBoards(@PathVariable Integer id,
                                           @RequestBody @Valid Board board,
                                           @CurrentUser Account currentUser) {
        Optional<Board> optionalBoard = this.boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Board existingBoard = optionalBoard.get();
        if (!existingBoard.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(board, existingBoard);
        existingBoard.setModifiedDateTime(LocalDateTime.now());
        Board savedBoard = this.boardRepository.save(existingBoard);

        BoardResource boardResource = new BoardResource(savedBoard);
        boardResource.add(new Link("/docs/index.html#resources-boards-update").withRel("profile"));
        return ResponseEntity.ok(boardResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteBoard(@PathVariable Integer id) {
        this.boardRepository.deleteById(id);
        this.boardRepository.deleteById(id);


        return ResponseEntity.ok().build();
    }
}
