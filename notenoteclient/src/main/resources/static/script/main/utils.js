(function(global){
  const NW = global.NW || (global.NW = {});
  NW.utils = {
    generateId(){
      return Date.now().toString(36) + Math.random().toString(36).substr(2);
    },
    escapeHtml(text){
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    }
  };
})(window);
