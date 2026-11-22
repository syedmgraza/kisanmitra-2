(() => {
  document.querySelectorAll('.field-log-form').forEach(form => {
    const type = form.querySelector('.activity-type');
    const rainfallFields = form.querySelector('.activity-rainfall-fields');
    const rainfallInput = rainfallFields?.querySelector('input');
    const inputFields = form.querySelector('.activity-input-fields');
    const inputName = inputFields?.querySelector('input[name="inputName"]');
    if (!type || !rainfallFields || !inputFields) return;

    const update = () => {
      const isRainfall = type.value === 'RAINFALL';
      const isInput = type.value === 'FERTILIZER' || type.value === 'PESTICIDE';
      rainfallFields.hidden = !isRainfall;
      inputFields.hidden = !isInput;
      rainfallInput.required = isRainfall;
      inputName.required = isInput;
    };
    type.addEventListener('change', update);
    update();
  });
})();
