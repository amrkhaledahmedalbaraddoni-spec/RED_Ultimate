import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Space, Input, Card, Modal, Descriptions, message, Tooltip } from 'antd';
import { SearchOutlined, UserOutlined, SafetyCertificateOutlined, StopOutlined, CrownOutlined, ReloadOutlined } from '@ant-design/icons';

/**
 * User Management page — full CRUD for all users.
 */
const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/admin/users');
      if (res.ok) setUsers(await res.json());
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

  const statusColors = {
    'PENDING': 'orange',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'BANNED': 'default'
  };

  const roleColors = {
    'ADMIN': 'gold',
    'USER': 'blue'
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'fullName',
      key: 'fullName',
      render: (name, record) => (
        <Button type="link" onClick={() => setSelectedUser(record)}>
          {name}
        </Button>
      ),
      sorter: (a, b) => a.fullName?.localeCompare(b.fullName)
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      ellipsis: true
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <Tag color={statusColors[s]}>{s}</Tag>,
      filters: Object.keys(statusColors).map(s => ({ text: s, value: s })),
      onFilter: (value, record) => record.status === value
    },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      render: (r) => <Tag color={roleColors[r]}>{r}</Tag>
    },
    {
      title: 'Phone',
      dataIndex: 'phoneNumber',
      key: 'phoneNumber',
      render: (p) => p || '-'
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (ts) => new Date(ts).toLocaleDateString(),
      sorter: (a, b) => a.createdAt - b.createdAt
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Tooltip title="Approve">
            <Button
              size="small"
              type="primary"
              icon={<SafetyCertificateOutlined />}
              onClick={() => act(record.id, 'approve', 'approved')}
              disabled={record.status === 'APPROVED'}
            />
          </Tooltip>
          <Tooltip title="Promote to Admin">
            <Button
              size="small"
              icon={<CrownOutlined />}
              onClick={() => act(record.id, 'promote', 'promoted')}
              disabled={record.role === 'ADMIN'}
            />
          </Tooltip>
          <Tooltip title="Ban">
            <Button
              size="small"
              danger
              icon={<StopOutlined />}
              onClick={() => act(record.id, 'ban', 'banned')}
              disabled={record.status === 'BANNED'}
            />
          </Tooltip>
        </Space>
      )
    }
  ];

  const filteredUsers = searchText
    ? users.filter(u =>
        u.fullName?.toLowerCase().includes(searchText.toLowerCase()) ||
        u.email?.toLowerCase().includes(searchText.toLowerCase())
      )
    : users;

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <h1><UserOutlined /> User Management</h1>
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            prefix={<SearchOutlined />}
            placeholder="Search users..."
            value={searchText}
            onChange={e => setSearchText(e.target.value)}
            style={{ width: 300 }}
          />
          <Button icon={<ReloadOutlined />} onClick={fetchUsers} loading={loading}>
            Refresh
          </Button>
        </Space>
      </Card>
      <Table
        dataSource={filteredUsers}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} users` }}
      />

      <Modal
        title="User Details"
        open={!!selectedUser}
        onCancel={() => setSelectedUser(null)}
        footer={null}
        width={600}
      >
        {selectedUser && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="ID">{selectedUser.id}</Descriptions.Item>
            <Descriptions.Item label="Name">{selectedUser.fullName}</Descriptions.Item>
            <Descriptions.Item label="Email">{selectedUser.email}</Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color={statusColors[selectedUser.status]}>{selectedUser.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Role">
              <Tag color={roleColors[selectedUser.role]}>{selectedUser.role}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Phone">{selectedUser.phoneNumber || 'Not set'}</Descriptions.Item>
            <Descriptions.Item label="Created">
              {new Date(selectedUser.createdAt).toLocaleString()}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default UserManagement;
