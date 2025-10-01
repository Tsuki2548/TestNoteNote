package com.project.notenote.board;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
	java.util.List<Board> findByNoteNoteIdOrderByOrderIndexAsc(Long noteId);
    
}
