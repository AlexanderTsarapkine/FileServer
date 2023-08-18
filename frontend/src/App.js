import './App.css';
import Login from "./components/login";
import Logout from "./components/logout";
import { useEffect } from 'react';
import {gapi} from 'gapi-script';

const clientId = process.env.REACT_APP_CLIENT_ID;

function App() {

  useEffect(() => {
    async function start() {
      await gapi.client.init({
        clientId: clientId,
        scope: "openid email profile"
      });

      const authInstance = gapi.auth2.getAuthInstance();
      const user = authInstance.currentUser.get();
      const authResponse = user.getAuthResponse();

      const idToken = authResponse.id_token;
      console.log("ID token?:");
      console.log(idToken);
    }

    gapi.load('client:auth2', start);
  }, []);

  return (
    <div className="App">
      <Login/>
      <Logout/>
    </div>
  );
}

export default App;