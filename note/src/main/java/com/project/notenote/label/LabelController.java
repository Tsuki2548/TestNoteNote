package com.project.notenote.label;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.notenote.card.Card;
import com.project.notenote.card.CardService;
import com.project.notenote.card.CardDTOResponse;


@RestController
@RequestMapping("/api/labels")
public class LabelController {
    @Autowired
    private LabelService labelService;
    @Autowired
    private CardService cardService;

    public LabelDTOResponse getLabelResponse (Label label){
        LabelDTOResponse response = new LabelDTOResponse(
                                            label.getLabelId(), 
                                            label.getLabelName(),
                                            label.getColor()
                                        );
        return response;
    }

    public List<LabelDTOResponse> getListLabelResponse(List<Label> labels){
        List<LabelDTOResponse> response = labels.stream()
                                    .map(c -> new LabelDTOResponse(
                                        c.getLabelId(), 
                                        c.getLabelName(),
                                        c.getColor()
                                    )).toList();
        return response;
    }


    @GetMapping
    public ResponseEntity<List<LabelDTOResponse>> getAllLabels() {
        List<Label> labels = labelService.getAllLabels();
        List<LabelDTOResponse> response = getListLabelResponse(labels);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{labelId}")
    public ResponseEntity<LabelDTOResponse> getLabelsByd(@PathVariable Long labelId) {
        Label label = labelService.getLabelById(labelId);
        LabelDTOResponse response = getLabelResponse(label);

        return ResponseEntity.ok(response);
    }

    // Fetch all labels attached to a specific card
    @GetMapping("/byCardId/{cardId}")
    public ResponseEntity<java.util.List<LabelDTOResponse>> getLabelsByCard(@PathVariable Long cardId){
        java.util.List<Label> list = labelService.getLabelsByCardId(cardId);
        return ResponseEntity.ok(getListLabelResponse(list));
    }

    // Batch fetch labels by ids: /api/labels/batch?ids=1,2,3
    @GetMapping("/batch")
    public ResponseEntity<java.util.List<LabelDTOResponse>> getLabelsBatch(@RequestParam(name = "ids") String idsCsv){
        if (idsCsv == null || idsCsv.trim().isEmpty()){
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        java.util.List<Long> ids = java.util.Arrays.stream(idsCsv.split(","))
                                    .map(String::trim)
                                    .filter(s->!s.isEmpty())
                                    .map(Long::valueOf)
                                    .toList();
        java.util.List<Label> list = labelService.getLabelsByIds(ids);
        return ResponseEntity.ok(getListLabelResponse(list));
    }

    @PostMapping
    public ResponseEntity<LabelDTOResponse> createLabel(@RequestBody LabelDTORequest request) { 
        Label label = labelService.createLabel(request);
        LabelDTOResponse response = getLabelResponse(label);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{labelId}")
    public ResponseEntity<LabelDTOResponse> updateLabel(@PathVariable Long labelId, @RequestBody LabelDTORequest request) {
        Label label = labelService.updateLabel(labelId, request);
        LabelDTOResponse response = getLabelResponse(label);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<LabelDTOResponse> deleteLabel(@PathVariable Long labelId) {
        Label label = labelService.deleteLabelById(labelId);
        LabelDTOResponse response = getLabelResponse(label);
        
        return ResponseEntity.ok(response);
    }

    // Assign a label to a card
    @PostMapping("/assign/{cardId}/{labelId}")
    public ResponseEntity<CardDTOResponse> assignLabelToCard(@PathVariable Long cardId, @PathVariable Long labelId){
        Card card = cardService.addLabelToCard(cardId, labelId);
    CardDTOResponse resp = new CardDTOResponse(
            card.getCardId(),
            card.getCardTitle(),
            card.getCardContent(),
            card.getCardColor(),
            card.getBoard()!=null? card.getBoard().getBoardID(): null,
            card.getDate()!=null? card.getDate().getDateId(): null,
            card.getLabels()!=null? card.getLabels().stream().map(l->l.getLabelId()).toList() : java.util.Collections.emptyList()
        );
    return ResponseEntity.ok(resp);
    }

    // Remove a label from a card
    @DeleteMapping("/remove/{cardId}/{labelId}")
    public ResponseEntity<CardDTOResponse> removeLabelFromCard(@PathVariable Long cardId, @PathVariable Long labelId){
        Card card = cardService.removeLabelFromCard(cardId, labelId);
    CardDTOResponse resp = new CardDTOResponse(
            card.getCardId(),
            card.getCardTitle(),
            card.getCardContent(),
            card.getCardColor(),
            card.getBoard()!=null? card.getBoard().getBoardID(): null,
            card.getDate()!=null? card.getDate().getDateId(): null,
            card.getLabels()!=null? card.getLabels().stream().map(l->l.getLabelId()).toList() : java.util.Collections.emptyList()
        );
        return ResponseEntity.ok(resp);
    }

    // optional: create-or-assign in one step using label name+color
    @PostMapping("/create-assign/{cardId}")
    public ResponseEntity<CardDTOResponse> createAssign(@PathVariable Long cardId, @RequestBody LabelDTORequest request){
        Card card = cardService.createOrAssignLabelToCard(cardId, request);
        CardDTOResponse resp = new CardDTOResponse(
            card.getCardId(),
            card.getCardTitle(),
            card.getCardContent(),
            card.getCardColor(),
            card.getBoard()!=null? card.getBoard().getBoardID(): null,
            card.getDate()!=null? card.getDate().getDateId(): null,
            card.getLabels()!=null? card.getLabels().stream().map(l->l.getLabelId()).toList() : java.util.Collections.emptyList()
        );
        return ResponseEntity.ok(resp);
    }

    // Get labels for a specific note (user-scoped)
    @GetMapping("/byNoteId/{noteId}")
    public ResponseEntity<java.util.List<LabelDTOResponse>> getLabelsByNoteId(@PathVariable Long noteId){
        // Get all cards in boards of this note, then get unique labels
        try {
            java.util.Set<Label> noteLabels = new java.util.HashSet<>();
            // This is a simplified approach - in production you might want a more efficient query
            java.util.List<Card> noteCards = cardService.getCardsByNoteId(noteId);
            for (Card card : noteCards) {
                if (card.getLabels() != null) {
                    noteLabels.addAll(card.getLabels());
                }
            }
            return ResponseEntity.ok(getListLabelResponse(new java.util.ArrayList<>(noteLabels)));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    // Update a label within a note scope; enforce unique color per note
    @PutMapping("/byNoteId/{noteId}/{labelId}")
    public ResponseEntity<LabelDTOResponse> updateLabelInNote(@PathVariable Long noteId, @PathVariable Long labelId, @RequestBody LabelDTORequest request){
        Label updated = labelService.updateLabelInNote(noteId, labelId, request);
        return ResponseEntity.ok(getLabelResponse(updated));
    }

    // Delete a label within a note scope and remove it from all cards in that note
    @DeleteMapping("/byNoteId/{noteId}/{labelId}")
    public ResponseEntity<Void> deleteLabelInNote(@PathVariable Long noteId, @PathVariable Long labelId){
        labelService.deleteLabelInNote(noteId, labelId);
        return ResponseEntity.noContent().build();
    }
}
