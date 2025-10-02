package com.project.notenote.checkbox;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.notenote.checklist.Checklist;
import com.project.notenote.checklist.ChecklistService;

@Service
public class CheckboxService {
    @Autowired
    private CheckboxRepository checkboxRepository;

    @Autowired
    private ChecklistService checklistService;

    public Checkbox saveCheckbox(CheckboxDTORequest request) {
        Checkbox checkbox = new Checkbox(request.getCheckboxTitle());
        Checklist checklist = checklistService.getChecklistById(request.getChecklistId());
        checkbox.setChecklist(checklist);

        return checkboxRepository.save(checkbox);
    }

    public List<Checkbox> getAllCheckboxs() {
        return (List<Checkbox>) checkboxRepository.findAll();
    }

    public Checkbox getCheckboxById(Long checkboxId) {
        if (checkboxId == null){
            return null;
        }
        return checkboxRepository.findById(checkboxId).orElseThrow(() -> new CheckboxNotFoundException("NotFound Checkbox"));
                
    }

    public Checkbox deleteCheckboxById(Long checkboxId) {
        Checkbox checkbox = checkboxRepository.findById(checkboxId).orElseThrow(() -> new CheckboxNotFoundException("NotFound Checkbox"));       
        checkboxRepository.delete(checkbox);

        return checkbox;
    }

    public Checkbox updateCheckbox(Long checkboxId, CheckboxDTORequest request) {
        Checkbox checkbox = checkboxRepository.findById(checkboxId).orElseThrow(() -> new CheckboxNotFoundException("NotFound Checkbox"));

        if (request.getCheckboxTitle() != null){
            checkbox.setCheckboxTitle(request.getCheckboxTitle());
        }
        
        if (request.getChecklistId() != null){
            Checklist checklist = checklistService.getChecklistById(request.getChecklistId());
            checkbox.setChecklist(checklist);
        }

        if (request.getCompleted() != null){
            checkbox.setCompleted(request.getCompleted());
        }

        return checkboxRepository.save(checkbox);
    }


    public List<Checkbox> getCheckboxsByChecklistId(Long checklistId){
        // Fetch directly via repository to avoid LazyInitializationException on detached entities
        return checkboxRepository.findByChecklist_ChecklistId(checklistId);
    }

}
