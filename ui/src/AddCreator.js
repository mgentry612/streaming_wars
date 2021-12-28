import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import 'react-dropdown/style.css';

function AddCreator(props) {
    const [loading, setLoading] = useState(false);
    const creatorName = useFormInput('');
    const creatorShortName = useFormInput('');
    const [error, setError] = useState(null);

    // handle click event of logout button
    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
    }

    const addCreatorLocal = () => {
        var FormData = require('form-data');
        var data = new FormData();
        data.append('long_name', creatorName.value);
        data.append('short_name', creatorShortName.value);
        data.append('x-loggedin-user',  getToken());

        var config = {
            method: 'post',
            url: 'http://18.117.138.183:8080/studio',
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
            Add Creator<br /><br />
            <div>
                Creator Name<br />
                <input type="text" {...creatorName} />
            </div>
            <div style={{ marginTop: 10 }}>
                Creator short name<br />
                <input type="text" {...creatorShortName} />
            </div>

            <input type="button" onClick={addCreatorLocal} value="Done" />
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

export default AddCreator;
