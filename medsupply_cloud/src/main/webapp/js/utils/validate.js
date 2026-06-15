function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
}

function validatePassword(password) {
  return password && password.length >= 6;
}

function validateRequired(value) {
  return value !== null && value !== undefined && value !== "";
}

function validateNumber(value, min = 0, max = Infinity) {
  const num = parseFloat(value);
  return !isNaN(num) && num >= min && num <= max;
}

function validatePhone(phone) {
  const re = /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,4}[-\s.]?[0-9]{1,9}$/;
  return re.test(phone);
}

function validateForm(formData, rules) {
  const errors = {};
  
  for (const field in rules) {
    const value = formData[field];
    const fieldRules = rules[field];
    
    for (const rule of fieldRules) {
      if (rule.required && !validateRequired(value)) {
        errors[field] = `${rule.label || field} est requis`;
        break;
      }
      
      if (rule.email && value && !validateEmail(value)) {
        errors[field] = `${rule.label || field} n'est pas un email valide`;
        break;
      }
      
      if (rule.password && value && !validatePassword(value)) {
        errors[field] = `${rule.label || field} doit contenir au moins 6 caractères`;
        break;
      }
      
      if (rule.phone && value && !validatePhone(value)) {
        errors[field] = `${rule.label || field} n'est pas un numéro valide`;
        break;
      }
      
      if (rule.number !== undefined && value && !validateNumber(value, rule.min, rule.max)) {
        errors[field] = `${rule.label || field} doit être entre ${rule.min} et ${rule.max}`;
        break;
      }
      
      if (rule.minLength && value && value.length < rule.minLength) {
        errors[field] = `${rule.label || field} doit contenir au moins ${rule.minLength} caractères`;
        break;
      }
      
      if (rule.maxLength && value && value.length > rule.maxLength) {
        errors[field] = `${rule.label || field} ne doit pas dépasser ${rule.maxLength} caractères`;
        break;
      }
    }
  }
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
}
