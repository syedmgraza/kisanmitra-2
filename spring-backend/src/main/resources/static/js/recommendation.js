(() => {
  const chart = document.querySelector('#profit-chart');
  const detailSelect = document.querySelector('#crop-detail-select');
  const detailStats = document.querySelector('#crop-detail-stats');
  const isHi = document.documentElement.lang === 'hi';
  const money = value => Number.isFinite(Number(value))
    ? `₹${new Intl.NumberFormat(isHi ? 'hi-IN' : 'en-IN', {maximumFractionDigits: 0}).format(Number(value))}`
    : (isHi ? 'उपलब्ध नहीं' : 'Not available');

  if (chart) {
    const rows = [...chart.querySelectorAll('.profit-chart-row')];
    const profits = rows.map(row => Number(row.dataset.profit)).filter(Number.isFinite);
    const max = Math.max(1, ...profits.map(Math.abs));
    rows.forEach(row => {
      const value = Number(row.dataset.profit);
      const fill = row.querySelector('.profit-chart-fill');
      row.classList.toggle('is-negative', value < 0);
      fill.style.width = `${Math.max(1.5, Math.abs(value) / max * 47)}%`;
    });
  }

  if (!detailSelect || !detailStats) return;
  const ds = detailSelect.dataset;
  const lblYield = ds.labelYield || (isHi ? 'उपज (टन/हे.)' : 'Predicted yield');
  const lblPrice = ds.labelPrice || (isHi ? 'संदर्भ भाव (₹/क्विंटल)' : 'Reference crop price');
  const lblCost = ds.labelCost || (isHi ? 'लागत (₹/हेक्टेयर)' : 'Estimated cost');
  const lblProfit = ds.labelProfit || (isHi ? 'शुद्ध लाभ / हानि' : 'Estimated net / ha');
  const lblBand = ds.labelBand || (isHi ? 'उपज श्रेणी' : 'Yield band');
  const lblConf = ds.labelConf || (isHi ? 'मॉडल विश्वसनीयता' : 'Model confidence');
  const confStd = ds.confStd || (isHi ? 'मानक (Standard)' : 'Standard');
  const confLtd = ds.confLtd || (isHi ? 'सीमित (Limited)' : 'Limited');
  const effortSuffix = isHi ? 'परिश्रम' : 'effort';

  const updateDetails = () => {
    const option = detailSelect.selectedOptions[0];
    if (!option) return;
    const entries = [
      [lblYield, `${Number(option.dataset.yield).toFixed(2)} ${isHi ? 'टन/हेक्टेयर' : 't/ha'}`],
      [lblPrice, `${money(option.dataset.price)} ${isHi ? '/ क्विंटल' : '/ quintal'}`],
      [lblCost, `${money(option.dataset.cost)} ${isHi ? '/ हेक्टेयर' : '/ ha'}`],
      [lblProfit, `${money(option.dataset.profit)} · ${option.dataset.effort || (isHi ? 'विविध' : 'Varies')} ${effortSuffix}`],
      [lblBand, option.dataset.performance || (isHi ? 'उपलब्ध नहीं' : 'Unavailable')],
      [lblConf, option.dataset.confidence === 'Low' ? confLtd : confStd]
    ];
    detailStats.replaceChildren(...entries.map(([label, value]) => {
      const card = document.createElement('article');
      const title = document.createElement('small');
      const output = document.createElement('b');
      title.textContent = label;
      output.textContent = value;
      card.append(title, output);
      return card;
    }));
  };
  detailSelect.addEventListener('change', updateDetails);
  updateDetails();
})();
