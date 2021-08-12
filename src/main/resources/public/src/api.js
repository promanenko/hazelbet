const API_URL = 'http://localhost:8080/api'

export const api = async (url, options) => {
    try {
        return await fetch(`${API_URL}${url}`, options).then(response => response.json());
    }
    catch (e) {
        console.log(e)
    }
}