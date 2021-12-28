import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function EditContent(props) {
    const [loading, setLoading] = useState(false);
    const movieName = useFormInput('');
    const movieYear = useFormInput('');
    const movieDuration = useFormInput('');
    const licenseFee = useFormInput('');
    const movieGenre = useFormInput('');
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
    var initialProdGroups = options;

    const contentTypes = [{id:1,name:"movie"},{id:2,name:"ppv"}];

    let [productionGroup, setProdGroup] = useState()

    const [items,setItems] = useState([]);

    let [content,setContent] = useState()

    const [events,setEvents] = useState([]);

    const [selectedEvent,setSelectedEvent] = useState([]);

    const [selectedEventName,setSelectedEventName] = useState([]);

    const [selectedEventYear,setSelectedEventYear] = useState([]);

    const [selectedEventDuration,setSelectedEventDuration] = useState([]);

    const [selectedEventLicenseFee,setSelectedEventLicenseFee] = useState([]);

    const [selectedEventType,setSelectedEventType] = useState([]);

  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

  const updateMovieLocal = () => {
      var FormData = require('form-data');
      var data = new FormData();
      data.append('name', selectedEventName);
      data.append('year_produced', selectedEventYear);
      data.append('duration', selectedEventDuration);
      data.append('studio_name', productionGroup);
      data.append('license_fee', selectedEventLicenseFee);
      data.append('type', content);
      data.append('x-loggedin-user', getToken());

      var config = {
          method: 'put',
          url: 'http://18.117.138.183:8080/event',
          data : data
      };

      axios(config).then(response => {
          setLoading(false);
          props.history.push('/creator');
      }).catch(error => {
          setLoading(false);
          setError("Something went wrong. Please try again later.");
      });
  }
    let unmounted = false;

  React.useEffect(() => {
      var config = {
          method: 'get',
          url: 'http://18.117.138.183:8080/studios?x-loggedin-user='+getToken(),
          headers: { }
      };
      async function getProductionGroups() {
          axios(config).then(response => {
              const body = response
              if (!unmounted) {
                  setItems(body.data.map((streamingGroup) => ({
                      shortName: streamingGroup.shortName,
                      longName: streamingGroup.longName
                  })));
              }
              // setLoading(false);
          }).catch(error => {
              setItems(options)
          });
      }
      getProductionGroups()
      return () => {
          unmounted = true;
      };
  },[productionGroup])

    React.useEffect(() => {
        let unmounted = false;
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/event/studio?studio='+productionGroup+'&x-loggedin-user='+getToken(),
            headers: { }
        };

        async function getEvents() {
            axios(config).then(response => {
                const body = response
                if (!unmounted) {
                    setEvents(body.data.map((content) => ({
                        name: content.name,
                        yearProduced: content.yearProduced,
                        uniqueId: content.name+"|"+content.yearProduced+"|"+content.duration+"|"+content.licenseFee+"|"+content.type
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
    },[productionGroup])

    let handleShowProductionGroups = (e) => {
        setProdGroup(e.currentTarget.value)
    }

    let handleContentSelection = (e) => {
        setContent(e.currentTarget.value)
    }

    let handleEventSelection = (e) => {
        setSelectedEvent(e.currentTarget.value)
        var eventData = e.currentTarget.value.split('|')
        var eventName = eventData[0]
        var eventYear = eventData[1]
        var eventDuration = eventData[2]
        var eventFee = eventData[3]
        var eventType = eventData[4]
        setSelectedEventName(eventName)
        setSelectedEventDuration(eventDuration)
        setSelectedEventLicenseFee(eventFee)
        setSelectedEventYear(eventYear)
        setSelectedEventType(eventType)
    }

    let handleNameChange = (e) => {
        setSelectedEventName(e.currentTarget.value)
    }

    let handleYearChange = (e) => {
        setSelectedEventYear(e.currentTarget.value)
    }

    let handleDurationChange = (e) => {
        setSelectedEventDuration(e.currentTarget.value)
    }

    let handleFeeChange = (e) => {
        setSelectedEventLicenseFee(e.currentTarget.value)
    }


  return (
    <div>
      Add Content<br /><br />
        <div>
            Content Name<br />
            <input type="text" {...movieName} value={selectedEventName} onChange={handleNameChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            Content Release Year<br />
            <input type="number" {...movieYear} value={selectedEventYear} onChange={handleYearChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            Content Duration<br />
            <input type="number" {...movieDuration} value={selectedEventDuration} onChange={handleDurationChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            License fee<br />
            <input type="number" {...licenseFee} value={selectedEventLicenseFee} onChange={handleFeeChange}/>
        </div>
        <div style={{ marginTop: 10 }}>
            Content Type <br />
            <select onChange={handleContentSelection}>
                <option value="Select a content type️"> -- Select a content type -- </option>
                {contentTypes.map((contentType) => <option value={contentType.name}>{contentType.name}</option>)}
            </select>
        </div>
        <div style={{ marginTop: 10 }}>
            Events<br />
            <select onChange={handleEventSelection} disabled={loading}>
                <option value="Select the event"> -- Select an event -- </option>
                {events.map((eventGroup) => <option value={eventGroup.uniqueId}>{eventGroup.name} {eventGroup.yearProduced}</option>)}
            </select>
        </div>
        <div style={{ marginTop: 10 }}>
            Production Studio group <br />
            <select onChange={handleShowProductionGroups}>
                <option value="Select a production group️"> -- Select a production group -- </option>
                {items.map((streamingGroup) => <option value={streamingGroup.shortName}>{streamingGroup.longName}</option>)}
            </select>
        </div>
        <input type="button" onClick={updateMovieLocal} value="Update" />
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

export default EditContent;
