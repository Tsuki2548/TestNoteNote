package com.project.notenote.checkbox;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CheckboxRepository extends CrudRepository<Checkbox,Long>{
	// Fetch all checkboxes for a given checklist id to avoid lazy loading issues
	List<Checkbox> findByChecklist_ChecklistId(Long checklistId);

}
