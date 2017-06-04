package com.fupan.SX;

/*
 * 2017年6月4日19:08:45 闪讯拨号 version 2
 * 添加拨号日志
 * 添加定时拨号
 * */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SX  extends JFrame implements ActionListener {
	
	
	String acc="";
    String pwd="";
    String Host="";
    String router_psw="";
    boolean reDialThreadEnd=false;
    
    String last_succ_dial_time="";
    String last_auto_dial_time="";
    String last_manual_dial_time="";
    
    boolean auto_dial=true;
    
    boolean net_off=false;
    
    
    boolean auto_redial=true;
    long auto_redial_interval=3000000;
    static boolean can_redial=true;
    
	JLabel lb_acc=new JLabel("账号：");
	JLabel lb_pwd=new JLabel("密码：");
	JLabel lb_host=new JLabel("地址：");
	JLabel lb_router_pwd=new JLabel("路由器密码：");
	
	
	JTextField tf_acc=new JTextField(20);
	JTextField tf_pwd=new JTextField(20);
	JTextField tf_host=new JTextField(20);
	JPasswordField tf_router_pwd=new JPasswordField(17);
	
	JLabel lb_result=new JLabel();
	
	JButton btn_dial=new JButton("拨号");
	JButton btn_set=new JButton("设置");
	JButton btn_save=new JButton("保存");
	JButton btn_cancel=new JButton("取消");
	
	JCheckBox cb_auto_redial=new JCheckBox();
	JLabel lb_auto_redial=new JLabel("定时重拨");
	
	Thread dialThread;
    Thread reDialThread;
    
    
    CardLayout card=new CardLayout();
    
	Container c;
	Container d;
	Container main;
	Container set=new Container();
	
	
	SX(){
		

		
		super("闪讯路由拨号器");
		super.setSize(300,200);
		super.setResizable(false);
		super.setLocationRelativeTo(null);
		super.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		loadConfig();
		
		tf_acc.setText(acc);
		tf_pwd.setText(pwd);
		tf_host.setText(Host);
		tf_router_pwd.setText(router_psw);
		
		btn_save.addActionListener(this);
		btn_cancel.addActionListener(this);
		btn_dial.addActionListener(this);
		btn_set.addActionListener(this);
		cb_auto_redial.addActionListener(this);
		
		c=new Container();
		d=new Container();
		c.setLayout(new FlowLayout(FlowLayout.CENTER,10,10));
		d.setLayout(new FlowLayout(FlowLayout.CENTER,10,10));
		
		c.add(lb_acc);
		c.add(tf_acc);
		c.add(lb_pwd);
		c.add(tf_pwd);
		
		

		
		
		c.add(btn_dial);
		c.add(btn_set);
		c.add(cb_auto_redial);
		c.add(lb_auto_redial);
		cb_auto_redial.setSelected(auto_redial);
		
		d.add(lb_host);
		d.add(tf_host);
		d.add(lb_router_pwd);
		d.add(tf_router_pwd);
		
		d.add(btn_save);
		d.add(btn_cancel);
		
		set.setLayout(card);
		
		set.add("dial panel",c);
		set.add("set panel",d);
		
		main=super.getContentPane();
		
		main.setLayout(new BorderLayout());
		main.add(set,"Center");
		main.add(lb_result,"South");
		
		Date curDate=new Date(System.currentTimeMillis());
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	last_succ_dial_time=sdf.format(curDate);
		
		
		
		String ifClassName="com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
		try {
			UIManager.setLookAndFeel(ifClassName);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setVisible(true);
		
		//计时器 检查当前网络状况
		 new Timer().schedule(new TimerTask() {
	            @Override
	            public void run() {
	             


	            		
	            		
	                if(HttpUtil.isNetWorkAvailable()){
	                  
	                	//System.out.println("available");
	                	Date curDate=new Date(System.currentTimeMillis());
	                	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            
	                	//在此之前没有互联网
	                	if(net_off){
	                		if(auto_dial){
	                			last_succ_dial_time=last_auto_dial_time;
		                		String str="["+last_auto_dial_time+"]\t 自动拨号后，成功接入互联网。";
		                		saveLog(str);
	                		}
	                		else{
	                			
	                			last_succ_dial_time=last_manual_dial_time;
	                			String str="["+last_manual_dial_time+"]\t 手动拨号后，成功接入互联网。";
		                		saveLog(str);
	                		}
	                		
	                		
	                	}
	                	net_off=false;
	                	lb_result.setText("网络畅通！\t"+sdf.format(curDate));
	                }
	                else{
	                	
	                	net_off=true;
	                    if(reDialThread==null||reDialThreadEnd){

	                        reDialThread = new Thread(new Runnable() {
	                            
	                            public void run() {
	                                reDialThreadEnd=false;

	                                if(!Host.isEmpty()&&!router_psw.isEmpty()&&!acc.isEmpty()&&!pwd.isEmpty()){
	                                    Router.Dial(Host, router_psw, Account.getAccount(acc), pwd);
	                                    auto_dial=true;
	                                    Date curDate=new Date(System.currentTimeMillis());
	            	                	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            	                	last_auto_dial_time=sdf.format(curDate);
	                                }


	                                reDialThreadEnd=true;
	                                
	                                Date curDate=new Date(System.currentTimeMillis());
	        	                	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                                lb_result.setText("检测到网络断开，已尝试重连！\t"+sdf.format(curDate));
	                                String str="["+sdf.format(curDate)+"]\t"+"网络断开";
	                                saveLog(str);



	                            }
	                        });
	                        reDialThread.start();
	                    }


	                }
	            }
	        },0,4000);
		
		 
		 new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}},0,auto_redial_interval/2);
		 
		
		 new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){

					while(auto_redial){
						
	
						SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date now=new Date(System.currentTimeMillis());
						//System.out.println(sdf.format(now));
						 try {
							Date date=sdf.parse(last_succ_dial_time);
							long diff=(date.getTime()+auto_redial_interval)-now.getTime();
							if(Math.abs(diff)<5000){
								
								if(!can_redial)continue;
								can_redial=false;
								Router.Dial(Host, router_psw, Account.getAccount(acc), pwd);
	                            auto_dial=true;
	                            Date curDate=new Date(System.currentTimeMillis());
	    	                	
	    	                	last_auto_dial_time=sdf.format(curDate);
	    	                	String str="["+sdf.format(curDate)+"]\t"+"固定时间间隔自动重拨";
								saveLog(str);
							}
							else{
								can_redial=true;
							}
							
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						 
						 //降频
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						 
					}
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}

			}
		 }).start();
		
		 
		 /*开始设置托盘图标*/
		 if(SystemTray.isSupported()){
				URL resource = this.getClass().getResource("ic_launcher.png");    //获得图片路径
	            ImageIcon icon = new ImageIcon(resource); //创建图片对象
			
					
				    Image image = icon.getImage(); // 获得Image对象  
				    TrayIcon trayIcon = new TrayIcon(image); // 创建托盘图标  
				    
				    trayIcon.addMouseListener(new MouseListener(){

						public void mouseClicked(MouseEvent e) {
							// TODO Auto-generated method stub
							if(e.getClickCount()==2){
								SX.this.setVisible(true);
							}
						}

						public void mousePressed(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}

						public void mouseReleased(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}

						public void mouseEntered(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}

						public void mouseExited(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}});
				    
				    trayIcon.setToolTip("闪讯路由拨号"); // 添加工具提示文本  
				    PopupMenu popupMenu = new PopupMenu(); // 创建弹出菜单  
				    MenuItem showDialog = new MenuItem("显示窗口"); // 创建菜单项
				    showDialog.addActionListener(new ActionListener(){

						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							SX.this.setVisible(true);
						}
				    	
				    });
				    MenuItem exit = new MenuItem("退出"); // 创建菜单项  
				    //响应方法  
				    exit.addActionListener(new ActionListener() {  
				  
				        public void actionPerformed(ActionEvent e) {  
				        	
				        	if(JOptionPane.showConfirmDialog(SX.this, "确定退出？退出后，断网无法自动重连！","闪讯路由拨号",0)==0){
				        		  System.exit(0);
				        	}
				        	
				          
				        }  
				      
				    });  
				    popupMenu.add(showDialog); 
				    popupMenu.add(exit); // 为弹出菜单添加菜单项  
				    trayIcon.setPopupMenu(popupMenu); // 为托盘图标加弹出菜弹  
				    SystemTray systemTray = SystemTray.getSystemTray(); // 获得系统托盘对象  
				    try {
				    	
				        systemTray.add(trayIcon); // 为系统托盘加托盘图标  
				        
				    } catch (Exception e) {  
				        e.printStackTrace();  
				    }  
				              
				
				
				}
		 
		
	/*
		System.setProperty("http.proxyHost", "localhost"); 
		System.setProperty("http.proxyPort", "8888"); 
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "8888");
	*/	
		

	}

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ifClassName="com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
		try {
			UIManager.setLookAndFeel(ifClassName);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//通过端口占用禁止多实例运行
		ServerSocket listenerSocket;
		  try {
		      listenerSocket = new ServerSocket(25565);
		      
		    } catch(java.net.BindException e) {
		    	JOptionPane.showMessageDialog(null, "本程序不允许多实例运行，若无实例运行请检查25565端口是否被占用！","闪讯路由拨号",1);
		     
		      System.exit(1);
		      
		    } catch(final IOException e) { // an unexpected exception occurred
		      System.exit(1);
		    }
	    new SX();

	}
	
	public void saveLog(String log){
		
		try{
		File file =new File("SXDial-log.txt");

	      //if file doesnt exists, then create it
	      if(!file.exists()){
	       file.createNewFile();
	      }

	      //true = append file
	      FileWriter fileWritter = new FileWriter(file.getName(),true);
	             BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	             bufferWritter.write(log+"\r\n");
	             bufferWritter.close();
		}catch(IOException e){
			e.printStackTrace();
		}

	}
	
	public void loadConfig(){
		
		try {
			
			DocumentBuilderFactory dbFactory =DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder=dbFactory.newDocumentBuilder();
			Document doc=dbBuilder.parse(new File("./SXDial-config.xml"));
			acc=doc.getElementsByTagName("acc").item(0).getFirstChild().getNodeValue();
			pwd=doc.getElementsByTagName("pwd").item(0).getFirstChild().getNodeValue();
			Host=doc.getElementsByTagName("host").item(0).getFirstChild().getNodeValue();
			router_psw=doc.getElementsByTagName("router_pwd").item(0).getFirstChild().getNodeValue();
			String s_auto_redial=doc.getElementsByTagName("auto_redial").item(0).getFirstChild().getNodeValue();
			auto_redial=s_auto_redial.equalsIgnoreCase("true")?true:false;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void saveConfig(){

		
		try {
		
			DocumentBuilderFactory  dbFactory=DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder;
			
			dbBuilder = dbFactory.newDocumentBuilder();
			Document doc=dbBuilder.newDocument();
			if(doc!=null){
				Element e_config=doc.createElement("SXDial-config");
				Element e_acc=doc.createElement("acc");
				e_acc.appendChild(doc.createTextNode(acc));
				Element e_pwd=doc.createElement("pwd");
				e_pwd.appendChild(doc.createTextNode(pwd));
				
				Element e_host=doc.createElement("host");
				e_host.appendChild(doc.createTextNode(Host));
				
				Element e_router_pwd=doc.createElement("router_pwd");
				e_router_pwd.appendChild(doc.createTextNode(router_psw));
				
				Element e_auto_redial=doc.createElement("auto_redial");
				String s_auto_redial=auto_redial?"true":"false";
				 e_auto_redial.appendChild(doc.createTextNode(s_auto_redial));
				
				e_config.appendChild(e_acc);
				e_config.appendChild(e_pwd);
				e_config.appendChild(e_host);
				e_config.appendChild(e_router_pwd);
				e_config.appendChild(e_auto_redial);
				
				doc.appendChild(e_config);
				
				TransformerFactory tFactory=TransformerFactory.newInstance();
				Transformer transformer=tFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
				transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source =new DOMSource(doc);
				StreamResult result=new StreamResult(new File("./SXDial-config.xml"));
				transformer.transform(source, result);
				
				
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==btn_dial){
			
			acc=tf_acc.getText();
			pwd=tf_pwd.getText();
			if(acc.isEmpty()||pwd.isEmpty()){
				JOptionPane.showMessageDialog(c,"账号或密码为空！");
			}
			else{
				  dialThread = new Thread(new Runnable() {

					public void run() {
						
						Router.Dial(Host, router_psw, Account.getAccount(acc), pwd);
						auto_dial=false;
						
						Date curDate=new Date(System.currentTimeMillis());
	                	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                	last_manual_dial_time=sdf.format(curDate);
	                	
						
						lb_result.setText("数据包已发送，请稍后查看连网状况！");
						saveConfig();
					}           
					  
 
                  });
                  dialThread.start();
				
			}
			
		}else if(e.getSource()==btn_set){
			
			acc=tf_acc.getText();
			pwd=tf_pwd.getText();
			
			card.next(set);
		
			
			
		}else if(e.getSource()==btn_save){
			
			Host=tf_host.getText();
			router_psw=new String(tf_router_pwd.getPassword());
			card.first(set);
			saveConfig();
			lb_result.setText("配置已保存！");
			
		}else if(e.getSource()==btn_cancel){
			card.first(set);
			lb_result.setText("已取消保存！");
		}
		else if(e.getSource()==cb_auto_redial){
			auto_redial=cb_auto_redial.isSelected();
			saveConfig();
		}
		
		
	}
	
	


}
