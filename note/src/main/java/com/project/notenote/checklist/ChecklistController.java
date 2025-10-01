package com.project.notenote.checklist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    @Autowired
    private ChecklistService checklistService;

    public ChecklistDTOResponse getChecklistResponse(Checklist checklist){
        ChecklistDTOResponse response = new ChecklistDTOResponse(
                                                    checklist.getChecklistId(), 
                                                    checklist.getChecklistTitle(),
                                                    checklist.getCard().getCardId());
        return response;
    }

    public List<ChecklistDTOResponse> getListChecklistResponses(List<Checklist> checklists){
        List<ChecklistDTOResponse> response = checklists.stream()
                                        .map(c -> new ChecklistDTOResponse(
                                            c.getChecklistId(), 
                                            c.getChecklistTitle(), 
                                            c.getCard().getCardId()
                                        )).toList();
        return response;
    }

    @GetMapping
    public ResponseEntity<List<ChecklistDTOResponse>> getAllChecklists() {
        List<Checklist> checklists = checklistService.getAllChecklist();
        List<ChecklistDTOResponse> response = getListChecklistResponses(checklists);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{checklistId}")
    public ResponseEntity<ChecklistDTOResponse> getChecklistById(@PathVariable Long checklistId) {
        Checklist checklist = checklistService.getChecklistById(checklistId);
        ChecklistDTOResponse response = getChecklistResponse(checklist);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/byCardId/{cardId}")
    public ResponseEntity<List<ChecklistDTOResponse>> getChecklistByCardId(@PathVariable Long cardId){
        List<Checklist> checklists = checklistService.getChecklistByCardId(cardId);
        List<ChecklistDTOResponse> response = getListChecklistResponses(checklists);

        return ResponseEntity.ok(response);
    }
    

    @PostMapping
    public ResponseEntity<ChecklistDTOResponse> createChecklist(@RequestBody ChecklistDTORequest request) {
        Checklist checklist = checklistService.saveChecklist(request);
        ChecklistDTOResponse response = getChecklistResponse(checklist);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{checklistId}")
    public ResponseEntity<ChecklistDTOResponse> updateChecklist(@PathVariable Long checklistId, @RequestBody ChecklistDTORequest request) {
        Checklist checklist = checklistService.updateChecklist(checklistId, request);
        ChecklistDTOResponse response = getChecklistResponse(checklist);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{checklistId}")
    public ResponseEntity<ChecklistDTOResponse> deleteChecklist(@PathVariable Long checklistId) {
        Checklist checklist = checklistService.deleteChecklistById(checklistId);
        ChecklistDTOResponse response = getChecklistResponse(checklist);

        return ResponseEntity.ok(response);
    }
}
