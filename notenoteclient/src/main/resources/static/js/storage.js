(function(global){
  const NW = global.NW || (global.NW = {});
  const S = NW.state; const U = NW.utils;

  function migrate(){
    const { boards, cards } = NW.state;
    if (Array.isArray(cards)) {
      cards.forEach(card => {
        if (!Array.isArray(card.labels)) card.labels = [];
        if (!Array.isArray(card.checklists)) card.checklists = [];
        card.checklists.forEach(cl => {
          if (!Array.isArray(cl.items)) cl.items = [];
          cl.items.forEach(it => {
            if (typeof it.completed === 'undefined' && typeof it.checked !== 'undefined') {
              it.completed = !!it.checked;
              delete it.checked;
            }
          });
        });
      });
    }
    if (Array.isArray(boards)) {
      boards.forEach(b => { if (!b.createdAt) b.createdAt = new Date().toISOString(); });
    }
  }

  NW.storage = {
    save(){
      localStorage.setItem('notes', JSON.stringify(S.notes));
      localStorage.setItem('boards', JSON.stringify(S.boards));
      localStorage.setItem('cards', JSON.stringify(S.cards));
      localStorage.setItem('currentNoteId', S.currentNoteId);
    },
    load(){
      const n = localStorage.getItem('notes');
      const b = localStorage.getItem('boards');
      const c = localStorage.getItem('cards');
      const cur = localStorage.getItem('currentNoteId');
      if (n) S.notes = JSON.parse(n);
      if (b) S.boards = JSON.parse(b);
      if (c) S.cards = JSON.parse(c);
      if (cur) S.currentNoteId = cur;
      migrate();
    },
    clearAll(){
      localStorage.removeItem('notes');
      localStorage.removeItem('boards');
      localStorage.removeItem('cards');
      localStorage.removeItem('currentNoteId');
    }
  };
})(window);
