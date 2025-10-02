package com.project.notenote.label;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LabelRepository extends CrudRepository<Label, Long> {
	Label findByLabelNameIgnoreCase(String name);
	List<Label> findByLabelNameIgnoreCaseAndColor(String name, String color);

	@Query("select l from Label l join l.cards c where c.cardId = :cardId")
	List<Label> findByCardId(Long cardId);
}
