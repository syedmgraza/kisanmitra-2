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
    const isHi = root.lang === 'hi';
    const next = theme === 'dark' ? 'light' : 'dark';
    const label = isHi
      ? (theme === 'dark' ? 'लाइट मोड चालू करें' : 'डार्क मोड चालू करें')
      : `Switch to ${next} mode`;
    button.setAttribute('aria-label', label);
    button.setAttribute('title', label);
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
