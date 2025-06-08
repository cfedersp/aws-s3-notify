
# Initialized by:
spring init --name aws-s3-notify --java-version=17 --package-name=com.epsilon.auto.poc --group=com.epsilon.auto.poc --artifact=aws-s3-notify --type=maven-project

# Login to AWS 
Requires AWS Identity Center login, including URL, username and pw
aws configure sso

# Test S3 access:
aws s3 ls s3://cjf-epsilon-demos/ --profile AdministratorAccess-339713066603

# Build deployable jar:
mvn clean package spring-boot:repackage -Dmaven.test.skip=true

# Build + Test deployable jar:
mvn clean package spring-boot:repackage -Daws.profile=AdministratorAccess-339713066603
or
update main/resources/application-local.yaml
mvn clean package spring-boot:repackage -Dspring.profiles.active=test,local


# Local Run:
java -jar target/aws-s3-notify-1.0.jar --spring.profiles.active=local


# Deploy the app:
scp target/aws-s3-notify-1.0.jar ec2-user@ec2-54-183-194-74.us-west-1.compute.amazonaws.com:~/

# Deploy via S3:
aws s3 cp target/aws-s3-notify-1.0.jar s3://cjf-epsilon-demos/installers/aws-demos/ --profile AdministratorAccess-339713066603

# Run command on EC2
The profile is optional, and the EC2 instance should already have an instance profile connected to an IAM Role that can access your bucket
java -jar aws-s3-notify-1.0.jar 

# Run from an SSH session:
ssh ec2-user@ec2-54-183-194-74.us-west-1.compute.amazonaws.com
sudo nohup java -jar aws-s3-notify-0.0.1-SNAPSHOT.jar &

# SNS Confirmation structure:
{
"Type": "SubscriptionConfirmation"
"MessageId": "aacc53d8-229d-4243-9b96-0f307791ba48",
"SigningCertURL" : "https://sns.us-west-1.amazonaws.com/SimpleNotificationService-9c6465fa7f48f5cacd23014631ec1136.pem"
"Token" : "2336412f37fb687f5d51e6e2425a8a5875c3b50f84304b9bf45da7db7b17d6aef16357ead2020dfdb5e465c4be709663891d1b9eef77cc881fd3d6efe0ea9a2847549e45f9a09d5667f899bc855e3b33ff1e34ed74dbec0db63d5328ab3c90bbe27411271f82995424759459e4f5f4cf50a797f12ef88a3aeb0f1dddb4f2e3592379a90f7766144657d9ee850165e756",
"TopicArn" : "arn:aws:sns:us-west-1:339713066603:cjf-epsilon-demos-notify-topic",
"Message" : "You have chosen to subscribe to the topic arn:aws:sns:us-west-1:339713066603:cjf-epsilon-demos-notify-topic.\nTo confirm the subscription, visit the SubscribeURL included in this message.",
"SubscribeURL" : "https://sns.us-west-1.amazonaws.com/?Action=ConfirmSubscription&TopicArn=arn:aws:sns:us-west-1:339713066603:cjf-epsilon-demos-notify-topic&Token=2336412f37fb687f5d51e6e2425a8a5875c3b50f84304b9bf45da7db7b17d6aef16357ead2020dfdb5e465c4be709663891d1b9eef77cc881fd3d6efe0ea9a2847549e45f9a09d5667f899bc855e3b33ff1e34ed74dbec0db63d5328ab3c90bbe27411271f82995424759459e4f5f4cf50a797f12ef88a3aeb0f1dddb4f2e3592379a90f7766144657d9ee850165e756",
"Timestamp" : "2025-05-08T17:43:49.876Z",
"SignatureVersion" : "1",
"SigningCertURL": "https://sns.us-west-1.amazonaws.com/SimpleNotificationService-9c6465fa7f48f5cacd23014631ec1136.pem"
}
# Send Event:
curl -X POST --data @src/test/resources/aws-s3-event-json.json -H 'Content-Type: application/json' http://localhost:8080/aws/s3event
curl --data @src/test/resources/aws-s3-event-json.json -H 'Content-Type: application/json' http://ec2-3-101-190-41.us-west-1.compute.amazonaws.com/aws/s3event
# View logs
journalctl -f -u s3-handler --since "1 hour ago"

# Build and Release the image using Docker (not tested)
Increment pom version.
Increment jar version in Dockerfile
Pom, Dockerfile and this build command should all match.
docker buildx build --platform linux/amd64,linux/arm64 -t aws-s3-notify:1.4 .
aws ecr create-repository --repository-name aws-s3-notify --image-tag-mutability IMMUTABLE --region us-east-1
docker tag aws-s3-notify:1.4 097273071583.dkr.ecr.us-east-1.amazonaws.com/aws-s3-notify:1.4
docker login -u AWS -p $(aws ecr get-login-password --region us-east-1) 097273071583.dkr.ecr.us-east-1.amazonaws.com
docker push 097273071583.dkr.ecr.us-east-1.amazonaws.com/aws-s3-notify:1.4

docker image inspect aws-s3-notify:1.4
docker buildx imagetools inspect aws-s3-notify:1.4

# Repository Setup:
Be sure podman desktop is running.
Update aws [sso] credentials: aws configure sso
aws ecr create-repository --repository-name aws-s3-notify --image-tag-mutability IMMUTABLE --region us-west-1 --profile AdministratorAccess-339713066603

# Get oriented:
podman run amazoncorretto pwd

# Build and Release the image with Podman:
# Differences - create the manifest manually for each specific tag. Dont use buildx for multi-arch. Since the manifest contains all images, we simply push the manifest.
podman manifest create localhost/aws-s3-notify:1.6
podman build --platform linux/amd64,linux/arm64 --manifest localhost/aws-s3-notify:1.6 .
podman tag localhost/aws-s3-notify:1.6 339713066603.dkr.ecr.us-west-1.amazonaws.com/aws-s3-notify:1.6
aws ecr get-login-password --region us-west-1 --profile AdministratorAccess-339713066603 | podman login --username AWS --password-stdin 339713066603.dkr.ecr.us-west-1.amazonaws.com
podman manifest push 339713066603.dkr.ecr.us-west-1.amazonaws.com/aws-s3-notify:1.6

podman image ls
podman image inspect aws-s3-notify:1.4
podman manifest inspect localhost/aws-s3-notify:1.4
podman manifest inspect localhost/aws-s3-notify:latest
# Update kubeconfig once your cluster is deployed
aws eks update-kubeconfig --name OuttaTimeOraclesClusterWest --region us-east-1 --profile AdministratorAccess-339713066603

# Create Deployment
kubectl create deployment s3-listener --replicas=1 --image=339713066603.dkr.ecr.us-west-1.amazonaws.com/aws-s3-notify:1.5

# Explore: 
AWS Docs:
https://docs.aws.amazon.com/sns/latest/dg/SendMessageToHttp.prepare.html

RestAssured:
https://medium.com/@bubu.tripathy/testing-restful-apis-with-rest-assured-6d245401deea

Spring CLoud S3:
https://mvnrepository.com/artifact/io.awspring.cloud/spring-cloud-aws-s3

Reactive S3:
https://mvnrepository.com/artifact/com.github.j5ik2o/reactive-aws-s3-core
