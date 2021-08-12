const API_URL = 'http://localhost:8080/api'

export const api = async (url, options) => await fetch(`${API_URL}${url}`, {
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    },
    ...options
}).then(response => response.json())