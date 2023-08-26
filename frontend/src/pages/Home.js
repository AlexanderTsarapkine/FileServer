import React from 'react'
import ReactTyped from "react-typed";
import { useNavigate } from 'react-router-dom';
import '../assets/styles.css';

import { useGoogleLogin } from '@react-oauth/google';

const Home = ({setOauthUser}) => {
    const  navigate = useNavigate();

    const login = useGoogleLogin({
        onSuccess: tokenResponse => {
            setOauthUser(tokenResponse);
            navigate('/MyData', {replace: true}); 
        },
      });

    const introText = 
        ['Welcome to my File Server',
         'Feel free to store some stuff...',
         '...only photos and videos though',
         'Made with React and Spring Data JPA...',
         '...using SQLite and a local db']
    return (
        <div className="split-login">
            <div className="intro-container">
                <div className="intro-text">
                    <ReactTyped strings={introText} typeSpeed={75} backDelay={1500} loop />
                </div>
            </div>
            <div className="login-container">
                <h1>Start Storing</h1>
                <div className="button-container">
                    <button className="sign-in-button" onClick={() => login()}>Sign In With Google</button>
                </div>
            </div>
        </div>
    )
}

export default Home;
