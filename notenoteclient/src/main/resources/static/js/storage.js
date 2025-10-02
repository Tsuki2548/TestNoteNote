(function(global){
  // Storage is now a no-op: all data comes from the backend, not localStorage.
  const NW = global.NW || (global.NW = {});

  NW.storage = {
    // Keep API surface so callers don't break, but do nothing.
    save(){ /* no-op: persisted by server */ },
    load(){ /* no-op: initial data fetched from server or injected */ },
    clearAll(){ /* no-op: nothing cached locally */ }
  };
})(window);
