import React, { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, message } from 'antd';

const Approvals = () => {
  const [pendingUsers, setPendingUsers] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/users/pending');
      if (res.ok) setPendingUsers(await res.json());
    } catch {
      /* ignore */
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const act = async (userId, endpoint, label) => {
    const res = await fetch(`/api/admin/users/${userId}/${endpoint}`, { method: 'POST' });
    if (res.ok) {
      message.success(`User ${label}`);
      fetchUsers();
    } else {
      message.error('Action failed');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', ellipsis: true },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Name', dataIndex: 'fullName', key: 'fullName' },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s) => <Tag color="orange">{s}</Tag> },
    { title: 'Action', key: 'action', render: (_, record) => (
      <Space>
        <Button type="primary" onClick={() => act(record.id, 'approve', 'approved')}>Approve</Button>
        <Button onClick={() => act(record.id, 'reject', 'rejected')}>Reject</Button>
        <Button danger onClick={() => act(record.id, 'ban', 'banned')}>Ban</Button>
      </Space>
    )}
  ];

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <h1>Pending User Approvals</h1>
      <Table dataSource={pendingUsers} columns={columns} rowKey="id" loading={loading} />
    </div>
  );
};

export default Approvals;
