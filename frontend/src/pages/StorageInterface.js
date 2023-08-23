import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import FilePreview from '../components/FilePreview';
import FileDashboard from '../components/FileDashboard';

// const serverUrl = process.env.REACT_SERVER_BASE_URL;

const StorageInterface = ({setOauthUser, oauthUser}) => {
    const [userPreviews, setUserPreviews] = useState(null);
    
    const  navigate = useNavigate();

    function getPreview() {
        const headers = {
            'Content-Type': 'application/json',
        };

        console.log("Sending preview request...");
        axios.post("http://localhost:8080/users/preview", { token: oauthUser.access_token }, { headers })
        .then(response => {
            console.log('Response:', response.data);
            setUserPreviews(response.data)
        })
        .catch(error => {
            console.error('Error retrieving preview:', error);
            setUserPreviews(null);
        });
    }

    useEffect(() => {
        if (oauthUser) {
            console.log(oauthUser);
            getPreview(); // make it create an account if no user
        } else {
            navigate('/', {replace: true});  
        }
    }, [oauthUser]);

    return (
        <div className="StorageInterface">
            <FileDashboard setOauthUser={setOauthUser} oauthUser={oauthUser}/>
            <div className="FilePreviewContainer">
                {userPreviews && userPreviews.length == 0 && 
                    <h3>Try uploading some photos and images!</h3>
                }
                {userPreviews && userPreviews.map((preview, index) => (
                    <FilePreview key={index} previewObj={preview} />
                ))}
            </div>
            
        </div>
        
    )
}

export default StorageInterface;