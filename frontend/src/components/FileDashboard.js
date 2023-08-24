import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { googleLogout } from '@react-oauth/google';
import UploadModal from './UploadModal';
import Modal from '@mui/material/Modal';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';



// const serverUrl = process.env.REACT_SERVER_BASE_URL;

const FileDashboard= ({setOauthUser, oauthUser, selected, downloadSelected, deleteSelected, getPreview}) => {

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
            axios.post("http://localhost:8080/verify", { access_token: oauthUser.access_token }, { headers })
            .then(response => {
                console.log('Response:', response.data);
                setUserProfile(response.data);
            })
            .catch(error => {
                console.error('Error retrieving profile:', error);
                setOauthUser(null);
            });
        }

        if (oauthUser) {
            getProfile();
        } // else logout()?
    }, [oauthUser, setOauthUser]);

    const style = {
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: 400,
        bgcolor: 'background.paper',
        border: '2px solid #000',
        boxShadow: 24,
        p: 4,
      };

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
                <Box sx={style}>
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