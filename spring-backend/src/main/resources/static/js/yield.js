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

  const scenario = document.querySelector('.what-if-panel');
  if (!scenario) return;
  const priceSlider = document.querySelector('#scenario-price');
  const priceOutput = document.querySelector('#scenario-price-output');
  const areaInput = document.querySelector('#scenario-area');
  const revenueOutput = document.querySelector('#scenario-revenue');
  const costOutput = document.querySelector('#scenario-cost');
  const profitOutput = document.querySelector('#scenario-profit');
  const profitLabel = document.querySelector('#scenario-profit-label');
  const predictedQuintalsPerHa = Number(scenario.dataset.yield);
  const costPerHa = Number(scenario.dataset.cost);
  const basePrice = Number(scenario.dataset.basePrice);
  const currency = value => `₹${new Intl.NumberFormat('en-IN', {maximumFractionDigits: 0}).format(value)}`;

  if (![predictedQuintalsPerHa, costPerHa, basePrice].every(Number.isFinite)) return;
  priceSlider.min = String(Math.max(1, Math.floor(basePrice * .5)));
  priceSlider.max = String(Math.max(Number(priceSlider.min) + 1, Math.ceil(basePrice * 1.5)));
  priceSlider.step = '1';
  priceSlider.value = String(Math.round(basePrice));

  const updateScenario = () => {
    const marketPrice = Number(priceSlider.value);
    const area = Number(areaInput.value);
    const maxArea = Number(areaInput.max);
    priceOutput.value = currency(marketPrice);
    priceOutput.textContent = currency(marketPrice);
    const invalidArea = !Number.isFinite(area) || area <= 0 || (Number.isFinite(maxArea) && area > maxArea);
    areaInput.setCustomValidity(invalidArea && Number.isFinite(area) && area > maxArea
      ? 'Area cannot exceed the registered land size.' : '');
    areaInput.setAttribute('aria-invalid', String(invalidArea));
    if (invalidArea) {
      revenueOutput.textContent = costOutput.textContent = profitOutput.textContent = '—';
      return;
    }
    const revenue = predictedQuintalsPerHa * marketPrice * area;
    const cost = costPerHa * area;
    const net = revenue - cost;
    revenueOutput.textContent = currency(revenue);
    costOutput.textContent = currency(cost);
    profitOutput.textContent = currency(net);
    profitLabel.textContent = net < 0 ? 'Estimated net loss' : 'Estimated net return';
    profitOutput.classList.toggle('value-negative', net < 0);
    profitOutput.classList.toggle('value-positive', net >= 0);
  };
  priceSlider.addEventListener('input', updateScenario);
  areaInput.addEventListener('input', updateScenario);
  updateScenario();
})();
