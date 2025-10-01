package com.project.notenote.checkbox;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/checkboxes")
public class CheckboxController {

    @Autowired
    private CheckboxService checkboxService;

    public CheckboxDTOResponse getCheckboxResponse(Checkbox checkbox){
        CheckboxDTOResponse response = new CheckboxDTOResponse(
                                                checkbox.getCheckboxId(),
                                                checkbox.getCheckboxTitle(),
                                                checkbox.getChecklist() != null ? checkbox.getChecklist().getChecklistId() : null,
                                                checkbox.getCompleted()
                                             );
        return response;
    }

    public List<CheckboxDTOResponse> getListCheckboxResponse(List<Checkbox> checkboxs){
        List<CheckboxDTOResponse> response = checkboxs.stream()
                                    .map(c -> new CheckboxDTOResponse(
                                        c.getCheckboxId(),
                                        c.getCheckboxTitle(),
                                        c.getChecklist() != null ? c.getChecklist().getChecklistId() : null,
                                        c.getCompleted()
                                    )).toList();
        return response;
    }


    @GetMapping
    public ResponseEntity<List<CheckboxDTOResponse>> getAllCheckboxs() {
        List<Checkbox> checkboxs = checkboxService.getAllCheckboxs();
        List<CheckboxDTOResponse> response = getListCheckboxResponse(checkboxs);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{checkboxId}")
    public ResponseEntity<CheckboxDTOResponse> getCheckboxById(@PathVariable Long checkboxId) {
        Checkbox checkbox = checkboxService.getCheckboxById(checkboxId);
        CheckboxDTOResponse response = getCheckboxResponse(checkbox);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/byChecklistId/{checklistId}")
    public ResponseEntity<List<CheckboxDTOResponse>> getCheckboxByChecklistId(@PathVariable Long checklistId){
        List<Checkbox> checkboxs = checkboxService.getCheckboxsByChecklistId(checklistId);
        List<CheckboxDTOResponse> response = getListCheckboxResponse(checkboxs);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CheckboxDTOResponse> createCheckbox(@RequestBody CheckboxDTORequest request) {    
        Checkbox checkbox = checkboxService.saveCheckbox(request);
        CheckboxDTOResponse response = getCheckboxResponse(checkbox);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{checkboxId}")
    public ResponseEntity<CheckboxDTOResponse> updateCheckbox(@PathVariable Long checkboxId, @RequestBody CheckboxDTORequest request) {
        Checkbox checkbox = checkboxService.updateCheckbox(checkboxId, request);
        CheckboxDTOResponse response = getCheckboxResponse(checkbox);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{checkboxId}")
    public ResponseEntity<CheckboxDTOResponse> deleteCheckbox(@PathVariable Long checkboxId) {
        Checkbox checkbox = checkboxService.deleteCheckboxById(checkboxId);
        CheckboxDTOResponse response = getCheckboxResponse(checkbox);
        
        return ResponseEntity.ok(response);
    }
}

