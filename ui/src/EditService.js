import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function EditService(props) {
    const [loading, setLoading] = useState(false);
    const serviceName = useFormInput('');
    const serviceShortName = useFormInput('');
    const serviceSubscription = useFormInput('');
    const [error, setError] = useState(null);
    const [items,setItems] = useState([]);
    let [streamingGroup, setStreamingGroup] = useState()


    const [selectedEventName,setSelectedEventName] = useState([]);

    const [selectedEventShortName,setSelectedEventShortName] = useState([]);

    const [selectedEventSubscription,setSelectedEventSubscription] = useState([]);

  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

    React.useEffect(() => {
        let unmounted = false;
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/streaming_services?x-loggedin-user='+getToken(),
            headers: { }
        };
        async function getStreamingServices() {
            axios(config).then(response => {
                const body = response
                if (!unmounted) {
                    setItems(body.data.map((streamingGroup) => ({
                        shortName: streamingGroup.shortName,
                        longName: streamingGroup.longName,
                        uniqueID: streamingGroup.shortName+'|'+streamingGroup.longName+'|'+streamingGroup.subscriptionPrice
                    })));
                }
                setLoading(false);

            }).catch(error => {
            });
        }
        getStreamingServices()
        return () => {
            unmounted = true;
        };
    },[streamingGroup])

  const updateServiceLocal = () => {
      var FormData = require('form-data');
      var data = new FormData();
      data.append('long_name', selectedEventName);
      data.append('short_name', selectedEventShortName);
      data.append('subscription_price', selectedEventSubscription);
      data.append('x-loggedin-user',  getToken());

      var config = {
          method: 'put',
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

    let handleShowStreaming = (e) => {
        setStreamingGroup(e.currentTarget.value)
        var streamingData = e.currentTarget.value.split('|')
        setSelectedEventShortName(streamingData[0])
        setSelectedEventName(streamingData[1])
        setSelectedEventSubscription(streamingData[2])
    }

    let handleNameChange = (e) => {
        setSelectedEventName(e.currentTarget.value)
    }

    let handleShortNameChange = (e) => {
        setSelectedEventShortName(e.currentTarget.value)
    }

    let handleFeeChange = (e) => {
        setSelectedEventSubscription(e.currentTarget.value)
    }

  return (
    <div>
      Add Service<br /><br />
        <div>
            Service Name<br />
            <input type="text" {...serviceName}  value={selectedEventName} onChange={handleNameChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            Service Short Name<br />
            <input type="text" {...serviceShortName}  value={selectedEventShortName} onChange={handleShortNameChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            Service Subscription<br />
            <input type="number" {...serviceSubscription}  value={selectedEventSubscription} onChange={handleFeeChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            Streaming Service<br />
            <select onChange={handleShowStreaming} disabled={loading}>
                <option value="Select a streaming service"> -- Select a streaming service -- </option>
                {items.map((streamingGroup) => <option value={streamingGroup.uniqueID}>{streamingGroup.longName}</option>)}
            </select>
        </div>
        <input type="button" onClick={updateServiceLocal} value="Update" />
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

export default EditService;
