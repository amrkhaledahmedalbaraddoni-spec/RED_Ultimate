import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic } from 'antd';
import { MessageOutlined, TeamOutlined, PictureOutlined } from '@ant-design/icons';

/**
 * RED Admin Live Monitor — pulls real stats from /api/admin/monitor/stats.
 */
const LiveMonitor = () => {
  const [stats, setStats] = useState({ messages_24h: 0, stories_active: 0, online_users: 0 });

  useEffect(() => {
    let alive = true;
    const poll = async () => {
      try {
        const res = await fetch('/api/admin/monitor/stats');
        if (res.ok) {
          const data = await res.json();
          if (alive) setStats(data);
        }
      } catch {
        /* backend unreachable */
      }
    };
    poll();
    const interval = setInterval(poll, 3000);
    return () => { alive = false; clearInterval(interval); };
  }, []);

  return (
    <div>
      <Row gutter={16}>
        <Col span={8}>
          <Card><Statistic title="Messages (24h)" value={stats.messages_24h} prefix={<MessageOutlined />} /></Card>
        </Col>
        <Col span={8}>
          <Card><Statistic title="Online Users" value={stats.online_users} prefix={<TeamOutlined />} valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
        <Col span={8}>
          <Card><Statistic title="Active Stories" value={stats.stories_active} prefix={<PictureOutlined />} /></Card>
        </Col>
      </Row>
      <Card title="System" style={{ marginTop: 24 }}>
        <p>Backend is serving live metrics from the sovereign server.</p>
      </Card>
    </div>
  );
};

export default LiveMonitor;
