(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state, U = NW.utils, ST = NW.storage;

  function updateCurrentNoteTitle(){
    const noteTitleBar = document.getElementById('noteTitleBar');
    const currentNoteTitle = document.getElementById('currentNoteTitle');
    const deleteNoteBtn = document.getElementById('deleteNoteBtn');
    const emptyState = document.getElementById('emptyState');
    const boardContainer = document.getElementById('boardContainer');
    if (S.notes.length === 0) {
      currentNoteTitle.textContent = '';
      noteTitleBar.style.display = 'flex';
      currentNoteTitle.innerHTML = '<span style="color:#fff;font-size:22px;font-weight:600;">สร้างโน๊ตแรกของคุณ</span>';
      const filterBtn = noteTitleBar.querySelector('.filter-btn');
      if (filterBtn) filterBtn.style.display = 'none';
      if (deleteNoteBtn) deleteNoteBtn.style.display = 'none';
      if (boardContainer) boardContainer.style.display = 'none';
      if (emptyState) emptyState.style.display = 'flex';
    } else {
      const note = S.notes.find(n => n.id === S.currentNoteId);
      if (note) {
        currentNoteTitle.textContent = note.name;
        const filterBtn = noteTitleBar.querySelector('.filter-btn');
        if (filterBtn) filterBtn.style.display = '';
        if (deleteNoteBtn) deleteNoteBtn.style.display = '';
        if (boardContainer) boardContainer.style.display = '';
        if (emptyState) emptyState.style.display = 'none';
      }
    }
  }

  function createNote(){
    // Deprecated prompt flow; now open modal
    openNoteCreateModal();
  }

  function openNoteCreateModal(){
    const modal = document.getElementById('noteCreateModal');
    const input = document.getElementById('noteNameInput');
    const err = document.getElementById('noteNameError');
    if (modal){
      modal.classList.add('open');
      if (err) err.style.display='none';
      if (input){ input.value=''; setTimeout(()=>input.focus(), 0); }
      // bind one-time key handler
      input?.addEventListener('keydown', handleNoteNameKeydown);
    }
  }

  function closeNoteCreateModal(){
    const modal = document.getElementById('noteCreateModal');
    const input = document.getElementById('noteNameInput');
    if (modal) modal.classList.remove('open');
    if (input) input.removeEventListener('keydown', handleNoteNameKeydown);
  }

  function handleNoteNameKeydown(e){
    if (e.key==='Enter') { e.preventDefault(); confirmCreateNote(); }
    if (e.key==='Escape') { e.preventDefault(); closeNoteCreateModal(); }
  }

  function confirmCreateNote(){
    const input = document.getElementById('noteNameInput');
    const err = document.getElementById('noteNameError');
    const name = (input?.value||'').trim();
    if (!name){ if (err) err.style.display='block'; input?.focus(); return; }
    const note = { id: U.generateId(), name, createdAt: new Date().toISOString() };
    S.notes.push(note);
    S.currentNoteId = note.id;
    ST.save();
    closeNoteCreateModal();
    updateCurrentNoteTitle();
    NW.boards.renderBoards();
  }

  function showNotes(){
    if (S.notes.length === 0) {
      const btn = document.querySelector('.notebtn-switch');
      if (btn){
        btn.classList.remove('shake'); // reset if already applied
        void btn.offsetWidth; // force reflow to restart animation
        btn.classList.add('shake');
        btn.addEventListener('animationend', ()=> btn.classList.remove('shake'), { once: true });
      }
      return;
    }
    openSwitchNoteModal();
  }

  let _selectedNoteIdForSwitch = null;
  let _switchKeyHandler = null;

  function openSwitchNoteModal(){
    const modal = document.getElementById('noteSwitchModal');
    const listEl = document.getElementById('noteSwitchList');
    if (!modal || !listEl) return;
    // render items
    listEl.innerHTML = S.notes.map(n=>{
      const initials = (n.name||'N').trim().charAt(0).toUpperCase();
      const isCurrent = n.id===S.currentNoteId;
      return `
        <div class="note-switch-item ${isCurrent? 'selected':''}" data-note-id="${n.id}" onclick="selectNoteToSwitch('${n.id}')">
          <div class="note-switch-badge">${initials}</div>
          <div style="display:flex;flex-direction:column;gap:2px;flex:1;">
            <div class="note-switch-title">${U.escapeHtml(n.name)}</div>
            <div class="note-switch-meta">สร้างเมื่อ ${new Date(n.createdAt).toLocaleDateString('th-TH',{day:'numeric',month:'short',year:'numeric'})}${isCurrent?' · ปัจจุบัน':''}</div>
          </div>
        </div>`;
    }).join('');
    _selectedNoteIdForSwitch = S.currentNoteId;
    modal.classList.add('open');
    _switchKeyHandler = (e)=>{ if (e.key==='Escape'){ e.preventDefault(); closeSwitchNoteModal(); } if (e.key==='Enter'){ e.preventDefault(); confirmSwitchNote(); } };
    document.addEventListener('keydown', _switchKeyHandler);
  }

  function closeSwitchNoteModal(){
    const modal = document.getElementById('noteSwitchModal');
    if (modal) modal.classList.remove('open');
    if (_switchKeyHandler){ document.removeEventListener('keydown', _switchKeyHandler); _switchKeyHandler=null; }
  }

  function selectNoteToSwitch(noteId){
    _selectedNoteIdForSwitch = noteId;
    const listEl = document.getElementById('noteSwitchList');
    if (!listEl) return;
    listEl.querySelectorAll('.note-switch-item').forEach(el=>{
      const id = el.getAttribute('data-note-id');
      if (id===noteId) el.classList.add('selected'); else el.classList.remove('selected');
    });
  }

  function confirmSwitchNote(){
    if (!_selectedNoteIdForSwitch) { closeSwitchNoteModal(); return; }
    if (_selectedNoteIdForSwitch !== S.currentNoteId){
      S.currentNoteId = _selectedNoteIdForSwitch;
      ST.save();
      updateCurrentNoteTitle();
      NW.boards.renderBoards();
    }
    closeSwitchNoteModal();
  }

  function deleteNote(){
    if (S.notes.length===0) return;
    const note = S.notes.find(n=>n.id===S.currentNoteId);
    const name = note? note.name : '';
    NW.ui.openConfirm({
      title: 'ลบโน๊ต',
      message: name? `ต้องการลบโน๊ต "${name}" หรือไม่?\nบอร์ดและการ์ดทั้งหมดภายในจะถูกลบด้วย` : 'ต้องการลบโน๊ตนี้หรือไม่? บอร์ดและการ์ดทั้งหมดภายในจะถูกลบด้วย',
      variant: 'danger',
      confirmText: 'ลบโน๊ต',
      onConfirm: ()=>{
        if (!S.currentNoteId) { if (S.notes.length>0) S.currentNoteId=S.notes[0].id; else return; }
        const deletedBoardIds = S.boards.filter(b=>b.noteId===S.currentNoteId).map(b=>b.id);
        S.boards = S.boards.filter(b=>b.noteId!==S.currentNoteId);
        S.cards = S.cards.filter(c=>!deletedBoardIds.includes(c.boardId));
        S.notes = S.notes.filter(n=>n.id!==S.currentNoteId);
        S.currentNoteId = S.notes.length>0 ? S.notes[0].id : null;
        ST.save();
        updateCurrentNoteTitle();
        if (S.notes.length===0){
          ST.clearAll();
          updateCurrentNoteTitle();
        } else {
          NW.boards.renderBoards();
          updateCurrentNoteTitle();
        }
      }
    });
  }

  NW.notes = { updateCurrentNoteTitle, createNote, openNoteCreateModal, closeNoteCreateModal, confirmCreateNote, showNotes, deleteNote, openSwitchNoteModal, closeSwitchNoteModal, selectNoteToSwitch, confirmSwitchNote };
  global.createNote = createNote;
  global.showNotes = showNotes;
  global.deleteNote = deleteNote;
  global.closeSwitchNoteModal = closeSwitchNoteModal;
  global.selectNoteToSwitch = selectNoteToSwitch;
  global.confirmSwitchNote = confirmSwitchNote;
})(window);
