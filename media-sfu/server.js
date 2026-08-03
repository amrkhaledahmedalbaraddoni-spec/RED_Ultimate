/**
 * RED Media SFU — Mediasoup signaling server (System A).
 *
 * Exposes a WebSocket on :4000 speaking a small JSON protocol:
 *   getRouterRtpCapabilities / createWebRtcTransport / connectTransport / produce / consume
 *
 * The router is configured for 4K-capable codecs: AV1, VP9 and H.264 (fallback) plus Opus audio.
 */
const mediasoup = require('mediasoup');
const http = require('http');
const WebSocket = require('ws');

const PORT = process.env.PORT || 4000;
const ANNOUNCED_IP = process.env.ANNOUNCED_IP || '127.0.0.1';

const ROUTER_RTP_CAPABILITIES = {
  codecs: [
    { kind: 'audio', mimeType: 'audio/opus', clockRate: 48000, channels: 2 },
    { kind: 'video', mimeType: 'video/AV1', clockRate: 90000, parameters: { ' scalabilityMode': 'L1T3' } },
    { kind: 'video', mimeType: 'video/VP9', clockRate: 90000, parameters: { 'profile-id': 2, 'scalabilityMode': 'L3T3' } },
    { kind: 'video', mimeType: 'video/h264', clockRate: 90000, parameters: { 'packetization-mode': 1, 'profile-level-id': '640c1f' } }
  ],
  rtcpFeedback: [
    { type: 'nack' },
    { type: 'nack', parameter: 'pli' },
    { type: 'ccm', parameter: 'fir' },
    { type: 'goog-remb' }
  ]
};

let worker;
let router;
const transports = new Map();   // id -> WebRtcTransport
const producers = new Map();    // id -> Producer
const peers = new Map();        // ws -> { transports: Set, producers: Set, consumers: Set }

async function bootstrap() {
  worker = await mediasoup.createWorker({ logLevel: 'warn' });
  router = await worker.createRouter({ mediaCodecs: ROUTER_RTP_CAPABILITIES });
  console.log(`RED Media SFU ready (AV1/VP9 4K) on :${PORT}, announced ip ${ANNOUNCED_IP}`);

  const server = http.createServer();
  const wss = new WebSocket.Server({ server });

  wss.on('connection', (ws) => onPeer(ws));

  server.listen(PORT);
}

async function onPeer(ws) {
  const peer = { transports: new Set(), producers: new Set(), consumers: new Set() };
  peers.set(ws, peer);

  ws.on('message', async (raw) => {
    let msg;
    try {
      msg = JSON.parse(raw.toString());
    } catch {
      return send(ws, { id: '', error: 'invalid json' });
    }
    try {
      switch (msg.action) {
        case 'getRouterRtpCapabilities':
          return send(ws, { id: msg.id, rtpCapabilities: router.rtpCapabilities });

        case 'createWebRtcTransport': {
          const transport = await router.createWebRtcTransport({
            listenIps: [{ ip: '0.0.0.0', announcedIp: ANNOUNCED_IP }],
            enableUdp: true,
            enableTcp: true,
            preferUdp: true,
            initialAvailableOutgoingBitrate: msg.producer ? 8_000_000 : 2_000_000
          });
          transports.set(transport.id, transport);
          peer.transports.add(transport.id);
          return send(ws, {
            id: msg.id,
            transport: {
              id: transport.id,
              iceParameters: transport.iceParameters,
              iceCandidates: transport.iceCandidates,
              dtlsParameters: transport.dtlsParameters
            }
          });
        }

        case 'connectTransport': {
          const transport = transports.get(msg.transportId);
          if (!transport) return send(ws, { id: msg.id, error: 'no transport' });
          await transport.connect({ dtlsParameters: msg.dtlsParameters });
          return send(ws, { id: msg.id, connected: true });
        }

        case 'produce': {
          const transport = transports.get(msg.transportId);
          if (!transport) return send(ws, { id: msg.id, error: 'no transport' });
          const producer = await transport.produce({
            kind: msg.kind,
            rtpParameters: msg.rtpParameters,
            // 4K-friendly encoding caps
            ...(msg.kind === 'video' ? { encodings: [{ maxBitrate: 8_000_000, scaleResolutionDownBy: 1 }] } : {})
          });
          producers.set(producer.id, producer);
          peer.producers.add(producer.id);
          return send(ws, { id: msg.id, producerId: producer.id });
        }

        case 'consume': {
          const producer = producers.get(msg.producerId);
          const transport = transports.get(msg.transportId);
          if (!producer || !transport) return send(ws, { id: msg.id, error: 'missing producer/transport' });
          if (!router.canConsume({ producerId: producer.id, rtpCapabilities: msg.rtpCapabilities })) {
            return send(ws, { id: msg.id, error: 'cannot consume' });
          }
          const consumer = await transport.consume({
            producerId: producer.id,
            rtpCapabilities: msg.rtpCapabilities,
            paused: true
          });
          peer.consumers.add(consumer.id);
          return send(ws, {
            id: msg.id,
            consumer: {
              id: consumer.id,
              producerId: producer.id,
              kind: consumer.kind,
              rtpParameters: consumer.rtpParameters
            }
          });
        }

        default:
          return send(ws, { id: msg.id, error: `unknown action ${msg.action}` });
      }
    } catch (e) {
      send(ws, { id: msg.id, error: e.message });
    }
  });

  ws.on('close', () => {
    const p = peers.get(ws);
    if (p) {
      [...p.transports].forEach((id) => transports.get(id)?.close());
      [...p.producers].forEach((id) => producers.get(id)?.close());
    }
    peers.delete(ws);
  });
}

function send(ws, obj) {
  if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(obj));
}

bootstrap().catch((e) => {
  console.error('SFU failed to start', e);
  process.exit(1);
});
