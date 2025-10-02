(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state, U = NW.utils;

  async function updateBoardsOrder(){
    const container = document.getElementById('boardContainer');
    const boardElements = container.querySelectorAll('.board-card');
    const newOrder = [];
    const orderedIds = [];
    boardElements.forEach(element => {
      const boardId = element.getAttribute('data-board-id');
      const board = S.boards.find(b => b.id === boardId);
      if (board){ newOrder.push(board); orderedIds.push(Number(board.id)); }
    });
    const otherBoards = S.boards.filter(b => b.noteId !== S.currentNoteId);
  S.boards = [...newOrder, ...otherBoards];
    // persist order to server
    if (S.currentNoteId && orderedIds.length>0){
      try { await fetch(`/boards/reorder/${encodeURIComponent(S.currentNoteId)}`, { method:'PUT', headers:{ 'Content-Type':'application/json','Accept':'application/json' }, body: JSON.stringify(orderedIds) }); } catch(_){}
    }
  }

  async function createBoard(){
    const nameInput = document.getElementById('newBoardName');
    const name = nameInput.value.trim();
    if (!name) return;
    if (!S.currentNoteId){
      alert('โปรดเลือกโน้ตก่อนสร้างบอร์ด');
      return;
    }
    try {
      const resp = await fetch('/boards/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ boardTitle: name, noteId: Number(S.currentNoteId) })
      });
      if (!resp.ok){
        const err = await resp.json().catch(()=>({ error: resp.statusText }));
        throw new Error(err.error || 'สร้างบอร์ดไม่สำเร็จ');
      }
      const data = await resp.json();
      const board = { id: String(data.boardId), noteId: String(data.noteId), name: data.boardTitle, createdAt: new Date().toISOString() };
      S.boards.push(board);
      nameInput.value='';
  NW.ui.toggleAddBoard();
      renderBoards();
    } catch (e){
      console.error('Create board failed', e);
      alert(e.message || 'เกิดข้อผิดพลาดในการสร้างบอร์ด');
    }
  }

  function renderBoards(){
    const container = document.getElementById('boardContainer');
    const currentBoards = S.boards.filter(b=>b.noteId===S.currentNoteId);
    const addBoardWrapper = container.querySelector('.add-board-wrapper');
    container.innerHTML='';
    currentBoards.forEach(board=>{
      const el = createBoardElement(board);
      container.appendChild(el);
    });
    if (addBoardWrapper) container.appendChild(addBoardWrapper);
    NW.cards.updateMoveToBoardSelect();
  }

  function createBoardElement(board){
    const boardDiv = document.createElement('div');
    boardDiv.className='board-card';
    boardDiv.setAttribute('data-board-id', board.id);
    boardDiv.setAttribute('draggable','true');

    const boardCards = S.cards.filter(c=>c.boardId===board.id);
    boardDiv.innerHTML = `
      <div class="board-header">
        <h3 class="board-title editable" title="คลิกเพื่อแก้ไขชื่อบอร์ด" onclick="startEditBoardTitle(this, '${board.id}')">${U.escapeHtml(board.name)}</h3>
        <button class="board-menu-btn" onclick="deleteBoardPrompt('${board.id}')">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
        </button>
      </div>
      <div class="cards-container" id="cards-${board.id}" data-board-id="${board.id}">
        ${boardCards.length>0 ? boardCards.map(NW.cards.createCardItemHTML).join('') : '<div class="empty-board-message" style="padding:20px;text-align:center;color:#999;font-size:14px;">ลากการ์ดมาที่นี่หรือคลิกเพิ่มการ์ด</div>'}
      </div>
      <button class="add-card-btn" onclick="addNewCard('${board.id}')">+ เพิ่มการ์ด</button>
    `;

    // Board drag
    boardDiv.addEventListener('dragstart', NW.cards.handleBoardDragStart);
    boardDiv.addEventListener('dragover', NW.cards.handleBoardDragOver);
    boardDiv.addEventListener('drop', NW.cards.handleBoardDrop);
    boardDiv.addEventListener('dragend', NW.cards.handleBoardDragEnd);

    const cardsContainer = boardDiv.querySelector('.cards-container');
    cardsContainer.addEventListener('dragover', NW.cards.handleCardContainerDragOver);
    cardsContainer.addEventListener('drop', NW.cards.handleCardContainerDrop);
    cardsContainer.addEventListener('dragenter', NW.cards.handleCardDragEnter);
    cardsContainer.addEventListener('dragleave', NW.cards.handleCardDragLeave);

    setTimeout(()=>{
      const cardElements = cardsContainer.querySelectorAll('.card-item');
      cardElements.forEach(NW.cards.setupCardDragEvents);
    },0);

    // Inline edit handler for board title
    const titleEl = boardDiv.querySelector('.board-title');
    titleEl.ondblclick = ()=> startEditBoardTitle(titleEl, board.id);

    return boardDiv;
  }

  function deleteBoardPrompt(boardId){
    const board = S.boards.find(b=>b.id===boardId);
    if (!board) return;
    const boardCards = S.cards.filter(c=>c.boardId===boardId);
    const message = boardCards.length>0 ? `ต้องการลบบอร์ด "${board.name}"?\nจะมีการ์ด ${boardCards.length} ใบถูกลบด้วย` : `ต้องการลบบอร์ด "${board.name}"?`;
    NW.ui.openConfirm({
      title: 'ลบบอร์ด',
      message,
      variant: 'danger',
      confirmText: 'ลบบอร์ด',
      onConfirm: async ()=>{
        try {
          const resp = await fetch(`/boards/delete/${encodeURIComponent(boardId)}`, { method:'DELETE', headers:{ 'Accept':'application/json' } });
          if (!resp.ok){ try{ const j=await resp.json(); alert(j.error||'ลบบอร์ดไม่สำเร็จ'); }catch(_){ alert('ลบบอร์ดไม่สำเร็จ'); } }
        } catch(_){ /* fall through to local removal */ }
        S.cards = S.cards.filter(c=>c.boardId!==boardId);
        S.boards = S.boards.filter(b=>b.id!==boardId);
        renderBoards();
      }
    });
  }

  NW.boards = { renderBoards, createBoard, updateBoardsOrder, createBoardElement, deleteBoardPrompt };
  global.createBoard = createBoard;
  global.deleteBoardPrompt = deleteBoardPrompt;
  // Inline board title editing helpers
  async function saveBoardTitle(boardId, newTitle){
    const t = (newTitle||'').trim();
    if (!t) return false;
    try {
      const resp = await fetch(`/boards/update/${encodeURIComponent(boardId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ boardTitle: t })
      });
      if (!resp.ok){ try{ const j=await resp.json(); alert(j.error||'แก้ไขชื่อบอร์ดไม่สำเร็จ'); }catch(_){ alert('แก้ไขชื่อบอร์ดไม่สำเร็จ'); } return false; }
  const b = S.boards.find(x=>x.id===boardId); if (b){ b.name = t; }
      return true;
    } catch(e){ console.error('Update board title failed', e); alert('เกิดข้อผิดพลาดในการแก้ไขชื่อบอร์ด'); return false; }
  }

  function startEditBoardTitle(titleElement, boardId) {
    if (titleElement.querySelector('.board-title-input')) return;
    const current = titleElement.textContent.trim();
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'board-title-input';
    input.value = current;
    input.setAttribute('aria-label', 'แก้ไขชื่อบอร์ด');
    input.style.width = Math.max(titleElement.clientWidth, 120) + 'px';
    titleElement.innerHTML = '';
    titleElement.appendChild(input);
    input.focus();
    input.select();
    let done = false;

    const cancel = () => {
      if (done) return; done = true;
      titleElement.textContent = current;
      titleElement.className = 'board-title editable';
    };
    const commit = async (nextVal) => {
      if (done) return; done = true;
      const next = (nextVal || '').trim();
      titleElement.textContent = next || current;
      titleElement.className = 'board-title editable';
      if (!next || next === current) return;
      
      const board = S.boards.find(b => b.id === boardId);
      if (board) {
        board.name = next;
        // Persist to server
        try {
          await fetch(`/boards/update/${encodeURIComponent(boardId)}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ boardTitle: next, noteId: Number(board.noteId) })
          });
        } catch (e) {
          console.error('Update board failed', e);
          // Revert on error
          board.name = current;
          titleElement.textContent = current;
          alert('แก้ไขชื่อบอร์ดไม่สำเร็จ');
        }
      }
    };

    input.addEventListener('keydown', function(e) {
      if (e.key === 'Enter') { e.preventDefault(); commit(input.value); }
      if (e.key === 'Escape') { e.preventDefault(); cancel(); }
    });
    input.addEventListener('blur', function() { commit(input.value); });
  }

  // Expose functions
  NW.boards = NW.boards || {};
  NW.boards.startEditBoardTitle = startEditBoardTitle;
  global.startEditBoardTitle = startEditBoardTitle;
})(window);
