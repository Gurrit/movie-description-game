FROM node:16 as BUILD

WORKDIR /usr/src/app/

COPY package.json package.json
COPY tsconfig.json tsconfig.json
# COPY yarn.lock yarn.lock

RUN yarn --production

COPY src src
COPY public public

ENV REACT_APP_BACKEND_IP 

RUN yarn build 

FROM nginx:1.21.1 as FRONTEND

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=BUILD /usr/src/app/build/ /usr/share/nginx/html/


CMD ["nginx", "-g", "daemon off;"]
