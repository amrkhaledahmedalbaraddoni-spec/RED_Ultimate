import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Tag, Button, message } from 'antd';

const DuminMonitor = () => {
  const [status, setStatus] = useState(null);

  const fetchStatus = async () => {
    try {
      const res = await fetch('/api/pstn/sim');
      if (res.ok) setStatus(await res.json());
    } catch {
      /* ignore */
    }
  };

  useEffect(() => { fetchStatus(); const i = setInterval(fetchStatus, 5000); return () => clearInterval(i); }, []);

  const reachable = status && status.status !== 'UNREACHABLE';

  return (
    <div style={{ padding: 24 }}>
      <h1>PSTN / Dumin Hardware Monitor</h1>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="Gateway">
            <p>Reachability: {reachable
              ? <Tag color="green">ONLINE</Tag>
              : <Tag color="red">OFFLINE</Tag>}</p>
            <p>Raw status: {JSON.stringify(status)}</p>
            <Button onClick={() => { fetchStatus(); message.info('Refreshed'); }}>Refresh</Button>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default DuminMonitor;
