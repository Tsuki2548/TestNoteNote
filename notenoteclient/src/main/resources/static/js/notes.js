(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state, U = NW.utils, ST = NW.storage;
  const BASE = window.location.origin; // align with Note.js style absolute URL

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
        // enable inline edit on title (double-click to edit)
        currentNoteTitle.title = 'ดับเบิลคลิกเพื่อแก้ไขชื่อโน๊ต';
        currentNoteTitle.ondblclick = startEditCurrentNoteTitle;
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

  async function confirmCreateNote(){
    const input = document.getElementById('noteNameInput');
    const err = document.getElementById('noteNameError');
    const name = (input?.value||'').trim();
    if (!name){ if (err) err.style.display='block'; input?.focus(); return; }
    try {
      const resp = await fetch(`/notes/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ noteTitle: name, username: window.CURRENT_USERNAME || undefined })
      });
      if (!resp.ok) {
        // Try parse error body as JSON; fallback to text
        let message = `สร้างโน๊ตไม่สำเร็จ (HTTP ${resp.status})`;
        try { const errJson = await resp.json(); message = errJson.error || JSON.stringify(errJson); } catch(_){ try { message = await resp.text(); } catch(__){} }
        throw new Error(message);
      }
      const data = await resp.json();
      const note = { id: String(data.noteId), name: data.noteTitle, createdAt: new Date().toISOString() };
      S.notes.push(note);
      S.currentNoteId = note.id;
      ST.save();
      closeNoteCreateModal();
      updateCurrentNoteTitle();
      NW.boards.renderBoards();
    } catch (e) {
      alert(e?.message || 'เกิดข้อผิดพลาดในการสร้างโน๊ต');
    }
  }

  // helper to fetch boards and cards for a specific note id
  async function fetchBoardsAndCardsForNote(noteId){
    const result = { boards: [], cards: [] };
    if (!noteId) return result;
    try {
      const boardsResp = await fetch(`/boards/byNoteId/${encodeURIComponent(noteId)}`, { headers: { 'Accept':'application/json' } });
      if (boardsResp.ok) {
        const boardsJson = await boardsResp.json();
        result.boards = Array.isArray(boardsJson)
          ? boardsJson.map(b=>({ id: String(b.boardID||b.boardId), noteId: String(b.noteId), name: b.boardTitle||b.name, createdAt: new Date().toISOString() }))
          : [];
      }
      
      // ดึง labels ที่เป็นของ user นี้เท่านั้น จาก noteId
      let userLabelMap = new Map();
      try {
        const labelsResp = await fetch(`/labels/byNoteId/${encodeURIComponent(noteId)}`, 
          { headers: { 'Accept':'application/json' } });
        if (labelsResp.ok) {
          const labelsJson = await labelsResp.json();
          if (Array.isArray(labelsJson)) {
            labelsJson.forEach(label => {
              userLabelMap.set(String(label.labelId), {
                id: String(label.labelId),
                text: label.labelName || '',
                labelName: label.labelName || '',
                color: label.color || '#6c757d'
              });
            });
          }
        }
      } catch(e) {
        console.warn('Failed to fetch user labels for note', noteId, e);
      }
      
      const allCards = [];
      for (const bd of result.boards){
        try {
          const cr = await fetch(`/api/cards/byBoardId/${encodeURIComponent(bd.id)}`, { headers: { 'Accept':'application/json' } });
          if (cr.ok){
            const cj = await cr.json();
            (cj||[]).forEach(c=>{
              // แปลง labelIds เป็น label objects ที่มีสี และเป็นของ user เท่านั้น
              const labels = [];
              if (Array.isArray(c.labelIds)) {
                c.labelIds.forEach(id => {
                  const labelData = userLabelMap.get(String(id));
                  if (labelData) {
                    labels.push(labelData);
                  }
                });
              }
              allCards.push({ id: String(c.cardId||c.id), boardId: String(c.boardId), title: c.cardTitle||c.title||'', description: c.cardContent||'', color: c.cardColor||'#ffffff', labels, dueDate: null, reminder: 0, checklists: [], createdAt: new Date().toISOString() });
            });
          }
        } catch(_){/* ignore per-board errors */}
      }
      result.cards = allCards;
    } catch(e){
      console.warn('Failed to fetch boards/cards for note', noteId, e);
    }
    return result;
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

  async function confirmSwitchNote(){
    if (!_selectedNoteIdForSwitch) { closeSwitchNoteModal(); return; }
    if (_selectedNoteIdForSwitch !== S.currentNoteId){
      S.currentNoteId = _selectedNoteIdForSwitch;
      try {
        const { boards, cards } = await fetchBoardsAndCardsForNote(S.currentNoteId);
        // Merge into state for current note context only
        S.boards = boards.concat(S.boards.filter(b=>String(b.noteId)!==String(S.currentNoteId)));
        // Replace cards for these boards
        const boardIds = new Set(boards.map(b=>String(b.id)));
        S.cards = cards.concat(S.cards.filter(c=>!boardIds.has(String(c.boardId))));
      } catch (e) {
        console.warn('Failed to load boards/cards for note switch', e);
      }
      ST.save();
      updateCurrentNoteTitle();
      NW.boards.renderBoards();
    }
    closeSwitchNoteModal();
  }

  // public: load boards for current note (used on initial page load)
  async function loadBoardsForCurrentNote(){
    if (!S.currentNoteId) return;
    const { boards, cards } = await fetchBoardsAndCardsForNote(S.currentNoteId);
    S.boards = boards.concat(S.boards.filter(b=>String(b.noteId)!==String(S.currentNoteId))); 
    const boardIds = new Set(boards.map(b=>String(b.id)));
    S.cards = cards.concat(S.cards.filter(c=>!boardIds.has(String(c.boardId))));
    // Enrich labels: gather all label ids and fetch details in batch
    try {
      const ids = Array.from(new Set(S.cards.flatMap(c=> (c.labels||[]).map(l=>String(l.id)))));
      if (ids.length>0){
  const resp = await fetch(`/labels/batch?ids=${encodeURIComponent(ids.join(','))}`, { headers:{ 'Accept':'application/json' } });
        if (resp.ok){
          const list = await resp.json();
          const byId = {};
          (list||[]).forEach(l=>{ byId[String(l.labelId)] = { id:String(l.labelId), text:l.labelName, name:l.labelName, color:l.color||'#6c757d' }; });
          S.cards.forEach(c=>{ if (Array.isArray(c.labels)) c.labels = c.labels.map(l=> byId[String(l.id)] || l); });
        }
      }
    } catch(_){ /* enrichment best-effort */ }
    ST.save();
    updateCurrentNoteTitle();
    NW.boards.renderBoards();
  }

  async function deleteNote(){
    if (S.notes.length===0) return;
    const note = S.notes.find(n=>n.id===S.currentNoteId);
    const name = note? note.name : '';
    NW.ui.openConfirm({
      title: 'ลบโน๊ต',
      message: name? `ต้องการลบโน๊ต "${name}" หรือไม่?\nบอร์ดและการ์ดทั้งหมดภายในจะถูกลบด้วย` : 'ต้องการลบโน๊ตนี้หรือไม่? บอร์ดและการ์ดทั้งหมดภายในจะถูกลบด้วย',
      variant: 'danger',
      confirmText: 'ลบโน๊ต',
      onConfirm: async ()=>{
        if (!S.currentNoteId) { if (S.notes.length>0) S.currentNoteId=S.notes[0].id; else return; }
        try {
          const resp = await fetch(`/notes/delete/${encodeURIComponent(S.currentNoteId)}`, { method:'DELETE', headers:{ 'Accept':'application/json' } });
          if (!resp.ok){
            try { const j = await resp.json(); alert(j.error || `ลบโน๊ตไม่สำเร็จ (HTTP ${resp.status})`); } catch(_){ alert(`ลบโน๊ตไม่สำเร็จ (HTTP ${resp.status})`); }
            return;
          }
        } catch(e){
          console.warn('Delete note API failed, fallback to local remove', e);
        }
        const deletedBoardIds = S.boards.filter(b=>String(b.noteId)===String(S.currentNoteId)).map(b=>String(b.id));
        S.boards = S.boards.filter(b=>String(b.noteId)!==String(S.currentNoteId));
        S.cards = S.cards.filter(c=>!deletedBoardIds.includes(String(c.boardId)));
        S.notes = S.notes.filter(n=>String(n.id)!==String(S.currentNoteId));
        S.currentNoteId = S.notes.length>0 ? S.notes[0].id : null;
        ST.save();
        if (S.notes.length===0){ ST.clearAll(); }
        updateCurrentNoteTitle();
        if (S.notes.length>0) NW.boards.renderBoards();
      }
    });
  }

  NW.notes = { updateCurrentNoteTitle, createNote, openNoteCreateModal, closeNoteCreateModal, confirmCreateNote, showNotes, deleteNote, openSwitchNoteModal, closeSwitchNoteModal, selectNoteToSwitch, confirmSwitchNote, loadBoardsForCurrentNote };
  global.createNote = createNote;
  global.showNotes = showNotes;
  global.deleteNote = deleteNote;
  global.closeSwitchNoteModal = closeSwitchNoteModal;
  global.selectNoteToSwitch = selectNoteToSwitch;
  global.confirmSwitchNote = confirmSwitchNote;
  // Inline note title editing
  function startEditCurrentNoteTitle(){
    if (!S.currentNoteId) return;
    const el = document.getElementById('currentNoteTitle');
    const note = S.notes.find(n=>n.id===S.currentNoteId);
    if (!el || !note) return;
    const input = document.createElement('input');
    input.type = 'text';
    input.value = note.name || '';
    input.className = 'note-name-input';
    el.replaceChildren(input);
    input.focus(); input.select();
    const keydown = async (e)=>{
      if (e.key==='Enter') { e.preventDefault(); await saveCurrentNoteTitle(input.value); }
      else if (e.key==='Escape'){ e.preventDefault(); updateCurrentNoteTitle(); }
    };
    input.addEventListener('keydown', keydown);
    input.addEventListener('blur', ()=>{ updateCurrentNoteTitle(); });
  }

  async function saveCurrentNoteTitle(newTitle){
    const t = (newTitle||'').trim();
    if (!t){ updateCurrentNoteTitle(); return; }
    try {
      const resp = await fetch(`/notes/update/${encodeURIComponent(S.currentNoteId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ noteTitle: t })
      });
      if (!resp.ok){
        try { const j = await resp.json(); alert(j.error || 'แก้ไขชื่อโน๊ตไม่สำเร็จ'); } catch(_) { alert('แก้ไขชื่อโน๊ตไม่สำเร็จ'); }
        return;
      }
      // Update local state
      const note = S.notes.find(n=>n.id===S.currentNoteId);
      if (note){ note.name = t; ST.save(); }
    } catch(e){ console.error('Update note title failed', e); alert('เกิดข้อผิดพลาดในการแก้ไขชื่อโน๊ต'); }
    finally {
      updateCurrentNoteTitle();
    }
  }
})(window);
