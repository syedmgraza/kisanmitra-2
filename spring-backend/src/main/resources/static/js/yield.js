(() => {
  const seasonSelect = document.querySelector('#yield-season');
  const cropSelect = document.querySelector('#yield-crop');
  const help = document.querySelector('#yield-crop-help');
  if (!seasonSelect || !cropSelect) return;

  const isHi = document.documentElement.lang === 'hi';
  const i18n = {
    loading: isHi ? 'उपलब्ध फसलें लोड हो रही हैं…' : 'Loading available crops…',
    searching: isHi ? 'आपके क्षेत्र और सीज़न के लिए फसलें खोजी जा रही हैं…' : 'Finding crops with model records for your farm and season…',
    choose: isHi ? 'फसल चुनें' : 'Choose a crop',
    noCrops: isHi ? 'इस सीज़न के लिए कोई फसल उपलब्ध नहीं' : 'No crops available for this season',
    countTrained: n => isHi ? `इस स्थान और सीज़न के लिए ${n} प्रशिक्षित फसलें उपलब्ध हैं।` : `${n} trained crop${n === 1 ? '' : 's'} available for this location and season.`,
    noRecords: isHi ? 'इस स्थान और सीज़न के लिए मॉडल में कोई रिकॉर्ड नहीं है। अन्य सीज़न चुनें।' : 'The yield model has no crop records for this location and season. Try another season.',
    unavailable: isHi ? 'फसल सूची उपलब्ध नहीं' : 'Crop list unavailable',
    retry: isHi ? 'यदि सेवा अभी शुरू हुई है, तो थोड़ा रुककर पुनः लोड करें।' : 'If the service just woke up, wait a moment and reload.',
    areaExceeded: isHi ? 'रकबा पंजीकृत भूमि आकार से अधिक नहीं हो सकता।' : 'Area cannot exceed the registered land size.'
  };

  const labelFor = value => value.replace(/\b\w/g, letter => letter.toUpperCase());
  const loadCrops = async requestedCrop => {
    cropSelect.disabled = true;
    cropSelect.replaceChildren(new Option(i18n.loading, ''));
    if (help) help.textContent = i18n.searching;
    try {
      const query = new URLSearchParams({season: seasonSelect.value});
      const response = await fetch(`/yield/crops?${query.toString()}`, {headers: {Accept: 'application/json'}});
      if (!response.ok) throw new Error(`Crop list request failed (HTTP ${response.status}).`);
      const crops = await response.json();
      cropSelect.replaceChildren(new Option(crops.length ? i18n.choose : i18n.noCrops, ''));
      crops.forEach(crop => cropSelect.add(new Option(labelFor(crop), crop)));
      cropSelect.disabled = crops.length === 0;
      const match = crops.find(crop => crop.toLowerCase() === (requestedCrop || '').trim().toLowerCase());
      if (match) cropSelect.value = match;
      if (help) help.textContent = crops.length
        ? i18n.countTrained(crops.length)
        : i18n.noRecords;
    } catch (error) {
      cropSelect.replaceChildren(new Option(i18n.unavailable, ''));
      if (help) help.textContent = `${error.message} ${i18n.retry}`;
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
  const profitLabelText = scenario.dataset.profitLabel || (isHi ? 'अनुमानित शुद्ध लाभ' : 'Estimated net return');
  const lossLabelText = scenario.dataset.lossLabel || (isHi ? 'अनुमानित शुद्ध हानि' : 'Estimated net loss');
  const currency = value => `₹${new Intl.NumberFormat(isHi ? 'hi-IN' : 'en-IN', {maximumFractionDigits: 0}).format(value)}`;

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
      ? i18n.areaExceeded : '');
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
    profitLabel.textContent = net < 0 ? lossLabelText : profitLabelText;
    profitOutput.classList.toggle('value-negative', net < 0);
    profitOutput.classList.toggle('value-positive', net >= 0);
  };
  priceSlider.addEventListener('input', updateScenario);
  areaInput.addEventListener('input', updateScenario);
  updateScenario();
})();
