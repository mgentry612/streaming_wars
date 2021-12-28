import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function AddService(props) {
    const [loading, setLoading] = useState(false);
    const serviceName = useFormInput('');
    const serviceShortName = useFormInput('');
    const serviceSubscription = useFormInput('');
    const [error, setError] = useState(null);


  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

  const addServiceLocal = () => {
      var FormData = require('form-data');
      var data = new FormData();
      data.append('long_name', serviceName.value);
      data.append('short_name', serviceShortName.value);
      data.append('subscription_price', serviceSubscription.value);
      data.append('x-loggedin-user',  getToken());

      var config = {
          method: 'post',
          url: 'http://18.117.138.183:8080/streaming_service',
          data : data
      };

      axios(config).then(response => {
          setLoading(false);
          props.history.push('/dashboard');
      }).catch(error => {
          setLoading(false);
          props.history.push('/dashboard');
          setError("Something went wrong. Please try again later.");
      });
  }

  return (
    <div>
      Add Service<br /><br />
        <div>
            Service Name<br />
            <input type="text" {...serviceName} />
        </div>
        <div style={{ marginTop: 10 }}>
            Service Short Name<br />
            <input type="text" {...serviceShortName} />
        </div>
        <div style={{ marginTop: 10 }}>
            Service Subscription<br />
            <input type="number" {...serviceSubscription} />
        </div>
        <input type="button" onClick={addServiceLocal} value="Done" />
      <input type="button" onClick={handleLogout} value="Logout" />
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

export default AddService;
