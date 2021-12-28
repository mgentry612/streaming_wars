import {removeUserSession, setUserSession} from './Utils/Common';
import React, { useState } from 'react';
import axios from 'axios';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';
import { getToken, getRole } from './Utils/Common';

function MonthlyRevenue (props)
{
    const [loading, setLoading] = useState(false);
    const studioName = useFormInput('');
    const streamingServiceName = useFormInput('');
    const [error, setError] = useState(null);
    const options = [
        'productionOne', 'productionTwo', 'productionThree'
    ];
    let [productionGroup, setProdGroup] = useState()
    let [revenue, setRevenue] = useState()

    const [studioitems,setStudioItems] = useState([]);
    const [items,setItems] = useState([]);
    let [streamingGroup, setStreamingGroup] = useState()

    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
      }

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
                    setStudioItems(body.data.map((streamingGroup) => ({
                        shortName: streamingGroup.shortName,
                        longName: streamingGroup.longName
                    })));
                }
                // setLoading(false);
            }).catch(error => {
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
    },[streamingGroup])

    const getStudioRevenueLocal = () => {
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/report/studio?name='+productionGroup+'&x-loggedin-user='+getToken(),
            headers: { }
        };

        axios(config).then(response => {
            setLoading(false);
            setRevenue(response)

        }).catch(error => {
            setLoading(false);
            setError("Something went wrong. Please try again later.");
        });
    }

    let handleShowProductionGroups = (e) => {
        setProdGroup(e.currentTarget.value)
    }

    let handleShowStreaming = (e) => {
        setStreamingGroup(e.currentTarget.value)
    }

    const getStreamingServiceRevenueLocal = () => {
        var config = {
            method: 'get',
            url: 'http://18.117.138.183:8080/report/streaming_service?name='+streamingGroup+'&x-loggedin-user='+getToken(),
            headers: { }
        };

        axios(config).then(response => {
                setLoading(false);
            setRevenue(response)
            }).catch(error => {
                setLoading(false);
                setError("Something went wrong. Please try again later.");
            });
        
        }

        function ServiceRevenue(){
            if(getRole() === "admin"){
                return (
                    <div style={{ marginTop: 10 }}>
                        Streaming Service<br />
                        <select onChange={handleShowStreaming} disabled={loading}>
                            <option value="Select a streaming service"> -- Select a streaming service -- </option>
                            {items.map((streamingGroup) => <option value={streamingGroup.shortName}>{streamingGroup.longName}</option>)}
                        </select>
                        <input type="button" onClick={getStreamingServiceRevenueLocal} value="Submit" />
                    </div>
                    );
            }
            else{
                return null
            }
        }

        function ActualRevenue() {
            if(revenue && revenue.data) {
                return (<div>
                    <div>{"Current period revenue: " + revenue.data.currentPeriodRevenue}</div>
                    <div>{"Previous Month Revenue: " + revenue.data.previousMonthRevenue}</div>
                    <div>{"Total revenue: " + revenue.data.totalRevenue}</div>
                    <div>{"Licensing : " + revenue.data.licensing}</div>
                </div>);
            } else{
                return null;
            }
        }

return (
    <div>
      Monthly Revenue<br /><br />
        <div>
            Revenue for Studio<br />
            <select onChange={handleShowProductionGroups}>
                <option value="Select a production group️"> -- Select a production group -- </option>
                {studioitems.map((productionGroup) => <option value={productionGroup.shortName}>{productionGroup.longName}</option>)}
            </select>
        </div>
        <input type="button" onClick={getStudioRevenueLocal} value="Submit" />
        <div><ServiceRevenue/></div>
        <div><ActualRevenue/></div>
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

export default MonthlyRevenue;
