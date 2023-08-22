import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { GoogleOAuthProvider } from '@react-oauth/google';

import Home from './pages/Home';
import StorageInterface from './pages/StorageInterface';

const clientId = process.env.REACT_APP_CLIENT_ID;

function App() {
  const [oauthToken, setToken] = useState(null); // consider secure state management or cookie storage

  return (
    <GoogleOAuthProvider clientId={clientId}> 
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home setToken={setToken}/>}/>
          <Route path="/MyData" element={<StorageInterface oauthToken={oauthToken}/>}/>
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}

export default App;
