import config.Const;
import dao.CreditDAO;
import dao.UserDAO;
import model.User;

import java.lang.String;
import java.io.*;
import java.net.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StartServer extends Thread {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ArrayList<ClientThread> clientsConnected = new ArrayList<ClientThread>();
    private ResultSet result = null;

    public static void main(String[] arg) {
        StartServer server = new StartServer();
        server.start();
    }

    @Override
    public void run() {
        startServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(Const.PORT);
            System.out.println("start started in Thread - " + Thread.currentThread().getName() + "\n" +
                    "Waiting for connection....");

            while (!serverSocket.isClosed()) {
                clientSocket = serverSocket.accept();

                System.out.println("connection established...");

                ClientThread client = new ClientThread(clientSocket);
                clientsConnected.add(client);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException in StartServer.run()");
        }
    }

    class ClientThread extends Thread {
        Socket clientSocket;

        UserDAO userDAO = new UserDAO();
        CreditDAO creditDAO = new CreditDAO();

        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                output = new ObjectOutputStream(clientSocket.getOutputStream());
                input = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            while (clientSocket.isConnected()) {
                try {
                    String clientMessage = (String)input.readObject();
                    System.out.println(clientMessage);
                    String[] message = clientMessage.split(" ", 2);
                    String commandNumberStr = message[0];
                    String command = message[1];

                    switch (commandNumberStr){
                        case "usersTable":
                            post(userDAO.getAll());
                            break;
                        case "logIn": {
                            String[] values = command.split(" ", 2);
                            List<User> users = userDAO.getAll();
                            User user = null;
                            for (User temp : users){
                                if(temp.getLogin().equals(values[0]) && temp.getPassword().equals(values[1])){
                                    user = temp;
                                    break;
                                }
                            }
                            post(user);
                            break;
                        }

                        case "sighUp": {
                            String[] valuesSighUp = command.split(" ", 4);
                            List<User> users = userDAO.getAll();
                            for(User user : users){
                                if(user.getLogin().equals(valuesSighUp[1])){
                                    post(null);
                                    break;
                                }
                            }

                            User user = new User();
                            user.setName(valuesSighUp[0]);
                            user.setLogin(valuesSighUp[1]);
                            user.setPassword(valuesSighUp[2]);
                            user.setAccount(Integer.parseInt(valuesSighUp[3]));
                            user.setRole("user");

                            userDAO.create(user);

                            List<User> usersNew = userDAO.getAll();

                            for(User user1 : usersNew){
                                if(user1.getLogin().equals(valuesSighUp[1])){
                                    post(user1);
                                    break;
                                }
                            }
                            break;
                        }

                        case "addUser":
                            String[] valuesSighUp = command.split(" ", 6);
                            User user = null;
                            Boolean test = null;

                            if(valuesSighUp[0].equals("null")){
                                user = new User(0, valuesSighUp[1], valuesSighUp[2], valuesSighUp[3], valuesSighUp[4], Integer.parseInt(valuesSighUp[5]));
                                test = userDAO.create(user);
                            }else{
                                user = new User(Integer.parseInt(valuesSighUp[0]), valuesSighUp[1], valuesSighUp[2], valuesSighUp[3], valuesSighUp[4], Integer.parseInt(valuesSighUp[5]));
                                test = userDAO.update(user);
                            }

                            post(test);
                            break;

                        case "deleteUser":{
                            post(userDAO.delete(Integer.parseInt(command)));
                            break;
                        }

                        case "blockUser":{
                            userDAO.block(Integer.parseInt(command));
                            break;
                        }

                        case "unlockUser":{
                            userDAO.unlock(Integer.parseInt(command));
                            break;
                        }

                        case "account":{
                            valuesSighUp = command.split(" ", 6);

                            post(userDAO.update(new User(Integer.parseInt(valuesSighUp[0]), valuesSighUp[1], valuesSighUp[2], valuesSighUp[3], valuesSighUp[4], Integer.parseInt(valuesSighUp[5]))));
                            break;
                        }

                        case "allCredit": {
                            post(creditDAO.getAll());
                            break;
                        }

                        case "allCreditUser": {
                            post(creditDAO.getAllByUserId(command));
                            break;
                        }

                        case "saveCredit": {
                            valuesSighUp = command.split(" ", 3);

                            post(creditDAO.createCreditByUser(Integer.parseInt(valuesSighUp[0]), Integer.parseInt(valuesSighUp[1]), Integer.parseInt(valuesSighUp[2])));
                            break;
                        }

                        case "updateAccount": {
                            valuesSighUp = command.split(" ", 4);

                            post(creditDAO.updateCreditUser(Integer.parseInt(valuesSighUp[0]), Integer.parseInt(valuesSighUp[1]), Integer.parseInt(valuesSighUp[2]), Integer.parseInt(valuesSighUp[3])));
                            break;
                        }

                        case "deleteCredit": {
                            valuesSighUp = command.split(" ", 3);

                            post(creditDAO.delete(Integer.parseInt(valuesSighUp[0]), Integer.parseInt(valuesSighUp[1]), Integer.parseInt(valuesSighUp[2])));
                            break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        void post(Object obj) {
            try {
                output.writeObject(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object get() {
            try {
                return input.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
