package com.project.notenote.label;

public class LabelDTOResponse {
    private Long labelId;
    private String labelName;
    private String color;


    public LabelDTOResponse() {}

    public LabelDTOResponse(Long labelId, String labelName, String color) {
        this.labelId = labelId;
        this.labelName = labelName;
        this.color = color;
    }

    public void setLabelId(Long labelId) { this.labelId = labelId; }
    public Long getLabelId() { return labelId; }

    public void setLabelName(String labelName) { this.labelName = labelName; }
    public String getLabelName() { return labelName; }
    public String getColor(){ return color; }
    public void setColor(String color){ this.color = color; }

}
