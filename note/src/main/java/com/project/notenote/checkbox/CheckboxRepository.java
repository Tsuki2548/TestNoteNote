package com.project.notenote.checkbox;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckboxRepository extends CrudRepository<Checkbox,Long>{

}
