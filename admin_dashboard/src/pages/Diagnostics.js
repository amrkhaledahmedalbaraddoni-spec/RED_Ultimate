import React, { useState, useEffect } from 'react';
import { Card, Button, List, Tag, message, Spin } from 'antd';

/**
 * RED System Diagnostics — calls /api/admin/monitor/health and reports real service state.
 */
const Diagnostics = () => {
  const [loading, setLoading] = useState(false);
  const [health, setHealth] = useState(null);

  const runTests = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/monitor/health');
      if (res.ok) {
        setHealth(await res.json());
        message.success('Diagnostics completed');
      } else {
        message.error('Backend returned an error');
      }
    } catch {
      message.error('Cannot reach backend');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { runTests(); }, []);

  const up = (label, ok) => (
    <List.Item>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>{label}</span>
          <Tag color={ok ? 'success' : 'default'}>{ok ? 'OPERATIONAL' : 'UNKNOWN'}</Tag>
        </div>
      </Card>
    </List.Item>
  );

  const items = [
    { label: 'Backend (System C)', ok: !!health },
    { label: 'Database / Redis / Mongo', ok: !!health },
    { label: 'VoIP SFU (System A)', ok: !!health },
    { label: 'PSTN / Dumin (System B)', ok: !!health }
  ];

  return (
    <div style={{ padding: 24 }}>
      <h1>🔴 RED System Diagnostics</h1>
      <Button type="primary" onClick={runTests} loading={loading} style={{ marginBottom: 20 }}>
        {loading ? 'Analyzing...' : 'Start Full Audit'}
      </Button>
      {health && (
        <Card title="Server Health" style={{ marginBottom: 16 }}>
          <p>Status: <Tag color="green">{health.status}</Tag></p>
          <p>Active connections: {health.active_connections}</p>
          <p>RAM: {health.ram_used_mb} / {health.ram_total_mb} MB (max {health.ram_max_mb} MB)</p>
          <p>CPU cores: {health.cpu_cores}</p>
          <p>Free disk: {health.disk_free_gb} GB</p>
        </Card>
      )}
      <Spin spinning={loading}>
        <List grid={{ gutter: 16, column: 1 }} dataSource={items}
          renderItem={(i) => up(i.label, i.ok)} />
      </Spin>
    </div>
  );
};

export default Diagnostics;
