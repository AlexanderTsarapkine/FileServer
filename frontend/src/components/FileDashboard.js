import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { googleLogout } from '@react-oauth/google';
import { useNavigate } from 'react-router-dom';
import UploadModal from './UploadModal';
import Modal from '@mui/material/Modal';
import Box from '@mui/material/Box';

import '../assets/styles.css';

const serverUrl = process.env.REACT_APP_SERVER_BASE_URL;

const FileDashboard= ({setOauthUser, oauthUser, selected, downloadSelected, deleteSelected, getPreview}) => {

    const  navigate = useNavigate();

    const [userProfile, setUserProfile] = useState(null);

    const [open, setOpen] = React.useState(false);
    const handleOpen = () => setOpen(true);
    const handleClose = () => setOpen(false);

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
            axios.post(`${serverUrl}/verify`, { access_token: oauthUser.access_token }, { headers })
            .then(response => {
                console.log("Profile Recieved");
                setUserProfile(response.data);
            })
            .catch(error => {
                console.error('Error retrieving profile:', error);
                setOauthUser(null);
            });
        }

        if (oauthUser) {
            getProfile();
        } else {
            navigate('/', { replace: true });
        }
    }, [oauthUser, setOauthUser, navigate]);

    return (
        <div className="FileDashboard">
            <div>
                <h1>Your Files</h1>
                <button className={`dashboard-button ${selected.length === 0 && "disabled"}`} id="download" onClick={()=>downloadSelected()}>Download</button>
                <button className="dashboard-button" id="upload" onClick={()=>handleOpen()}>Upload</button>
                <button className={`dashboard-button ${selected.length === 0 && "disabled"}`} id="delete" onClick={()=>deleteSelected()}>Delete</button>
                <p className="files-selected">{selected.length} Files Selected</p>
            </div>
            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box className="upload-modal-content">
                    <UploadModal oauthUser={oauthUser} handleClose={handleClose} getPreview={getPreview}/>
                </Box>
            </Modal>
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
                
                <button className="sign-out-button"  onClick={()=>logout()}>Sign Out</button>
            </div>
           
        </div>
    )
}

export default FileDashboard;