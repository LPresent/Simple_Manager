import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


class DBFrame extends JFrame implements ActionListener{
	private Connection conn = null;
	private JMenuBar menuBar;
	private JMenu menu1, menu2;
	private JMenuItem menuItem1, menuItem2, menuItem3;
	private ModifyPanel mPanel;
	private LookupPanel lPanel;
	
	public DBFrame() {
		mainFrame();
	}
	public void mainFrame() {
		setTitle("SimpleManager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(320,300);
		setLocationRelativeTo(null);
		setMenuBar();
		setResizable(false);
		add(new LoginPanel());
		setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == menuItem1) {
			lPanel.setLookup();
		}else if (e.getSource() == menuItem2) {
			insertMessageBox();
		}else if (e.getSource() == menuItem3) {
			mPanel.setModify();
		}
	}
	public void setMenuBar() {
		menuBar = new JMenuBar();
		menu1 = new JMenu("조회");
		menuBar.add(menu1);
		menuItem1 = new JMenuItem("View");
		menuItem1.addActionListener(this);
		menu1.add(menuItem1);
		menu2 = new JMenu("수정");
		menuBar.add(menu2);
		menuItem2 = new JMenuItem("Insert");
		menuItem2.addActionListener(this);
		menu2.add(menuItem2);
		menuItem3 = new JMenuItem("Update");
		menuItem3.addActionListener(this);
		menu2.add(menuItem3);
		setJMenuBar(menuBar);
		menu1.setEnabled(false);
		menu2.setEnabled(false);
	}
	public void changePanel(String panelName) {
		getContentPane().removeAll();
		if("LPanel".equals(panelName))
			getContentPane().add(lPanel);
		else if("MPanel".equals(panelName))
			getContentPane().add(mPanel);
		revalidate();
		repaint();
	}
	public void insertMessageBox() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tabs");
			ArrayList<String> strArray = new ArrayList<String>();
			for(int i=0; rs.next();i++){
				strArray.add(rs.getString(1));
			}
			String[] selections = strArray.toArray(new String[strArray.size()]);
			String tableName = (String) JOptionPane.showInputDialog(null, "데이터를 추가할 테이블을 선택하세요.", 
					"테이블 선택", JOptionPane.QUESTION_MESSAGE, null, selections, "");
			if(tableName == null)
				return;
			rs = stmt.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();
			String data, dataResult ="", values;
			String column, columnResult ="", columnValues;
			for(int i=0; i<cols;i++) {
				if((data = JOptionPane.showInputDialog((column = rsmd.getColumnName(i+1))+"란에 입력할 데이터를 입력해주세요."))==null) return;
				if(rsmd.getColumnTypeName(i+1).equalsIgnoreCase("NUMBER"))
					dataResult += (data+", ");
				else
					dataResult += ("'"+data+"', ");
				columnResult += (column+", ");
			}
			columnValues = columnResult.substring(0, columnResult.length()-2);
			values = dataResult.substring(0, dataResult.length()-2);
			if(stmt.executeUpdate("INSERT INTO "+tableName+"("+ columnValues +")"+" VALUES ("+ values +")")<1)
				JOptionPane.showMessageDialog(null, "추가되지 않았습니다!", "추가 오류", JOptionPane.WARNING_MESSAGE);
			else
				JOptionPane.showMessageDialog(null, "추가 완료", "추가 완료", JOptionPane.INFORMATION_MESSAGE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			String error[] = e.toString().split(":", 3);
			JOptionPane.showMessageDialog(null, error[2], "추가 오류", JOptionPane.WARNING_MESSAGE);
		}
	}
	public void defaultWindow() {
		menu1.setEnabled(true);
		menu2.setEnabled(true);
		setSize(600,400);
		setLocationRelativeTo(null);
		setResizable(true);
	}
	class LoginPanel extends JPanel implements ActionListener{	
		private JTextField userField, sidField, portField;
		private JPasswordField pwField;
		private JButton loginButton;
		public LoginPanel() {
			setLayout(null);
			Font font = new Font("Arial Black",Font.BOLD,30);
			JLabel titleLabel = new JLabel("Simple Manager");
			titleLabel.setFont(font);
			titleLabel.setSize(300,50);
			titleLabel.setLocation(10,10);
			add(titleLabel);
			
			int px = 50; // userLabel 기준
			int py = 70; // userLabel 기준
			JLabel userLabel = new JLabel("User: ");
			userLabel.setSize(200,20);
			userLabel.setLocation(px,py);
			add(userLabel);
			userField = new JTextField();
			userField.setSize(120, 20);
			userField.setLocation(px+80,py);
			userField.setText("scott");
			add(userField);
			
			JLabel pwLabel = new JLabel("Password: ");
			pwLabel.setSize(200, 20);
			pwLabel.setLocation(px,py+30);
			add(pwLabel);
			pwField = new JPasswordField();
			pwField.setSize(120, 20);
			pwField.setLocation(px+80,py+30);
			pwField.setText("");
			add(pwField);
			
			JLabel portLabel = new JLabel("Port: ");
			portLabel.setSize(200, 20);
			portLabel.setLocation(px, py+60);
			add(portLabel);
			portField = new JTextField();
			portField.setSize(120, 20);
			portField.setLocation(px+80, py+60);
			portField.setText("1521");
			add(portField);
			
			JLabel sidLabel = new JLabel("Sid: ");
			sidLabel.setSize(200, 20);
			sidLabel.setLocation(px, py+90);
			add(sidLabel);
			sidField = new JTextField();
			sidField.setSize(120, 20);
			sidField.setLocation(px+80, py+90);
			sidField.setText("xe");
			add(sidField);
			
			loginButton = new JButton("로그인");
			loginButton.addActionListener(this);
			loginButton.setSize(100, 30);
			loginButton.setLocation(px+48, py+120);
			add(loginButton);
		}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == loginButton) {
				String user, pw, port, sid;
				Connection connect = null;
				user = userField.getText();
				pw = pwField.getText();
				port = portField.getText();
				sid = sidField.getText();
				connect = DBConnection.getConnection(user, pw, port, sid);
				if(connect == null) {
					JOptionPane.showMessageDialog(null, "데이터베이스 접속 실패");
					return ;
				}
				else {
					conn = connect;
					lPanel = new LookupPanel();
					mPanel = new ModifyPanel();
					defaultWindow();
					changePanel("LPanel");
				}
			}
		}
	}
	class LookupPanel extends JPanel{
		JScrollPane scrollPane = null;
		String[] columnNames = null;
		Object[][] data = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		ArrayList<String> strArray = null;
		JTable table = null;
		public LookupPanel() {
			setLayout(new GridLayout(1,1));
		}
		public void setLookup() {
			try {
				//테이블선택
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM tabs");
				strArray = new ArrayList<String>();
				while(rs.next()){
					strArray.add(rs.getString(1));
				}
				String[] selections = strArray.toArray(new String[strArray.size()]);
				String tableName = (String) JOptionPane.showInputDialog(null, "조회할 테이블을 선택하세요.", 
						"테이블 선택", JOptionPane.QUESTION_MESSAGE, null, selections, "");
				if(tableName == null)
					return;
				// 컬럼 추출
				rs = stmt.executeQuery("SELECT * FROM "+tableName);
				rsmd = rs.getMetaData();
				int cols = rsmd.getColumnCount();
				columnNames = new String[cols];
				for(int i=0; i<cols;i++) {
					columnNames[i] = rsmd.getColumnName(i+1);
				}
				// 데이터 추출
				while(rs.next());
				int row = rs.getRow();
				rs = stmt.executeQuery("SELECT * FROM "+tableName);
				data = new Object[row][cols];
				for(int i=0; rs.next(); i++) {
					for(int j=0; j<cols; j++)
						data[i][j]=rs.getString(j+1);
				}
				DefaultTableModel model = new DefaultTableModel(data,columnNames) {
					public boolean isCellEditable(int i, int c) {
						return false;
					}
				};
				table = new JTable(model);
				table.setFillsViewportHeight(true);
				table.setAutoCreateRowSorter(true);
				if(scrollPane != null)
					remove(scrollPane);
				scrollPane = new JScrollPane(table);
				add(scrollPane);
				changePanel("LPanel");
				setVisible(true);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, "테이블 조회에 실패했습니다.");
				e.printStackTrace();
				return ;
			}
		}
	}
	class ModifyPanel extends JPanel implements TableModelListener{
		JScrollPane scrollPane = null;
		String[] columnNames = null;
		Object[][] data = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		ArrayList<String> strArray = null;
		JTable table = null;
		String tableName = null;
		ResultSet pkColumns = null;
		public ModifyPanel() {
			setLayout(new GridLayout(1,1));
		}
		public void setModify() {
			try {
				//테이블선택
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM tabs");
				strArray = new ArrayList<String>();
				while(rs.next()){
					strArray.add(rs.getString(1));
				}
				String[] selections = strArray.toArray(new String[strArray.size()]);
				tableName = (String) JOptionPane.showInputDialog(null, "수정할 테이블을 선택하세요.", 
						"테이블 선택", JOptionPane.QUESTION_MESSAGE, null, selections, "");
				if(tableName == null)
					return;
				// 컬럼 추출
				rs = stmt.executeQuery("SELECT * FROM "+tableName);
				rsmd = rs.getMetaData();
				int cols = rsmd.getColumnCount();
				columnNames = new String[cols];
				for(int i=0; i<cols;i++) {
					columnNames[i] = rsmd.getColumnName(i+1);
				}
				// 데이터 추출
				while(rs.next());
				int row = rs.getRow();
				rs = stmt.executeQuery("SELECT * FROM "+tableName);
				data = new Object[row][cols];
				for(int i=0; rs.next(); i++) {
					for(int j=0; j<cols; j++)
						data[i][j]=rs.getString(j+1);
				}
				DefaultTableModel model = new DefaultTableModel(data,columnNames);
				table = new JTable(model);
				table.getModel().addTableModelListener(this);
				table.setFillsViewportHeight(true);
				table.setAutoCreateRowSorter(true);
				if(scrollPane != null)
					remove(scrollPane);
				scrollPane = new JScrollPane(table);
				add(scrollPane);
				changePanel("MPanel");
				setVisible(true);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, "테이블 조회에 실패했습니다.");
				e.printStackTrace();
				return ;
			}
		}
		public void tableChanged(TableModelEvent e) {
			try {
				pkColumns= conn.getMetaData().getPrimaryKeys(null,null,tableName.toUpperCase());
				int row = e.getFirstRow();
				int column = e.getColumn();
				pkColumns.next();
				String pkName = pkColumns.getString("COLUMN_NAME");
				int pkPosition = table.getColumn(pkName).getModelIndex();
				if(column == pkPosition) {
					JOptionPane.showMessageDialog(null, "pk수정은 금지되어 있습니다. (미완성)", "알림", JOptionPane.WARNING_MESSAGE);
					return;
				}
				TableModel model = (TableModel) e.getSource();
				String columnName = model.getColumnName(column);
				String updateData = (String)model.getValueAt(row, column);
				String pkData = (String)model.getValueAt(row, pkPosition);
				String updateQuery = "UPDATE " + tableName + " SET "+columnName+" =?" + " WHERE " + pkName + " =?";
				if(JOptionPane.showConfirmDialog(null, updateData+"로 수정하시겠습니까?","수정 확인",JOptionPane.OK_CANCEL_OPTION) != JOptionPane.YES_OPTION) return;
				PreparedStatement pstmt = conn.prepareStatement(updateQuery);
				pstmt.setString(1, updateData);
				pstmt.setString(2, pkData);
				if(pstmt.executeUpdate()<1)
					JOptionPane.showMessageDialog(null, "수정되지 않았습니다!", "수정 오류", JOptionPane.WARNING_MESSAGE);
				else
					JOptionPane.showMessageDialog(null, "수정 완료", "수정 완료", JOptionPane.INFORMATION_MESSAGE);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				String error[] = e1.toString().split(":", 3);
				JOptionPane.showMessageDialog(null, error[2], "수정 오류", JOptionPane.WARNING_MESSAGE);
			}
			
		}
	}
}
class DBConnection {
	private static Connection dbConn = null;
	public static Connection getConnection(String id, String pw, String port, String sid){
		String url = "jdbc:oracle:thin:@localhost:"+port+":"+sid;
			try {
				dbConn = DriverManager.getConnection(url, id, pw);
			} catch (SQLException e) {}
		return dbConn;
	}
}
public class SimpleManager{
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new DBFrame();
	}
}
