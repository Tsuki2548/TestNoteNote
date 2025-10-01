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


@RestController
@RequestMapping("/api/labels")
public class LabelController {
    @Autowired
    private LabelService labelService;

    public LabelDTOResponse getLabelResponse (Label label){
        LabelDTOResponse response = new LabelDTOResponse(
                                            label.getLabelId(), 
                                            label.getLabelName()
                                        );
        return response;
    }

    public List<LabelDTOResponse> getListLabelResponse(List<Label> labels){
        List<LabelDTOResponse> response = labels.stream()
                                    .map(c -> new LabelDTOResponse(
                                        c.getLabelId(), 
                                        c.getLabelName()
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
}
