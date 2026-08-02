const mediasoup = require('mediasoup');

async function startSfu() {
    const worker = await mediasoup.createWorker();
    
    const router = await worker.createRouter({
        mediaCodecs: [
            {
                kind: 'audio',
                mimeType: 'audio/opus',
                clockRate: 48000,
                channels: 2
            },
            {
                kind: 'video',
                mimeType: 'video/VP9',
                clockRate: 90000,
                parameters: {
                    'profile-id': 2,
                    'bitrate': 4000000 // Support up to 4Mbps for Live
                }
            }
        ]
    });

    console.log('🔴 RED Media SFU: Live Streaming Engine READY (VP9 1080p)');
}

startSfu();
