import React, { useState, useEffect } from 'react';
import {
  Card, Form, Input, Switch, Button, Space, Divider, message, Row, Col,
  Typography, Tabs, InputNumber, Select, Alert, Tag, Tooltip
} from 'antd';
import {
  SettingOutlined, SafetyCertificateOutlined, BellOutlined,
  CloudServerOutlined, MailOutlined, LockOutlined, GlobalOutlined,
  SaveOutlined, ReloadOutlined, ApiOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { Option } = Select;

/**
 * Settings page — full system configuration.
 */
const Settings = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saved, setSaved] = useState(false);
  const [activeTab, setActiveTab] = useState('general');

  // Load settings from localStorage (simulates backend API)
  useEffect(() => {
    const stored = localStorage.getItem('red_admin_settings');
    if (stored) {
      try {
        form.setFieldsValue(JSON.parse(stored));
      } catch { /* ignore */ }
    } else {
      form.setFieldsValue({
        // General
        instanceName: 'RED Sovereign',
        defaultLocale: 'en',
        maxUploadSizeMB: 50,
        enableRegistration: true,
        enableStories: true,
        enablePstn: true,
        enableVideoCalls: true,
        enableGroups: true,
        maintenanceMode: false,

        // Security
        requireAdminApproval: true,
        passwordMinLength: 8,
        enableTwoFactor: false,
        maxLoginAttempts: 5,
        sessionTimeoutMinutes: 60,
        enableKillSwitch: true,
        enableAuditLog: true,
        enableIpWhitelist: false,
        ipWhitelist: '',

        // Notifications
        enableEmailNotifications: false,
        enablePushNotifications: true,
        smtpHost: '',
        smtpPort: 587,
        smtpUser: '',
        smtpFrom: 'noreply@red-ultimate.local',
        notificationRateLimit: 10,

        // Server
        apiBaseUrl: 'http://192.168.1.50:8080',
        wsUrl: 'ws://192.168.1.50:8080/ws/chat',
        redisUrl: 'redis://redis:6379',
        mongodbUrl: 'mongodb://mongo:27017/red',
        postgresUrl: 'jdbc:postgresql://db:5432/red',
        minioEndpoint: 'minio:9000',
        enableTls: false,
        tlsCertPath: '',
        tlsKeyPath: '',
      });
    }
  }, [form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      // Simulate API call
      localStorage.setItem('red_admin_settings', JSON.stringify(values));
      await new Promise(r => setTimeout(r, 500));
      message.success('Settings saved successfully');
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      message.error('Please fix validation errors');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    localStorage.removeItem('red_admin_settings');
    form.resetFields();
    message.info('Settings reset to defaults');
  };

  const generalTab = (
    <Row gutter={[24, 16]}>
      <Col span={24}>
        <Card title="Instance Settings" size="small">
          <Form.Item label="Instance Name" name="instanceName" rules={[{ required: true }]}>
            <Input placeholder="RED Sovereign" />
          </Form.Item>
          <Form.Item label="Default Language" name="defaultLocale">
            <Select>
              <Option value="en">English</Option>
              <Option value="ar">العربية</Option>
              <Option value="fr">Français</Option>
              <Option value="es">Español</Option>
              <Option value="de">Deutsch</Option>
            </Select>
          </Form.Item>
          <Form.Item label="Max Upload Size (MB)" name="maxUploadSizeMB">
            <InputNumber min={1} max={500} style={{ width: '100%' }} />
          </Form.Item>
        </Card>
      </Col>
      <Col span={24}>
        <Card title="Feature Flags" size="small">
          <Form.Item label="Enable Registration" name="enableRegistration" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable Stories" name="enableStories" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable PSTN Calls" name="enablePstn" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable Video Calls" name="enableVideoCalls" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable Group Chats" name="enableGroups" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Maintenance Mode" name="maintenanceMode" valuePropName="checked">
            <Switch />
          </Form.Item>
          {form.getFieldValue('maintenanceMode') && (
            <Alert
              message="Maintenance mode is ON — users will see a maintenance page"
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
        </Card>
      </Col>
    </Row>
  );

  const securityTab = (
    <Row gutter={[24, 16]}>
      <Col span={24}>
        <Card title="Authentication" size="small">
          <Form.Item label="Require Admin Approval" name="requireAdminApproval" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Minimum Password Length" name="passwordMinLength">
            <InputNumber min={6} max={128} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="Enable Two-Factor Auth" name="enableTwoFactor" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Max Login Attempts" name="maxLoginAttempts">
            <InputNumber min={1} max={20} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="Session Timeout (minutes)" name="sessionTimeoutMinutes">
            <InputNumber min={5} max={1440} style={{ width: '100%' }} />
          </Form.Item>
        </Card>
      </Col>
      <Col span={24}>
        <Card title="Security Features" size="small">
          <Form.Item label="Enable Kill Switch" name="enableKillSwitch" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable Audit Log" name="enableAuditLog" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable IP Whitelist" name="enableIpWhitelist" valuePropName="checked">
            <Switch />
          </Form.Item>
          {form.getFieldValue('enableIpWhitelist') && (
            <Form.Item label="Allowed IPs (comma-separated)" name="ipWhitelist">
              <Input.TextArea rows={3} placeholder="192.168.1.0/24, 10.0.0.1" />
            </Form.Item>
          )}
        </Card>
      </Col>
    </Row>
  );

  const notificationsTab = (
    <Row gutter={[24, 16]}>
      <Col span={24}>
        <Card title="Notification Channels" size="small">
          <Form.Item label="Enable Email Notifications" name="enableEmailNotifications" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Enable Push Notifications (FCM)" name="enablePushNotifications" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Notification Rate Limit (per hour)" name="notificationRateLimit">
            <InputNumber min={1} max={1000} style={{ width: '100%' }} />
          </Form.Item>
        </Card>
      </Col>
      <Col span={24}>
        <Card title="SMTP Configuration" size="small">
          <Form.Item label="SMTP Host" name="smtpHost">
            <Input placeholder="smtp.example.com" />
          </Form.Item>
          <Form.Item label="SMTP Port" name="smtpPort">
            <InputNumber min={1} max={65535} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="SMTP Username" name="smtpUser">
            <Input placeholder="user@example.com" />
          </Form.Item>
          <Form.Item label="From Address" name="smtpFrom">
            <Input placeholder="noreply@red-ultimate.local" />
          </Form.Item>
          <Form.Item label="SMTP Password" name="smtpPassword">
            <Input.Password placeholder="••••••••" />
          </Form.Item>
        </Card>
      </Col>
    </Row>
  );

  const serverTab = (
    <Row gutter={[24, 16]}>
      <Col span={24}>
        <Card title="API Endpoints" size="small">
          <Form.Item label="API Base URL" name="apiBaseUrl">
            <Input addonBefore={<ApiOutlined />} />
          </Form.Item>
          <Form.Item label="WebSocket URL" name="wsUrl">
            <Input addonBefore={<GlobalOutlined />} />
          </Form.Item>
        </Card>
      </Col>
      <Col span={24}>
        <Card title="Database Connections" size="small">
          <Form.Item label="Redis URL" name="redisUrl">
            <Input addonBefore={<CloudServerOutlined />} />
          </Form.Item>
          <Form.Item label="MongoDB URL" name="mongodbUrl">
            <Input addonBefore={<CloudServerOutlined />} />
          </Form.Item>
          <Form.Item label="PostgreSQL URL" name="postgresUrl">
            <Input addonBefore={<CloudServerOutlined />} />
          </Form.Item>
          <Form.Item label="MinIO Endpoint" name="minioEndpoint">
            <Input addonBefore={<CloudServerOutlined />} />
          </Form.Item>
        </Card>
      </Col>
      <Col span={24}>
        <Card title="TLS Configuration" size="small">
          <Form.Item label="Enable TLS" name="enableTls" valuePropName="checked">
            <Switch />
          </Form.Item>
          {form.getFieldValue('enableTls') && (
            <>
              <Form.Item label="Certificate Path" name="tlsCertPath">
                <Input placeholder="/etc/ssl/certs/red.pem" />
              </Form.Item>
              <Form.Item label="Private Key Path" name="tlsKeyPath">
                <Input placeholder="/etc/ssl/private/red.key" />
              </Form.Item>
            </>
          )}
        </Card>
      </Col>
    </Row>
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>
          <SettingOutlined /> System Settings
        </Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>Reset</Button>
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={loading}>Save</Button>
        </Space>
      </div>
      {saved && <Alert message="Settings saved successfully" type="success" showIcon style={{ marginBottom: 16 }} />}
      <Form form={form} layout="vertical">
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'general', label: <span><GlobalOutlined /> General</span>, children: generalTab },
          { key: 'security', label: <span><SafetyCertificateOutlined /> Security</span>, children: securityTab },
          { key: 'notifications', label: <span><BellOutlined /> Notifications</span>, children: notificationsTab },
          { key: 'server', label: <span><CloudServerOutlined /> Server</span>, children: serverTab },
        ]} />
      </Form>
    </div>
  );
};

export default Settings;
