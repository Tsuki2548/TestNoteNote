(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state;

  document.addEventListener('DOMContentLoaded', function(){
    // Prefer server-provided notes on first load, fallback to local storage
    try {
      if (Array.isArray(global.BOOT_NOTES) && global.BOOT_NOTES.length>0){
        S.notes = global.BOOT_NOTES.map(n=>({ id: String(n.noteId), name: n.noteTitle, createdAt: new Date().toISOString() }));
        S.currentNoteId = S.notes[0]?.id || null;
      } else {
        NW.storage.load();
        if (S.notes.length>0) S.currentNoteId = S.notes[0].id;
      }
    } catch (_) { NW.storage.load(); if (S.notes.length>0) S.currentNoteId = S.notes[0].id; }

    NW.notes.updateCurrentNoteTitle();
    if (S.notes.length>0) NW.boards.renderBoards();

    // search
    const search = document.getElementById('searchInput');
    if (search) search.addEventListener('input', NW.ui.handleSearch);

    // create board enter key
    const input = document.getElementById('newBoardName');
    if (input) input.addEventListener('keypress', function(e){ if (e.key==='Enter') NW.boards.createBoard(); });

    // color picker click
    document.querySelectorAll('.color-option').forEach(option=>{
      option.addEventListener('click', function(){
        document.querySelectorAll('.color-option').forEach(o=>o.classList.remove('selected'));
        this.classList.add('selected');
      });
    });

    // label color picker click (in label create modal)
    document.addEventListener('click', function(e){
      const opt = e.target.closest('.label-color-option');
      if (opt){
        document.querySelectorAll('.label-color-option').forEach(o=>o.classList.remove('selected'));
        opt.classList.add('selected');
      }
    });

    // modal outside click
  const modal = document.getElementById('cardModal');
  if (modal) modal.addEventListener('click', function(e){ if (e.target===this) NW.cards.closeCardModal(); });
  const noteModal = document.getElementById('noteCreateModal');
  if (noteModal) noteModal.addEventListener('click', function(e){ if (e.target===this) NW.notes.closeNoteCreateModal(); });
  const labelCreate = document.getElementById('labelCreateModal');
  if (labelCreate) labelCreate.addEventListener('click', function(e){ if (e.target===this) NW.cards.closeLabelCreateModal(); });
  const checklistCreate = document.getElementById('checklistCreateModal');
  if (checklistCreate) checklistCreate.addEventListener('click', function(e){ if (e.target===this) NW.cards.closeChecklistCreateModal(); });

  // open note create modal from empty-state primary button is inline onclick="createNote()"
  // but we replace prompt flow by modal; override global
  global.createNote = function(){ NW.notes.openNoteCreateModal(); };

    // close dropdowns when clicking outside
    document.addEventListener('click', function(e){
      if (!e.target.closest('.add-board-wrapper')){
        const dd = document.getElementById('addBoardDropdown'); if (dd && dd.classList.contains('open')) dd.classList.remove('open');
      }
      if (!e.target.closest('.circle-btn') && !e.target.closest('.settings-dropdown')){
        const s = document.getElementById('settingsDropdown'); if (s && s.classList.contains('open')) s.classList.remove('open');
      }
      if (!e.target.closest('.filter-btn') && !e.target.closest('.filter-dropdown')){
        const f = document.getElementById('filterDropdown'); if (f && f.classList.contains('open')) f.classList.remove('open');
      }
    });

    // Prevent default drag behavior
    document.addEventListener('dragover', function(e){ e.preventDefault(); }, false);
    document.addEventListener('drop', function(e){ e.preventDefault(); }, false);

    // Notification permission
    if ("Notification" in window && Notification.permission === 'default') Notification.requestPermission();
  });

  // expose some app-level functions used by inline HTML
  global.createBoard = function(){ NW.boards.createBoard(); };
  global.addNewCard = function(boardId){ NW.cards.addNewCard(boardId); };
  global.openCardModal = function(cardId){ NW.cards.openCardModal(cardId); };
  global.closeCardModal = function(){ NW.cards.closeCardModal(); };
  global.saveCard = function(){ NW.cards.saveCard(); };
  global.deleteCard = function(){ NW.cards.deleteCard(); };
  global.openLabelCreateModal = function(){ NW.cards.openLabelCreateModal(); };
  global.closeLabelCreateModal = function(){ NW.cards.closeLabelCreateModal(); };
  global.confirmCreateLabel = function(){ NW.cards.confirmCreateLabel(); };
  global.removeLabel = function(id){ NW.cards.removeLabel(id); };
  global.openChecklistCreateModal = function(){ NW.cards.openChecklistCreateModal(); };
  global.closeChecklistCreateModal = function(){ NW.cards.closeChecklistCreateModal(); };
  global.confirmCreateChecklist = function(){ NW.cards.confirmCreateChecklist(); };
  global.checklistInlineKey = function(e, id){ NW.cards.checklistInlineKey(e, id); };
  global.toggleCheckItem = function(cid,iid){ NW.cards.toggleCheckItem(cid,iid); };

  global.createNote = function(){ NW.notes.createNote(); };
  global.showNotes = function(){ NW.notes.showNotes(); };
  global.deleteNote = function(){ NW.notes.deleteNote(); };
  global.closeNoteCreateModal = function(){ NW.notes.closeNoteCreateModal(); };
  global.confirmCreateNote = function(){ NW.notes.confirmCreateNote(); };

})(window);
