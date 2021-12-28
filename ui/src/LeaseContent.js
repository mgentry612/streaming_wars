import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function LeaseContent (props)
{
    const [loading, setLoading] = useState(false);
    const eventName = useFormInput('');
    const eventYear = useFormInput('');
    const eventPrice = useFormInput('');
    const [error, setError] = useState(null);
    const options = [
        {
            "shortName": "disney",
            "longName": "Walt Disney Animation Studios",
            "events": []
        },
        {
            "shortName": "\"disney\"",
            "longName": "\"Walt Disney Animation Studios\"",
            "events": []
        },
        {
            "shortName": "ting",
            "longName": "testing",
            "events": []
        }
    ];
    const [items,setItems] = useState([]);
    const [studios,setStudios] = useState([]);
    const [selectedStudio,setSelectedStudio] = useState([]);
    const [events,setEvents] = useState([]);
    var initialStreaming = options;

    let [streamingGroup, setStreamingGroup] = useState()
    let [content,setContent] = useState()
    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
      }
      const LeaseContentLocal = () => {
          var FormData = require('form-data');
          if(content) {
              var contentData = content.split('|');
              var data = new FormData();
              data.append('streaming_service_name', streamingGroup);
              if (contentData[3] === "ppv") {
                  data.append('ppv_name', contentData[0]);
              } else {
                  data.append('movie_name', contentData[0]);
              }
              data.append('year_produced', contentData[1]);
              if (contentData[3] === "ppv") {
                  data.append('viewing_price', contentData[2]);
              }
              data.append('x-loggedin-user', getToken());

              var ppvConfig = {
                  method: 'post',
                  url: 'http://18.117.138.183:8080/offer_ppv',
                  data: data
              };

              var movieConfig = {
                  method: 'post',
                  url: 'http://18.117.138.183:8080/offer_movie',
                  data: data
              };

              var config = movieConfig
              if (contentData[3] === "ppv") {
                  config = ppvConfig
              }
              axios(config).then(response => {
                  setLoading(false);
                  props.history.push("/creator");
              }).catch(error => {
                  setLoading(false);
                  setError("Something went wrong. Please try again later.");
              });
          } else{
              props.history.push("/creator");
          }
    }
    React.useEffect(() => {
        let unmounted = false;
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/event/studio?studio='+selectedStudio+'&x-loggedin-user='+getToken(),
            headers: { }
        };

        async function getEvents() {
            axios(config).then(response => {
                const body = response
                if (!unmounted) {
                    setEvents(body.data.map((content) => ({
                        name: content.name,
                        yearProduced: content.yearProduced,
                        uniqueId: content.name+"|"+content.yearProduced+"|"+content.licenseFee+"|"+content.type
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
    },[selectedStudio])

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
                setItems(options)
            });
        }
        getStreamingServices()
        return () => {
            unmounted = true;
        };
    },[selectedStudio])

    React.useEffect(() => {
        let unmounted = false;
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/studios?x-loggedin-user='+getToken(),
            headers: { }
        };
        async function getProductionGroups() {
            axios(config).then(response => {
                const body = response
                if (!unmounted) {
                    setStudios(body.data.map((streamingGroup) => ({
                        shortName: streamingGroup.shortName,
                        longName: streamingGroup.longName
                    })));
                }
                setLoading(false);
            }).catch(error => {
                setStudios(options)
            });
        }
        getProductionGroups()
        return () => {
            unmounted = true;
        };
    },[selectedStudio])

    let handleShowStreaming = (e) => {
        setStreamingGroup(e.currentTarget.value)
    }

    let handleContentSelection = (e) => {
        setContent(e.currentTarget.value)
    }

    let handleStudioSelection = (e) => {
        setSelectedStudio(e.currentTarget.value)
    }

return (
    <div>
      Lease Event<br /><br />
        <div style={{ marginTop: 10 }}>
            Events<br />
            <select onChange={handleContentSelection} disabled={loading}>
                <option value="Select the event"> -- Select an event -- </option>
                {events.map((eventGroup) => <option value={eventGroup.uniqueId}>{eventGroup.name} {eventGroup.yearProduced}</option>)}
            </select>
        </div>
        <div style={{ marginTop: 10 }}>
            Streaming Service<br />
            <select onChange={handleShowStreaming} disabled={loading}>
                <option value="Select a streaming service"> -- Select a streaming service -- </option>
                {items.map((streamingGroup) => <option value={streamingGroup.shortName}>{streamingGroup.longName}</option>)}
            </select>
        </div>
        <div style={{ marginTop: 10 }}>
            Studio Selection<br />
            <select onChange={handleStudioSelection} disabled={loading}>
                <option value="Select a studio"> -- Select a studio -- </option>
                {studios.map((studioGroup) => <option value={studioGroup.shortName}>{studioGroup.longName}</option>)}
            </select>
        </div>
        <input type="button" onClick={LeaseContentLocal} value="Submit" />
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

export default LeaseContent;
