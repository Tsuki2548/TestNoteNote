package com.project.notenote.label;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LabelRepository extends CrudRepository<Label, Long> {
	Label findByLabelNameIgnoreCase(String name);
	List<Label> findByLabelNameIgnoreCaseAndColor(String name, String color);

	@Query("select l from Label l join l.cards c where c.cardId = :cardId")
	List<Label> findByCardId(Long cardId);

	@Query("select distinct l from Label l join l.cards c join c.board b join b.note n where n.noteId = :noteId")
	List<Label> findByNoteId(Long noteId);

	@Query("select distinct l from Label l join l.cards c join c.board b join b.note n where n.noteId = :noteId and upper(l.color) = upper(:color)")
	List<Label> findByNoteIdAndColor(Long noteId, String color);

	@Query("select distinct l from Label l join l.cards c join c.board b join b.note n where n.noteId = :noteId and lower(l.labelName) = lower(:name) and upper(l.color) = upper(:color)")
	List<Label> findByNoteIdAndNameAndColor(Long noteId, String name, String color);

	// Count total card associations for a label to decide if it is orphaned
	@Query("SELECT COUNT(c) FROM Card c JOIN c.labels l WHERE l.labelId = :labelId")
	long countCardsByLabelId(@Param("labelId") Long labelId);
}
