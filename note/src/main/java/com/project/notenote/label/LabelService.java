package com.project.notenote.label;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.notenote.card.Card;
import com.project.notenote.card.CardRepository;

@Service
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private CardRepository cardRepository;

    public Label createLabel(LabelDTORequest request) {
        Label label = new Label(
                            request.getLabelName(),
                            request.getColor() != null ? request.getColor() : "#6c757d"
                        );
        return labelRepository.save(label);
    }

    public Label getLabelById(Long labelId) {
        if (labelId == null){
            return null;
        }
        return labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
    }

    public List<Label> getAllLabels() {
        return (List<Label>) labelRepository.findAll();
    }

    public Label deleteLabelById(Long labelId) {
        Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
        labelRepository.delete(label);
        return label;
    }

    public Label updateLabel(Long labelId, LabelDTORequest request) {
        Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
        
        label.setLabelName(request.getLabelName());
        if (request.getColor()!=null) label.setColor(request.getColor());

        return labelRepository.save(label);
    }

    public List<Card> getCardsByLabelId(Long labelId) {
        Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
        return new java.util.ArrayList<>(label.getCards());
    }

    public java.util.List<Label> getLabelsByCardId(Long cardId){
        return labelRepository.findByCardId(cardId);
    }

    public java.util.List<Label> getLabelsByIds(java.util.List<Long> ids){
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<Label> out = new java.util.ArrayList<>();
        for (Label l : labelRepository.findAllById(ids)){
            out.add(l);
        }
        return out;
    }

    @Transactional
    public Label updateLabelInNote(Long noteId, Long labelId, LabelDTORequest request){
        Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
        String newName = request.getLabelName()!=null? request.getLabelName() : label.getLabelName();
        String newColor = request.getColor()!=null? request.getColor() : label.getColor();
        // Enforce unique color per note: if another label in this note uses the color, reject
        java.util.List<Label> sameColor = labelRepository.findByNoteIdAndColor(noteId, newColor);
        for (Label l : sameColor){
            if (!l.getLabelId().equals(labelId)){
                throw new IllegalArgumentException("Color already used by another label in this note. Edit or delete it first.");
            }
        }
        // Check if this label is used outside this note
        boolean usedOutside = false;
        java.util.List<Card> cardsOfLabel = new java.util.ArrayList<>(label.getCards());
        for (Card c : cardsOfLabel){
            try {
                Long nid = c.getBoard()!=null && c.getBoard().getNote()!=null ? c.getBoard().getNote().getNoteID() : null;
                if (nid != null && !nid.equals(noteId)) { usedOutside = true; break; }
            } catch(Exception ignored) { }
        }
        if (!usedOutside){
            // Safe to update the label directly
            label.setLabelName(newName);
            label.setColor(newColor);
            return labelRepository.save(label);
        } else {
            // Clone label for this note, reassign cards in this note to the new label
            Label clone = new Label(newName, newColor);
            clone = labelRepository.save(clone);
            for (Card c : cardsOfLabel){
                try {
                    Long nid = c.getBoard()!=null && c.getBoard().getNote()!=null ? c.getBoard().getNote().getNoteID() : null;
                    if (nid != null && nid.equals(noteId)){
                        c.getLabels().remove(label);
                        c.getLabels().add(clone);
                        cardRepository.save(c);
                    }
                } catch(Exception ignored) {}
            }
            return clone;
        }
    }

    @Transactional
    public void deleteLabelInNote(Long noteId, Long labelId){
        Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
        // Remove this label from all cards in the note
        java.util.List<Label> noteLabels = labelRepository.findByNoteId(noteId);
        boolean existsInNote = false;
        for (Label l : noteLabels){ if (l.getLabelId().equals(labelId)) { existsInNote = true; break; } }
        if (!existsInNote) return; // no-op if label not in this note scope
        for (Card c : new java.util.ArrayList<>(label.getCards())){
            try {
                if (c.getBoard()!=null && c.getBoard().getNote()!=null && c.getBoard().getNote().getNoteID().equals(noteId)){
                    c.removeLabel(label);
                    cardRepository.save(c);
                }
            } catch(Exception ignored) {}
        }
        // If label is now orphaned (no cards), delete it
        if (label.getCards().isEmpty()){
            labelRepository.delete(label);
        }
    }
}
