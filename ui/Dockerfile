FROM node:16-alpine3.11 as react-build
WORKDIR /app
COPY . ./
RUN npm install
RUN npm run build --prod

FROM nginx:1.21
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=react-build /app/build /usr/share/nginx/html
EXPOSE 80
