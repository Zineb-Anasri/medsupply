function showToast(message, type = "info") {
  const colors = {
    success: "bg-green-500",
    error: "bg-red-500", 
    warning: "bg-amber-500",
    info: "bg-blue-500"
  };
  const icons = {
    success: "check-circle",
    error: "x-circle",
    warning: "alert-triangle",
    info: "info"
  };
  
  const toast = document.createElement("div");
  toast.className = `fixed bottom-4 right-4 z-50 px-6 py-3 rounded-xl 
    text-white font-medium shadow-lg ${colors[type]} 
    transform transition-all duration-300 translate-y-full opacity-0 flex items-center gap-3`;
  toast.innerHTML = `
    <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      ${getIconPath(icons[type])}
    </svg>
    <span>${message}</span>
  `;
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.classList.remove("translate-y-full", "opacity-0");
  }, 10);
  
  setTimeout(() => {
    toast.classList.add("translate-y-full", "opacity-0");
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function getIconPath(icon) {
  const paths = {
    "check-circle": '<path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />',
    "x-circle": '<path stroke-linecap="round" stroke-linejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />',
    "alert-triangle": '<path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />',
    "info": '<path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />'
  };
  return paths[icon] || paths["info"];
}
