
#!/bin/bash

docker-compose -f docker-compose.local.ci.yml up --exit-code-from prepago
docker-compose -f docker-compose.local.ci.yml down