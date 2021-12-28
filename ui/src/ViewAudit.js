import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState,useRef } from 'react';
import axios from 'axios';
import 'react-dropdown/style.css';

function ViewAudit(props) {
    const [loading, setLoading] = useState(false);
    const roleName = useFormInput('');
    const doFileDownload = useRef(null);
    const [auditLogs,setAuditLogs] = useState([]);
    const [fileDownloadUrl,setFileDownlodUrl] = useState();

    // handle click event of logout button
    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
    }

    const onButtonClick = () => {
        // `current` points to the mounted text input element
        console.log("Donwloading file "+fileDownloadUrl);
    };

    const updateAuditLogs = () => {
        setAuditLogs("Getting data ...");
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/audit_logs?x-loggedin-user='+roleName.value+'&username='+getToken(),
            headers: { }
        };
        let output;
        axios(config)
            .then(function (response) {
                output = JSON.stringify(response)
                setAuditLogs("Success");
                const blob = new Blob([output]);
                const fileDownloadUrl = URL.createObjectURL(blob);
                console.log(fileDownloadUrl)

                setFileDownlodUrl(fileDownloadUrl);

                doFileDownload.current.click();
                URL.revokeObjectURL(fileDownloadUrl);
                setFileDownlodUrl("")

                // this.setState ({fileDownloadUrl: fileDownloadUrl},
                //     () => {
                //         URL.revokeObjectURL(fileDownloadUrl);  // free up storage--no longer needed.
                //         this.setState({fileDownloadUrl: ""})
                //     })
            })
            .catch(function (error) {
                console.log(error);
                setAuditLogs("Failure");
            });
    }


    return (
        <div>
            Role details<br /><br />
            <div>
                Role name<br />
                <input type="text" {...roleName}/>
            </div>
            <div>
                 <label>Status: </label>
                 <input type="textarea"
                name="textValue"
                value={auditLogs}
                />
            </div>
            <a style={{display: "none"}}
               download="audit_logs.json"
               href={fileDownloadUrl}
               ref={doFileDownload}
            >download it</a>
            <input type="button" onClick={updateAuditLogs} value="Show logs" />
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

export default ViewAudit;
