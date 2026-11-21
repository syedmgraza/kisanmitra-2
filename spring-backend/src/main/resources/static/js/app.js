(() => {
  const input = document.querySelector('#image');
  const zone = document.querySelector('#drop-zone');
  const previewWrap = document.querySelector('#preview-wrap');
  const preview = document.querySelector('#image-preview');
  if (!input || !zone) return;
  const showFile = file => {
    if (!file || !file.type.startsWith('image/')) return;
    const transfer = new DataTransfer(); transfer.items.add(file); input.files = transfer.files;
    preview.src = URL.createObjectURL(file); previewWrap.hidden = false;
    zone.classList.add('has-image');
  };
  input.addEventListener('change', () => showFile(input.files[0]));
  ['dragenter', 'dragover'].forEach(type => zone.addEventListener(type, event => { event.preventDefault(); zone.classList.add('dragging'); }));
  ['dragleave', 'drop'].forEach(type => zone.addEventListener(type, event => { event.preventDefault(); zone.classList.remove('dragging'); }));
  zone.addEventListener('drop', event => showFile(event.dataTransfer.files[0]));
  document.querySelector('#remove-image')?.addEventListener('click', () => { input.value = ''; preview.src = ''; previewWrap.hidden = true; zone.classList.remove('has-image'); });
})();
