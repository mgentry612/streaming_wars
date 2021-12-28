import {removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function SetupPrices (props)
{
    const [loading, setLoading] = useState(false);
    const studioName = useFormInput('');
    const eventName = useFormInput('');
    const [error, setError] = useState(null);
    const options = [
        'productionOne', 'productionTwo', 'productionThree'
    ];

    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
      }
      const PricesLocal = () => {
        axios.post('http://localhost:4000/prices', { studioName: studioName.value,
        eventName: eventName.value}).then(response => {
            setLoading(false);
            props.history.push('/dashboard');
        }).catch(error => {
            setLoading(false);
            setUserSession("123232", "sarnab");
            props.history.push('/dashboard');
            setError("Something went wrong. Please try again later.");
        });
    }

return (
    <div>
      Prices <br /><br />
        <div>
            Studio <br />
            <input type="text" {...studioName} />
        </div>
        <div>
            Event Name <br/>
            <input type="text" {...eventName} />
        </div>
        <input type="button" onClick={PricesLocal} value="Submit" />
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

export default SetupPrices;
