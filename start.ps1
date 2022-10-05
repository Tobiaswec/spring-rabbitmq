param(
    [Parameter()]
    [int]$workers = 1
)

cd consumer-calc-persist
./gradlew bootJar
cd ..
cd consumer-log
./gradlew bootJar
cd ..
cd producer-service
./gradlew bootJar
cd ..
docker-compose up --build -d --scale consumer-calc-persist-service=$workers