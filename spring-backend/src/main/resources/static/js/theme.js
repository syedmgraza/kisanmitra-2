(() => {
  const key = 'kisanmitra-theme';
  const root = document.documentElement;
  const readTheme = () => {
    try { return localStorage.getItem(key) === 'dark' ? 'dark' : 'light'; }
    catch { return 'light'; }
  };
  const applyTheme = theme => {
    root.dataset.theme = theme;
    try { localStorage.setItem(key, theme); } catch { /* Theme still works for this page. */ }
    const button = document.querySelector('#theme-toggle');
    if (!button) return;
    const next = theme === 'dark' ? 'light' : 'dark';
    button.setAttribute('aria-label', `Switch to ${next} mode`);
    button.setAttribute('title', `Switch to ${next} mode`);
    button.setAttribute('aria-pressed', String(theme === 'dark'));
    button.innerHTML = `<i data-lucide="${theme === 'dark' ? 'sun' : 'moon'}"></i>`;
    window.lucide?.createIcons();
  };

  applyTheme(readTheme());
  document.addEventListener('DOMContentLoaded', () => {
    const button = document.querySelector('#theme-toggle');
    button?.addEventListener('click', () => applyTheme(root.dataset.theme === 'dark' ? 'light' : 'dark'));
  }, {once: true});
})();
