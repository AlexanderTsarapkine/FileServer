import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { GoogleOAuthProvider } from '@react-oauth/google';

import Home from './pages/Home';
import StorageInterface from './pages/StorageInterface';

const clientId = process.env.REACT_APP_CLIENT_ID;

function App() {
  const [oauthUser, setOauthUser] = useState({}); // consider secure state management or cookie storage

  return (
    <GoogleOAuthProvider clientId={clientId}> 
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home setOauthUser={setOauthUser}/>}/>
          <Route path="/MyData" element={<StorageInterface setOauthUser={setOauthUser} oauthUser={oauthUser}/>}/>
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}

export default App;
