(() => {
  const seasonSelect = document.querySelector('#yield-season');
  const cropSelect = document.querySelector('#yield-crop');
  const help = document.querySelector('#yield-crop-help');
  if (!seasonSelect || !cropSelect) return;

  const labelFor = value => value.replace(/\b\w/g, letter => letter.toUpperCase());
  const loadCrops = async requestedCrop => {
    cropSelect.disabled = true;
    cropSelect.replaceChildren(new Option('Loading available crops…', ''));
    if (help) help.textContent = 'Finding crops with model records for your farm and season…';
    try {
      const query = new URLSearchParams({season: seasonSelect.value});
      const response = await fetch(`/yield/crops?${query.toString()}`, {headers: {Accept: 'application/json'}});
      if (!response.ok) throw new Error(`Crop list request failed (HTTP ${response.status}).`);
      const crops = await response.json();
      cropSelect.replaceChildren(new Option(crops.length ? 'Choose a crop' : 'No crops available for this season', ''));
      crops.forEach(crop => cropSelect.add(new Option(labelFor(crop), crop)));
      cropSelect.disabled = crops.length === 0;
      const match = crops.find(crop => crop.toLowerCase() === (requestedCrop || '').trim().toLowerCase());
      if (match) cropSelect.value = match;
      if (help) help.textContent = crops.length
        ? `${crops.length} trained crop${crops.length === 1 ? '' : 's'} available for this location and season.`
        : 'The yield model has no crop records for this location and season. Try another season.';
    } catch (error) {
      cropSelect.replaceChildren(new Option('Crop list unavailable', ''));
      if (help) help.textContent = `${error.message} If the service just woke up, wait a moment and reload.`;
    }
  };

  loadCrops(cropSelect.dataset.selectedCrop || '');
  seasonSelect.addEventListener('change', () => loadCrops(''));
})();
