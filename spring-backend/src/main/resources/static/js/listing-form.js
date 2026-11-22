(() => {
  const crop = document.querySelector('#listing-crop');
  const quantity = document.querySelector('#listing-quantity');
  const price = document.querySelector('#listing-price');
  const title = document.querySelector('#listing-preview-crop');
  const amount = document.querySelector('#listing-preview-price');
  const stock = document.querySelector('#listing-preview-quantity');
  const image = document.querySelector('#listing-preview-image');
  const previewCard = document.querySelector('.listing-preview');
  if (!crop || !title || !image) return;

  const isHi = document.documentElement.lang === 'hi';
  const ds = previewCard ? previewCard.dataset : {};
  const defaultCrop = ds.defaultCrop || (isHi ? 'आपकी फसल' : 'Your crop');
  const defaultPrice = ds.defaultPrice || (isHi ? '₹— / किलो' : '₹— / kg');
  const defaultQty = ds.defaultQty || (isHi ? 'मात्रा यहां दिखाई देगी' : 'Quantity will appear here');
  const perKg = ds.perKg || (isHi ? 'प्रति किलो' : '/ kg');
  const kgAvail = ds.kgAvail || (isHi ? 'किलो उपलब्ध' : 'kg available');

  const update = () => {
    const name = crop.value.trim();
    title.textContent = name || defaultCrop;
    const slug = name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
    image.onerror = () => { image.onerror = null; image.src = '/assets/crop-placeholder.png'; };
    image.src = slug ? `/assets/${slug}.png` : '/assets/crop-placeholder.png';
    const numericPrice = Number(price?.value);
    amount.textContent = numericPrice > 0 ? `₹${numericPrice.toFixed(2)} ${perKg}` : defaultPrice;
    const numericQuantity = Number(quantity?.value);
    stock.textContent = numericQuantity > 0
      ? `${new Intl.NumberFormat(isHi ? 'hi-IN' : 'en-IN').format(numericQuantity)} ${kgAvail}`
      : defaultQty;
  };
  [crop, quantity, price].forEach(input => input?.addEventListener('input', update));
  update();
})();
