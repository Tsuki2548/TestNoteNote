package com.project.notenote.checklist;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistRepository extends CrudRepository<Checklist,Long>{

}
