import React from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import {
  DashboardOutlined,
  SafetyCertificateOutlined,
  DesktopOutlined,
  PhoneOutlined,
  MonitorOutlined
} from '@ant-design/icons';

import LiveMonitor from './components/LiveMonitor';
import Approvals from './pages/Approvals';
import Diagnostics from './pages/Diagnostics';
import DuminMonitor from './pages/DuminMonitor';

const { Header, Sider, Content } = Layout;

const App = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const items = [
    { key: '/', icon: <DashboardOutlined />, label: 'Dashboard' },
    { key: '/approvals', icon: <SafetyCertificateOutlined />, label: 'Approvals' },
    { key: '/diagnostics', icon: <DesktopOutlined />, label: 'Diagnostics' },
    { key: '/dumin', icon: <PhoneOutlined />, label: 'Dumin / PSTN' }
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible breakpoint="lg">
        <div style={{ height: 48, margin: 16, color: '#fff', fontWeight: 700, textAlign: 'center', fontSize: 18 }}>
          🔴 RED
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={items}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#001529', color: '#fff', padding: '0 24px', fontSize: 18 }}>
          RED Sovereign Admin
        </Header>
        <Content style={{ margin: 24 }}>
          <Routes>
            <Route path="/" element={<LiveMonitor />} />
            <Route path="/approvals" element={<Approvals />} />
            <Route path="/diagnostics" element={<Diagnostics />} />
            <Route path="/dumin" element={<DuminMonitor />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
};

export default App;
