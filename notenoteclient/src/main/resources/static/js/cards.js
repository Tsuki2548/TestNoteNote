(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state, U = NW.utils;

  let _creatingBoardId = null;
  let _createCardKeyHandler = null;
  // Draft state for editing a card within modal
  let _cardDraft = null;
  let _cardOriginal = null; // snapshot for diffing on save

  function addNewCard(boardId){
    openCardCreateModal(boardId);
  }

  function openCardCreateModal(boardId){
    _creatingBoardId = boardId;
    const modal = document.getElementById('cardCreateModal');
    const input = document.getElementById('cardCreateInput');
    const err = document.getElementById('cardCreateError');
    if (!modal || !input) return;
    if (err) err.style.display='none';
    input.value='';
    modal.classList.add('open');
    _createCardKeyHandler = (e)=>{ if (e.key==='Enter'){ e.preventDefault(); confirmCreateCard(); } if (e.key==='Escape'){ e.preventDefault(); closeCardCreateModal(); } };
    document.addEventListener('keydown', _createCardKeyHandler);
    setTimeout(()=>input.focus(),0);
  }

  function closeCardCreateModal(){
    const modal = document.getElementById('cardCreateModal');
    if (modal) modal.classList.remove('open');
    if (_createCardKeyHandler){ document.removeEventListener('keydown', _createCardKeyHandler); _createCardKeyHandler=null; }
    _creatingBoardId = null;
  }

  async function confirmCreateCard(){
    const input = document.getElementById('cardCreateInput');
    const err = document.getElementById('cardCreateError');
    const title = (input?.value||'').trim();
    if (!title){ if (err) err.style.display='block'; input?.focus(); return; }
    const boardId = _creatingBoardId;
    if (!boardId){ closeCardCreateModal(); return; }
    try {
      const resp = await fetch('/api/cards', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ cardTitle: title, cardContent: '', cardColor: '#ffffff', boardId: Number(boardId) })
      });
      if (!resp.ok){
        try { const j = await resp.json(); alert(j.error||'สร้างการ์ดไม่สำเร็จ'); } catch(_){ alert('สร้างการ์ดไม่สำเร็จ'); }
        return;
      }
      const data = await resp.json();
      S.cards.push({ id: String(data.cardId), boardId: String(data.boardId), title: data.cardTitle, description: data.cardContent||'', color: data.cardColor||'#ffffff', labels: [], dueDate: null, reminder: 0, checklists: [], createdAt: new Date().toISOString() });
      
      NW.boards.renderBoards();
      closeCardCreateModal();
    } catch(e){
      console.error('Create card failed', e);
      alert('เกิดข้อผิดพลาดในการสร้างการ์ด');
    }
  }

  function openCardModal(cardId){
    S.currentCardId = cardId;
    const card = S.cards.find(c=>c.id===cardId);
    if (!card) return;
    // create deep clone as draft
    _cardDraft = JSON.parse(JSON.stringify(card));
    _cardOriginal = JSON.parse(JSON.stringify(card));
    const modal = document.getElementById('cardModal');
    document.getElementById('cardTitle').value = _cardDraft.title || '';
    document.getElementById('cardDescription').value = _cardDraft.description || '';
  document.getElementById('cardDueDate').value = _cardDraft.dueDate || '';
  renderLabels();
    renderChecklists();
    // Load checklists from backend to keep in sync
    loadChecklistsForCard(cardId);
    document.querySelectorAll('.color-option').forEach(option=>{
      option.classList.remove('selected');
      if (option.getAttribute('data-color') === (_cardDraft.color||'#ffffff')) option.classList.add('selected');
    });
    modal.classList.add('open');
  }

  function closeCardModal(){
    document.getElementById('cardModal').classList.remove('open');
    // discard draft on cancel/close
  _cardDraft = null;
  _cardOriginal = null;
    S.currentCardId = null;
  }

  async function saveCard(){
    if (!S.currentCardId || !_cardDraft) return;
    const card = S.cards.find(c=>c.id===S.currentCardId);
    if (!card) return;
    // read latest from inputs into draft
    _cardDraft.title = document.getElementById('cardTitle').value;
    _cardDraft.description = document.getElementById('cardDescription').value;
    _cardDraft.dueDate = document.getElementById('cardDueDate').value;
  // Reminder feature removed; force no reminder
  _cardDraft.reminder = 0;
    const selectedColor = document.querySelector('.color-option.selected');
    if (selectedColor){ _cardDraft.color = selectedColor.getAttribute('data-color'); }
  // persist via proxy PUT before committing
    try {
      const payload = { cardTitle: _cardDraft.title, cardContent: _cardDraft.description, cardColor: _cardDraft.color, boardId: Number(card.boardId) };
      const resp = await fetch(`/api/cards/${encodeURIComponent(S.currentCardId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!resp.ok){ try{ const j=await resp.json(); alert(j.error||'บันทึกการ์ดไม่สำเร็จ'); }catch(_){ alert('บันทึกการ์ดไม่สำเร็จ'); } return; }

      // Flush label and checklist changes (create/assign/remove/rename) after card update succeeds
      await flushLabelChanges(S.currentCardId);
      await flushChecklistChanges(S.currentCardId);
    } catch(e){ console.error('Update card failed', e); alert('เกิดข้อผิดพลาดในการบันทึกการ์ด'); return; }
    // commit draft to local state and UI after success
    card.title = _cardDraft.title;
    card.description = _cardDraft.description;
    card.dueDate = _cardDraft.dueDate;
    card.reminder = _cardDraft.reminder;
    card.color = _cardDraft.color;
    card.labels = Array.isArray(_cardDraft.labels) ? _cardDraft.labels : [];
    card.checklists = Array.isArray(_cardDraft.checklists) ? _cardDraft.checklists : [];
    
    NW.boards.renderBoards();
  // Reminder feature removed; no scheduling needed
    _cardDraft = null;
    closeCardModal();
  }

  // Persist label changes accumulated in draft vs original
  async function flushLabelChanges(cardId){
    const orig = (_cardOriginal && Array.isArray(_cardOriginal.labels)) ? _cardOriginal.labels : [];
    const draft = (_cardDraft && Array.isArray(_cardDraft.labels)) ? _cardDraft.labels : [];
    const origById = new Map(orig.map(l=>[String(l.id), l]));
    const draftById = new Map(draft.map(l=>[String(l.id), l]));
    const noteId = S.currentNoteId;
    // Track labels that were renamed successfully to propagate across all cards
    const renamed = new Map(); // id -> newName

    // 1) Rename existing labels (note scoped) where name changed
    for (const [id, dl] of draftById.entries()){
      if (id.startsWith('tmp_')) continue;
      const ol = origById.get(id);
      const newName = (dl.text||dl.name||'').trim();
      const changedFromOrig = ol && (String(ol.text||ol.name||'').trim() !== newName);
      const changedFromCaptured = dl.origName && String(dl.origName).trim() !== newName;
      if (changedFromOrig || changedFromCaptured){
        const resp = await fetch(`/labels/byNoteId/${encodeURIComponent(noteId)}/${encodeURIComponent(id)}`, {
          method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' },
          body: JSON.stringify({ labelName: newName })
        });
        if (resp && resp.ok) renamed.set(String(id), newName);
      }
    }

    // 2) Create+assign for new temp labels
    for (const dl of draft){
      const id = String(dl.id||'');
      if (id.startsWith('tmp_')){
        await fetch(`/labels/create-assign/${encodeURIComponent(cardId)}`, {
          method:'POST', headers:{ 'Content-Type':'application/json','Accept':'application/json' },
          body: JSON.stringify({ labelName: (dl.text||dl.name||'').trim(), color: dl.color||'#6c757d' })
        });
      }
    }

    // 3) Assign labels that exist but were not previously on the card
    for (const [id, dl] of draftById.entries()){
      if (id.startsWith('tmp_')) continue; // already created+assigned above
      if (!origById.has(id)){
        await fetch(`/labels/assign/${encodeURIComponent(cardId)}/${encodeURIComponent(id)}`, { method:'POST', headers:{ 'Accept':'application/json' } });
      }
    }

    // 4) Remove labels that were on the card but not in draft
    for (const [id, ol] of origById.entries()){
      if (!draftById.has(id)){
        await fetch(`/labels/remove/${encodeURIComponent(cardId)}/${encodeURIComponent(id)}`, { method:'DELETE', headers:{ 'Accept':'application/json' } });
      }
    }

    // Sync from backend to ensure final label state
    try {
      const lr = await fetch(`/labels/byCardId/${encodeURIComponent(cardId)}`, { headers:{ 'Accept':'application/json' }});
      if (lr.ok){
        const labels = await lr.json();
        const mapped = (labels||[]).map(l=>({ id:String(l.labelId), text: l.labelName, name: l.labelName, color: l.color||'#6c757d' }));
        _cardDraft.labels = mapped;
      }
    } catch(_){ }

    // Propagate any successful renames to all cards in local state and refresh UI immediately
    if (renamed.size > 0){
      const updates = Object.fromEntries(renamed.entries());
      (S.cards||[]).forEach(c=>{
        if (Array.isArray(c.labels)){
          c.labels = c.labels.map(l=>{
            const newName = updates[String(l.id)];
            return newName ? { ...l, text: newName, name: newName, labelName: newName } : l;
          });
        }
      });
      
      if (NW && NW.boards) NW.boards.renderBoards();
      // Also refresh labels in modal if open
      try { renderLabels(); } catch(_){ }
    }
  }

  // Compute and persist checklist changes since modal opened
  async function flushChecklistChanges(cardId){
    const orig = (_cardOriginal && Array.isArray(_cardOriginal.checklists)) ? _cardOriginal.checklists : [];
    const draft = (_cardDraft && Array.isArray(_cardDraft.checklists)) ? _cardDraft.checklists : [];
    const byIdOrig = new Map(orig.map(ch=>[String(ch.id), ch]));
    const tasks = [];
    // create or rename
    for (const ch of draft){
      const id = String(ch.id);
      if (id.startsWith('tmp_')){
        // New local-only checklist -> create once
        tasks.push(fetch('/checklists/create', {
          method:'POST', headers:{ 'Content-Type':'application/json','Accept':'application/json' },
          body: JSON.stringify({ checklistTitle: ch.title, cardId: Number(cardId) })
        }).then(r=>{ if (!r.ok) throw new Error('create-failed'); }));
      } else if (byIdOrig.has(id)) {
        const origCh = byIdOrig.get(id);
        if ((origCh.title||'') !== (ch.title||'')){
          tasks.push(fetch(`/checklists/update/${encodeURIComponent(id)}`, {
            method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' },
            body: JSON.stringify({ checklistTitle: ch.title, cardId: Number(cardId) })
          }).then(r=>{ if (!r.ok) throw new Error('update-failed'); }));
        }
      } else {
        // Real ID but not in original snapshot: treat as already existing from server; do not create to avoid duplicates
      }
    }
    // deletions: present in original but not in draft
    const draftIds = new Set(draft.map(ch=>String(ch.id)));
    for (const [id, och] of byIdOrig.entries()){
      if (!draftIds.has(id)){
        tasks.push(fetch(`/checklists/delete/${encodeURIComponent(id)}`, { method:'DELETE', headers:{ 'Accept':'application/json' } }).then(r=>{ if (!r.ok) throw new Error('delete-failed'); }));
      }
    }
    if (tasks.length>0){
      await Promise.all(tasks);
    }
    // Sync final checklists from backend to capture real IDs and their items
    try{
      const r = await fetch(`/checklists/byCardId/${encodeURIComponent(cardId)}`, { headers:{ 'Accept':'application/json' } });
      if (r.ok){
        const list = await r.json();
        const mapped = (list||[]).map(x=>({ id: String(x.checklistId), title: x.checklistTitle, items: [] }));
        for (const ch of mapped){
          try{
            const rr = await fetch(`/api/checkboxes/byChecklistId/${encodeURIComponent(ch.id)}`, { headers:{ 'Accept':'application/json' } });
            if (rr.ok){
              const items = await rr.json();
              ch.items = (items||[]).map(i=>({ id: String(i.checkboxId), text: i.checkboxTitle, completed: !!i.completed }));
            }
          } catch(_){ }
        }
        // Replace draft and also update original snapshot so subsequent saves diff correctly
        _cardDraft.checklists = mapped;
        if (_cardOriginal){ _cardOriginal.checklists = JSON.parse(JSON.stringify(mapped)); }
      }
    }catch(_){ }
  }

  function deleteCard(){
    if (!S.currentCardId) return;
    const card = S.cards.find(c=>c.id===S.currentCardId);
    const name = card? card.title : '';
    NW.ui.openConfirm({
      title: 'ลบการ์ด',
      message: name? `ต้องการลบการ์ด "${name}" หรือไม่?` : 'ต้องการลบการ์ดนี้หรือไม่?',
      variant: 'danger',
      confirmText: 'ลบการ์ด',
      onConfirm: ()=>{
        S.cards = S.cards.filter(c=>c.id!==S.currentCardId);
        
        NW.boards.renderBoards();
        closeCardModal();
      }
    });
  }

  function renderLabels(){
    if (!_cardDraft) return;
    const labels = (_cardDraft.labels || []).slice().sort((a,b)=> (a.color||'').localeCompare(b.color||''));
    const container = document.getElementById('labelContainer');
    container.innerHTML = labels.map(label=>{
      const bg = label.color || '#6c757d';
      const text = (label.text || label.name || '').trim();
      const id = String(label.id);
      return `<span class="label-tag" style="background:${bg}">${U.escapeHtml(text)}<button onclick="removeLabel('${id}')">×</button></span>`;
    }).join('');
  }

  // Label create modal flow
  let _labelKeyHandler = null;
  async function openLabelCreateModal(){
    const modal = document.getElementById('labelCreateModal');
    const input = document.getElementById('labelTextInput');
    const err = document.getElementById('labelTextError');
    if (!modal || !input) return;
    if (err) err.style.display='none';
    input.value='';
    // Build color options from user's existing labels (by current note)
    try {
      const noteId = NW.state.currentNoteId || S.currentNoteId;
      const picker = document.querySelector('#labelCreateModal .label-color-picker');
      if (picker && noteId){
        const resp = await fetch(`/labels/byNoteId/${encodeURIComponent(noteId)}`, { headers: { 'Accept':'application/json' } });
        if (resp.ok){
          const labels = await resp.json();
          // Deduplicate by color, keep first occurrence
          const byColor = new Map();
          (labels||[]).forEach(l=>{
            const color = String(l.color||'').trim().toUpperCase();
            if (!/^#([0-9A-F]{3}){1,2}$/.test(color)) return;
            if (!byColor.has(color)) byColor.set(color, { id: String(l.labelId), name: l.labelName||'', color });
          });
          const arr = Array.from(byColor.values());
          picker.innerHTML = arr.map((it, idx)=>
            `<div class="label-color-option${idx===0?' selected':''}" data-color="${it.color}" data-label-id="${it.id}" data-label-name="${it.name}" style="background:${it.color};">
               <span class="label-name">${U.escapeHtml(it.name)}</span>
               <button type="button" class="remove-color-btn">×</button>
             </div>`
          ).join('');
        }
      }
    } catch(_) { /* ignore and keep existing DOM */ }
    modal.classList.add('open');
    _labelKeyHandler = (e)=>{ if (e.key==='Enter'){ e.preventDefault(); confirmCreateLabel(); } if (e.key==='Escape'){ e.preventDefault(); closeLabelCreateModal(); } };
    document.addEventListener('keydown', _labelKeyHandler);
    setTimeout(()=>input.focus(),0);
  }
  function closeLabelCreateModal(){
    const modal = document.getElementById('labelCreateModal');
    if (modal) modal.classList.remove('open');
    if (_labelKeyHandler){ document.removeEventListener('keydown', _labelKeyHandler); _labelKeyHandler=null; }
  }
  function confirmCreateLabel(){
    const input = document.getElementById('labelTextInput');
    const err = document.getElementById('labelTextError');
    const text = (input?.value||'').trim();
    if (!text){ if (err) err.style.display='block'; input?.focus(); return; }
    if (!_cardDraft) return;
    const colorEl = document.querySelector('#labelCreateModal .label-color-option.selected');
    const color = colorEl ? colorEl.getAttribute('data-color') : '#6c757d';
    const existingLabelId = colorEl ? colorEl.getAttribute('data-label-id') : null;
    const existingLabelName = colorEl ? colorEl.getAttribute('data-label-name') : null;
    // Local-only: update draft labels; persistence will happen on Save Card
    if (!_cardDraft.labels) _cardDraft.labels = [];
    if (existingLabelId){
      // Upsert existing label into draft with possibly edited name
      const idStr = String(existingLabelId);
      const exists = _cardDraft.labels.some(l=> String(l.id)===idStr);
      const entry = { id: idStr, text, name: text, labelName: text, color, origName: existingLabelName };
      if (exists){
        _cardDraft.labels = _cardDraft.labels.map(l=> String(l.id)===idStr ? { ...l, ...entry } : l);
      } else {
        _cardDraft.labels.push(entry);
      }
    } else {
      // New color -> create temp label
      const tempId = 'tmp_' + (U.generateId ? U.generateId() : Math.random().toString(36).slice(2));
      _cardDraft.labels.push({ id: tempId, text, name: text, labelName: text, color });
    }
    renderLabels();
    closeLabelCreateModal();
  }

  function removeLabel(labelId){
    if (!_cardDraft) return;
    const idStr = String(labelId);
    _cardDraft.labels = (_cardDraft.labels||[]).filter(l=> String(l.id)!==idStr);
    renderLabels();
  }

  // Update current card draft and state after external note-scoped delete
  function removeLabelLocally(labelId){
    if (!_cardDraft) return;
    const idStr = String(labelId);
    _cardDraft.labels = (_cardDraft.labels||[]).filter(l=> String(l.id)!==idStr);
    const card = S.cards.find(c=>c.id===S.currentCardId);
  if (card){ card.labels = (card.labels||[]).filter(l=> String(l.id)!==idStr); }
    // Reflect immediately in UI
    NW.boards.renderBoards();
    renderLabels();
  }

  // Remove labels from draft (and current card state) by color value
  function removeLabelByColor(color){
    if (!_cardDraft) return;
    const col = String(color||'').toUpperCase();
    if (!col) return;
    _cardDraft.labels = (_cardDraft.labels||[]).filter(l=> String(l.color||'').toUpperCase() !== col);
    // Reflect only in modal UI; do not touch persisted state until Save
    renderLabels();
  }

  // Update current card draft and state after external note-scoped rename
  function renameLabelLocally(labelId, newName){
    if (!_cardDraft) return;
    const idStr = String(labelId);
    const name = (newName||'').trim();
    _cardDraft.labels = (_cardDraft.labels||[]).map(l=> String(l.id)===idStr ? { ...l, text: name, name, labelName: name } : l);
    const card = S.cards.find(c=>c.id===S.currentCardId);
  if (card){ card.labels = (card.labels||[]).map(l=> String(l.id)===idStr ? { ...l, text: name, name, labelName: name } : l); }
    // Reflect immediately in UI
    NW.boards.renderBoards();
    renderLabels();
  }

  function renderChecklists(){
    if (!_cardDraft) return;
    const checklists = _cardDraft.checklists || [];
    const container = document.getElementById('checklistContainer');
    container.innerHTML = checklists.map(ch=>{
      const progress = getChecklistItemProgress(ch);
      return `
        <div class="checklist" data-checklist-id="${ch.id}">
          <div class="checklist-header">
            <span class="checklist-title editable" title="คลิกเพื่อแก้ชื่อ">${U.escapeHtml(ch.title)}</span>
            <div class="checklist-header-right">
              <span class="checklist-progress">${progress.completed}/${progress.total}</span>
              <button class="checklist-delete-btn" title="ลบเช็คลิสต์" onclick="deleteChecklist('${ch.id}')">×</button>
            </div>
          </div>
          <div class="checklist-items">
            ${ch.items.map(it=>`
              <div class="checklist-item ${it.completed?'completed':''}">
                <input type="checkbox" ${it.completed?'checked':''} onchange="toggleCheckItem('${ch.id}','${it.id}')">
                <label>${U.escapeHtml(it.text)}</label>
              </div>
            `).join('')}
          </div>
          <div class="checklist-inline-add">
            <input type="text" class="checklist-inline-input" placeholder="เพิ่มรายการ..." onkeydown="checklistInlineKey(event, '${ch.id}')" />
          </div>
        </div>
      `;
    }).join('');
  }

  function getChecklistItemProgress(checklist){
    const total = (checklist.items||[]).length;
    const completed = (checklist.items||[]).filter(i=>i.completed).length;
    return { total, completed };
  }

  function addChecklist(){
    const title = prompt('ชื่อเช็คลิสต์:');
    if (!title || !title.trim()) return;
    const card = S.cards.find(c=>c.id===S.currentCardId); if (!card) return;
    if (!card.checklists) card.checklists = [];
    card.checklists.push({ id: U.generateId(), title: title.trim(), items: [] });
    renderChecklists(card.checklists);
    
  }
    // Checklist create modal flow
      let _checklistKeyHandler = null;
      let _creatingChecklist = false; // reentrancy guard to prevent double submit
    function openChecklistCreateModal(){
      const modal = document.getElementById('checklistCreateModal');
      const input = document.getElementById('checklistTitleInput');
      const err = document.getElementById('checklistCreateError');
      if (!modal || !input) return;
      if (err) err.style.display='none';
      input.value='';
      modal.classList.add('open');
      _checklistKeyHandler = (e)=>{ if (e.key==='Enter'){ e.preventDefault(); confirmCreateChecklist(); } if (e.key==='Escape'){ e.preventDefault(); closeChecklistCreateModal(); } };
      document.addEventListener('keydown', _checklistKeyHandler);
      setTimeout(()=>input.focus(),0);
    }
    function closeChecklistCreateModal(){
      const modal = document.getElementById('checklistCreateModal');
      if (modal) modal.classList.remove('open');
      if (_checklistKeyHandler){ document.removeEventListener('keydown', _checklistKeyHandler); _checklistKeyHandler=null; }
    }
    function confirmCreateChecklist(){
      if (_creatingChecklist) return; // prevent double submission
      const input = document.getElementById('checklistTitleInput');
      const err = document.getElementById('checklistCreateError');
      const title = (input?.value||'').trim();
      if (!title){ if (err) err.style.display='block'; input?.focus(); return; }
      if (!_cardDraft) return;
      _creatingChecklist = true;
      // disable confirm button if present to avoid double click
      try{ const btn = document.querySelector('#checklistCreateModal .confirm-btn'); if (btn){ btn.disabled = true; btn.classList.add('disabled'); } }catch(_){ }
      // Local-only create (defer persistence until Save Card)
      if (!_cardDraft.checklists) _cardDraft.checklists = [];
      const localId = 'tmp_' + (U.generateId ? U.generateId() : Math.random().toString(36).slice(2));
      _cardDraft.checklists.push({ id: localId, title, items: [] });
      renderChecklists();
      closeChecklistCreateModal();
      _creatingChecklist = false;
      try{ const btn = document.querySelector('#checklistCreateModal .confirm-btn'); if (btn){ btn.disabled = false; btn.classList.remove('disabled'); } }catch(_){ }
    }

  function addCheckItem(checklistId){
    const text = prompt('รายการ:');
    if (!text || !text.trim()) return;
    const card = S.cards.find(c=>c.id===S.currentCardId); if (!card) return;
    const cl = card.checklists.find(x=>x.id===checklistId); if (!cl) return;
    cl.items.push({ id: U.generateId(), text: text.trim(), completed: false });
    renderChecklists(card.checklists);
    
  }
    async function addCheckItemInline(checklistId, text){
      const t = (text||'').trim();
      if (!t) return;
      if (!_cardDraft) return;
      const cl = (_cardDraft.checklists||[]).find(x=>x.id===checklistId); if (!cl) return;
      // If checklist not yet persisted, add locally only
      if (String(checklistId).startsWith('tmp_')){
        cl.items.push({ id: U.generateId(), text: t, completed: false });
        renderChecklists();
        return;
      }
      try {
        const resp = await fetch('/api/checkboxes', {
          method: 'POST', headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
          body: JSON.stringify({ checkboxTitle: t, checklistId: Number(checklistId) })
        });
        if (!resp.ok) throw new Error('create-failed');
        const dto = await resp.json();
        const item = { id: String(dto.checkboxId), text: dto.checkboxTitle, completed: !!dto.completed };
        // Update draft checklist items (modal view)
        cl.items = (cl.items||[]);
        if (!cl.items.some(i=>String(i.id)===item.id)) cl.items.push(item);
        // Optionally mirror to S.cards only if it's a different object reference to avoid double-append
        try {
          const card = S.cards.find(c=>c.id===S.currentCardId);
          const cc = card?.checklists?.find(x=>String(x.id)===String(checklistId));
          if (cc && cc !== cl){
            cc.items = (cc.items||[]);
            if (!cc.items.some(i=>String(i.id)===item.id)) cc.items.push(item);
          }
        } catch(_){ }
        renderChecklists();
        NW.boards.renderBoards();
      } catch (e) {
        console.warn('Create checkbox failed', e);
        alert('เพิ่มรายการเช็คลิสต์ไม่สำเร็จ');
      }
    }
    function checklistInlineKey(e, checklistId){
      if (e.key==='Enter'){
        e.preventDefault();
        addCheckItemInline(checklistId, e.target.value);
        // keep input for next add
        e.target.value = '';
        // focus back to allow fast entry
        setTimeout(()=>e.target.focus(),0);
      }
    }

  async function toggleCheckItem(checklistId,itemId){
    if (!_cardDraft) return;
    const cl = (_cardDraft.checklists||[]).find(x=>x.id===checklistId); if (!cl) return;
    const it = (cl.items||[]).find(i=>i.id===itemId); if (!it) return;
    // optimistic toggle
    it.completed = !it.completed;
    renderChecklists();
    if (!String(itemId).startsWith('tmp_')){
      try{
        const resp = await fetch(`/api/checkboxes/${encodeURIComponent(itemId)}`, {
          method: 'PUT', headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
          body: JSON.stringify({ completed: it.completed })
        });
        if (!resp.ok) throw new Error('toggle-failed');
      } catch(e){
        // revert
        it.completed = !it.completed;
        renderChecklists();
        alert('อัปเดตสถานะเช็คลิสต์ไม่สำเร็จ');
      }
    }
    if (NW && NW.boards) NW.boards.renderBoards();
  }

  function deleteChecklist(checklistId){
    if (!_cardDraft) return;
    const idStr = String(checklistId);
    _cardDraft.checklists = (_cardDraft.checklists||[]).filter(ch=> String(ch.id)!==idStr);
    renderChecklists();
  }

  // Fetch and sync checklists for a card from backend
  async function loadChecklistsForCard(cardId){
    try{
  const r = await fetch(`/checklists/byCardId/${encodeURIComponent(cardId)}`, { headers:{ 'Accept':'application/json' } });
      if (!r.ok) return;
      const list = await r.json();
      const mapped = (list||[]).map(x=>({ id: String(x.checklistId), title: x.checklistTitle, items: [] }));
      // Load checkbox items for each checklist
      for (const ch of mapped){
        try {
          const rr = await fetch(`/api/checkboxes/byChecklistId/${encodeURIComponent(ch.id)}`, { headers:{ 'Accept':'application/json' } });
          if (rr.ok){
            const items = await rr.json();
            ch.items = (items||[]).map(i=>({ id: String(i.checkboxId), text: i.checkboxTitle, completed: !!i.completed }));
          }
        } catch(_){ }
      }
      if (!_cardDraft) return;
      // Update both draft and original so diff-on-save doesn't re-create existing checklists
      _cardDraft.checklists = mapped;
      if (_cardOriginal){ _cardOriginal.checklists = JSON.parse(JSON.stringify(mapped)); }
      const card = S.cards.find(c=>c.id===cardId);
  if (card){ card.checklists = mapped; }
      NW.boards.renderBoards();
      renderChecklists();
    } catch(e){ /* ignore: best-effort sync */ }
  }

  function updateMoveToBoardSelect(){
    const select = document.getElementById('moveToBoardSelect');
    if (!select) return;
    const currentBoards = S.boards.filter(b=>b.noteId===S.currentNoteId);
    select.innerHTML = '<option value="">เลือกบอร์ด...</option>' + currentBoards.map(b=>`<option value="${b.id}">${U.escapeHtml(b.name)}</option>`).join('');
  }

    // remove move card feature (drag-and-drop available)

  // Drag & Drop for cards and boards (shared by boards.js)
  function setupCardDragEvents(cardElement){
    cardElement.setAttribute('draggable','true');
    let dragStarted=false, startX, startY;
    cardElement.addEventListener('dragstart', handleCardDragStart);
    cardElement.addEventListener('dragend', handleCardDragEnd);
    cardElement.addEventListener('dragover', handleCardItemDragOver);
    cardElement.addEventListener('mousedown', e=>{ dragStarted=false; startX=e.clientX; startY=e.clientY; });
    cardElement.addEventListener('mousemove', e=>{
      if (startX && startY){
        const dx=Math.abs(e.clientX-startX), dy=Math.abs(e.clientY-startY);
        if (dx>5 || dy>5) dragStarted=true;
      }
    });
    cardElement.addEventListener('click', e=>{ if (!dragStarted){ const id=cardElement.getAttribute('data-card-id'); openCardModal(id);} });
    cardElement.addEventListener('mouseup', ()=>{ startX=null; startY=null;});
  }

  function createCardItemHTML(card){
    const labels = Array.isArray(card.labels) && card.labels.length>0
      ? card.labels.map(l=>{
          const bg = l.color || '#6c757d';
          // รองรับทั้ง text, name, และ labelName
          const txt = (l.text || l.name || l.labelName || '').trim();
          return `<span class="card-label" style="background:${bg}">${U.escapeHtml(txt)}</span>`;
        }).join('')
      : '';
    const dueDate = card.dueDate ? new Date(card.dueDate).toLocaleDateString('th-TH',{day:'numeric',month:'short',year:'numeric'}) : '';
    const isDue = card.dueDate && new Date(card.dueDate) < new Date();
    const checklistProgress = (function(){
      const t = (card.checklists||[]).reduce((acc,cl)=>{ acc.total += (cl.items||[]).length; acc.completed += (cl.items||[]).filter(i=>i.completed).length; return acc; }, {total:0,completed:0});
      return t.total>0? `${t.completed}/${t.total}` : '';
    })();
    const dueAttr = isDue ? 'style="color:#f44336;font-weight:700;"' : '';
    return `
      <div class="card-item" data-card-id="${card.id}" draggable="true" style="background:${card.color||'white'}">
        <div class="card-item-content">
          <div class="card-item-title">${U.escapeHtml(card.title)}</div>
          ${labels ? `<div class="card-item-labels">${labels}</div>` : ''}
          <div class="card-item-footer">
            ${dueDate ? `<span ${dueAttr}>📅 ${dueDate}</span>` : ''}
            ${checklistProgress ? `<span>✓ ${checklistProgress}</span>` : ''}
          </div>
        </div>
      </div>`;
  }

  function handleCardDragStart(e){
    S.draggedCardElement = this;
    const cardId = this.getAttribute('data-card-id');
    S.draggedCard = S.cards.find(c=>c.id===cardId);
    this.classList.add('dragging');
    e.dataTransfer.effectAllowed='move';
    e.dataTransfer.setData('text/plain', cardId);
    setTimeout(()=>{ document.querySelectorAll('.cards-container').forEach(c=>c.classList.add('drag-active')); },0);
  }
  function handleCardDragEnd(){
    this.classList.remove('dragging');
    document.querySelectorAll('.cards-container').forEach(c=>c.classList.remove('drag-active','drag-over-board'));
    document.querySelectorAll('.card-item').forEach(ci=>ci.classList.remove('drag-over'));
    S.draggedCard=null; S.draggedCardElement=null;
  }
  function handleCardItemDragOver(e){
    e.preventDefault();
    if (this===S.draggedCardElement) return;
    this.classList.add('drag-over');
    [...this.parentElement.children].forEach(s=>{ if (s!==this && s.classList.contains('card-item')) s.classList.remove('drag-over'); });
    e.dataTransfer.dropEffect='move';
  }
  function handleCardDragEnter(e){ if (S.draggedCard && this.classList.contains('cards-container')) this.classList.add('drag-over-board'); }
  function handleCardDragLeave(e){
    const rect = this.getBoundingClientRect();
    if (e.clientX<=rect.left || e.clientX>=rect.right || e.clientY<=rect.top || e.clientY>=rect.bottom){ this.classList.remove('drag-over-board'); }
  }
  function getDragAfterElement(container, y){
    const els=[...container.querySelectorAll('.card-item:not(.dragging)')];
    return els.reduce((closest,child)=>{
      const box=child.getBoundingClientRect();
      const offset=y - box.top - box.height/2;
      if (offset<0 && offset>closest.offset){ return {offset, element: child}; }
      else return closest;
    }, {offset: Number.NEGATIVE_INFINITY}).element;
  }
  function handleCardContainerDragOver(e){
    e.preventDefault(); e.dataTransfer.dropEffect='move';
    if (!S.draggedCard) return;
    const afterEl = getDragAfterElement(this, e.clientY);
    if (afterEl==null){ const msg=this.querySelector('.empty-board-message'); if (msg) msg.remove(); }
  }
  function handleCardContainerDrop(e){
    e.preventDefault(); e.stopPropagation();
    if (!S.draggedCard) return;
    const container = this; const targetBoardId = container.getAttribute('data-board-id');
    const msg = container.querySelector('.empty-board-message'); if (msg) msg.remove();
    const afterEl = getDragAfterElement(container, e.clientY);
    if (afterEl==null) container.appendChild(S.draggedCardElement); else container.insertBefore(S.draggedCardElement, afterEl);
    const prevBoardId = S.draggedCard.boardId;
    const cardToMove = S.cards.find(c=>c.id===S.draggedCard.id); if (cardToMove) cardToMove.boardId = targetBoardId;
    // recompute order for target board (and previous if changed)
    reorderCardsInBoard(targetBoardId);
    if (prevBoardId !== targetBoardId){ reorderCardsInBoard(prevBoardId); }
    
    // persist: if moved across boards, first update the card's boardId
    (async ()=>{
      try {
        if (prevBoardId !== targetBoardId){
          await fetch(`/api/cards/${encodeURIComponent(S.draggedCard.id)}`, {
            method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' },
            body: JSON.stringify({ boardId: Number(targetBoardId) })
          });
        }
        // then reorder target board
        const idsTarget = [...container.querySelectorAll('.card-item')].map(el=>Number(el.getAttribute('data-card-id')));
        fetch(`/api/cards/reorder/${encodeURIComponent(targetBoardId)}`, { method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' }, body: JSON.stringify(idsTarget) });
        // also reorder previous board to compact indices if changed
        if (prevBoardId !== targetBoardId){
          const prevContainer = document.querySelector(`#cards-${prevBoardId}`);
          if (prevContainer){
            const idsPrev = [...prevContainer.querySelectorAll('.card-item')].map(el=>Number(el.getAttribute('data-card-id')));
            fetch(`/api/cards/reorder/${encodeURIComponent(prevBoardId)}`, { method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' }, body: JSON.stringify(idsPrev) });
          }
        }
      } catch(_){ /* best-effort */ }
    })();
    container.classList.remove('drag-over-board');
  }
  function reorderCardsInBoard(boardId){
    const container = document.querySelector(`#cards-${boardId}`); if (!container) return;
    const ids = [...container.querySelectorAll('.card-item')].map(el=>el.getAttribute('data-card-id'));
    const boardCards = S.cards.filter(c=>c.boardId===boardId);
    const otherCards = S.cards.filter(c=>c.boardId!==boardId);
    const ordered = ids.map(id=>boardCards.find(c=>c.id===id)).filter(Boolean);
    S.cards = [...ordered, ...otherCards];
  }

  // Board drag shared handlers
  function handleBoardDragStart(e){ if (S.draggedCard) return; S.draggedBoard = this; this.style.opacity='0.4'; e.dataTransfer.effectAllowed='move'; }
  function handleBoardDragOver(e){ if (!S.draggedBoard || S.draggedCard) return; e.preventDefault(); e.dataTransfer.dropEffect='move'; }
  function handleBoardDrop(e){ if (!S.draggedBoard || S.draggedCard) return; e.stopPropagation(); e.preventDefault(); if (S.draggedBoard!==this){ const container=document.getElementById('boardContainer'); const all=[...container.querySelectorAll('.board-card')]; const draggedIndex=all.indexOf(S.draggedBoard); const targetIndex=all.indexOf(this); if (draggedIndex<targetIndex) this.parentNode.insertBefore(S.draggedBoard, this.nextSibling); else this.parentNode.insertBefore(S.draggedBoard, this); NW.boards.updateBoardsOrder(); } }
  function handleBoardDragEnd(){ if (this.style) this.style.opacity='1'; S.draggedBoard=null; }

  // Reminder feature removed

  NW.cards = {
    // UI and CRUD
    addNewCard, openCardCreateModal, closeCardCreateModal, confirmCreateCard, openCardModal, closeCardModal, saveCard, deleteCard,
    // labels
  openLabelCreateModal, closeLabelCreateModal, confirmCreateLabel, removeLabel, renderLabels,
  // local helpers for external note-scoped ops
  removeLabelLocally, removeLabelByColor, renameLabelLocally,
  // checklists
  renderChecklists, openChecklistCreateModal, closeChecklistCreateModal, confirmCreateChecklist, addCheckItemInline, checklistInlineKey, toggleCheckItem,
  deleteChecklist,
  loadChecklistsForCard,
  // compatibility no-op for boards.js call
  updateMoveToBoardSelect,
    // Drag operations
    setupCardDragEvents, createCardItemHTML,
    handleCardDragStart, handleCardDragEnd, handleCardItemDragOver,
    handleCardDragEnter, handleCardDragLeave, handleCardContainerDragOver,
    handleCardContainerDrop, reorderCardsInBoard,
    // Board drag
    handleBoardDragStart, handleBoardDragOver, handleBoardDrop, handleBoardDragEnd,
  // Misc
    // expose draft state
    isDraftActive: ()=> Boolean(_cardDraft),
    renameChecklistLocally: (id, name)=>{ if (!_cardDraft) return; const s=String(id); const t=(name||'').trim(); (_cardDraft.checklists||[]).forEach(ch=>{ if (String(ch.id)===s) ch.title=t; }); renderChecklists(); }
  };

  // expose for inline handlers
  global.addNewCard = addNewCard;
  global.openCardCreateModal = openCardCreateModal;
  global.closeCardCreateModal = closeCardCreateModal;
  global.confirmCreateCard = confirmCreateCard;
  global.openCardModal = openCardModal;
  global.closeCardModal = closeCardModal;
  global.saveCard = saveCard;
  global.deleteCard = deleteCard;
  global.openLabelCreateModal = openLabelCreateModal;
  global.closeLabelCreateModal = closeLabelCreateModal;
  global.confirmCreateLabel = confirmCreateLabel;
  global.removeLabel = removeLabel;
  // expose local helpers for app-level handlers
  global.removeLabelLocally = removeLabelLocally;
  global.removeLabelByColor = removeLabelByColor;
  global.renameLabelLocally = renameLabelLocally;
  global.openChecklistCreateModal = openChecklistCreateModal;
  global.closeChecklistCreateModal = closeChecklistCreateModal;
  global.confirmCreateChecklist = confirmCreateChecklist;
  global.loadChecklistsForCard = loadChecklistsForCard;
  global.addCheckItemInline = addCheckItemInline;
  global.checklistInlineKey = checklistInlineKey;
  global.toggleCheckItem = toggleCheckItem;
  global.deleteChecklist = deleteChecklist;
})(window);
