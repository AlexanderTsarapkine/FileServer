import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import FilePreview from '../components/FilePreview';
import FileDashboard from '../components/FileDashboard';

const StorageInterface = ({setOauthUser, oauthUser}) => {
    const [userPreviews, setUserPreviews] = useState(null);
    const [selected, setSelected] = useState([]);
    
    const  navigate = useNavigate();

    


    function downloadSelected() {
        const mimeTypeExtensions = {
            'image/png': 'png',
            'image/jpeg': 'jpeg',
            'video/quicktime': 'mov',
        };
    
        selected.forEach(selection => {
            const headers = {
                'Content-Type': 'application/json',
            };
    
            const requestBody = {
                token: oauthUser.access_token,
            };
    
            axios.post(`http://localhost:8080/users/files/download?id=${selection.id}`, requestBody, {
                headers,
                responseType: 'blob'
            })
            .then(response => {
                const blob = new Blob([response.data]);
    
                let fileExtension = mimeTypeExtensions[selection.type] || 'unknown';    
                let filename = `${selection.name}.${fileExtension}`;
    
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = filename;
                a.click();
                URL.revokeObjectURL(url);
            })
            .catch(error => {
                console.error('Error retrieving file:', error);
                setOauthUser(null);
            });
        })
    }
    

    function deleteSelected() {
        selected.forEach(selection => {
            const headers = {
                'Content-Type': 'application/json',
            };
    
            const requestBody = {
                token: oauthUser.access_token,
            };
    
            axios.delete(`http://localhost:8080/users/files?id=${selection.id}`, {
                data: requestBody,
                headers,
                responseType: 'blob'
            })
            .then(response => {
               console.log("deleted");
               setSelected(selected.filter(obj => obj.id !== selection.id));
               console.log(selected.length)
               getPreview();
            })
            .catch(error => {
                console.error('Error deleting file:', error);
                setOauthUser(null);
            });
        });
    }
    
    const getPreview = useCallback(() => {
        const headers = {
            'Content-Type': 'application/json',
        };

        console.log("Sending preview request...");
        axios.post("http://localhost:8080/users/preview", { token: oauthUser.access_token }, { headers })
            .then(response => {
                console.log('Preview recieved');
                setUserPreviews(response.data);
            })
            .catch(error => {
                console.error('Error retrieving preview:', error);
                setUserPreviews(null);
            });
    }, [oauthUser.access_token]);

    useEffect(() => {
        if (oauthUser) {
            getPreview();
        } else {
            navigate('/', { replace: true });
        }
    }, [oauthUser, navigate, getPreview]);

    return (
        <div className="StorageInterface">
            <FileDashboard setOauthUser={setOauthUser} oauthUser={oauthUser} selected={selected} downloadSelected={downloadSelected} deleteSelected={deleteSelected} getPreview={getPreview}/>
            <div className="FilePreviewContainer">
                {userPreviews && userPreviews.length === 0 && 
                    <h3>Try uploading some photos and images!</h3>
                }
                {userPreviews && userPreviews.map((preview, index) => (
                    <FilePreview key={index} previewObj={preview} selected={selected} setSelected={setSelected}/>
                ))}
            </div>
        </div>
    )
}

export default StorageInterface;