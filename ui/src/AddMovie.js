import {getToken, removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';

function AddMovie(props) {
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

  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

  const addMovieLocal = () => {
      var FormData = require('form-data');
      var data = new FormData();
      data.append('name', movieName.value);
      data.append('year_produced', movieYear.value);
      data.append('duration', movieDuration.value);
      data.append('studio_name', productionGroup);
      data.append('license_fee', licenseFee.value);
      data.append('type', content);
      data.append('x-loggedin-user', getToken());

      var config = {
          method: 'post',
          url: 'http://18.117.138.183:8080/event',
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

    let handleShowProductionGroups = (e) => {
        setProdGroup(e.currentTarget.value)
    }

    let handleContentSelection = (e) => {
        setContent(e.currentTarget.value)
    }


  return (
    <div>
      Add Content<br /><br />
        <div>
            Content Name<br />
            <input type="text" {...movieName} />
        </div>
        <div style={{ marginTop: 10 }}>
            Content Release Year<br />
            <input type="number" {...movieYear} />
        </div>
        <div style={{ marginTop: 10 }}>
            Content Duration<br />
            <input type="number" {...movieDuration} />
        </div>
        <div style={{ marginTop: 10 }}>
            License fee<br />
            <input type="number" {...licenseFee} />
        </div>
        <div style={{ marginTop: 10 }}>
            Genre<br />
            <input type="text" {...movieGenre} />
        </div>
        <div style={{ marginTop: 10 }}>
            Content Type <br />
            <select onChange={handleContentSelection}>
                <option value="Select a content type️"> -- Select a content type -- </option>
                {contentTypes.map((contentType) => <option value={contentType.name}>{contentType.name}</option>)}
            </select>
        </div>
        <div style={{ marginTop: 10 }}>
            Production Studio group <br />
            <select onChange={handleShowProductionGroups}>
                <option value="Select a production group️"> -- Select a production group -- </option>
                {items.map((streamingGroup) => <option value={streamingGroup.shortName}>{streamingGroup.longName}</option>)}
            </select>
        </div>
        <input type="button" onClick={addMovieLocal} value="Done" />
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

export default AddMovie;
