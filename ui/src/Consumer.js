import React from 'react';
import { getUser, removeUserSession } from './Utils/Common';

function Consumer(props) {
  const user = getUser();

  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

    const watchEvent = () => {
        props.history.push('/watchEvent')
    }

  return (
    <div>
      Welcome {user.name}!<br /><br />
      <input type="button" onClick={watchEvent} value="Watch Content"/>
      <input type="button" onClick={handleLogout} value="Logout" />
    </div>
  );
}

export default Consumer;
