import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import TerminalPanel from "./pages/TerminalPanel";
import Statistics from "./pages/Statistics";
import AuditLog from "./pages/AuditLog";

function App() {
  const isAuthenticated = () => {
    return localStorage.getItem("token") !== null;
  };

  const PrivateRoute = ({ children }) => {
    return isAuthenticated() ? children : <Navigate to="/login" />;
  };

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Dashboard />
            </PrivateRoute>
          }
        />
        <Route
          path="/terminals"
          element={
            <PrivateRoute>
              <TerminalPanel />
            </PrivateRoute>
          }
        />
        <Route
          path="/statistics"
          element={
            <PrivateRoute>
              <Statistics />
            </PrivateRoute>
          }
        />
        <Route
          path="/audit"
          element={
            <PrivateRoute>
              <AuditLog />
            </PrivateRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
