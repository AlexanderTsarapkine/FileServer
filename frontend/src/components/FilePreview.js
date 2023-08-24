import React, { useState, useEffect } from 'react'
import '../assets/styles.css';

const FilePreview = ({previewObj, selected, setSelected}) => {

    const [isSelected, setIsSelected] = useState(false);

    const imageDataUrl = `data:image/png;base64, ${previewObj.filePreview}`;

    useEffect(() => {
        if (isSelected) {
            setSelected([...selected, previewObj]);
        } else {
            setSelected(selected.filter(obj => obj.id !== previewObj.id));
        }
    }, [isSelected]);
   
    return (
        <div className={`FilePreview ${isSelected && "selected"}`} onClick={()=>setIsSelected(!isSelected)}>
            
            {/* <h4>{previewObj.name}</h4>
            <p>{previewObj.dateUploaded}</p> */}
            <h4>{previewObj.type}</h4>
            <img src={imageDataUrl} alt="File Preview" />
        </div>
    )
}

export default FilePreview;
