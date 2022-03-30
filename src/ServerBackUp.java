// TCPServer2.java: Multithreaded server

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/*class myNameComparator implements Comparator<User>
{
	public int compare(User s1, User s2)
	{
		return s1.username.compareTo(s2.username);
	}
}*/
class Cmd extends Thread{
    public boolean flagHideOrShow=false;
    UDPPingClient ping;
    Scanner sc = new Scanner(System.in);

    public Cmd(UDPPingClient ping){
        this.ping=ping;

    }
    public void run() {
        try {
            do {
                String hideOrShow;
                if((hideOrShow = sc.nextLine()).equals("")){
                    hideOrShow = hideOrShow.toLowerCase();
                    if (hideOrShow.equals("hide") || hideOrShow.equals("h"))
                        ping.flagHideOrShow = true;
                    else
                        ping.flagHideOrShow = false;

                }
            } while ( true );
        }
        catch (Exception e) {
            System.out.println("Exception handled");
        }
    }
}

public class ServerBackUp {
    // TreeSet<User> tree= new TreeSet<User>(new myNameComparator());
    Path root;
    static Path currentDir;
    boolean statusMainServer =true;
    static HashSet<User> hs = new HashSet<>();
    static File myObj;

    static private void readUsersData() {
        try {
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.length() != 0 && data.charAt(0) != '#') {
                    User user = new User(data);
                    if (user.valid) {
                        user.root = createDir("./home/" + user.username);
                        user.rootServer = createDir("./MainServer/usr/" + user.username);
                        createDir("./MainServer/usr/" + user.username);
                        if (user.currentDir == null)
                            user.currentDir = user.root;
                        if (user.currentDirServer == null)
                            user.currentDirServer = user.rootServer;
                        hs.add(user);
                    }
                }
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static private Path createDir(String name) {
        Path path = Paths.get(name + "/");
        try {
            Files.createDirectories(path);
            return path;
        } catch (NoSuchFileException e) {
            System.out.println("Parent directory doesn't exist!");
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            System.out.println("Directory already exists!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO " + e.getMessage());
            e.printStackTrace();
        }
        return Paths.get("./home/");
    }

   public static void main(String[] args)
            throws InterruptedException
    {
        int numero = 0;
        int serverPort = 6003;

        myObj = new File("./MainServer/info/usersData.txt");
        readUsersData(); // abre o ficheiro com as infos dos users e guarda toda a info
        ServerBackUp server= new  ServerBackUp();
        UDPPingClient t= new UDPPingClient(server);
        t.start();
        t.join();

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            System.out.println("A escuta no porto " + serverPort);
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                numero++;
                new Connection(clientSocket, hs, numero);
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
        
    }
}
