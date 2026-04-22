import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { auditAPI } from "../services/api";

function AuditLog() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    level: "",
    module: "",
  });

  useEffect(() => {
    fetchLogs();
  }, [filters]);

  const fetchLogs = async () => {
    try {
      const params = {};
      if (filters.level) params.level = filters.level;
      if (filters.module) params.module = filters.module;

      const response = await auditAPI.getLogs(params);
      setLogs(response.data);
    } catch (error) {
      console.error("Failed to fetch logs:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const params = {};
      if (filters.level) params.level = filters.level;
      if (filters.module) params.module = filters.module;

      const response = await auditAPI.exportLogs(params);
      const blob = new Blob([response.data], { type: "text/csv" });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "audit_logs.csv";
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Failed to export logs:", error);
    }
  };

  const getLevelBadgeClass = (level) => {
    switch (level) {
      case "ERROR":
        return "status-stopped";
      case "WARN":
        return "status-active";
      case "INFO":
        return "status-active";
      default:
        return "";
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="header">
        <h1>审计日志</h1>
      </div>

      <div className="container">
        <div className="card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: "20px",
            }}
          >
            <h3>日志列表</h3>
            <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
              <select
                value={filters.level}
                onChange={(e) =>
                  setFilters({ ...filters, level: e.target.value })
                }
                style={{
                  padding: "8px",
                  borderRadius: "4px",
                  border: "1px solid #d9d9d9",
                }}
              >
                <option value="">全部级别</option>
                <option value="INFO">INFO</option>
                <option value="WARN">WARN</option>
                <option value="ERROR">ERROR</option>
              </select>
              <select
                value={filters.module}
                onChange={(e) =>
                  setFilters({ ...filters, module: e.target.value })
                }
                style={{
                  padding: "8px",
                  borderRadius: "4px",
                  border: "1px solid #d9d9d9",
                }}
              >
                <option value="">全部模块</option>
                <option value="FOG_NODE">雾节点</option>
                <option value="CONTROL_CENTER">控制中心</option>
              </select>
              <button className="btn btn-primary" onClick={fetchLogs}>
                刷新
              </button>
              <button className="btn btn-success" onClick={handleExport}>
                导出CSV
              </button>
              <Link to="/">
                <button className="btn btn-primary">返回主页</button>
              </Link>
            </div>
          </div>

          <table className="table">
            <thead>
              <tr>
                <th>时间</th>
                <th>级别</th>
                <th>模块</th>
                <th>设备ID</th>
                <th>事件类型</th>
                <th>消息</th>
              </tr>
            </thead>
            <tbody>
              {logs.length === 0 ? (
                <tr>
                  <td
                    colSpan="6"
                    style={{ textAlign: "center", padding: "40px" }}
                  >
                    暂无日志记录
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.auditId}>
                    <td>{new Date(log.eventTime).toLocaleString()}</td>
                    <td>
                      <span
                        className={`status-badge ${getLevelBadgeClass(log.level)}`}
                      >
                        {log.level}
                      </span>
                    </td>
                    <td>{log.module}</td>
                    <td>{log.deviceId || "-"}</td>
                    <td>{log.eventType}</td>
                    <td>{log.message}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default AuditLog;
