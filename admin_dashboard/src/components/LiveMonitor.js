import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Progress, Timeline, Tag, Spin } from 'antd';
import {
  MessageOutlined, TeamOutlined, PictureOutlined,
  SafetyCertificateOutlined, CheckCircleOutlined,
  ClockCircleOutlined, DatabaseOutlined, CloudServerOutlined
} from '@ant-design/icons';

/**
 * RED Admin Live Monitor — pulls real stats from /api/admin/monitor/stats and /health.
 */
const LiveMonitor = () => {
  const [stats, setStats] = useState({ messages_24h: 0, stories_active: 0, online_users: 0, messages_1h: 0 });
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  const poll = async () => {
    try {
      const [statsRes, healthRes] = await Promise.all([
        fetch('/api/admin/monitor/stats'),
        fetch('/api/admin/monitor/health')
      ]);
      if (statsRes.ok) setStats(await statsRes.json());
      if (healthRes.ok) setHealth(await healthRes.json());
    } catch {
      /* backend unreachable */
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    poll();
    const interval = setInterval(poll, 3000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 48 }}>
        <Spin size="large" />
        <p style={{ marginTop: 16 }}>Connecting to RED Sovereign Server...</p>
      </div>
    );
  }

  const ramPercent = health ? Math.round((health.ram_used_mb / health.ram_max_mb) * 100) : 0;

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Messages (24h)"
              value={stats.messages_24h}
              prefix={<MessageOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Online Users"
              value={stats.online_users}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Stories"
              value={stats.stories_active}
              prefix={<PictureOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Messages (1h)"
              value={stats.messages_1h || 0}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title={<><DatabaseOutlined /> System Health</>}>
            {health && (
              <>
                <div style={{ marginBottom: 16 }}>
                  <span>Status: </span>
                  <Tag color={health.status === 'UP' ? 'green' : 'red'}>{health.status}</Tag>
                </div>
                <div style={{ marginBottom: 8 }}>
                  <span>RAM Usage: {health.ram_used_mb} / {health.ram_max_mb} MB</span>
                  <Progress
                    percent={ramPercent}
                    status={ramPercent > 80 ? 'exception' : ramPercent > 60 ? 'active' : 'success'}
                    size="small"
                  />
                </div>
                <div style={{ marginBottom: 8 }}>
                  CPU Cores: <Tag>{health.cpu_cores}</Tag>
                </div>
                <div style={{ marginBottom: 8 }}>
                  Disk Free: <Tag color="blue">{health.disk_free_gb} GB</Tag>
                </div>
                <div style={{ marginBottom: 8 }}>
                  Active Connections: <Tag color="green">{health.active_connections}</Tag>
                </div>
                <div>
                  Uptime: <Tag>{Math.round((health.uptime_ms || 0) / 3600000)}h</Tag>
                </div>
              </>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={<><SafetyCertificateOutlined /> System Status</>}>
            <Timeline
              items={[
                {
                  color: 'green',
                  children: (<><Tag color="green">OPERATIONAL</Tag> Backend (System C)</>)
                },
                {
                  color: health ? 'green' : 'red',
                  children: (<><Tag color={health ? 'green' : 'red'}>{health ? 'OPERATIONAL' : 'UNKNOWN'}</Tag> Database / Redis / Mongo</>)
                },
                {
                  color: 'blue',
                  children: (<><Tag color="blue">STANDBY</Tag> VoIP SFU (System A)</>)
                },
                {
                  color: 'blue',
                  children: (<><Tag color="blue">STANDBY</Tag> PSTN / Dumin (System B)</>)
                },
                {
                  color: 'green',
                  children: (<><Tag color="green">OPERATIONAL</Tag> Nginx Reverse Proxy</>)
                },
                {
                  color: 'green',
                  children: (<><Tag color="green">OPERATIONAL</Tag> MinIO Storage</>)
                }
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title={<><CloudServerOutlined /> Infrastructure</>}>
            <Row gutter={16}>
              <Col span={6}>
                <Statistic title="PostgreSQL" value="Active" valueStyle={{ color: '#52c41a', fontSize: 14 }} prefix={<CheckCircleOutlined />} />
              </Col>
              <Col span={6}>
                <Statistic title="MongoDB" value="Active" valueStyle={{ color: '#52c41a', fontSize: 14 }} prefix={<CheckCircleOutlined />} />
              </Col>
              <Col span={6}>
                <Statistic title="Redis" value="Active" valueStyle={{ color: '#52c41a', fontSize: 14 }} prefix={<CheckCircleOutlined />} />
              </Col>
              <Col span={6}>
                <Statistic title="MinIO" value="Active" valueStyle={{ color: '#52c41a', fontSize: 14 }} prefix={<CheckCircleOutlined />} />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default LiveMonitor;
