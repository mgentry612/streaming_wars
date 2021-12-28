import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import 'react-dropdown/style.css';

function AddDemoGroup(props) {
    const [loading, setLoading] = useState(false);
    const demoGroupShortName = useFormInput('');
    const demoGroupLongName = useFormInput('');
    const demoGroupSize = useFormInput('');
    const [error, setError] = useState(null);

    // handle click event of logout button
    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
    }

    const addDemoGroupLocal = () => {
        var FormData = require('form-data');
        var data = new FormData();
        data.append('short_name', demoGroupShortName.value);
        data.append('long_name', demoGroupLongName.value);
        data.append('num_accounts', demoGroupSize.value);
        data.append('x-loggedin-user', getToken());
        
        var config = {
            method: 'post',
            url: 'http://18.117.138.183:8080/demographic',
            data : data
        };

        axios(config).then(response => {
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
            Add Demographic Group<br /><br />
            <div>
                Demographic Group Name<br />
                <input type="text" {...demoGroupLongName} />
            </div>
            <div style={{ marginTop: 10 }}>
                Demographic Group Short Name<br />
                <input type="text" {...demoGroupShortName} />
            </div>
            <div style={{ marginTop: 10 }}>
                Number of Accounts<br />
                <input type="text" {...demoGroupSize} />
            </div>


            <input type="button" onClick={addDemoGroupLocal} value="Done" />
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

export default AddDemoGroup;
