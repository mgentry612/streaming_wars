# streaming_wars_api

docker build -t gatech/streamingwars_api -f Dockerfile ./

docker run -p 8080:8080 gatech/streamingwars_api

url: 18.117.138.183:8080


example endpoint: 18.117.138.183:8080/demographic?short_name=testname



## Git workflow
First time: git clone <this_url>

Get latest code updates: git pull origin main

Make changes

Stage changes: git add -A

Commit changes: git commit -m "add a message describing your changes"

Push changes: git push origin main
