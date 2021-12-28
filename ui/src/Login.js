import React, { useState } from 'react';
import axios from 'axios';
import { setUserSession } from './Utils/Common';

function Login(props) {
  const [loading, setLoading] = useState(false);
  const username = useFormInput('');
  const password = useFormInput('');
  const [error, setError] = useState(null);

  // handle button click of login form
  const handleLogin = () => {
    setError(null);
    setLoading(true);
    axios.post('http://18.117.138.183:8080/users/signin', { username: username.value, password: password.value }).then(response => {
      setLoading(false);
      setUserSession(response.data.username, response.data.roles);

      switch (response.data.roles) {
        case "creator":{
          props.history.push('/creator');
          break;
        }
        case "admin":{
          props.history.push('/dashboard');
          break;
        }
        case "consumer":{
          props.history.push('/consumer');
          break;
        }

      }

    }).catch(error => {
      setLoading(false);
      setUserSession("123232", "sarnab");
      var role = "admin"
      switch (role) {
        case "creator":{
          props.history.push('/creator');
          break;
        }
        case "admin":{
          props.history.push('/dashboard');
          break;
        }
        case "consumer":{
          props.history.push('/consumer');
          break;
        }

      }
      setError("Something went wrong. Please try again later.");
    });
  }

  return (
    <div>
      Login<br /><br />
      <div>
        Username<br />
        <input type="text" {...username} autoComplete="new-password" />
      </div>
      <div style={{ marginTop: 10 }}>
        Password<br />
        <input type="password" {...password} autoComplete="new-password" />
      </div>
      {error && <><small style={{ color: 'red' }}>{error}</small><br /></>}<br />
      <input type="button" value={loading ? 'Loading...' : 'Login'} onClick={handleLogin} disabled={loading} /><br />
    </div>
  );
}

const useFormInput = initialValue => {
  const [value, setValue] = useState(initialValue);

  const handleChange = e => {
    setValue(e.target.value);
  }
  return {
    value,
    onChange: handleChange
  }
}

export default Login;