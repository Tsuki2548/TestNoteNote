package com.project.notenote.card;

public class CardDTORequest {
    private String cardTitle;
    private String cardContent;
    private String cardColor;
    private java.util.List<Long> labelIds;
    private Long dateId;
    private Long boardId;

    public CardDTORequest() {}

    public CardDTORequest(String cardTitle,String cardContent,String cardColor, Long boardId,Long dateId,java.util.List<Long> labelIds) {
        this.cardTitle = cardTitle;
        this.cardContent = cardContent;
        this.cardColor = cardColor;
        this.boardId = boardId;
        this.dateId = dateId;
        this.labelIds = labelIds;
    }

    public String getCardTitle() {return cardTitle;}
    public void setCardTitle(String cardTitle) {this.cardTitle = cardTitle;}

    public void setCardColor(String cardColor){this.cardColor = cardColor;}
    public String getCardColor(){return cardColor;}

    public void setCardContent(String cardContent){this.cardContent = cardContent;}
    public String getCardContent(){return cardContent;}

    public Long getBoardId() {return boardId;}
    public void setBoardId(Long boardId) {this.boardId = boardId;}

    public void setLabelIds(java.util.List<Long> labelIds){this.labelIds = labelIds;}
    public java.util.List<Long> getLabelIds(){return labelIds;}

    public void setDateId(Long dateId){this.dateId = dateId;}
    public Long getDateId(){return dateId;}
}

