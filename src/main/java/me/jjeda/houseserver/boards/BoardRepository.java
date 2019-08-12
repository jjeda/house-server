package me.jjeda.houseserver.boards;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Integer> {

    Page<Board> findAllByBoardType(BoardType boardType, Pageable pageable);
}
