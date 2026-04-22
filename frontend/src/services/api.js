import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export const authAPI = {
  login: (username, password) =>
    api.post("/auth/login", { username, password }),
  logout: () => api.post("/auth/logout"),
  verifyToken: () => api.get("/auth/verify"),
};

export const simulatorAPI = {
  start: (deviceIds) =>
    api.post("/simulator/start", deviceIds ? { deviceIds } : {}),
  stop: (deviceIds) =>
    api.post("/simulator/stop", deviceIds ? { deviceIds } : {}),
  getStatus: () => api.get("/simulator/status"),
  getDeviceStatus: (deviceId) => api.get(`/simulator/status/${deviceId}`),
};

export const centerAPI = {
  getStatistics: (startTime, endTime) =>
    api.get("/center/statistics", {
      params: { startTime, endTime },
    }),
};

export const auditAPI = {
  getLogs: (params) => api.get("/audit/logs", { params }),
  exportLogs: (params) => api.get("/audit/export", { params }),
};

export default api;
