import React, { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, message, Card } from 'antd';

const UserApproval: React.FC = () => {
    const [pendingUsers, setPendingUsers] = useState<any[]>([]);

    const fetchUsers = async () => {
        // Fetch from real Spring Boot endpoint
        const response = await fetch('/api/auth/admin/pending-list');
        if (response.ok) {
            const data = await response.json();
            setPendingUsers(data);
        }
    };

    useEffect(() => { fetchUsers(); }, []);

    const handleAction = async (email: string, action: 'APPROVED' | 'BANNED') => {
        const response = await fetch(`/api/auth/admin/approve?email=${email}&status=${action}`, { method: 'POST' });
        if (response.ok) {
            message.success(`RED: User ${email} is now ${action}`);
            fetchUsers();
        }
    };

    const columns = [
        { title: 'Email', dataIndex: 'email', key: 'email' },
        { title: 'Registered Date', dataIndex: 'date', key: 'date' },
        { title: 'Status', key: 'status', render: () => <Tag color="orange">PENDING</Tag> },
        { title: 'Operations', key: 'action', render: (_: any, record: any) => (
            <Space>
                <Button type="primary" onClick={() => handleAction(record.email, 'APPROVED')}>Approve</Button>
                <Button danger onClick={() => handleAction(record.email, 'BANNED')}>Ban</Button>
            </Space>
        ) },
    ];

    return (
        <Card title="🔴 RED Sovereign Account Management">
            <Table dataSource={pendingUsers} columns={columns} rowKey="email" />
        </Card>
    );
};

export default UserApproval;
