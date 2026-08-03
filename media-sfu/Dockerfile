FROM node:20-bookworm-slim

# Build tools required to compile mediasoup native worker
RUN apt-get update && apt-get install -y --no-install-recommends python3 make g++ && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev
COPY server.js ./

ENV PORT=4000
ENV ANNOUNCED_IP=127.0.0.1
EXPOSE 4000 40000-40100/udp

CMD ["node", "server.js"]
