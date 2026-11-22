(() => {
  const chart = document.querySelector('#profit-chart');
  const detailSelect = document.querySelector('#crop-detail-select');
  const detailStats = document.querySelector('#crop-detail-stats');
  const money = value => Number.isFinite(Number(value))
    ? `₹${new Intl.NumberFormat('en-IN', {maximumFractionDigits: 0}).format(Number(value))}`
    : 'Not available';

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
  const updateDetails = () => {
    const option = detailSelect.selectedOptions[0];
    if (!option) return;
    const entries = [
      ['Predicted yield', `${Number(option.dataset.yield).toFixed(2)} t/ha`],
      ['Reference crop price', `${money(option.dataset.price)} / quintal`],
      ['Estimated cost', `${money(option.dataset.cost)} / ha`],
      ['Estimated net / ha', `${money(option.dataset.profit)} · ${option.dataset.effort || 'Effort varies'} effort`],
      ['Yield band', option.dataset.performance || 'Unavailable'],
      ['Model confidence', option.dataset.confidence === 'Low' ? 'Limited' : 'Standard']
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
