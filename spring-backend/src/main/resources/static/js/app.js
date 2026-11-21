(() => {
  const input = document.querySelector('#image');
  const zone = document.querySelector('#drop-zone');
  const previewWrap = document.querySelector('#preview-wrap');
  const preview = document.querySelector('#image-preview');
  const error = document.querySelector('#upload-error');
  const name = document.querySelector('#preview-name');
  const size = document.querySelector('#preview-size');
  if (!input || !zone || !preview || !previewWrap) return;

  const maxBytes = 7 * 1024 * 1024;
  const accepted = new Set(['image/jpeg', 'image/png', 'image/webp']);
  let objectUrl = null;
  const clearPreview = () => {
    if (objectUrl) URL.revokeObjectURL(objectUrl);
    objectUrl = null;
    preview.removeAttribute('src');
    previewWrap.hidden = true;
    zone.classList.remove('has-image');
  };
  const showError = message => {
    error.textContent = message;
    error.hidden = !message;
  };
  const showFile = file => {
    if (!file) return;
    const extensionAllowed = /\.(jpe?g|png|webp)$/i.test(file.name);
    if (!(accepted.has(file.type) || (!file.type && extensionAllowed))) {
      input.value = '';
      clearPreview();
      showError('Choose a JPG, PNG, or WEBP image.');
      return;
    }
    if (file.size > maxBytes) {
      input.value = '';
      clearPreview();
      showError('This image is larger than 7 MB. Choose a smaller photo.');
      return;
    }
    showError('');
    if (input.files[0] !== file) {
      const transfer = new DataTransfer();
      transfer.items.add(file);
      input.files = transfer.files;
    }
    if (objectUrl) URL.revokeObjectURL(objectUrl);
    const nextObjectUrl = URL.createObjectURL(file);
    objectUrl = nextObjectUrl;
    preview.onload = () => {
      if (objectUrl !== nextObjectUrl) return;
      previewWrap.hidden = false;
      zone.classList.add('has-image');
      name.textContent = file.name;
      size.textContent = `${(file.size / 1024 / 1024).toFixed(2)} MB`;
    };
    preview.onerror = () => {
      if (objectUrl !== nextObjectUrl) return;
      clearPreview();
      input.value = '';
      showError('The selected file could not be previewed. Try another JPG, PNG, or WEBP image.');
    };
    preview.src = nextObjectUrl;
  };

  input.addEventListener('change', () => showFile(input.files[0]));
  ['dragenter', 'dragover'].forEach(type => zone.addEventListener(type, event => {
    event.preventDefault();
    zone.classList.add('dragging');
  }));
  ['dragleave', 'drop'].forEach(type => zone.addEventListener(type, event => {
    event.preventDefault();
    zone.classList.remove('dragging');
  }));
  zone.addEventListener('drop', event => showFile(event.dataTransfer.files[0]));
  document.querySelector('#remove-image')?.addEventListener('click', () => {
    input.value = '';
    clearPreview();
    showError('');
  });
})();
