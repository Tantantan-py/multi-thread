# **README - Deploying and Running the Client-Server Application**

## **1. Deploy the Server on EC2**
Upload the server WAR file (`/Assignment1/server/multi-threads_war.war`) to your EC2 instance using the following command:

### **Command Pattern**
```bash
sudo scp -i </path/to/pem/file> </local/path/to/war/file> ec2-user@EC2_IP_ADDR:/remote/path/to/tomcat_webapp/directory
```

Example
```bash
sudo scp -i CS6650.pem java-servlet-demo_war.war ec2-user@18.236.165.56:/usr/share/tomcat/webapps
```

## **2. Run the Client**
Client 1
Open IntelliJ and navigate to:
/Assignment1/client1/HttpClient/src/main/java/SkierRidePost.java


Client 2
Open IntelliJ and navigate to:
```
/Assignment1/client2/HttpClient/src/main/java/SkierRidePost.java
```

Update the EC2 Address
In both files, update line 15 with your own EC2 address.

Current Example
```java
private static final String SERVER_URL = "http://34.221.190.17:8080/multi-threads_war/skiers/12/seasons/2019/day/1/skier/123";
```
Update it to:
```java
private static final String SERVER_URL = "http://[your-ec2-address]:8080/multi-threads_war/skiers/12/seasons/2019/day/1/skier/123";
```

Once updated, run the client to start sending requests to the server.
