function checkSessionExpiration(sessionToken) {
    if (!sessionToken) {
        clearCookiesAndRedirect();
        return; // No authToken cookie, user is not logged in
    }

    // Make an AJAX call to server to validate session
    fetch('rest/login/checkPermissions', {
	    method: 'POST',
	    headers: {
            'Cookie': `sessionToken=${sessionToken}` // Set the cookie in headers
        }
    })
    .then(response => response.text())  // Parse the response as text
    .then(data => {
        if (data === "true") {
            clearCookiesAndRedirect();
        }
    })
    .catch(error => {
        console.error('Error checking session:', error);
    });
}




function getCookie(name) {
	        const value = `; ${document.cookie}`;
	        const parts = value.split(`; ${name}=`);
	        if (parts.length === 2) return parts.pop().split(';').shift();
	    }

function clearCookiesAndRedirect() {
    // Remove cookies
    document.cookie = "authToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "authTokenRole=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "username=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "userRole=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    
    // Redirect to login page
    window.location.href = '/login.html';
}
