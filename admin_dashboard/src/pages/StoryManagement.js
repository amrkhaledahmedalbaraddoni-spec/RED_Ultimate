import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Card, Space, message, Popconfirm } from 'antd';
import { PictureOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';

/**
 * Story Management page — view and moderate stories.
 */
const StoryManagement = () => {
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchStories = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/stories');
      if (res.ok) setStories(await res.json());
    } catch {
      /* ignore */
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchStories(); }, []);

  const deleteStory = async (id) => {
    try {
      const res = await fetch(`/api/stories/${id}`, { method: 'DELETE' });
      if (res.ok) {
        message.success('Story deleted');
        fetchStories();
      } else {
        message.error('Delete failed');
      }
    } catch {
      message.error('Network error');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      ellipsis: true,
      width: 120
    },
    {
      title: 'Owner',
      dataIndex: 'ownerId',
      key: 'ownerId',
      ellipsis: true,
      width: 120
    },
    {
      title: 'Media URL',
      dataIndex: 'mediaUrl',
      key: 'mediaUrl',
      ellipsis: true,
      render: (url) => <a href={url} target="_blank" rel="noopener noreferrer">{url}</a>
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (ts) => new Date(ts).toLocaleString(),
      sorter: (a, b) => a.createdAt - b.createdAt
    },
    {
      title: 'Expires',
      dataIndex: 'expiresAt',
      key: 'expiresAt',
      render: (ts) => {
        const remaining = ts - Date.now();
        const color = remaining < 3600000 ? 'red' : remaining < 14400000 ? 'orange' : 'green';
        return <Tag color={color}>{remaining > 0 ? `${Math.round(remaining / 3600000)}h left` : 'Expired'}</Tag>;
      }
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Popconfirm
          title="Delete this story?"
          description="This action cannot be undone."
          onConfirm={() => deleteStory(record.id)}
          okText="Yes, Delete"
          cancelText="Cancel"
        >
          <Button danger size="small" icon={<DeleteOutlined />}>
            Delete
          </Button>
        </Popconfirm>
      )
    }
  ];

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <h1><PictureOutlined /> Story Management</h1>
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={fetchStories} loading={loading}>
            Refresh
          </Button>
          <Tag color="blue">{stories.length} active stories</Tag>
        </Space>
      </Card>
      <Table
        dataSource={stories}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 20, showTotal: (total) => `${total} stories` }}
      />
    </div>
  );
};

export default StoryManagement;
