(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state;

  document.addEventListener('DOMContentLoaded', function(){
    // Guard against cross-user leakage: if username changed, clear cached state
    try {
      const currentUser = global.CURRENT_USERNAME || null;
      const storedUser = localStorage.getItem('username');
      if (currentUser && storedUser && storedUser !== currentUser) {
        if (NW.storage && typeof NW.storage.clearAll === 'function') {
          NW.storage.clearAll();
        } else {
          localStorage.clear();
        }
      }
      if (currentUser) localStorage.setItem('username', currentUser);
      else localStorage.removeItem('username');
    } catch (_) { /* ignore */ }
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
    if (S.notes.length>0) {
      // fetch and render boards (and cards) for the initial/current note
      NW.notes.loadBoardsForCurrentNote();
    }

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

    // label color picker click (in label create modal): edit and remove are note-scoped and propagate across cards
    document.addEventListener('click', function(e){
      const removeBtn = e.target.closest('.remove-color-btn');
      if (removeBtn){
        const opt = removeBtn.closest('.label-color-option');
        if (opt){
          const labelId = opt.getAttribute('data-label-id');
          const noteId = NW.state.currentNoteId || (NW.state && NW.state.currentNoteId) || (NW && NW.state && NW.state.currentNoteId);
          if (labelId && noteId){
            // Delete on server (note scope), then update local state for all cards in this note
            fetch(`/labels/byNoteId/${encodeURIComponent(noteId)}/${encodeURIComponent(labelId)}`, { method:'DELETE' })
              .then(()=>{
                const picker = opt.parentElement;
                const wasSelected = opt.classList.contains('selected');
                picker.removeChild(opt);
                if (wasSelected){
                  const first = picker.querySelector('.label-color-option');
                  if (first) first.classList.add('selected');
                }
                // Remove label from all cards in current note
                const noteBoards = (NW.state.boards||[]).filter(b=>String(b.noteId)===String(NW.state.currentNoteId)).map(b=>String(b.id));
                (NW.state.cards||[]).forEach(c=>{
                  if (noteBoards.includes(String(c.boardId))){ c.labels = (c.labels||[]).filter(l=> String(l.id)!==String(labelId)); }
                });
                try { if (window.NW.storage) window.NW.storage.save(); } catch(_){}
                // If a card modal is open and draft exists, update it too
                try { if (window.NW.boards) window.NW.boards.renderBoards(); if (window.NW.cards) window.NW.cards.renderLabels(); } catch(_){}
              })
              .catch(()=>{ alert('ลบป้ายกำกับไม่สำเร็จ'); });
          }
        }
        return;
      }
      const editBtn = e.target.closest('.edit-label-btn');
      if (editBtn){
        const opt = editBtn.closest('.label-color-option');
        if (opt){
          const labelId = opt.getAttribute('data-label-id');
          const currentName = opt.getAttribute('data-label-name') || '';
          const noteId = NW.state.currentNoteId;
          const newName = prompt('แก้ไขชื่อป้ายกำกับ', currentName);
          if (newName && newName.trim() && labelId && noteId){
            fetch(`/labels/byNoteId/${encodeURIComponent(noteId)}/${encodeURIComponent(labelId)}`, {
              method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' }, body: JSON.stringify({ labelName: newName.trim() })
            }).then(r=>{
              if (!r.ok) throw new Error('update-failed');
              // Update UI in picker
              opt.setAttribute('data-label-name', newName.trim());
              const nameSpan = opt.querySelector('.label-name'); if (nameSpan) nameSpan.textContent = newName.trim();
              // Update across all cards in this note
              const noteBoards = (NW.state.boards||[]).filter(b=>String(b.noteId)===String(NW.state.currentNoteId)).map(b=>String(b.id));
              (NW.state.cards||[]).forEach(c=>{
                if (noteBoards.includes(String(c.boardId))){
                  c.labels = (c.labels||[]).map(l=> String(l.id)===String(labelId) ? { ...l, text: newName.trim(), name: newName.trim(), labelName: newName.trim() } : l);
                }
              });
              try { if (window.NW.storage) window.NW.storage.save(); if (window.NW.boards) window.NW.boards.renderBoards(); if (window.NW.cards) window.NW.cards.renderLabels(); } catch(_){}
            }).catch(()=> alert('แก้ไขป้ายกำกับไม่สำเร็จ'));
          }
        }
        return;
      }
      const opt = e.target.closest('.label-color-option');
      if (opt){
        document.querySelectorAll('.label-color-option').forEach(o=>o.classList.remove('selected'));
        opt.classList.add('selected');
        // If this color corresponds to an existing label, populate input with its name for inline editing
        const input = document.getElementById('labelTextInput');
        const err = document.getElementById('labelTextError');
        if (input){
          const name = opt.getAttribute('data-label-name');
          if (name && name.trim()){
            input.value = name.trim();
            if (err) err.style.display='none';
          }
        }
      }
    });

    // add new color into the list via native color input
    const addColorBtn = document.getElementById('addLabelColorBtn');
    const palette = document.getElementById('labelColorPalette');
    if (addColorBtn && palette){
      // Define 30 preset colors
      const PRESET_COLORS = [
        '#F44336','#E91E63','#9C27B0','#673AB7','#3F51B5',
        '#2196F3','#03A9F4','#00BCD4','#009688','#4CAF50',
        '#8BC34A','#CDDC39','#FFEB3B','#FFC107','#FF9800',
        '#FF5722','#795548','#9E9E9E','#607D8B','#000000',
        '#1B4332','#2D6A4F','#40916C','#52B788','#74C69D',
        '#95D5B2','#B7E4C7','#D8F3DC','#6C757D','#2C3E50'
      ];
      // Build palette items once
      if (!palette.dataset.built){
        PRESET_COLORS.forEach(color=>{
          const dot = document.createElement('div');
          dot.className = 'palette-item';
          dot.style.background = color;
          dot.setAttribute('data-color', color);
          palette.appendChild(dot);
        });
        palette.dataset.built = '1';
      }
      // Toggle palette visibility
      addColorBtn.addEventListener('click', function(){
        palette.style.display = (palette.style.display==='none' || palette.style.display==='') ? 'grid' : 'none';
      });
      // When user picks a palette color, add to list as a new selectable row
      palette.addEventListener('click', function(e){
        const dot = e.target.closest('.palette-item');
        if (!dot) return;
        const val = dot.getAttribute('data-color');
        const picker = document.querySelector('#labelCreateModal .label-color-picker');
        if (!picker) return;
        // avoid duplicates: if color exists, just select it
        const exists = picker.querySelector(`.label-color-option[data-color="${val}"]`);
        if (exists){
          document.querySelectorAll('#labelCreateModal .label-color-option').forEach(o=>o.classList.remove('selected'));
          exists.classList.add('selected');
        } else {
          const el = document.createElement('div');
          el.className = 'label-color-option selected';
          el.style.background = val;
          el.setAttribute('data-color', val);
          const btn = document.createElement('button');
          btn.className = 'remove-color-btn';
          btn.type = 'button';
          btn.textContent = '×';
          el.appendChild(btn);
          document.querySelectorAll('#labelCreateModal .label-color-option').forEach(o=>o.classList.remove('selected'));
          picker.appendChild(el);
        }
        // hide palette after picking
        palette.style.display = 'none';
      });
    }

    // When opening modal, prevent body scroll; when closing, restore
    const observeModal = (id)=>{
      const modal = document.getElementById(id);
      if (!modal) return;
      const obs = new MutationObserver(()=>{
        if (modal.classList.contains('open')) document.body.classList.add('no-scroll');
        else document.body.classList.remove('no-scroll');
      });
      obs.observe(modal, { attributes: true, attributeFilter: ['class'] });
    };
    ['labelCreateModal','cardCreateModal','cardModal','noteCreateModal','checklistCreateModal','confirmModal'].forEach(observeModal);

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
