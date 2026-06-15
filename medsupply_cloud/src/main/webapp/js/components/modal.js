function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove("hidden");
    modal.classList.add("flex");
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("hidden");
    modal.classList.remove("flex");
  }
}

function initModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    const closeBtn = modal.querySelector("[data-modal-close]");
    const backdrop = modal.querySelector("[data-modal-backdrop]");
    
    if (closeBtn) {
      closeBtn.addEventListener("click", () => closeModal(modalId));
    }
    
    if (backdrop) {
      backdrop.addEventListener("click", () => closeModal(modalId));
    }
  }
}
