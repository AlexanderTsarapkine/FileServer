import React, { useState } from 'react';
import Dropzone from 'react-dropzone';
import axios from 'axios';

const UploadModal = ({ oauthUser, handleClose, getPreview }) => {
    const [selectedFile, setSelectedFile] = useState(null);

    const serverUrl = process.env.REACT_APP_SERVER_BASE_URL;

    const handleFileDrop = (acceptedFiles) => {
        if (acceptedFiles && acceptedFiles.length > 0) {
            setSelectedFile(acceptedFiles[0]);
        }
    };

    const handleUpload = async () => {
        if (selectedFile) {
            const formData = new FormData();
            formData.append('token', oauthUser.access_token);
            formData.append('file', selectedFile);
    
            try {
                const response = await axios.post(`${serverUrl}/users/files/upload`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });
    
                console.log('Upload successful');
                handleClose();
                getPreview();
            } catch (error) {
                console.error('Upload failed:', error);
            }
        }
    };
    
    return (
        <div className="upload-modal">
            <h2>Upload Files</h2>
            <Dropzone onDrop={handleFileDrop}>
                {({ getRootProps, getInputProps }) => (
                    <div {...getRootProps()} className="dropzone">
                        <input {...getInputProps()} />
                        {selectedFile ? (
                            <p>Selected: {selectedFile.name}</p>
                        ) : (
                            <p>Drag & drop a file here, or click to select one</p>
                        )}
                    </div>
                )}
            </Dropzone>
            <button onClick={handleUpload}>Upload</button>
        </div>
    );
}

export default UploadModal;
