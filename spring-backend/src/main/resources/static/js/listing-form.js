(() => {
  const crop = document.querySelector('#listing-crop');
  const quantity = document.querySelector('#listing-quantity');
  const price = document.querySelector('#listing-price');
  const title = document.querySelector('#listing-preview-crop');
  const amount = document.querySelector('#listing-preview-price');
  const stock = document.querySelector('#listing-preview-quantity');
  const image = document.querySelector('#listing-preview-image');
  if (!crop || !title || !image) return;

  const update = () => {
    const name = crop.value.trim();
    title.textContent = name || 'Your crop';
    const slug = name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
    image.onerror = () => { image.onerror = null; image.src = '/assets/crop-placeholder.png'; };
    image.src = slug ? `/assets/${slug}.png` : '/assets/crop-placeholder.png';
    const numericPrice = Number(price?.value);
    amount.textContent = numericPrice > 0 ? `₹${numericPrice.toFixed(2)} / kg` : '₹— / kg';
    const numericQuantity = Number(quantity?.value);
    stock.textContent = numericQuantity > 0 ? `${new Intl.NumberFormat().format(numericQuantity)} kg available` : 'Quantity will appear here';
  };
  [crop, quantity, price].forEach(input => input?.addEventListener('input', update));
  update();
})();
