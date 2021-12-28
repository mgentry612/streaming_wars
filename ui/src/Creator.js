import React from 'react';
import { getUser, removeUserSession } from './Utils/Common';

function Creator(props) {
    const user = getUser();

    // handle click event of logout button
    const handleLogout = () => {
        removeUserSession();
        props.history.push('/login');
    }

    const leaseMovie = () => {
        props.history.push('/leaseContent')
    }

    const viewRevenue = () => {
        props.history.push('/viewRevenue')
    }

    const updateLease = () => {
        props.history.push('/updateLease')
    }

    const editContent = () => {
        props.history.push('/editContent')
    }

    return (
        <div>
            Welcome {user.name}!<br /><br />
            <input type="button" onClick={leaseMovie} value="Lease Content to service"/>
            <input type="button" onClick={updateLease} value="Update Lease"/>
            <input type="button" onClick={viewRevenue} value="View Revenue "/>
            <input type="button" onClick={editContent} value="Edit Content "/>
            <input type="button" onClick={handleLogout} value="Logout" />
        </div>
    );
}

export default Creator;