(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state, U = NW.utils, ST = NW.storage;

  function updateBoardsOrder(){
    const container = document.getElementById('boardContainer');
    const boardElements = container.querySelectorAll('.board-card');
    const newOrder = [];
    boardElements.forEach(element => {
      const boardId = element.getAttribute('data-board-id');
      const board = S.boards.find(b => b.id === boardId);
      if (board) newOrder.push(board);
    });
    const otherBoards = S.boards.filter(b => b.noteId !== S.currentNoteId);
    S.boards = [...newOrder, ...otherBoards];
    ST.save();
  }

  function createBoard(){
    const nameInput = document.getElementById('newBoardName');
    const name = nameInput.value.trim();
    if (!name) return;
    const board = { id: U.generateId(), noteId: S.currentNoteId, name, createdAt: new Date().toISOString() };
    S.boards.push(board);
    nameInput.value='';
    NW.ui.toggleAddBoard();
    ST.save();
    renderBoards();
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
        <h3 class="board-title">${U.escapeHtml(board.name)}</h3>
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
      onConfirm: ()=>{
        S.cards = S.cards.filter(c=>c.boardId!==boardId);
        S.boards = S.boards.filter(b=>b.id!==boardId);
        ST.save();
        renderBoards();
      }
    });
  }

  NW.boards = { renderBoards, createBoard, updateBoardsOrder, createBoardElement, deleteBoardPrompt };
  global.createBoard = createBoard;
  global.deleteBoardPrompt = deleteBoardPrompt;
})(window);
