function showLoader() {
  let loader = document.getElementById("global-loader");
  if (!loader) {
    loader = document.createElement("div");
    loader.id = "global-loader";
    loader.className = "fixed inset-0 bg-white bg-opacity-90 flex items-center justify-center z-50";
    loader.innerHTML = `
      <div class="flex flex-col items-center">
        <div class="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
        <p class="mt-4 text-gray-600">Chargement...</p>
      </div>
    `;
    document.body.appendChild(loader);
  }
  loader.classList.remove("hidden");
}

function hideLoader() {
  const loader = document.getElementById("global-loader");
  if (loader) {
    loader.classList.add("hidden");
  }
}
