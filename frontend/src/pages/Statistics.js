import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import ReactECharts from "echarts-for-react";
import { centerAPI } from "../services/api";

function Statistics() {
  const [statistics, setStatistics] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStatistics();
    const interval = setInterval(fetchStatistics, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchStatistics = async () => {
    try {
      const now = new Date();
      const start = new Date(now.getTime() - 24 * 60 * 60 * 1000);
      const response = await centerAPI.getStatistics(
        start.toISOString(),
        now.toISOString(),
      );
      setStatistics(response.data);
    } catch (error) {
      console.error("Failed to fetch statistics:", error);
    } finally {
      setLoading(false);
    }
  };

  const getChartOption = () => {
    const xAxisData = statistics.map((s) =>
      new Date(s.windowStart).toLocaleTimeString(),
    );

    return {
      title: {
        text: "电力数据统计",
      },
      tooltip: {
        trigger: "axis",
      },
      legend: {
        data: ["平均电压(V)", "平均电流(A)", "平均功率(W)"],
      },
      xAxis: {
        type: "category",
        data: xAxisData,
      },
      yAxis: {
        type: "value",
      },
      series: [
        {
          name: "平均电压(V)",
          type: "line",
          data: statistics.map((s) => s.avgVoltage),
          smooth: true,
        },
        {
          name: "平均电流(A)",
          type: "line",
          data: statistics.map((s) => s.avgCurrent),
          smooth: true,
        },
        {
          name: "平均功率(W)",
          type: "line",
          data: statistics.map((s) => s.avgPower),
          smooth: true,
        },
      ],
    };
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="header">
        <h1>数据统计</h1>
      </div>

      <div className="container">
        <div className="card" style={{ marginBottom: "20px" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: "20px",
            }}
          >
            <h3>实时数据趋势</h3>
            <Link to="/">
              <button className="btn btn-primary">返回主页</button>
            </Link>
          </div>

          {statistics.length > 0 ? (
            <ReactECharts
              option={getChartOption()}
              style={{ height: "400px" }}
            />
          ) : (
            <div className="loading">暂无数据，请先启动终端模拟器</div>
          )}
        </div>

        <div className="card">
          <h3 style={{ marginBottom: "20px" }}>历史记录</h3>
          <table className="table">
            <thead>
              <tr>
                <th>时间窗口</th>
                <th>参与设备数</th>
                <th>平均电压(V)</th>
                <th>平均电流(A)</th>
                <th>平均功率(W)</th>
                <th>签名验证</th>
              </tr>
            </thead>
            <tbody>
              {statistics.map((stat, index) => (
                <tr key={index}>
                  <td>
                    {new Date(stat.windowStart).toLocaleString()} -
                    {new Date(stat.windowEnd).toLocaleString()}
                  </td>
                  <td>{stat.participantCount}</td>
                  <td>{stat.avgVoltage ? stat.avgVoltage.toFixed(2) : "-"}</td>
                  <td>{stat.avgCurrent ? stat.avgCurrent.toFixed(2) : "-"}</td>
                  <td>{stat.avgPower ? stat.avgPower.toFixed(2) : "-"}</td>
                  <td>
                    <span
                      className={`status-badge ${stat.signatureVerified ? "status-active" : "status-stopped"}`}
                    >
                      {stat.signatureVerified ? "通过" : "失败"}
                    </span>
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

export default Statistics;
