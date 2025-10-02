package com.project.notenoteclient.label.DTO;

public class LabelDTORequest {
    private String labelName;
    private String color;

    public LabelDTORequest() { }

    public LabelDTORequest(String labelName, String color) { this.labelName = labelName; this.color = color; }

    public void setLabelName(String labelName) { this.labelName = labelName; }
    public String getLabelName() { return labelName; }
    public String getColor(){ return color; }
    public void setColor(String color){ this.color = color; }
}
