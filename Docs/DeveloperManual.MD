# Developer’s Manual - Trux Application

## Structure
Trux application was developed using a client-server architecture. The client side is responsible for the interaction with the user. It manages user interface, user input and output, application logic, business and safety rules, and communication with the server. The server side is responsible for taking requests from the application, connecting to the database, retrieving/storing data from/to the database and sending the data back to the application.

### Database
We use MariaDB which is a relational database. It's a MySQL fork, interchangeable with MySQL, so our server application should work with either one.

### Server
The server is made out of a set of different classes which are ordered by responsibilities in packages: <br>
<b>datastructure</b> - package including our protocol. Shared with the client/app.<br>
<b>truxserver</b> - package including all main classes necessary running the server and establishing a connection to a client. TruxServer is the main class, it creates a ServerHandler that listens for incoming connections. They are then handled by instances of ServerRunnable managed in a thread pool. Includes also a CleanupRunnable which takes care of removing invalid sessions as well as a HeartbeatHandler which sets sessions and users to active/online and returns notifications if something new happens (like a new message).<br>
<b>config</b> - package containing two classes responsible for the server configuration. The Config class is a wrapper with getters and setters which allows the others classes to get config values for the server.<br>
<b>dataswitch</b> - package containing a set of switcher classes which decide what method to call.<br>
<b>dbconnect</b> - package containing all classes which handle database stuff. This includes a class representing a DBConnection, a ConnectionPool as well as classes which handle inserts, selects, etc.<br>
<b>file</b> - package containing classes which read and write to the server disk.<br>
<b>logger</b> - package including a logging environment which allows to write to a log file as well as printing output on the console.<br>
<b>mail</b> - package including a class to send eMails to the users.<br>


### Client
The application is split into packages separating the different layers of the system:<br>
<b>technical_services</b> - contains those classes responsible for providing different services that the application is using. AGAParser captures signals sent from AGA for monitoring various signals like speed, fuel, distance travelled and driver distraction level. LocationService handles the location updates for the map from the Google API. DataPoller regularly checks for data changes from RealTimeDataHandler and sends it to the ServerConnector for processing. ServerConnector is the bridge between the application and the server. It sends and receives data to and from the server. We have two service classes that take care of the keeping the application alive and push notifications to the user when the app is in the background - BackgroundService and NotificationService respectively.<br>
<b>application</b> - contains the classes performing system logic. DataHandler handles the routing of data between the GUI and the technical services. It also stores the currently logged in user and handles other operations like caching fetched statistics. SocialHandler is responsible for the community based side of the application. It handles the fetching and caching of friends, friend requests and pictures. SettingsHandler handles the settings options. LoginService handles the login, logout, hashing the user password, reading and writing to and from the config file used for storing user information. (Note that the autologin feature is currently in MainActivity)<br>
<b>datastructure</b> - contains the data objects used in the application. Data is the base class for the data objects. All other classes in datastructure extend Data.<br>
<b>gui</b> - contains the user interface classes responsible with interacting with the user. This package itself is split into several other packages grouping the different components of the system.<br>
<b>base</b> - holds the base activities that are accessible from anywhere in the application. BaseAppActivity is the main base class for the Trux app. It handles the action bar and the menu which are active throughout the app lifecycle. All other activities extend BaseAppActivity.<br>
<b>community</b> - contains the components for the community based features like the map, friend request window and others.<br>
<b>main_home</b> - contains the main activity (responsible for the login screen) and the home activity which is responsible for handling the display and interactions with the fragments when logged in (the main windows for the application).<br>
<b>messaging</b> - contains the classes handling the conversation display and the chat window.<br>
<b>statistics</b> - holds the classes responsible for displaying the different statistics screens.<br>     

## Protocol
### Server  
Threads management - we use a thread pool to track and manage the server connections. Each time a request is received by the server, a new thread is created to process that request. Once the request is received it is channeled to the appropriate classes that are responsible for taking care of the kind of request. To clean up the environment we have a thread running in fixed time-intervals to purge sessions that are not active. 

### Client
Requests are routed in two ways in the application. We have a class DataHandler that takes care of routing requests (for example when the GUI needs data) based on if they are on the server side or not. For this we have multiple data structures like ProtocolMessage, Notification, MetricData and User. Each data object knows if their data resides on the server side or not, by their implementation of a boolean method. Example: for the metric data, any object with a timeframe of 0 is current data, so the data is fetched from AGA on the phone. For a longer timeframe, the average or total is calculated on the server.<br>
<b>Login procedure</b> - when a user wants to login, a User object is created that contains the information needed to login such as username and password. This object is then send to the server for authentication. The server sends back a ProtocolMessage as a reply if this user is allowed to login or not. The ProtocolMessage also contains a session id and a user id for managing the current session the user is in.<br>
<b>Auto-login procedure</b> - upon auto-login, a ProtocolMessage is sent to the server with the required information. The information is read from a config file where we keep the username, hashed password, session id and user id.<br>
<b>Heartbeats</b> - a heartbeat is sent to the server every 10 seconds to keep the connection is alive. The server replies with a notification object making us aware of any new messages or friends.<br>

## New Features and Maintainability
Components work independently of each other in the Trux app. Therefore, it is fairly straight forward to remove and add a new feature. For a consistent look and feel across the application a new Activity class should extend BaseAppActivity. For new data objects the Data class should be extended which provides an implementation of the Serializable interface needed to send object through the ObjectInputStream and ObjectOutputStream for communication. Any additional classes needed for providing services or logic to the system should be put in the corresponding packages and make use of the already provided interfaces if possible.
To add a new feature or handling of a new datatype to the server, the DataSwitcher class that handles the main routing of data would need to be updated to know what to do with the data.
