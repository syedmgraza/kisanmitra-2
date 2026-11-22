(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const targets = document.querySelectorAll(
    '.hero-panel, .page-intro, .section-heading, .module-card, .market-toolbar, .market-results-head, .listing-card, .weather-board, .yield-result, .yield-layout, .results-section, .crop-plan-create, .crop-plan-card, .guidance-note, .empty-panel, .detail-card, .auth-card'
  );
  if (!targets.length) return;

  if (!('IntersectionObserver' in window)) return;
  const observer = new IntersectionObserver((entries, currentObserver) => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('is-visible');
      currentObserver.unobserve(entry.target);
    });
  }, {threshold: 0.08, rootMargin: '0px 0px -28px 0px'});

  targets.forEach((element, index) => {
    element.classList.add('motion-reveal');
    element.style.setProperty('--reveal-delay', `${Math.min(index % 6, 5) * 45}ms`);
    observer.observe(element);
  });
})();