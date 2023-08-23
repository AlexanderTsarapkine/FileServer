import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { googleLogout } from '@react-oauth/google';


// const serverUrl = process.env.REACT_SERVER_BASE_URL;

const FileDashboard= ({setOauthUser, oauthUser}) => {

    const [userProfile, setUserProfile] = useState(null);

    

    function logout() {
        googleLogout();
        setOauthUser(null);
    }

    useEffect(() => {
        function getProfile() {
            const headers = {
                'Content-Type': 'application/json',
            };
    
            console.log("Sending profile request...");
            axios.post("http://localhost:8080/verify", { access_token: oauthUser.access_token }, { headers })
            .then(response => {
                console.log('Response:', response.data);
                setUserProfile(response.data);
            })
            .catch(error => {
                console.error('Error retrieving profile:', error);
                // setOauthUser(null);
            });
        }

        if (oauthUser) {
            getProfile();
        }
    }, [oauthUser]);


    return (
        <div className="FileDashboard">
            <div>
                <h1>Your Files</h1>
                <button className="dashboard-button disabled" id="download">Download</button>
                <button className="dashboard-button" id="upload">Upload</button>
                <button className="dashboard-button" id="delete">Delete</button>
            </div>
            <div>
                {userProfile &&
                    <div className="profile-info">
                        <img src={userProfile.picture} alt="profile"/>
                        <div className="profile-name">
                            <p>{userProfile.given_name}</p>
                            <p>{userProfile.family_name}</p>
                        </div>
                    </div>  
                }
                
                <button className="sign-out-button"  onClick={() => logout()}>Sign Out</button>
            </div>
           
        </div>
    )
}

export default FileDashboard;