import React from 'react';
import { getUser, removeUserSession } from './Utils/Common';

function Dashboard(props) {
  const user = getUser();

  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

  const addMovie = () => {
      props.history.push('/addMovie')
  }

  const addCreator = () => {
      props.history.push('/addCreator')
  }

  const editService = () => {
      props.history.push('/editService')
  }

    const addService = () => {
        props.history.push('/addService')
    }


  const addDemoGroup =() =>{
      props.history.push('/addDemoGroup')  
  }
  const updateDemoGroup =() =>{
    props.history.push('/updateDemoGroup')
  }

    const viewRevenue = () => {
        props.history.push('/viewRevenue')
    }

    const viewAuditEvent = () => {
        props.history.push('/viewAudit')
    }

  return (
    <div>
      Welcome {user.name}!<br /><br />
      <input type="button" onClick={addCreator} value="Add new Creator"/>
      <input type="button" onClick={addService} value="Add new Service"/>
        <input type="button" onClick={editService} value="Edit Service"/>
      <input type="button" onClick={addMovie} value="Add new Content"/>
        <input type="button" onClick={viewRevenue} value="View Revenue"/>
      <input type="button" onClick={addDemoGroup} value="Add new Demographic Group"/>
        <input type="button" onClick={viewAuditEvent} value="View Audit events"/>
      <input type="button" onClick={updateDemoGroup} value="Update Demographic Group"/>
      <input type="button" onClick={handleLogout} value="Logout" />
    </div>
  );
}

export default Dashboard;
