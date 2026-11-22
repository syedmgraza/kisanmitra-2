(() => {
  const stateSelect = document.querySelector('#state');
  const districtSelect = document.querySelector('#district');
  const districtHelp = document.querySelector('#district-help');
  if (!stateSelect || !districtSelect) return;

  const isHi = document.documentElement.lang === 'hi';
  const chooseDistrictText = isHi ? 'अपना ज़िला चुनें' : 'Choose your district';
  const chooseStateFirstText = isHi ? 'पहले राज्य चुनें' : 'Choose a state first';
  const helpWithDistricts = isHi
    ? 'ज़िले फसल इतिहास डेटा के अनुसार उपलब्ध हैं। कुछ सीज़नों में रिकॉर्ड सीमित हो सकते हैं।'
    : 'Districts are limited to locations in the crop history data. Some seasons may still have no records.';
  const helpNoDistricts = isHi
    ? 'फसल इतिहास डेटा में उपलब्ध ज़िलों को देखने के लिए पहले एक राज्य चुनें।'
    : 'Choose a state to see districts available in the crop history data.';
  const errorText = isHi
    ? 'ज़िला सूची लोड नहीं हो सकी। कृपया पृष्ठ पुनः लोड कर प्रयास करें।'
    : 'Could not load the district list. Refresh the page and try again.';

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
        addOption(stateSelect.value ? chooseDistrictText : chooseStateFirstText, '');
        districts.forEach(district => addOption(district, district));
        districtSelect.disabled = districts.length === 0;
        districtSelect.value = districts.includes(selectedDistrict) ? selectedDistrict : '';
        if (districtHelp) {
          districtHelp.textContent = districts.length ? helpWithDistricts : helpNoDistricts;
        }
      };

      const savedDistrict = districtSelect.dataset.selected || '';
      populateDistricts(savedDistrict);
      stateSelect.addEventListener('change', () => populateDistricts(''));
    })
    .catch(() => {
      districtSelect.disabled = true;
      if (districtHelp) districtHelp.textContent = errorText;
    });
})();
