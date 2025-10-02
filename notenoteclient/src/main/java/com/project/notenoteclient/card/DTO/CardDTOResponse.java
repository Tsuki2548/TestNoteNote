package com.project.notenoteclient.card.DTO;

public class CardDTOResponse {
    private Long cardId;
    private String cardTitle;
    private String cardContent;
    private String cardColor;
    private Long boardId;
    private Long dateId;
    private java.util.List<Long> labelIds;

    public CardDTOResponse(Long cardId, String cardTitle,String cardContent,String cardColor, Long boardId,Long dateId, java.util.List<Long> labelIds){
        this.cardId = cardId;
        this.cardTitle = cardTitle;
        this.cardContent = cardContent;
        this.cardColor = cardColor;
        this.boardId = boardId;
        this.dateId = dateId;
        this.labelIds = labelIds;
    }

    public Long getCardId() { return cardId; }
    public String getCardTitle() { return cardTitle; }
    public String getCardContent(){return cardContent;}
    public String getCardColor(){return cardColor;}
    public Long getBoardId() { return boardId; }
    public Long getDateId(){return dateId;}
    public java.util.List<Long> getLabelIds(){return labelIds;}
}
