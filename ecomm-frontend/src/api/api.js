import axios from "axios";

const api = axios.create({
  baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
  withCredentials: true, // your frontend will include cookie from BE in the network section cookies.
});

export default api;

/*
For token based access.
const {user}  = getState().auth;
await api.post(`/addresses`,sendData, {
  headers: {Authorization: "Bearer"+user.jwtToken}
})
*/
