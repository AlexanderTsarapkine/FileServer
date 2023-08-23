import React from 'react'
import '../assets/styles.css';

const FilePreview = ({previewObj}) => {

    const imageDataUrl = `data:image/png;base64, ${previewObj.filePreview}`;
   
    return (
        <div className="FilePreview">
            <img src={imageDataUrl} alt="File Preview" />
            {/* <h4>{previewObj.name}</h4>
            <p>{previewObj.dateUploaded}</p> */}
            <h4>{previewObj.type}</h4>
        </div>
    )
}

export default FilePreview;
