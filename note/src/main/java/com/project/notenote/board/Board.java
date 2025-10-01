package com.project.notenote.board;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.notenote.card.Card;
import com.project.notenote.note.Note;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "boards")
public class Board {
    // Attributes
    // Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long boardId;

    @Column(name = "boardTitle", nullable = false)
    private String boardTitle;

    // order within a note (0-based). Nullable for legacy rows; service will set when reordering
    @Column(name = "orderIndex")
    private Integer orderIndex;

    // relationships   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noteId", nullable = false)
    @JsonBackReference
    private Note note;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Card> cards = new ArrayList<>();

    public Board() {
        super();
    }

    public Board(String boardTitle, Note note) {
        super();
        this.boardTitle = boardTitle;
        this.note = note;
    }

    public Long getBoardID() { return boardId;}

    public void setBoardTitle(String boardTitle) { this.boardTitle = boardTitle; }
    public String getBoardTitle() { return boardTitle; }

    public void setNote(Note note) { this.note = note; }
    public Note getNote() { return note; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public void setCards(List<Card> cards) { this.cards = cards; }
    public List<Card> getCards() { return cards; }

    public void addCard(Card card) { 
        cards.add(card);
        card.setBoard(this);
    }
    public void removeCard(Card card) { 
        cards.remove(card);
        card.setBoard(null);
    }


}
