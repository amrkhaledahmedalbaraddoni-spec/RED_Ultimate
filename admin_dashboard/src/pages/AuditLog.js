import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Space, Input, Card } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';

/**
 * Audit Log page — shows security-relevant events from the backend.
 */
const AuditLog = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const since = Date.now() - 7 * 24 * 60 * 60 * 1000; // Last 7 days
      const res = await fetch(`/api/admin/audit?since=${since}&limit=500`);
      if (res.ok) {
        setEvents(await res.json());
      }
    } catch {
      /* ignore */
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchEvents(); }, []);

  const actionColors = {
    'USER_APPROVED': 'green',
    'USER_REJECTED': 'orange',
    'USER_BANNED': 'red',
    'USER_PROMOTED': 'blue',
    'KILL_SWITCH': 'red',
    'USER_STATUS_CHANGED': 'purple',
    'LOGIN_SUCCESS': 'green',
    'LOGIN_FAILURE': 'red',
  };

  const columns = [
    {
      title: 'Time',
      dataIndex: 'timestamp',
      key: 'timestamp',
      render: (ts) => new Date(ts).toLocaleString(),
      sorter: (a, b) => a.timestamp - b.timestamp,
      defaultSortOrder: 'descend'
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      render: (action) => (
        <Tag color={actionColors[action] || 'default'}>{action}</Tag>
      ),
      filters: Object.keys(actionColors).map(a => ({ text: a, value: a })),
      onFilter: (value, record) => record.action === value
    },
    {
      title: 'Actor',
      dataIndex: 'actorId',
      key: 'actorId',
      ellipsis: true
    },
    {
      title: 'Target',
      dataIndex: 'targetId',
      key: 'targetId',
      ellipsis: true
    },
    {
      title: 'Details',
      dataIndex: 'details',
      key: 'details',
      ellipsis: true
    }
  ];

  const filteredEvents = searchText
    ? events.filter(e =>
        e.action?.toLowerCase().includes(searchText.toLowerCase()) ||
        e.actorId?.toLowerCase().includes(searchText.toLowerCase()) ||
        e.targetId?.toLowerCase().includes(searchText.toLowerCase()) ||
        e.details?.toLowerCase().includes(searchText.toLowerCase())
      )
    : events;

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <h1>🔴 Security Audit Log</h1>
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            prefix={<SearchOutlined />}
            placeholder="Search events..."
            value={searchText}
            onChange={e => setSearchText(e.target.value)}
            style={{ width: 300 }}
          />
          <Button icon={<ReloadOutlined />} onClick={fetchEvents} loading={loading}>
            Refresh
          </Button>
        </Space>
      </Card>
      <Table
        dataSource={filteredEvents}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 25, showSizeChanger: true, showTotal: (total) => `${total} events` }}
        scroll={{ y: 600 }}
      />
    </div>
  );
};

export default AuditLog;
