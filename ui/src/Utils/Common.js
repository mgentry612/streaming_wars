// return the user data from the session storage
export const getUser = () => {
  const userStr = sessionStorage.getItem('username');
  if (userStr) return userStr;
  else return null;
}

// return the token from the session storage
export const getToken = () => {
  return sessionStorage.getItem('username') || null;
}

export const getRole = () => {
  return sessionStorage.getItem('roles') || null;
}

// remove the token and user from the session storage
export const removeUserSession = () => {
  sessionStorage.removeItem('username');
  sessionStorage.removeItem('roles');
}

// set the token and user from the session storage
export const setUserSession = (username, roles) => {
  sessionStorage.setItem('username', username);
  sessionStorage.setItem('roles', roles);
}