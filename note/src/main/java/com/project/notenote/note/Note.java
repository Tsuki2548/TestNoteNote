package com.project.notenote.note;
import java.util.ArrayList;
import java.util.List;

import com.project.notenote.board.Board;
import com.project.notenote.user.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "notes")
public class Note {
    // Attributes
    // Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long noteId;
    
    @Column(name = "noteTitle", nullable = false)
    private String noteTitle;

    // relationships
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Board> boards = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    public Note() {
        super();
    }

    public Note(String noteTitle) {
        super();
        this.noteTitle = noteTitle;
    }

    public Long getNoteID() { return noteId; }

    public void setNoteTitle(String noteTitle) { this.noteTitle = noteTitle; }
    public String getNoteTitle() { return noteTitle; }

    public void setBoards(List<Board> boards) { this.boards = boards; }
    public List<Board> getBoards() { return boards; }

    public void addBoard(Board board) {
        boards.add(board);
        board.setNote(this);
    }
    public void removeBoard(Board board) {
        boards.remove(board);
        board.setNote(null);
    }



}
