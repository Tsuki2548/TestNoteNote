(function(global){
  const NW = global.NW || (global.NW = {});
  // In-memory state
  NW.state = {
    notes: [],
    boards: [],
    cards: [],
    currentNoteId: null,
    currentCardId: null,
    draggedCard: null,
    draggedCardElement: null,
    draggedBoard: null
  };
})(window);
