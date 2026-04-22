import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { simulatorAPI } from "../services/api";

function Dashboard() {
  const [stats, setStats] = useState({
    total: 0,
    running: 0,
    stopped: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchStats = async () => {
    try {
      const response = await simulatorAPI.getStatus();
      const devices = response.data;
      setStats({
        total: devices.length,
        running: devices.filter((d) => d.status === "running").length,
        stopped: devices.filter((d) => d.status === "stopped").length,
      });
    } catch (error) {
      console.error("Failed to fetch stats:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    window.location.href = "/login";
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div
        className="header"
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <h1>智能电网隐私保护系统</h1>
        <div>
          <span style={{ marginRight: "20px" }}>
            欢迎, {localStorage.getItem("username")}
          </span>
          <button className="btn btn-danger" onClick={handleLogout}>
            退出
          </button>
        </div>
      </div>

      <div className="container">
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(3, 1fr)",
            gap: "20px",
            marginBottom: "20px",
          }}
        >
          <div className="card" style={{ textAlign: "center" }}>
            <h3 style={{ fontSize: "36px", color: "#1890ff" }}>
              {stats.total}
            </h3>
            <p>终端总数</p>
          </div>
          <div className="card" style={{ textAlign: "center" }}>
            <h3 style={{ fontSize: "36px", color: "#52c41a" }}>
              {stats.running}
            </h3>
            <p>运行中</p>
          </div>
          <div className="card" style={{ textAlign: "center" }}>
            <h3 style={{ fontSize: "36px", color: "#ff4d4f" }}>
              {stats.stopped}
            </h3>
            <p>已停止</p>
          </div>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: "20px" }}>快速操作</h3>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            <Link to="/terminals">
              <button className="btn btn-primary">终端管理</button>
            </Link>
            <Link to="/statistics">
              <button className="btn btn-success">数据统计</button>
            </Link>
            <Link to="/audit">
              <button className="btn btn-primary">审计日志</button>
            </Link>
          </div>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: "20px" }}>系统说明</h3>
          <div style={{ color: "#666", lineHeight: "1.8" }}>
            <p>
              本系统实现了一套"智能电表—雾节点—控制中心"三层架构的隐私保护数据聚合系统。
            </p>
            <ul style={{ marginLeft: "20px", marginTop: "10px" }}>
              <li>隐私保护：雾节点无法解密个体用户数据</li>
              <li>完整性：签名验证机制检测篡改行为</li>
              <li>抗重放：基于序列号+时间戳的组合检测</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
