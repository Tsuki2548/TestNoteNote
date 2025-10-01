(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state, U = NW.utils, ST = NW.storage;

  let _creatingBoardId = null;
  let _createCardKeyHandler = null;
  // Draft state for editing a card within modal
  let _cardDraft = null;

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

  function confirmCreateCard(){
    const input = document.getElementById('cardCreateInput');
    const err = document.getElementById('cardCreateError');
    const title = (input?.value||'').trim();
    if (!title){ if (err) err.style.display='block'; input?.focus(); return; }
    const boardId = _creatingBoardId;
    if (!boardId){ closeCardCreateModal(); return; }
    S.cards.push({ id: U.generateId(), boardId, title, description: '', color: '#ffffff', labels: [], dueDate: null, reminder: 0, checklists: [], createdAt: new Date().toISOString() });
    ST.save();
    NW.boards.renderBoards();
    closeCardCreateModal();
  }

  function openCardModal(cardId){
    S.currentCardId = cardId;
    const card = S.cards.find(c=>c.id===cardId);
    if (!card) return;
    // create deep clone as draft
    _cardDraft = JSON.parse(JSON.stringify(card));
    const modal = document.getElementById('cardModal');
    document.getElementById('cardTitle').value = _cardDraft.title || '';
    document.getElementById('cardDescription').value = _cardDraft.description || '';
    document.getElementById('cardDueDate').value = _cardDraft.dueDate || '';
    document.getElementById('reminderTime').value = _cardDraft.reminder || 0;
    renderLabels();
    renderChecklists();
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
    S.currentCardId = null;
  }

  function saveCard(){
    if (!S.currentCardId || !_cardDraft) return;
    const card = S.cards.find(c=>c.id===S.currentCardId);
    if (!card) return;
    // read latest from inputs into draft
    _cardDraft.title = document.getElementById('cardTitle').value;
    _cardDraft.description = document.getElementById('cardDescription').value;
    _cardDraft.dueDate = document.getElementById('cardDueDate').value;
    _cardDraft.reminder = parseInt(document.getElementById('reminderTime').value);
    const selectedColor = document.querySelector('.color-option.selected');
    if (selectedColor){ _cardDraft.color = selectedColor.getAttribute('data-color'); }
    // commit draft to card
    card.title = _cardDraft.title;
    card.description = _cardDraft.description;
    card.dueDate = _cardDraft.dueDate;
    card.reminder = _cardDraft.reminder;
    card.color = _cardDraft.color;
    card.labels = Array.isArray(_cardDraft.labels) ? _cardDraft.labels : [];
    card.checklists = Array.isArray(_cardDraft.checklists) ? _cardDraft.checklists : [];
    ST.save();
    NW.boards.renderBoards();
    // set reminder after save
    if (card.dueDate && card.reminder>0) setReminder(card);
    // clear and close
    _cardDraft = null;
    closeCardModal();
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
        ST.save();
        NW.boards.renderBoards();
        closeCardModal();
      }
    });
  }

  function renderLabels(){
    if (!_cardDraft) return;
    const labels = _cardDraft.labels || [];
    const container = document.getElementById('labelContainer');
    container.innerHTML = labels.map(label=>`
      <span class="label-tag" style="background:${label.color}">${U.escapeHtml(label.text)}<button onclick="removeLabel('${label.id}')">×</button></span>
    `).join('');
  }

  // Label create modal flow
  let _labelKeyHandler = null;
  function openLabelCreateModal(){
    const modal = document.getElementById('labelCreateModal');
    const input = document.getElementById('labelTextInput');
    const err = document.getElementById('labelTextError');
    if (!modal || !input) return;
    if (err) err.style.display='none';
    input.value='';
    // select first color by default
    document.querySelectorAll('.label-color-option').forEach((o,i)=>{ o.classList.toggle('selected', i===0); });
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
    if (!_cardDraft.labels) _cardDraft.labels = [];
    _cardDraft.labels.push({ id: U.generateId(), text, color });
    renderLabels();
    closeLabelCreateModal();
  }

  function removeLabel(labelId){
    if (!_cardDraft) return;
    _cardDraft.labels = (_cardDraft.labels||[]).filter(l=>l.id!==labelId);
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
            <span class="checklist-title">${U.escapeHtml(ch.title)}</span>
            <span class="checklist-progress">${progress.completed}/${progress.total}</span>
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
    ST.save();
  }
    // Checklist create modal flow
    let _checklistKeyHandler = null;
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
      const input = document.getElementById('checklistTitleInput');
      const err = document.getElementById('checklistCreateError');
      const title = (input?.value||'').trim();
      if (!title){ if (err) err.style.display='block'; input?.focus(); return; }
      if (!_cardDraft) return;
      if (!_cardDraft.checklists) _cardDraft.checklists = [];
      _cardDraft.checklists.push({ id: U.generateId(), title, items: [] });
      renderChecklists();
      closeChecklistCreateModal();
    }

  function addCheckItem(checklistId){
    const text = prompt('รายการ:');
    if (!text || !text.trim()) return;
    const card = S.cards.find(c=>c.id===S.currentCardId); if (!card) return;
    const cl = card.checklists.find(x=>x.id===checklistId); if (!cl) return;
    cl.items.push({ id: U.generateId(), text: text.trim(), completed: false });
    renderChecklists(card.checklists);
    ST.save();
  }
    function addCheckItemInline(checklistId, text){
      const t = (text||'').trim();
      if (!t) return;
      if (!_cardDraft) return;
      const cl = (_cardDraft.checklists||[]).find(x=>x.id===checklistId); if (!cl) return;
      cl.items.push({ id: U.generateId(), text: t, completed: false });
      renderChecklists();
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

  function toggleCheckItem(checklistId,itemId){
    if (!_cardDraft) return;
    const cl = (_cardDraft.checklists||[]).find(x=>x.id===checklistId); if (!cl) return;
    const it = (cl.items||[]).find(i=>i.id===itemId); if (!it) return;
    it.completed = !it.completed;
    renderChecklists();
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
    const labels = card.labels ? card.labels.map(l=>`<span class="card-label" style="background:${l.color}">${U.escapeHtml(l.text)}</span>`).join('') : '';
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
    const cardToMove = S.cards.find(c=>c.id===S.draggedCard.id); if (cardToMove) cardToMove.boardId = targetBoardId;
    reorderCardsInBoard(targetBoardId);
    if (S.draggedCard.boardId !== targetBoardId){ reorderCardsInBoard(S.draggedCard.boardId); }
    ST.save();
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

  function setReminder(card){
    if (!card.dueDate || card.reminder===0) return;
    const dueDate = new Date(card.dueDate);
    const remindAt = new Date(dueDate.getTime() - (card.reminder*60000));
    const now = new Date();
    if (remindAt>now){
      const ms = remindAt - now;
      setTimeout(()=>{
        if ("Notification" in window && Notification.permission === 'granted'){
          new Notification('แจ้งเตือนการ์ด',{ body: `"${card.title}" ครบกำหนดในอีก ${card.reminder} นาที` });
        } else {
          alert(`แจ้งเตือน: "${card.title}" ครบกำหนดในอีก ${card.reminder} นาที`);
        }
      }, ms);
    }
  }

  NW.cards = {
    // UI and CRUD
    addNewCard, openCardCreateModal, closeCardCreateModal, confirmCreateCard, openCardModal, closeCardModal, saveCard, deleteCard,
    // labels
    openLabelCreateModal, closeLabelCreateModal, confirmCreateLabel, removeLabel, renderLabels,
  // checklists
  renderChecklists, openChecklistCreateModal, closeChecklistCreateModal, confirmCreateChecklist, addCheckItemInline, checklistInlineKey, toggleCheckItem,
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
    setReminder
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
  global.openChecklistCreateModal = openChecklistCreateModal;
  global.closeChecklistCreateModal = closeChecklistCreateModal;
  global.confirmCreateChecklist = confirmCreateChecklist;
  global.addCheckItemInline = addCheckItemInline;
  global.checklistInlineKey = checklistInlineKey;
  global.toggleCheckItem = toggleCheckItem;
})(window);
