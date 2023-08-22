import React, { useEffect } from 'react';

const StorageInterface = ({oauthToken}) => {

    useEffect(() => {
        console.log("ASDSD", oauthToken);
    }, [oauthToken]);

    console.log(oauthToken);
    return (
        <>
            <h1>dasdasda</h1>
            <h1>{oauthToken}</h1>
        </>
    )
}

export default StorageInterface;