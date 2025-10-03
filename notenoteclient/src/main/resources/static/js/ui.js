(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state;

  function toggleAddBoard(){
    const dropdown = document.getElementById('addBoardDropdown');
    dropdown.classList.toggle('open');
    if (dropdown.classList.contains('open')){
      const input = document.getElementById('newBoardName');
      if (input) input.focus();
    }
  }

  function toggleSettings(){
    const dropdown = document.getElementById('settingsDropdown');
    dropdown.classList.toggle('open');
  }

  function toggleFilter(){
    const dropdown = document.getElementById('filterDropdown');
    dropdown.classList.toggle('open');
  }

  function filterBoards(type){
    const currentBoards = S.boards.filter(b=>b.noteId===S.currentNoteId);
    let sorted = [...currentBoards];
    switch(type){
      case 'date': sorted.sort((a,b)=> new Date(b.createdAt)-new Date(a.createdAt)); break;
      case 'name': sorted.sort((a,b)=> a.name.localeCompare(b.name,'th')); break;
      case 'cards':
        sorted.sort((a,b)=>{
          const ac = S.cards.filter(c=>c.boardId===a.id).length;
          const bc = S.cards.filter(c=>c.boardId===b.id).length;
          return bc - ac;
        });
        break;
    }
  const other = S.boards.filter(b=>b.noteId!==S.currentNoteId);
  S.boards = [...sorted, ...other];
    NW.boards.renderBoards();
    const fd = document.getElementById('filterDropdown');
    if (fd) fd.classList.remove('open');
  }

  function handleSearch(e){
    const q = e.target.value.toLowerCase().trim();
    const boardCards = document.querySelectorAll('.board-card');
    if (q===''){
      boardCards.forEach(b=>{ b.style.display='flex'; b.querySelectorAll('.card-item').forEach(c=>c.style.display='block'); });
      return;
    }
    boardCards.forEach(board=>{
      const name = board.querySelector('.board-title').textContent.toLowerCase();
      const cardItems = board.querySelectorAll('.card-item');
      let hasMatch = name.includes(q);
      cardItems.forEach(ci=>{
        const title = ci.querySelector('.card-item-title').textContent.toLowerCase();
        const match = title.includes(q);
        ci.style.display = match ? 'block' : 'none';
        if (match) hasMatch = true;
      });
      board.style.display = hasMatch ? 'flex' : 'none';
    });
  }

  function switchAccount(){ alert('ฟีเจอร์สลับบัญชีกำลังพัฒนา'); }
  function logout(){
    openConfirm({
      title: 'ออกจากระบบ',
      message: 'ต้องการออกจากระบบหรือไม่?\n\nการทำงานทั้งหมดจะถูกบันทึกอัตโนมัติ\nคุณสามารถเข้าสู่ระบบอีกครั้งได้ตลอดเวลา',
      variant: 'danger',
      confirmText: 'ออกจากระบบ',
      onConfirm: ()=>{
        try {
          // Clear any browser local cache best-effort (no app-specific storage now)
          try { localStorage.clear(); } catch(_) {}
          // Create and submit a real form to ensure redirect and cookie clearing happen server-side
          const form = document.createElement('form');
          form.method = 'POST';
          form.action = '/logout';
          document.body.appendChild(form);
          form.submit();
        } catch (e) {
          alert('ไม่สามารถออกจากระบบได้: '+ (e?.message||e));
        }
      }
    });
  }

  // Reusable Confirm Modal
  let _confirmCb = null;
  let _confirmKeyHandler = null;
  function openConfirm({ title='ยืนยันการทำรายการ', message='', variant='danger', confirmText='ยืนยัน', onConfirm=null }={}){
    const modal = document.getElementById('confirmModal');
    const t = document.getElementById('confirmTitle');
    const m = document.getElementById('confirmMessage');
    const btn = document.getElementById('confirmPrimaryBtn');
    if (!modal || !t || !m || !btn) return;
    t.textContent = title;
    m.textContent = message;
    btn.textContent = confirmText || 'ยืนยัน';
    // style variant
    btn.classList.remove('note-create-btn');
    btn.classList.remove('save-card-btn');
    btn.classList.remove('delete-card-btn');
    if (variant==='primary') btn.classList.add('note-create-btn');
    else if (variant==='success') btn.classList.add('save-card-btn');
    else btn.classList.add('delete-card-btn');
    _confirmCb = typeof onConfirm==='function' ? onConfirm : null;
    modal.classList.add('open');
    // keyboard shortcuts
    _confirmKeyHandler = (e)=>{
      if (e.key==='Escape'){ e.preventDefault(); closeConfirm(); }
      if (e.key==='Enter'){ e.preventDefault(); doConfirm(); }
    };
    document.addEventListener('keydown', _confirmKeyHandler);
    // focus primary for quick Enter
    setTimeout(()=>btn.focus(), 0);
  }
  function closeConfirm(){
    const modal=document.getElementById('confirmModal');
    if (modal) modal.classList.remove('open');
    _confirmCb=null;
    if (_confirmKeyHandler){ document.removeEventListener('keydown', _confirmKeyHandler); _confirmKeyHandler=null; }
  }
  function doConfirm(){ const cb=_confirmCb; closeConfirm(); if (cb) cb(); }

  NW.ui = { toggleAddBoard, toggleSettings, toggleFilter, filterBoards, handleSearch, switchAccount, logout, openConfirm, closeConfirm, doConfirm };
  global.toggleAddBoard = toggleAddBoard;
  global.toggleSettings = toggleSettings;
  global.toggleFilter = toggleFilter;
  global.filterBoards = filterBoards;
  global.switchAccount = switchAccount;
  global.logout = logout;
  global.closeConfirm = closeConfirm;
  global.doConfirm = doConfirm;
  // openConfirm is used by modules, no need to expose globally for HTML except via NW.ui
})(window);
