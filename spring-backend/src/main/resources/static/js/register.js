(() => {
  const stateSelect = document.querySelector('#state');
  const districtSelect = document.querySelector('#district');
  const districtHelp = document.querySelector('#district-help');
  if (!stateSelect || !districtSelect) return;

  const addOption = (label, value) => {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = label;
    districtSelect.append(option);
  };

  fetch('/data/districts.json')
    .then(response => {
      if (!response.ok) throw new Error('District list request failed.');
      return response.json();
    })
    .then(districtsByState => {
      const populateDistricts = selectedDistrict => {
        const districts = districtsByState[stateSelect.value] || [];
        districtSelect.replaceChildren();
        addOption(stateSelect.value ? 'Choose your district' : 'Choose a state first', '');
        districts.forEach(district => addOption(district, district));
        districtSelect.disabled = districts.length === 0;
        districtSelect.value = districts.includes(selectedDistrict) ? selectedDistrict : '';
        if (districtHelp) {
          districtHelp.textContent = districts.length
            ? 'Districts are limited to locations in the crop history data. Some seasons may still have no records.'
            : 'Choose a state to see districts available in the crop history data.';
        }
      };

      const savedDistrict = districtSelect.dataset.selected || '';
      populateDistricts(savedDistrict);
      stateSelect.addEventListener('change', () => populateDistricts(''));
    })
    .catch(() => {
      districtSelect.disabled = true;
      if (districtHelp) districtHelp.textContent = 'Could not load the district list. Refresh the page and try again.';
    });
})();
