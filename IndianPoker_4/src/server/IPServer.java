package server;

import java.io.IOException;
import java.util.Vector;

import Model.IndianPoker;
import Model.Player;

public class IPServer extends AbstractServer {
	int turn = 1;
	Vector<ConnectionToClient> cli = new Vector<>(); //접속한 클라이언트들
	String[][] userList;
	public IPServer(int port) {
		super(port);
		// TODO Auto-generated constructor stub
	}

	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		// TODO Auto-generated method stub
		String m = msg.toString();
		switch (m) {
		case "Play":
			System.out.println("Client로부터  play 요청을 받았습니다." + client);
			if(cli.size()%2 == 1){
				System.out.println("인원이 부족하여 게임을 시작할 수 없습니다. 다른 클라이언트 접속을 기다립니다...");
				client.sendToClient("msg"+"다른 플레이어가 접속할 때 까지 기다려야합니다...");
			}else{
				try {
					cli.get(0).sendToClient("msg"+"Player1("+IndianPoker.getInstance().p1.name+")과"+
							"Player2("+IndianPoker.getInstance().p2.name+")의 게임을 시작합니다.");
					cli.get(1).sendToClient("msg"+"Player1("+IndianPoker.getInstance().p1.name+")과"+
							"Player2("+IndianPoker.getInstance().p2.name+")의 게임을 시작합니다.");
					System.out.println("게임을 시작합니다.");
				} catch (Exception e) {
					System.out.println("예기치 못한 오류가 발생되었습니다." + e);
				}
			}
			break;
		case "turn": //구현안함
			System.out.println(IndianPoker.getInstance().p1.name+"님으로부터  turn 요청을 받았습니다." + client);
			String s = ServerMain.dg.play();
			String r;
			if(turn == 10){
				r = turn+"!"+s+"!"+DiceGame.p.name;
				String[] tmp = r.split("!");
				IndianPoker.getInstance().db.insert(findClient(client), Integer.parseInt(tmp[1]));
				turn = 0;
			}else 
				r = turn+"!"+s; 
			try {
				client.sendToClient("Roll"+r);
				turn++;
			} catch (Exception e) {
				System.out.println("예기치 못한 오류가 발생되었습니다." + e);
			}
			break;
		case "Rank":
			System.out.println(findClient(client)+"님으로부터 Rank보기 요청을 받았습니다." + client);
			try {
				String rk = IndianPoker.getInstance().db.Ranking();
				client.sendToClient("Ranking"+rk);
			} catch (Exception e) {
				System.out.println("예기치 못한 오류가 발생되었습니다. " + e);
			}
			break;
		case "Exit":
			System.out.println(findClient(client) + "님이 종료하였습니다."+client);
			break;
		default:
			if(m.startsWith("Login")){
				String id = "";
				String pwd = "";
				
				id = m.substring(5, m.indexOf("!"));
				pwd = m.substring(m.indexOf("!")+1);
				
				boolean hasid = false;
				boolean ispwd = false;
				
				System.out.println("Client로부터 login 요청을 받았습니다."+client);
				String[] s1 = IndianPoker.getInstance().db.getList().split("!");
				userList = new String[s1.length][2]; // 0:id, 1:pwd
				String[] s2;
				
				for(int i = 0;i<s1.length;i++){
					s2 = s1[i].split("@");
					for(int j = 0 ;j < 3;j++){
						userList[i][j] = new String();
						userList[i][j] = s2[j];
					}
				}
				for(int i = 0;i<userList.length;i++){
					if(userList[i][0].equals(id)){ //회원 목록에 아이디 있음
						hasid = true;
						if(userList[i][1].equals(pwd)){ //패스워드 일치
							ispwd = true;
						}
					}
				}
				
				if(hasid){
					if(ispwd){
						if(cli.size() % 2 ==1){
							IndianPoker.getInstance().p1 = new Player(id);
							System.out.println(IndianPoker.getInstance().p1.name+"님이 로그인하였습니다."+client);
						}else{
							IndianPoker.getInstance().p2 = new Player(id);
							System.out.println(IndianPoker.getInstance().p2.name+"님이 로그인하였습니다."+client);
						}

						try {
							client.sendToClient("Start");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						System.out.println("비밀번호 오류 - 로그인 실패"+client);
						try {
							client.sendToClient("Quit잘못된 비밀번호입니다. 다시 로그인하세요.");
							client.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					System.out.println("미등록 사용자 - 로그인 실패"+client);
					try {
						client.sendToClient("Quit등록된 사용자가 아닙니다. 다시 로그인하세요.");
						client.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else if(m.startsWith("Cancel")){
				m = m.substring(6);
				if(m.equals("Login")){
					System.out.println("Client가 login 취소요청을 하였습니다."+client);
				}else if(m.equals("Roll")){
					System.out.println(findClient(client) + "님이 게임을 그만두었습니다."+client);
				}
			} 
			else if(m.startsWith("Insert")){
				m = m.substring(3);
				String[] n = m.split("!");
				IndianPoker.getInstance().db.newID(n[0], n[1]);
				if(cli.size() % 2 ==1){
					IndianPoker.getInstance().p1 = new Player(n[0]);
					System.out.println(IndianPoker.getInstance().p1.name+"님이 로그인하였습니다."+client);
				}else{
					IndianPoker.getInstance().p2 = new Player(n[0]);
					System.out.println(IndianPoker.getInstance().p2.name+"님이 로그인하였습니다."+client);
				}
				System.out.println(findClient(client)+"님이 로그인하였습니다."+client);	
				try {
					client.sendToClient("Start");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else
				try {
					client.sendToClient("올바른 명령어가 아닙니다." + msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		}
	}

	/**
	 * Hook method called each time a new client connection is accepted. The
	 * default implementation does nothing.
	 * 
	 * @param client
	 *            the connection connected to the client.
	 */
	protected void clientConnected(ConnectionToClient client) {
		System.out.println("Client가 접속하였습니다.");
		cli.add(client);
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("서버가 포트 " + getPort() + "에서 연결을 기다리는 중입니다...");
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * stops listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("서버가 중단되었습니다.");
	}
	private String findClient(ConnectionToClient client){
		String id;
		if(client.equals(cli.get(0))){
			id = IndianPoker.getInstance().p1.name;
		}else
			id = IndianPoker.getInstance().p2.name;
		
		return id;
	}

}
