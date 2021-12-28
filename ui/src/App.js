import React, { useState, useEffect } from 'react';
import { BrowserRouter, Switch, Route, NavLink } from 'react-router-dom';
import axios from 'axios';

import Login from './Login';
import Dashboard from './Dashboard';
import Home from './Home';

import PrivateRoute from './Utils/PrivateRoute';
import PublicRoute from './Utils/PublicRoute';
import { getToken, removeUserSession, setUserSession } from './Utils/Common';
import AddMovie from "./AddMovie";
import LeaseContent from "./LeaseContent"
import WatchContent from './WatchContent';
import SetupPrices from './SetupPrices';
import MonthlyRevenue from './MonthlyRevenue';
import AddCreator from "./AddCreator";
import Creator from "./Creator";
import Consumer from "./Consumer";
import AddService from "./AddService";
import AddDemoGroup from './AddDemoGroup';
import UpdateDemoGroup from './UpdateDemoGroup';
import UpdateLeaseContent from "./UpdateLeaseContent";
import EditContent from "./EditContent"
import EditService from "./EditService";
import ViewAudit from "./ViewAudit";
function App() {
  const [authLoading, setAuthLoading] = useState(true);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      return;
    }

    setAuthLoading(false);
    // axios.get(`http://localhost:4000/verifyToken?token=${token}`).then(response => {
    //   setUserSession(response.data.token, response.data.user);
    //   setAuthLoading(false);
    // }).catch(error => {
    //   removeUserSession();
    //   setAuthLoading(false);
    // });
  }, []);

  if (authLoading && getToken()) {
    return <div className="content">Checking Authentication...</div>
  }

  return (
    <div className="App">
      <BrowserRouter>
        <div>
          <div className="header">
            <NavLink exact activeClassName="active" to="/">Home</NavLink>
            <NavLink activeClassName="active" to="/login">Login</NavLink><small>(Access without token only)</small>
          </div>
          <div className="content">
            <Switch>
              <Route exact path="/" component={Home} />
              <PublicRoute path="/login" component={Login} />
              <PrivateRoute path="/dashboard" component={Dashboard} />
              <PrivateRoute path="/addMovie" component={AddMovie} />
              <PrivateRoute path="/leaseContent" component={LeaseContent} />
              <PrivateRoute path="/updateLease" component={UpdateLeaseContent} />
              <PrivateRoute path="/watchEvent" component={WatchContent} />
              <PrivateRoute path="/viewRevenue" component={MonthlyRevenue} />
              <PrivateRoute path="/addCreator" component={AddCreator} />
              <PrivateRoute path="/creator" component={Creator} />
              <PrivateRoute path="/consumer" component={Consumer} />
              <PrivateRoute path="/addService" component={AddService} />
              <PrivateRoute path="/addDemoGroup" component={AddDemoGroup} />
              <PrivateRoute path="/updateDemoGroup" component={UpdateDemoGroup} />
              <PrivateRoute path="/editContent" component={EditContent} />
              <PrivateRoute path="/editService" component={EditService} />
              <PrivateRoute path="/viewAudit" component={ViewAudit} />
            </Switch>
          </div>
        </div>
      </BrowserRouter>
    </div>
  );
}

export default App;
