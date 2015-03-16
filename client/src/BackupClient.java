import java.net.*;

public class BackupClient {
	public static void main(String[] args) {
		try {
			String serverHostname = "127.0.0.1";

			Socket socket = new Socket(serverHostname, 10008);
			socket.close();
		}
		catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}
	}
}
