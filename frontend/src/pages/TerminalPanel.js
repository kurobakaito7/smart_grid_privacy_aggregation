import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { simulatorAPI } from "../services/api";

function TerminalPanel() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetchDevices();
    const interval = setInterval(fetchDevices, 3000);
    return () => clearInterval(interval);
  }, []);

  const fetchDevices = async () => {
    try {
      const response = await simulatorAPI.getStatus();
      setDevices(response.data);
    } catch (error) {
      console.error("Failed to fetch devices:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleStartAll = async () => {
    setActionLoading(true);
    setMessage("");
    try {
      await simulatorAPI.start();
      setMessage("所有终端已启动");
      fetchDevices();
    } catch (error) {
      setMessage("启动失败");
    } finally {
      setActionLoading(false);
    }
  };

  const handleStopAll = async () => {
    setActionLoading(true);
    setMessage("");
    try {
      await simulatorAPI.stop();
      setMessage("所有终端已停止");
      fetchDevices();
    } catch (error) {
      setMessage("停止失败");
    } finally {
      setActionLoading(false);
    }
  };

  const handleStartDevice = async (deviceId) => {
    setActionLoading(true);
    try {
      await simulatorAPI.start([deviceId]);
      fetchDevices();
    } catch (error) {
      console.error("Failed to start device:", error);
    } finally {
      setActionLoading(false);
    }
  };

  const handleStopDevice = async (deviceId) => {
    setActionLoading(true);
    try {
      await simulatorAPI.stop([deviceId]);
      fetchDevices();
    } catch (error) {
      console.error("Failed to stop device:", error);
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="header">
        <h1>终端管理</h1>
      </div>

      <div className="container">
        {message && <div className="success">{message}</div>}

        <div className="card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginBottom: "20px",
            }}
          >
            <h3>终端列表</h3>
            <div style={{ display: "flex", gap: "10px" }}>
              <button
                className="btn btn-success"
                onClick={handleStartAll}
                disabled={actionLoading}
              >
                启动全部
              </button>
              <button
                className="btn btn-danger"
                onClick={handleStopAll}
                disabled={actionLoading}
              >
                停止全部
              </button>
              <Link to="/">
                <button className="btn btn-primary">返回主页</button>
              </Link>
            </div>
          </div>

          <table className="table">
            <thead>
              <tr>
                <th>设备ID</th>
                <th>状态</th>
                <th>最后上报时间</th>
                <th>序列号</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {devices.map((device) => (
                <tr key={device.deviceId}>
                  <td>{device.deviceId}</td>
                  <td>
                    <span
                      className={`status-badge ${device.status === "running" ? "status-active" : "status-stopped"}`}
                    >
                      {device.status === "running" ? "运行中" : "已停止"}
                    </span>
                  </td>
                  <td>{device.lastReportTime || "-"}</td>
                  <td>{device.lastSeq || 0}</td>
                  <td>
                    {device.status === "running" ? (
                      <button
                        className="btn btn-danger"
                        onClick={() => handleStopDevice(device.deviceId)}
                        disabled={actionLoading}
                        style={{ padding: "4px 12px", fontSize: "12px" }}
                      >
                        停止
                      </button>
                    ) : (
                      <button
                        className="btn btn-success"
                        onClick={() => handleStartDevice(device.deviceId)}
                        disabled={actionLoading}
                        style={{ padding: "4px 12px", fontSize: "12px" }}
                      >
                        启动
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default TerminalPanel;
