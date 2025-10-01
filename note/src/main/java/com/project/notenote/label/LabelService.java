package com.project.notenote.label;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.notenote.card.Card;

@Service
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;

    public Label createLabel(LabelDTORequest request) {
        Label label = new Label(
                            request.getLabelName()
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

        return labelRepository.save(label);
    }

    public List<Card> getCardsByLabelId(Long labelId) {
        Label label = labelRepository.findById(labelId).orElseThrow(() -> new LabelNotFoundExeption(labelId));
        return label.getCard();
    }
}
