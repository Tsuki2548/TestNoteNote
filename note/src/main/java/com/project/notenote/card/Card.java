package com.project.notenote.card;

import java.util.ArrayList;
import java.util.List;

import com.project.notenote.board.Board;
import com.project.notenote.checklist.Checklist;
import com.project.notenote.date.Date;
import com.project.notenote.label.Label;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class Card {
    // Attributes
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cardId;

    @Column(name = "cardTitle", nullable = false)
    private String cardTitle;

    @Column(name = "cardContent", nullable = false)
    private String cardContent;

    @Column(name = "cardColor",nullable = false)
    private String cardColor;

    // order within a board (0-based). Nullable for legacy rows; service will set when reordering
    @Column(name = "orderIndex")
    private Integer orderIndex;

    // relationships
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Checklist> Checklists = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardId", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labelId", nullable = true)
    private Label label;

    @OneToOne
    @JoinColumn(name = "dateId", unique = true,nullable = true)
    private Date date;

    public Card(){super();}

    public Card(String title,Board board,String cardColor,String cardContent) {
        super();
        this.cardTitle = title;
        this.board = board;
        this.cardContent = cardContent;
        this.cardColor = cardColor;
    }

    public Long getCardId(){return cardId;}

    public void setCardTitle(String cardTitle) { this.cardTitle = cardTitle; }
    public String getCardTitle() { return cardTitle; }

    public void setCardColor(String cardColor){this.cardColor = cardColor;}
    public String getCardColor(){return cardColor;}

    public void setCardContent(String cardContent){this.cardContent = cardContent;}
    public String getCardContent(){return cardContent;}

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public void setBoard(Board board) { this.board = board; }
    public Board getBoard() { return board; }

    public void setLabel(Label label) { this.label = label; }
    public Label getLabel() { return label; }

    public void setDate(Date date){this.date = date;}
    public Date getDate(){return date;}

    public void setChecklists(List<Checklist> checklists) { Checklists = checklists; }
    public List<Checklist> getChecklists() { return Checklists; }

    public void addChecklist(Checklist checklist) {
        Checklists.add(checklist);
        checklist.setCard(this);
    }
    public void removeChecklist(Checklist checklist) {
        Checklists.remove(checklist);
        checklist.setCard(null);
    }


}
