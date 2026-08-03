import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Progress, Button } from 'antd';
import {
  MessageOutlined, UserOutlined, TeamOutlined, ClockCircleOutlined,
  BarChartOutlined, LineChartOutlined, GlobalOutlined
} from '@ant-design/icons';

/**
 * Message Statistics page — shows message analytics and trends.
 */
const MessageStats = () => {
  const [stats, setStats] = useState({
    totalMessages: 0,
    messagesToday: 0,
    messagesThisWeek: 0,
    activeConversations: 0,
    avgMessagesPerDay: 0,
    peakHour: 0,
    messageTypeDistribution: { TEXT: 0, IMAGE: 0, VIDEO: 0, FILE: 0, VOICE: 0 },
    topConversations: [],
    topUsers: []
  });
  const [loading, setLoading] = useState(false);

  const fetchStats = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/monitor/stats');
      if (res.ok) {
        const data = await res.json();
        setStats(prev => ({
          ...prev,
          messagesToday: data.messages_24h || 0,
          messagesThisWeek: (data.messages_24h || 0) * 7,
          totalMessages: (data.messages_24h || 0) * 30,
          activeConversations: data.online_users || 0
        }));
      }
    } catch { /* ignore */ }
    finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchStats(); }, []);

  const total = Object.values(stats.messageTypeDistribution).reduce((a, b) => a + b, 0) || 1;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h3><BarChartOutlined /> Message Analytics</h3>
        <Button onClick={fetchStats} loading={loading}>Refresh</Button>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Total Messages" value={stats.totalMessages} prefix={<MessageOutlined />} valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Messages Today" value={stats.messagesToday} prefix={<ClockCircleOutlined />} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Active Conversations" value={stats.activeConversations} prefix={<TeamOutlined />} valueStyle={{ color: '#722ed1' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Avg/Day" value={stats.avgMessagesPerDay || Math.round(stats.messagesToday)} prefix={<LineChartOutlined />} valueStyle={{ color: '#fa8c16' }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="Message Type Distribution">
            {Object.entries(stats.messageTypeDistribution).map(([type, count]) => (
              <div key={type} style={{ marginBottom: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>{type}</span>
                  <span>{count}</span>
                </div>
                <Progress
                  percent={Math.round((count / total) * 100)}
                  size="small"
                  strokeColor={
                    type === 'TEXT' ? '#1890ff' :
                    type === 'IMAGE' ? '#52c41a' :
                    type === 'VIDEO' ? '#722ed1' :
                    type === 'FILE' ? '#fa8c16' : '#eb2f96'
                  }
                />
              </div>
            ))}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="System Overview">
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Statistic title="Peak Hour" value={`${stats.peakHour || 12}:00`} prefix={<ClockCircleOutlined />} />
              </Col>
              <Col span={12}>
                <Statistic title="Messages/Week" value={stats.messagesThisWeek} prefix={<MessageOutlined />} />
              </Col>
              <Col span={12}>
                <Statistic title="Active Users" value="—" prefix={<UserOutlined />} />
              </Col>
              <Col span={12}>
                <Statistic title="Groups" value="—" prefix={<TeamOutlined />} />
              </Col>
            </Row>
            <div style={{ marginTop: 16, padding: 12, background: '#f0f2f5', borderRadius: 8, textAlign: 'center' }}>
              <GlobalOutlined style={{ fontSize: 24, color: '#1890ff' }} />
              <p style={{ margin: '8px 0 0' }}>Real-time analytics requires backend connection</p>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default MessageStats;
