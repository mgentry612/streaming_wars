import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function WatchContent (props)
{
    const [loading, setLoading] = useState(false);
    const eventName = useFormInput('');
    const eventYear = useFormInput('');
    const percentage = useFormInput('');
    const groupDemo = useFormInput('');
    const [error, setError] = useState(null);

    const groups = [];
    const [items,setItems] = useState([]);
    const [selectedDemo, setSelectedDemo] = useState([]);
    const [events,setEvents] = useState([]);

    const [streamingGroup, setStreamingGroup] = useState([]);
    const [groupDemographic, setGroupDemographic] = useState([]);
    const [content,setContent] = useState();

    const [selectedDemoName,setSelectedDemoName] = useState([]);

    const [selectedEventName,setSelectedEventName] = useState([]);

    const [selectedEventYear,setSelectedEventYear] = useState([]);

    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
      }
    const WatchContentLocal = () => {
        if(selectedDemoName) {
            var FormData = require('form-data');
            var contentData = content.split('|');
            var data = new FormData();
            data.append('demographic', selectedDemoName);
            data.append('demographic_percentage', percentage.value);
            data.append('streaming_service', streamingGroup);
            data.append('event_name', selectedEventName);
            data.append('event_year_produced', selectedEventYear);
            data.append('x-loggedin-user', getToken());

            var config = {
                method: 'post',
                url: 'http://18.117.138.183:8080/watch_event',
                data: data
            };

            axios(config).then(response => {
                setLoading(false);
                props.history.push('/consumer');
            }).catch(error => {
                setLoading(false);
                setError("Something went wrong. Please try again later.");
            });
        }
    }
    React.useEffect(() => {
        let unmounted = false;
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/demographics?x-loggedin-user='+getToken(),
            headers: { }
        };
        async function getDemographicGroups() {
            axios(config).then(response => {
                const body = response
                if (!unmounted) {
                    setGroupDemographic(body.data.map((groupDemographic) => ({
                        shortName: groupDemographic.shortName,
                        longName: groupDemographic.longName,
                        uniqueId : groupDemographic.shortName+"|"+groupDemographic.numAccounts
                    })));
                }
                setLoading(false);
            }).catch(error => {
            });
        }
        getDemographicGroups()
        return () => {
            unmounted = true;
        };
    },[])
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
                        longName: streamingGroup.longName
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
    React.useEffect(() => {
        let unmounted = false;
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/event/streaming_service?streaming_service='+streamingGroup+'&x-loggedin-user='+getToken(),
            headers: { }
        };

        async function getEvents() {
            axios(config).then(response => {
                const body = response
                if (!unmounted) {
                    setEvents(body.data.map((content) => ({
                        name: content.name,
                        yearProduced: content.yearProduced,
                        uniqueId: content.name+"|"+content.yearProduced+"|"+content.type
                    })));
                }
                setLoading(false);

            }).catch(error => {

            });
        }
        getEvents()
        return () => {
            unmounted = true;
        };
    },[streamingGroup])


    let handleShowStreaming = (e) => {
        setStreamingGroup(e.currentTarget.value)
    }

    let handleDemoGraphicSelection = (e) => {
        setSelectedDemo(e.currentTarget.value)
        var demoData = e.currentTarget.value.split('|')
        setSelectedDemoName(demoData[0])
    }

    let handleContentSelection = (e) => {
        setContent(e.currentTarget.value)
        var contentData = e.currentTarget.value.split('|')
        setSelectedEventName(contentData[0])
        setSelectedEventYear(contentData[1])
    }
return (
    <div>
      Watch Event<br /><br />
        <div>
            Event<br />
            <select onChange={handleContentSelection} disabled={loading}>
                <option value="Select the event"> -- Select an event -- </option>
                {events.map((eventGroup) => <option value={eventGroup.uniqueId}>{eventGroup.name} {eventGroup.yearProduced}</option>)}
            </select>
        </div>
        <div>
            Demographic<br />
            <select onChange={handleDemoGraphicSelection} disabled={loading}>
                <option value="Select the demographics"> -- Select a demographic -- </option>
                {groupDemographic.map((demoGroup) => <option value={demoGroup.uniqueId}>{demoGroup.longName}</option>)}
            </select>
        </div>
        <div>
            Streaming Service<br />
            <select onChange={handleShowStreaming} disabled={loading}>
                <option value="Select a streaming service"> -- Select a streaming service -- </option>
                {items.map((streamingGroup) => <option value={streamingGroup.shortName}>{streamingGroup.longName}</option>)}
            </select>
        </div>
        <div>
            Percentage <br />
            <input type="text" {...percentage} />
        </div>
        <input type="button" onClick={WatchContentLocal} value="Submit" />
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

export default WatchContent;