// ================================================================
// Smart Community Resource Rental & Booking System
// DARK THEME + LIQUID RIPPLE ANIMATION EDITION
// ================================================================
// File   : SmartCommunityResourceRental.java
// Tech   : Java Swing + MySQL JDBC
// Compile: javac -cp ".;lib\mysql-connector-j-9.7.0.jar" SmartCommunityResourceRental.java
// Run    : java  -cp ".;lib\mysql-connector-j-9.7.0.jar" SmartCommunityResourceRental
// ================================================================

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmartCommunityResourceRental {

    // ── JDBC ─────────────────────────────────────────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/smart_community_rental";
    static final String DB_USER = "root";
    static final String DB_PASS = "1234";

    // ── DARK THEME PALETTE ────────────────────────────────────────
    static final Color BG_DARK    = new Color(8,   8,  20);   // main bg
    static final Color BG_CARD    = new Color(16,  16, 36);   // card bg
    static final Color BG_CARD2   = new Color(24,  24, 50);   // lighter card
    static final Color BG_INPUT   = new Color(22,  22, 44);   // input bg
    static final Color BORDER_COL = new Color(45,  45, 90);   // borders

    static final Color CYAN       = new Color(0,   210, 255); // primary accent
    static final Color GREEN      = new Color(0,   220, 140); // success
    static final Color RED        = new Color(255,  70,  70); // danger
    static final Color ORANGE     = new Color(255, 145,  30); // warning
    static final Color PURPLE     = new Color(160,  80, 255); // purple
    static final Color PINK       = new Color(255,  80, 150); // pink
    static final Color GOLD       = new Color(255, 200,  50); // gold

    static final Color TXT_WHITE  = Color.WHITE;
    static final Color TXT_GRAY   = new Color(160, 170, 195);
    static final Color TXT_DIM    = new Color(90,  100, 130);

    // ── FONTS ─────────────────────────────────────────────────────
    static final Font F_BIG   = new Font("Segoe UI", Font.BOLD,  28);
    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  20);
    static final Font F_SUB   = new Font("Segoe UI", Font.BOLD,  15);
    static final Font F_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font F_BTN   = new Font("Segoe UI", Font.BOLD,  13);
    static final Font F_MONO  = new Font("Consolas",  Font.PLAIN, 12);

    // ════════════════════════════════════════════════════════════════
    // SESSION
    // ════════════════════════════════════════════════════════════════
    static class Session {
        static int    userId    = -1;
        static String userName  = "";
        static String userEmail = "";
        static String userRole  = "";
        static void login(int id,String n,String e,String r){userId=id;userName=n;userEmail=e;userRole=r;}
        static void logout(){userId=-1;userName="";userEmail="";userRole="";}
        static boolean isLoggedIn(){return userId!=-1;}
    }

    // ════════════════════════════════════════════════════════════════
    // DB HELPER
    // ════════════════════════════════════════════════════════════════
    static class DB {
        static Connection get() throws Exception {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
        }
        static void close(Connection c,Statement s,ResultSet r){
            try{if(r!=null)r.close();}catch(Exception e){}
            try{if(s!=null)s.close();}catch(Exception e){}
            try{if(c!=null)c.close();}catch(Exception e){}
        }
        static void close(Connection c,Statement s){close(c,s,null);}
    }

    // ════════════════════════════════════════════════════════════════
    // RIPPLE BUTTON — Liquid animation on click
    // ════════════════════════════════════════════════════════════════
    static class RippleButton extends JButton {
        private final Color bgColor;
        private Point  ripplePt   = null;
        private float  rippleAlpha= 0f;
        private int    rippleR    = 0;
        private Timer  timer;

        RippleButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setFont(F_BTN);
            setForeground(TXT_WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10,22,10,22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    ripplePt = e.getPoint();
                    rippleAlpha = 0.55f;
                    rippleR = 0;
                    if (timer != null) timer.stop();
                    timer = new Timer(14, ev -> {
                        rippleR     += 10;
                        rippleAlpha -= 0.025f;
                        if (rippleAlpha <= 0) { rippleAlpha = 0; timer.stop(); }
                        repaint();
                    });
                    timer.start();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background with rounded corners
            Color drawBg = getModel().isRollover() ? bgColor.brighter() : bgColor;
            g2.setColor(drawBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

            // Liquid ripple
            if (ripplePt != null && rippleAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rippleAlpha));
                g2.setColor(Color.WHITE);
                g2.fillOval(ripplePt.x - rippleR, ripplePt.y - rippleR, rippleR*2, rippleR*2);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor.brighter());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // HOVER CARD — menu card with glow on hover
    // ════════════════════════════════════════════════════════════════
    static class HoverCard extends JPanel {
        private final Color accent;
        private float glowAlpha = 0f;
        private Timer glowTimer;
        private boolean hovering = false;

        HoverCard(Color accent) {
            this.accent = accent;
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovering = true; startGlow();
                }
                public void mouseExited(MouseEvent e) {
                    hovering = false; startGlow();
                }
            });
        }

        void startGlow() {
            if (glowTimer != null) glowTimer.stop();
            glowTimer = new Timer(16, e -> {
                if (hovering) { glowAlpha = Math.min(1f, glowAlpha + 0.08f); }
                else           { glowAlpha = Math.max(0f, glowAlpha - 0.08f); }
                repaint();
                if ((!hovering && glowAlpha <= 0) || (hovering && glowAlpha >= 1)) glowTimer.stop();
            });
            glowTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Card background
            g2.setColor(BG_CARD2);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            // Glow border
            if (glowAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha * 0.7f));
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 18, 18);
                // top accent line
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha));
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(20, 2, getWidth()-20, 2);
            } else {
                g2.setColor(BORDER_COL);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SHARED UI HELPERS
    // ════════════════════════════════════════════════════════════════
    static RippleButton btn(String t, Color c){ return new RippleButton(t,c); }

    static JLabel lbl(String t, Font f, Color c){
        JLabel l=new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }

    static JTextField tf(int cols) {
        JTextField t=new JTextField(cols);
        t.setFont(F_LABEL);
        t.setBackground(BG_INPUT);
        t.setForeground(TXT_WHITE);
        t.setCaretColor(CYAN);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL,1,true),
            BorderFactory.createEmptyBorder(7,12,7,12)));
        return t;
    }

    static JPasswordField pf(int cols) {
        JPasswordField p=new JPasswordField(cols);
        p.setFont(F_LABEL);
        p.setBackground(BG_INPUT);
        p.setForeground(TXT_WHITE);
        p.setCaretColor(CYAN);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL,1,true),
            BorderFactory.createEmptyBorder(7,12,7,12)));
        return p;
    }

    static JComboBox<String> combo(String... items){
        JComboBox<String> c=new JComboBox<>(items);
        c.setFont(F_LABEL);
        c.setBackground(BG_CARD2);
        c.setForeground(TXT_WHITE);
        return c;
    }

    static void styleTable(JTable t){
        t.setFont(F_LABEL);
        t.setRowHeight(32);
        t.setBackground(BG_CARD);
        t.setForeground(TXT_WHITE);
        t.setGridColor(BORDER_COL);
        t.setShowGrid(true);
        t.setSelectionBackground(new Color(0,210,255,50));
        t.setSelectionForeground(CYAN);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        t.getTableHeader().setBackground(BG_CARD2);
        t.getTableHeader().setForeground(CYAN);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0,CYAN));
        t.getTableHeader().setReorderingAllowed(false);
        t.setFillsViewportHeight(true);
    }

    static JScrollPane darkScroll(JTable t){
        JScrollPane sp=new JScrollPane(t);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COL,1));
        return sp;
    }

    static JPanel hdr(String title, String sub){
        JPanel p=new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL),
            BorderFactory.createEmptyBorder(16,24,16,24)));
        JPanel tp=new JPanel(new GridLayout(2,1,0,4)); tp.setOpaque(false);
        JLabel tl=lbl(title,F_TITLE,TXT_WHITE);
        JLabel sl=lbl(sub,  F_SMALL,CYAN);
        tp.add(tl); tp.add(sl);
        p.add(tp,BorderLayout.WEST);
        return p;
    }

    static JPanel darkPanel(LayoutManager lm){
        JPanel p=new JPanel(lm); p.setBackground(BG_DARK); return p;
    }
    static JPanel cardPanel(LayoutManager lm){
        JPanel p=new JPanel(lm); p.setBackground(BG_CARD); return p;
    }

    static boolean validEmail(String e){ return e.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"); }
    static String today(){ return new SimpleDateFormat("yyyy-MM-dd").format(new Date()); }

    static void showMsg(Component parent, String msg, String title, int type){
        JOptionPane.showMessageDialog(parent,msg,title,type);
    }

    // ════════════════════════════════════════════════════════════════
    // DATABASE INITIALISER
    // ════════════════════════════════════════════════════════════════
    static void initDB(){
        Connection con=null; Statement st=null;
        try{
            con=DB.get(); st=con.createStatement();

            st.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(100) NOT NULL,email VARCHAR(100) UNIQUE NOT NULL,phone VARCHAR(15),password VARCHAR(255) NOT NULL,role ENUM('USER','ADMIN') DEFAULT 'USER',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS categories(category_id INT PRIMARY KEY AUTO_INCREMENT,category_name VARCHAR(100) UNIQUE NOT NULL,description VARCHAR(255),created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS resources(resource_id INT PRIMARY KEY AUTO_INCREMENT,category_id INT,resource_name VARCHAR(150) NOT NULL,description VARCHAR(255),quantity INT DEFAULT 1,available_quantity INT DEFAULT 1,rent_per_day DECIMAL(10,2) DEFAULT 0.00,status ENUM('AVAILABLE','UNAVAILABLE','MAINTENANCE') DEFAULT 'AVAILABLE',FOREIGN KEY(category_id) REFERENCES categories(category_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bookings(booking_id INT PRIMARY KEY AUTO_INCREMENT,user_id INT,resource_id INT,booking_date DATE,start_date DATE,end_date DATE,quantity INT DEFAULT 1,total_amount DECIMAL(10,2) DEFAULT 0.00,status ENUM('PENDING','APPROVED','REJECTED','CANCELLED','COMPLETED') DEFAULT 'PENDING',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(user_id),FOREIGN KEY(resource_id) REFERENCES resources(resource_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS payments(payment_id INT PRIMARY KEY AUTO_INCREMENT,booking_id INT,amount DECIMAL(10,2),payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,payment_method ENUM('UPI','DEBIT_CARD','CREDIT_CARD','CASH') DEFAULT 'CASH',payment_status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') DEFAULT 'PENDING',FOREIGN KEY(booking_id) REFERENCES bookings(booking_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS returns(return_id INT PRIMARY KEY AUTO_INCREMENT,booking_id INT UNIQUE,actual_return_date DATE,fine_amount DECIMAL(10,2) DEFAULT 0.00,return_status ENUM('RETURNED','LATE') DEFAULT 'RETURNED',FOREIGN KEY(booking_id) REFERENCES bookings(booking_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS feedback(feedback_id INT PRIMARY KEY AUTO_INCREMENT,user_id INT,resource_id INT,rating INT,comment VARCHAR(500),feedback_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(user_id),FOREIGN KEY(resource_id) REFERENCES resources(resource_id))");

            // categories
            String[][] cats={{"Sports","Sports equipment"},{"Electronics","Electronic devices"},{"Tools","Hardware tools"},{"Educational","Learning resources"},{"Mobility","Mobility aids"},{"Camping","Camping gear"},{"Gardening","Garden tools"},{"Medical","Medical equipment"},{"Event","Event supplies"}};
            for(String[] c:cats) st.executeUpdate("INSERT IGNORE INTO categories(category_name,description) VALUES('"+c[0]+"','"+c[1]+"')");

            ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM resources"); rs.next();
            if(rs.getInt(1)==0){
                int sp=1,el=2,to=3,ed=4,mo=5,ca=6,ga=7,me=8,ev=9;
                ResultSet cr=st.executeQuery("SELECT category_id,category_name FROM categories");
                while(cr.next()){switch(cr.getString(2)){case"Sports":sp=cr.getInt(1);break;case"Electronics":el=cr.getInt(1);break;case"Tools":to=cr.getInt(1);break;case"Educational":ed=cr.getInt(1);break;case"Mobility":mo=cr.getInt(1);break;case"Camping":ca=cr.getInt(1);break;case"Gardening":ga=cr.getInt(1);break;case"Medical":me=cr.getInt(1);break;case"Event":ev=cr.getInt(1);break;}}
                cr.close();
                Object[][] res={{sp,"Cricket Kit","Full cricket kit","5","200.00"},{sp,"Football","Size 5 football","10","80.00"},{sp,"Badminton Kit","Rackets & shuttles","8","150.00"},{el,"Projector","HD Projector","3","500.00"},{el,"Speaker System","Bluetooth speaker","4","300.00"},{el,"Laptop","Core i5 laptop","5","500.00"},{to,"Drill Machine","Power drill","6","150.00"},{to,"Ladder","6-foot ladder","4","100.00"},{mo,"Wheelchair","Manual wheelchair","3","300.00"},{mo,"Walker","Aluminium walker","5","200.00"},{ca,"Camping Tent","4-person tent","6","400.00"},{ga,"Lawn Mower","Electric mower","2","350.00"},{me,"BP Monitor","Digital BP monitor","4","100.00"},{ev,"Plastic Chairs","Stackable chairs","100","10.00"},{ev,"Tables","Folding tables","20","50.00"}};
                for(Object[] r2:res) st.executeUpdate("INSERT INTO resources(category_id,resource_name,description,quantity,available_quantity,rent_per_day,status) VALUES("+r2[0]+",'"+r2[1]+"','"+r2[2]+"',"+r2[3]+","+r2[3]+","+r2[4]+",'AVAILABLE')");
            }
            rs.close();
            st.executeUpdate("INSERT IGNORE INTO users(name,email,phone,password,role) VALUES('Admin','admin@smartcommunity.com','9999999999','admin123','ADMIN')");
            System.out.println("[DB] Ready.");
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"DB Error!\n"+e.getMessage()+"\n\nStart MySQL and create database 'smart_community_rental'","DB Error",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }finally{ DB.close(con,st); }
    }

    // ════════════════════════════════════════════════════════════════
    // LOGIN FRAME
    // ════════════════════════════════════════════════════════════════
    static class LoginFrame extends JFrame {
        JTextField     emailF;
        JPasswordField passF;

        LoginFrame(){
            setTitle("Smart Community Resource Rental & Booking System");
            setSize(480,560); setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE); setResizable(false);
            getContentPane().setBackground(BG_DARK); build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout()); root.setBackground(BG_DARK);

            // Top logo panel
            JPanel top=new JPanel(new GridLayout(3,1,0,8));
            top.setBackground(BG_CARD);
            top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL),
                BorderFactory.createEmptyBorder(28,20,28,20)));
            JLabel ico=new JLabel("\uD83C\uDFD8",SwingConstants.CENTER);
            ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,42));
            JLabel t1=lbl("Smart Community Rental",F_BIG,TXT_WHITE);
            t1.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel t2=lbl("Resource Rental & Booking System",F_LABEL,CYAN);
            t2.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(ico); top.add(t1); top.add(t2);
            root.add(top,BorderLayout.NORTH);

            // Form
            JPanel form=cardPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(30,50,20,50));
            GridBagConstraints g=new GridBagConstraints();
            g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(8,4,8,4);

            g.gridx=0;g.gridy=0;g.gridwidth=2;
            form.add(lbl("Sign In to Your Account",F_SUB,TXT_WHITE),g);

            g.gridwidth=1;
            g.gridx=0;g.gridy=1;g.weightx=0.3; form.add(lbl("Email:",F_LABEL,TXT_GRAY),g);
            g.gridx=1;g.weightx=0.7; emailF=tf(18); form.add(emailF,g);

            g.gridx=0;g.gridy=2;g.weightx=0.3; form.add(lbl("Password:",F_LABEL,TXT_GRAY),g);
            g.gridx=1;g.weightx=0.7; passF=pf(18); form.add(passF,g);

            // hint
            g.gridx=0;g.gridy=3;g.gridwidth=2;g.insets=new Insets(0,4,8,4);
            JLabel hint=lbl("Admin: admin@smartcommunity.com / admin123",F_SMALL,TXT_DIM);
            form.add(hint,g);

            RippleButton userBtn=btn("  User Login  ",CYAN.darker());
            RippleButton regBtn =btn("  Register  ",  new Color(40,160,100));
            RippleButton admBtn =btn("  Admin Login  ",new Color(80,60,160));
            RippleButton exitBtn=btn("  Exit  ",       RED.darker());

            JPanel bp=new JPanel(new GridLayout(2,2,10,10));
            bp.setBackground(BG_CARD);
            bp.add(userBtn); bp.add(regBtn); bp.add(admBtn); bp.add(exitBtn);
            g.gridx=0;g.gridy=4;g.gridwidth=2;g.insets=new Insets(16,4,4,4);
            form.add(bp,g);

            userBtn.addActionListener(e->doLogin("USER"));
            regBtn .addActionListener(e->{new RegisterFrame(this).setVisible(true);setVisible(false);});
            admBtn .addActionListener(e->doLogin("ADMIN"));
            passF  .addActionListener(e->doLogin("USER"));
            exitBtn.addActionListener(e->{ if(JOptionPane.showConfirmDialog(this,"Exit?","Confirm",JOptionPane.YES_NO_OPTION)==0)System.exit(0); });

            root.add(form,BorderLayout.CENTER);

            JLabel foot=lbl("© 2025 Smart Community Rental  |  College Project  |  Java Swing + MySQL",F_SMALL,TXT_DIM);
            foot.setHorizontalAlignment(SwingConstants.CENTER);
            foot.setBorder(BorderFactory.createEmptyBorder(6,0,12,0));
            JPanel fp=darkPanel(new BorderLayout()); fp.add(foot,BorderLayout.CENTER);
            root.add(fp,BorderLayout.SOUTH);

            setContentPane(root);
        }

        void doLogin(String role){
            String email=emailF.getText().trim();
            String pass=new String(passF.getPassword()).trim();
            if(email.isEmpty()||pass.isEmpty()){showMsg(this,"Enter email and password.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                ps=con.prepareStatement("SELECT * FROM users WHERE email=? AND password=? AND role=?");
                ps.setString(1,email); ps.setString(2,pass); ps.setString(3,role);
                rs=ps.executeQuery();
                if(rs.next()){
                    Session.login(rs.getInt("user_id"),rs.getString("name"),rs.getString("email"),rs.getString("role"));
                    showMsg(this,"Welcome, "+Session.userName+"!","Login Successful",JOptionPane.INFORMATION_MESSAGE);
                    if("ADMIN".equals(role)){new AdminDashboard().setVisible(true);}
                    else                   {new UserDashboard().setVisible(true);}
                    dispose();
                }else{
                    showMsg(this,"Invalid credentials for "+role+".\nCheck email and password.","Login Failed",JOptionPane.ERROR_MESSAGE);
                }
            }catch(Exception ex){showMsg(this,"DB Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // REGISTER FRAME
    // ════════════════════════════════════════════════════════════════
    static class RegisterFrame extends JFrame {
        JFrame parent;
        JTextField nf,ef,phf; JPasswordField pwf,cpf;

        RegisterFrame(JFrame p){
            parent=p;
            setTitle("Register — Smart Community Rental");
            setSize(460,490); setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); setResizable(false);
            build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout());
            root.add(hdr("Create Account","Register to access community resources"),BorderLayout.NORTH);

            JPanel form=cardPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
            GridBagConstraints g=new GridBagConstraints();
            g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(8,5,8,5);

            nf=tf(16); ef=tf(16); phf=tf(16); pwf=pf(16); cpf=pf(16);
            String[] ls={"Full Name:","Email:","Phone:","Password:","Confirm Password:"};
            JComponent[] fs={nf,ef,phf,pwf,cpf};
            for(int i=0;i<ls.length;i++){
                g.gridx=0;g.gridy=i;g.weightx=0.38; form.add(lbl(ls[i],F_LABEL,TXT_GRAY),g);
                g.gridx=1;g.weightx=0.62; form.add(fs[i],g);
            }

            RippleButton rb=btn("Register",GREEN.darker());
            RippleButton bb=btn("Back to Login",new Color(60,60,100));
            JPanel bp=new JPanel(new FlowLayout(FlowLayout.CENTER,14,10));
            bp.setBackground(BG_CARD); bp.add(rb); bp.add(bb);
            g.gridx=0;g.gridy=ls.length;g.gridwidth=2;g.insets=new Insets(16,5,5,5);
            form.add(bp,g);

            rb.addActionListener(e->doReg());
            bb.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            root.add(form,BorderLayout.CENTER);
            setContentPane(root);
        }

        void doReg(){
            String name=nf.getText().trim(),email=ef.getText().trim(),phone=phf.getText().trim();
            String pass=new String(pwf.getPassword()).trim(),cpas=new String(cpf.getPassword()).trim();
            if(name.isEmpty()||email.isEmpty()||phone.isEmpty()||pass.isEmpty()||cpas.isEmpty()){showMsg(this,"All fields required.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            if(!validEmail(email)){showMsg(this,"Invalid email address.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            if(!pass.equals(cpas)){showMsg(this,"Passwords do not match.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            if(pass.length()<4){showMsg(this,"Password min 4 characters.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null;
            try{
                con=DB.get();
                PreparedStatement ck=con.prepareStatement("SELECT email FROM users WHERE email=?");
                ck.setString(1,email); ResultSet rs=ck.executeQuery();
                if(rs.next()){showMsg(this,"Email already registered.","Duplicate",JOptionPane.ERROR_MESSAGE);rs.close();ck.close();return;}
                rs.close(); ck.close();
                ps=con.prepareStatement("INSERT INTO users(name,email,phone,password,role) VALUES(?,?,?,?,'USER')");
                ps.setString(1,name); ps.setString(2,email); ps.setString(3,phone); ps.setString(4,pass);
                ps.executeUpdate();
                showMsg(this,"Registration successful!\nYou can now login.","Success",JOptionPane.INFORMATION_MESSAGE);
                parent.setVisible(true); dispose();
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // USER DASHBOARD — Dark + Glow Cards
    // ════════════════════════════════════════════════════════════════
    static class UserDashboard extends JFrame {
        UserDashboard(){
            setTitle("User Dashboard — Smart Community Rental");
            setSize(780,570); setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE); build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));

            // ── Header ──
            JPanel hdrP=new JPanel(new BorderLayout());
            hdrP.setBackground(BG_CARD);
            hdrP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL),
                BorderFactory.createEmptyBorder(16,24,16,24)));
            JLabel logo=lbl("\uD83C\uDFD8  Smart Community Rental",F_TITLE,TXT_WHITE);
            JPanel info=new JPanel(new GridLayout(2,1,0,3)); info.setOpaque(false);
            info.add(logo);
            info.add(lbl("Welcome, "+Session.userName+"   |   "+Session.userEmail,F_SMALL,CYAN));
            hdrP.add(info,BorderLayout.WEST);
            JLabel tag=lbl(" USER ",F_SMALL,BG_DARK);
            tag.setOpaque(true); tag.setBackground(GREEN);
            tag.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
            hdrP.add(tag,BorderLayout.EAST);
            root.add(hdrP,BorderLayout.NORTH);

            // ── Menu Cards ──
            String[][] items={
                {"\uD83D\uDCE6","Browse Resources",  "View & book resources"},
                {"\uD83D\uDCCB","My Bookings",         "Your booking history"},
                {"\uD83D\uDCB3","Payments",             "Make & view payments"},
                {"\uD83D\uDD01","Return Resource",      "Return with fine calc"},
                {"\u2B50",       "Feedback & Rating",   "Rate resources"},
                {"\uD83D\uDEAA","Logout",               "Sign out"}
            };
            Color[] accs={CYAN, GREEN, ORANGE, PURPLE, GOLD, RED};

            JPanel grid=darkPanel(new GridLayout(2,3,16,16));
            grid.setBorder(BorderFactory.createEmptyBorder(24,28,16,28));

            for(int i=0;i<items.length;i++){
                String[] it=items[i]; Color ac=accs[i]; int idx=i;
                HoverCard card=new HoverCard(ac);
                card.setLayout(new GridLayout(3,1,0,6));
                card.setBorder(BorderFactory.createEmptyBorder(18,14,18,14));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel ic=new JLabel(it[0],SwingConstants.CENTER);
                ic.setFont(new Font("Segoe UI Emoji",Font.PLAIN,28));
                JLabel tl=lbl(it[1],new Font("Segoe UI",Font.BOLD,13),TXT_WHITE);
                tl.setHorizontalAlignment(SwingConstants.CENTER);
                JLabel dl=lbl(it[2],F_SMALL,new Color(160,170,195));
                dl.setHorizontalAlignment(SwingConstants.CENTER);
                card.add(ic); card.add(tl); card.add(dl);

                card.addMouseListener(new MouseAdapter(){
                    public void mouseClicked(MouseEvent e){onMenu(idx);}
                });
                grid.add(card);
            }
            root.add(grid,BorderLayout.CENTER);

            JLabel foot=lbl("Smart Community Rental System  |  College Project  |  Java Swing + MySQL",F_SMALL,TXT_DIM);
            foot.setHorizontalAlignment(SwingConstants.CENTER);
            foot.setBorder(BorderFactory.createEmptyBorder(4,0,10,0));
            JPanel fp=darkPanel(new BorderLayout()); fp.add(foot,BorderLayout.CENTER);
            root.add(fp,BorderLayout.SOUTH);

            setContentPane(root);
        }

        void onMenu(int i){
            switch(i){
                case 0:new BrowseResourcesFrame(this).setVisible(true);setVisible(false);break;
                case 1:new MyBookingsFrame(this).setVisible(true);setVisible(false);break;
                case 2:new PaymentFrame(this).setVisible(true);setVisible(false);break;
                case 3:new ReturnResourceFrame(this).setVisible(true);setVisible(false);break;
                case 4:new FeedbackFrame(this).setVisible(true);setVisible(false);break;
                case 5:
                    if(JOptionPane.showConfirmDialog(this,"Logout?","Confirm",JOptionPane.YES_NO_OPTION)==0){
                        Session.logout(); new LoginFrame().setVisible(true); dispose();
                    }
                    break;
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // BROWSE RESOURCES
    // ════════════════════════════════════════════════════════════════
    static class BrowseResourcesFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl; JTextField srch;

        BrowseResourcesFrame(JFrame p){
            parent=p; setTitle("Browse Resources");
            setSize(950,540); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load(null);
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Browse Resources","Search and book available community resources"),BorderLayout.NORTH);

            // search bar
            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,12,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            srch=tf(22);
            RippleButton sb=btn("Search",CYAN.darker()), rb=btn("Refresh",new Color(40,100,60));
            top.add(lbl("Search:",F_LABEL,TXT_GRAY)); top.add(srch); top.add(sb); top.add(rb);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"ID","Resource Name","Category","Avail Qty","Rent/Day (₹)","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            int[] w={50,220,130,90,110,110};
            for(int i=0;i<w.length;i++) tbl.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            JScrollPane sp=darkScroll(tbl);
            sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,16,12));
            RippleButton bookBtn=btn("\uD83D\uDCE6  Book Selected Resource",new Color(0,140,100));
            RippleButton back   =btn("← Back to Dashboard",new Color(55,55,90));
            bp.add(bookBtn); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            sb.addActionListener(e->load(srch.getText().trim()));
            rb.addActionListener(e->{srch.setText("");load(null);});
            srch.addActionListener(e->load(srch.getText().trim()));
            bookBtn.addActionListener(e->openBook());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(String q){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                String sql="SELECT r.resource_id,r.resource_name,c.category_name,r.available_quantity,r.rent_per_day,r.status FROM resources r JOIN categories c ON r.category_id=c.category_id WHERE r.status='AVAILABLE' AND r.available_quantity>0";
                if(q!=null&&!q.isEmpty()){sql+=" AND (r.resource_name LIKE ? OR c.category_name LIKE ?)";ps=con.prepareStatement(sql);ps.setString(1,"%"+q+"%");ps.setString(2,"%"+q+"%");}
                else ps=con.prepareStatement(sql+" ORDER BY r.resource_name");
                rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),"₹"+rs.getString(5),rs.getString(6)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }

        void openBook(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Please select a resource first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int rid=(int)mdl.getValueAt(row,0);
            String rn=(String)mdl.getValueAt(row,1);
            double rent=Double.parseDouble(mdl.getValueAt(row,4).toString().replace("₹",""));
            int aqty=(int)mdl.getValueAt(row,3);
            new BookResourceFrame(this,rid,rn,rent,aqty).setVisible(true);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // BOOK RESOURCE — with transaction
    // ════════════════════════════════════════════════════════════════
    static class BookResourceFrame extends JFrame {
        JFrame parent; int rid; String rname; double rent; int aqty;
        JTextField qtyF,startF,endF,daysF,amtF;

        BookResourceFrame(JFrame p,int rid,String rn,double rent,int aqty){
            parent=p; this.rid=rid; rname=rn; this.rent=rent; this.aqty=aqty;
            setTitle("Book — "+rname); setSize(500,520);
            setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE); build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout());
            root.add(hdr("Book Resource","Complete booking for: "+rname),BorderLayout.NORTH);

            JPanel form=cardPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
            GridBagConstraints g=new GridBagConstraints();
            g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(8,5,8,5);

            // info rows
            iRow(form,g,0,"Resource:",rname,TXT_WHITE);
            iRow(form,g,1,"Rent / Day:","₹"+String.format("%.2f",rent),GOLD);
            iRow(form,g,2,"Available Qty:",String.valueOf(aqty),GREEN);

            // input fields
            g.gridx=0;g.gridy=3;g.weightx=0.40; form.add(lbl("Quantity:",F_LABEL,TXT_GRAY),g);
            g.gridx=1;g.weightx=0.60; qtyF=tf(12); form.add(qtyF,g);

            g.gridx=0;g.gridy=4; form.add(lbl("Start Date (yyyy-MM-dd):",F_LABEL,TXT_GRAY),g);
            g.gridx=1; startF=tf(12); startF.setText(today()); form.add(startF,g);

            g.gridx=0;g.gridy=5; form.add(lbl("End Date (yyyy-MM-dd):",F_LABEL,TXT_GRAY),g);
            g.gridx=1; endF=tf(12); form.add(endF,g);

            g.gridx=0;g.gridy=6; form.add(lbl("Number of Days:",F_LABEL,TXT_GRAY),g);
            g.gridx=1; daysF=tf(12); daysF.setEditable(false); daysF.setForeground(CYAN); form.add(daysF,g);

            g.gridx=0;g.gridy=7; form.add(lbl("Total Amount (₹):",F_LABEL,TXT_WHITE),g);
            g.gridx=1; amtF=tf(12); amtF.setEditable(false);
            amtF.setFont(new Font("Segoe UI",Font.BOLD,15)); amtF.setForeground(GOLD); form.add(amtF,g);

            RippleButton calc=btn("Calculate",ORANGE.darker());
            RippleButton conf=btn("✔ Confirm Booking",new Color(0,140,80));
            RippleButton back=btn("← Back",new Color(55,55,90));
            JPanel bp=cardPanel(new FlowLayout(FlowLayout.CENTER,12,12));
            bp.add(calc); bp.add(conf); bp.add(back);
            g.gridx=0;g.gridy=8;g.gridwidth=2;g.insets=new Insets(14,5,5,5); form.add(bp,g);

            calc.addActionListener(e->calc());
            conf.addActionListener(e->confirm());
            back.addActionListener(e->dispose());
            root.add(form,BorderLayout.CENTER); setContentPane(root);
        }

        void iRow(JPanel p,GridBagConstraints g,int r,String label,String val,Color vc){
            g.gridx=0;g.gridy=r;g.weightx=0.40; p.add(lbl(label,F_LABEL,TXT_GRAY),g);
            g.gridx=1;g.weightx=0.60; p.add(lbl(val,new Font("Segoe UI",Font.BOLD,13),vc),g);
        }

        void calc(){
            try{
                int qty=Integer.parseInt(qtyF.getText().trim());
                if(qty<=0){showMsg(this,"Quantity must be greater than 0.","Validation",JOptionPane.WARNING_MESSAGE);return;}
                if(qty>aqty){showMsg(this,"Quantity exceeds available ("+aqty+").","Validation",JOptionPane.WARNING_MESSAGE);return;}
                java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date s=sdf.parse(startF.getText().trim()), e=sdf.parse(endF.getText().trim());
                if(e.before(s)){showMsg(this,"End date cannot be before start date.","Validation",JOptionPane.WARNING_MESSAGE);return;}
                long days=(e.getTime()-s.getTime())/86400000L+1;
                daysF.setText(String.valueOf(days));
                amtF.setText(String.format("%.2f",qty*rent*days));
            }catch(NumberFormatException ex){showMsg(this,"Invalid quantity.","Error",JOptionPane.ERROR_MESSAGE);
            }catch(Exception ex){showMsg(this,"Invalid date. Use yyyy-MM-dd format.","Error",JOptionPane.ERROR_MESSAGE);}
        }

        void confirm(){
            if(amtF.getText().isEmpty()){showMsg(this,"Calculate amount first.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            if(!Session.isLoggedIn()){showMsg(this,"You must be logged in.","Error",JOptionPane.ERROR_MESSAGE);return;}
            if(JOptionPane.showConfirmDialog(this,
                "Confirm booking for "+rname+"?\n\nTotal Amount: ₹"+amtF.getText(),
                "Confirm Booking",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;

            Connection con=null; PreparedStatement ins=null,upd=null;
            try{
                int qty=Integer.parseInt(qtyF.getText().trim());
                double total=Double.parseDouble(amtF.getText().trim());
                con=DB.get(); con.setAutoCommit(false);

                ins=con.prepareStatement(
                    "INSERT INTO bookings(user_id,resource_id,booking_date,start_date,end_date,quantity,total_amount,status) VALUES(?,?,?,?,?,?,?,'PENDING')",
                    Statement.RETURN_GENERATED_KEYS);
                ins.setInt(1,Session.userId); ins.setInt(2,rid);
                ins.setString(3,today()); ins.setString(4,startF.getText().trim());
                ins.setString(5,endF.getText().trim()); ins.setInt(6,qty); ins.setDouble(7,total);
                ins.executeUpdate();
                ResultSet gk=ins.getGeneratedKeys(); int bid=-1; if(gk.next())bid=gk.getInt(1); gk.close();

                upd=con.prepareStatement("UPDATE resources SET available_quantity=available_quantity-? WHERE resource_id=?");
                upd.setInt(1,qty); upd.setInt(2,rid); upd.executeUpdate();
                con.commit();

                showMsg(this,
                    "✅  Booking Successful!\n\n"
                    +"Booking ID : #"+bid+"\n"
                    +"Resource   : "+rname+"\n"
                    +"Total Amt  : ₹"+String.format("%.2f",total)+"\n"
                    +"Status     : PENDING (awaiting admin approval)",
                    "Booking Confirmed",JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }catch(Exception ex){
                try{if(con!=null)con.rollback();}catch(Exception ig){}
                showMsg(this,"Booking failed!\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }finally{
                try{if(con!=null)con.setAutoCommit(true);}catch(Exception ig){}
                DB.close(con,ins);
                if(upd!=null)try{upd.close();}catch(Exception ig){}
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MY BOOKINGS — with cancel (transaction)
    // ════════════════════════════════════════════════════════════════
    static class MyBookingsFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl;

        MyBookingsFrame(JFrame p){
            parent=p; setTitle("My Bookings");
            setSize(1000,520); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("My Bookings","Your booking history — "+Session.userName),BorderLayout.NORTH);

            String[] cols={"Booking ID","Resource","Start Date","End Date","Qty","Amount (₹)","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);

            // color status column
            tbl.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer(){
                public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                    JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    String s=v!=null?v.toString():"";
                    if(sel){l.setBackground(new Color(0,210,255,50));l.setForeground(CYAN);}
                    else{l.setBackground(BG_CARD);
                        switch(s){case"PENDING":l.setForeground(ORANGE);break;case"APPROVED":l.setForeground(GREEN);break;case"CANCELLED":l.setForeground(RED);break;case"COMPLETED":l.setForeground(CYAN);break;case"REJECTED":l.setForeground(PINK);break;default:l.setForeground(TXT_WHITE);}
                    }
                    return l;
                }
            });

            JScrollPane sp=darkScroll(tbl);
            sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton ref=btn("↻ Refresh",CYAN.darker());
            RippleButton can=btn("✖ Cancel Booking",RED.darker());
            RippleButton back=btn("← Back",new Color(55,55,90));
            bp.add(ref); bp.add(can); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            ref .addActionListener(e->load());
            can .addActionListener(e->cancel());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                ps=con.prepareStatement("SELECT b.booking_id,r.resource_name,b.start_date,b.end_date,b.quantity,b.total_amount,b.status FROM bookings b JOIN resources r ON b.resource_id=r.resource_id WHERE b.user_id=? ORDER BY b.booking_id DESC");
                ps.setInt(1,Session.userId); rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(5),rs.getString(6),rs.getString(7)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }

        void cancel(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a booking to cancel.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int bid=(int)mdl.getValueAt(row,0);
            String st=(String)mdl.getValueAt(row,6);
            if("CANCELLED".equals(st)||"COMPLETED".equals(st)){
                showMsg(this,"Cannot cancel booking with status: "+st,"Not Allowed",JOptionPane.WARNING_MESSAGE);return;
            }
            if(JOptionPane.showConfirmDialog(this,"Cancel Booking #"+bid+"?\n\nQuantity will be restored to resource stock.","Confirm Cancel",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;

            int qty=(int)mdl.getValueAt(row,4);
            int resId=-1;
            // get resource_id
            Connection c2=null; PreparedStatement ps2=null; ResultSet rs2=null;
            try{
                c2=DB.get();
                ps2=c2.prepareStatement("SELECT resource_id FROM bookings WHERE booking_id=?");
                ps2.setInt(1,bid); rs2=ps2.executeQuery();
                if(rs2.next()) resId=rs2.getInt(1);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);return;}
            finally{DB.close(c2,ps2,rs2);}

            Connection con=null; PreparedStatement p1=null,p2=null;
            try{
                con=DB.get(); con.setAutoCommit(false);
                p1=con.prepareStatement("UPDATE bookings SET status='CANCELLED' WHERE booking_id=?");
                p1.setInt(1,bid); p1.executeUpdate();
                if(resId!=-1){
                    p2=con.prepareStatement("UPDATE resources SET available_quantity=available_quantity+? WHERE resource_id=?");
                    p2.setInt(1,qty); p2.setInt(2,resId); p2.executeUpdate();
                }
                con.commit();
                showMsg(this,"✅ Booking #"+bid+" cancelled successfully.\n\nResource quantity has been restored.","Cancelled",JOptionPane.INFORMATION_MESSAGE);
                load();
            }catch(Exception ex){
                try{if(con!=null)con.rollback();}catch(Exception ig){}
                showMsg(this,"Cancel failed: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }finally{
                try{if(con!=null)con.setAutoCommit(true);}catch(Exception ig){}
                DB.close(con,p1);
                if(p2!=null)try{p2.close();}catch(Exception ig){}
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PAYMENT FRAME
    // ════════════════════════════════════════════════════════════════
    static class PaymentFrame extends JFrame {
        JFrame parent; JTextField bidF,amtF; JComboBox<String> methodC;
        JTable tbl; DefaultTableModel mdl;

        PaymentFrame(JFrame p){
            parent=p; setTitle("Payments");
            setSize(860,560); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Payment Module","Make payments for your bookings"),BorderLayout.NORTH);

            // form bar
            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,12,12));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            bidF=tf(8); amtF=tf(10);
            methodC=combo("UPI","DEBIT_CARD","CREDIT_CARD","CASH");
            RippleButton pay=btn("✔ Make Payment",new Color(0,140,80));
            RippleButton ref=btn("↻ Refresh",CYAN.darker());
            top.add(lbl("Booking ID:",F_LABEL,TXT_GRAY)); top.add(bidF);
            top.add(lbl("Amount (₹):",F_LABEL,TXT_GRAY)); top.add(amtF);
            top.add(lbl("Method:",F_LABEL,TXT_GRAY)); top.add(methodC);
            top.add(pay); top.add(ref);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"Payment ID","Booking ID","Amount (₹)","Date","Method","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            // color status
            tbl.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){
                public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                    JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    l.setBackground(sel?new Color(0,210,255,50):BG_CARD);
                    String s=v!=null?v.toString():"";
                    switch(s){case"SUCCESS":l.setForeground(GREEN);break;case"PENDING":l.setForeground(ORANGE);break;case"FAILED":l.setForeground(RED);break;case"REFUNDED":l.setForeground(PURPLE);break;default:l.setForeground(TXT_WHITE);}
                    return l;
                }
            });
            JScrollPane sp=darkScroll(tbl);
            sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90));
            bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            pay .addActionListener(e->pay());
            ref .addActionListener(e->load());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void pay(){
            String bs=bidF.getText().trim(),as=amtF.getText().trim();
            if(bs.isEmpty()||as.isEmpty()){showMsg(this,"Enter Booking ID and Amount.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                int bid=Integer.parseInt(bs);
                double amt=Double.parseDouble(as);
                String method=(String)methodC.getSelectedItem();
                con=DB.get();
                // verify booking belongs to user
                ps=con.prepareStatement("SELECT booking_id,status FROM bookings WHERE booking_id=? AND user_id=?");
                ps.setInt(1,bid); ps.setInt(2,Session.userId);
                rs=ps.executeQuery();
                if(!rs.next()){showMsg(this,"Booking #"+bid+" not found or does not belong to you.","Not Found",JOptionPane.ERROR_MESSAGE);return;}
                rs.close(); ps.close();

                ps=con.prepareStatement("INSERT INTO payments(booking_id,amount,payment_method,payment_status) VALUES(?,?,?,'SUCCESS')");
                ps.setInt(1,bid); ps.setDouble(2,amt); ps.setString(3,method);
                ps.executeUpdate();

                showMsg(this,"✅  Payment Successful!\n\nBooking ID : #"+bid+"\nAmount     : ₹"+String.format("%.2f",amt)+"\nMethod     : "+method,"Payment Done",JOptionPane.INFORMATION_MESSAGE);
                bidF.setText(""); amtF.setText(""); load();
            }catch(NumberFormatException ex){showMsg(this,"Invalid Booking ID or Amount.","Error",JOptionPane.ERROR_MESSAGE);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                ps=con.prepareStatement("SELECT p.payment_id,p.booking_id,p.amount,p.payment_date,p.payment_method,p.payment_status FROM payments p JOIN bookings b ON p.booking_id=b.booking_id WHERE b.user_id=? ORDER BY p.payment_id DESC");
                ps.setInt(1,Session.userId); rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // RETURN RESOURCE — with fine calculation + transaction
    // ════════════════════════════════════════════════════════════════
    static class ReturnResourceFrame extends JFrame {
        JFrame parent;
        JTextField bidF,dueF,retF,fpdF,ldF,fineF;

        ReturnResourceFrame(JFrame p){
            parent=p; setTitle("Return Resource");
            setSize(490,500); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE); build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout());
            root.add(hdr("Return Resource","Calculate fine & confirm return"),BorderLayout.NORTH);

            JPanel form=cardPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(18,40,18,40));
            GridBagConstraints g=new GridBagConstraints();
            g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(9,5,9,5);

            bidF=tf(15); dueF=tf(15); retF=tf(15);
            fpdF=tf(15); ldF=tf(15); fineF=tf(15);
            retF.setText(today()); fpdF.setText("50");
            ldF.setEditable(false); ldF.setForeground(CYAN);
            fineF.setEditable(false); fineF.setForeground(GOLD);
            fineF.setFont(new Font("Segoe UI",Font.BOLD,14));

            String[] ls={"Booking ID:","Due Date (yyyy-MM-dd):","Actual Return Date:","Fine Per Day (₹):","Late Days:","Fine Amount (₹):"};
            JTextField[] fs={bidF,dueF,retF,fpdF,ldF,fineF};
            for(int i=0;i<ls.length;i++){
                g.gridx=0;g.gridy=i;g.weightx=0.44; form.add(lbl(ls[i],F_LABEL,TXT_GRAY),g);
                g.gridx=1;g.weightx=0.56; form.add(fs[i],g);
            }

            RippleButton calc=btn("Calculate Fine",ORANGE.darker());
            RippleButton conf=btn("✔ Confirm Return",new Color(0,140,80));
            RippleButton back=btn("← Back",new Color(55,55,90));
            JPanel bp=cardPanel(new FlowLayout(FlowLayout.CENTER,12,12));
            bp.add(calc); bp.add(conf); bp.add(back);
            g.gridx=0;g.gridy=ls.length;g.gridwidth=2;g.insets=new Insets(14,5,5,5); form.add(bp,g);

            calc.addActionListener(e->calcFine());
            conf.addActionListener(e->confirmReturn());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            root.add(form,BorderLayout.CENTER); setContentPane(root);
        }

        void calcFine(){
            try{
                java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date due=sdf.parse(dueF.getText().trim()), ret=sdf.parse(retF.getText().trim());
                double fpd=Double.parseDouble(fpdF.getText().trim());
                long late=0; double fine=0;
                if(ret.after(due)){late=(ret.getTime()-due.getTime())/86400000L; fine=late*fpd;}
                ldF.setText(String.valueOf(late));
                fineF.setText(String.format("%.2f",fine));
            }catch(Exception ex){showMsg(this,"Invalid date. Use yyyy-MM-dd.","Error",JOptionPane.ERROR_MESSAGE);}
        }

        void confirmReturn(){
            if(bidF.getText().trim().isEmpty()||fineF.getText().trim().isEmpty()){
                showMsg(this,"Fill all fields and calculate fine first.","Validation",JOptionPane.WARNING_MESSAGE);return;
            }
            int bid; double fine; long late; String retDate;
            try{
                bid=Integer.parseInt(bidF.getText().trim());
                fine=Double.parseDouble(fineF.getText().trim());
                late=Long.parseLong(ldF.getText().trim());
                retDate=retF.getText().trim();
            }catch(NumberFormatException ex){showMsg(this,"Invalid Booking ID.","Error",JOptionPane.ERROR_MESSAGE);return;}

            String status=late>0?"LATE":"RETURNED";

            // Step 1: get resource_id and quantity
            int resId=-1; int qty=0;
            Connection c2=null; PreparedStatement ps2=null; ResultSet rs2=null;
            try{
                c2=DB.get();
                ps2=c2.prepareStatement("SELECT resource_id,quantity FROM bookings WHERE booking_id=? AND user_id=?");
                ps2.setInt(1,bid); ps2.setInt(2,Session.userId);
                rs2=ps2.executeQuery();
                if(!rs2.next()){showMsg(this,"Booking #"+bid+" not found or does not belong to you.","Not Found",JOptionPane.ERROR_MESSAGE);return;}
                resId=rs2.getInt(1); qty=rs2.getInt(2);
            }catch(Exception ex){showMsg(this,"DB Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);return;}
            finally{DB.close(c2,ps2,rs2);}

            // Step 2: transaction — insert return, update booking, restore qty
            Connection con=null; PreparedStatement p1=null,p2=null,p3=null;
            try{
                con=DB.get(); con.setAutoCommit(false);
                p1=con.prepareStatement("INSERT INTO returns(booking_id,actual_return_date,fine_amount,return_status) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE actual_return_date=?,fine_amount=?,return_status=?");
                p1.setInt(1,bid); p1.setString(2,retDate); p1.setDouble(3,fine); p1.setString(4,status);
                p1.setString(5,retDate); p1.setDouble(6,fine); p1.setString(7,status);
                p1.executeUpdate();
                p2=con.prepareStatement("UPDATE bookings SET status='COMPLETED' WHERE booking_id=?");
                p2.setInt(1,bid); p2.executeUpdate();
                p3=con.prepareStatement("UPDATE resources SET available_quantity=available_quantity+? WHERE resource_id=?");
                p3.setInt(1,qty); p3.setInt(2,resId); p3.executeUpdate();
                con.commit();

                showMsg(this,
                    "✅  Return Confirmed!\n\n"
                    +"Booking #"+bid+" → COMPLETED\n"
                    +"Fine Amount : ₹"+String.format("%.2f",fine)+"\n"
                    +(late>0?"Late by "+late+" day(s)":"Returned on time ✓"),
                    "Return Successful",JOptionPane.INFORMATION_MESSAGE);
                bidF.setText(""); dueF.setText(""); retF.setText(today()); fpdF.setText("50"); ldF.setText(""); fineF.setText("");
            }catch(Exception ex){
                try{if(con!=null)con.rollback();}catch(Exception ig){}
                showMsg(this,"Return failed: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }finally{
                try{if(con!=null)con.setAutoCommit(true);}catch(Exception ig){}
                DB.close(con,p1);
                if(p2!=null)try{p2.close();}catch(Exception ig){}
                if(p3!=null)try{p3.close();}catch(Exception ig){}
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // FEEDBACK FRAME
    // ════════════════════════════════════════════════════════════════
    static class FeedbackFrame extends JFrame {
        JFrame parent; JComboBox<String> resC; JTextField rateF,comF;
        JTable tbl; DefaultTableModel mdl;
        java.util.List<Integer> resIds=new java.util.ArrayList<>();

        FeedbackFrame(JFrame p){
            parent=p; setTitle("Feedback & Rating");
            setSize(800,520); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); loadRes(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Feedback & Rating","Rate the resources you have used"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,12,12));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            resC=new JComboBox<>(); resC.setFont(F_LABEL); resC.setBackground(BG_CARD2); resC.setForeground(TXT_WHITE);
            resC.setPreferredSize(new Dimension(200,32));
            rateF=tf(5); rateF.setToolTipText("Enter 1-5");
            comF=tf(24);
            RippleButton sub=btn("✔ Submit",new Color(0,140,80));
            RippleButton ref=btn("↻ Refresh",CYAN.darker());
            top.add(lbl("Resource:",F_LABEL,TXT_GRAY)); top.add(resC);
            top.add(lbl("Rating (1-5):",F_LABEL,TXT_GRAY)); top.add(rateF);
            top.add(lbl("Comment:",F_LABEL,TXT_GRAY)); top.add(comF);
            top.add(sub); top.add(ref);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"ID","Resource","Rating","Comment","Date"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            tbl.getColumnModel().getColumn(3).setPreferredWidth(280);
            JScrollPane sp=darkScroll(tbl);
            sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            sub .addActionListener(e->submit());
            ref .addActionListener(e->load());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void loadRes(){
            resC.removeAllItems(); resIds.clear();
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT resource_id,resource_name FROM resources ORDER BY resource_name");
                while(rs.next()){resIds.add(rs.getInt(1));resC.addItem(rs.getString(2));}}
            catch(Exception ex){}finally{DB.close(con,st,rs);}
        }

        void submit(){
            if(resC.getSelectedIndex()<0){showMsg(this,"Select a resource.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            String rs=rateF.getText().trim();
            if(rs.isEmpty()){showMsg(this,"Enter a rating (1-5).","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null;
            try{
                int rating=Integer.parseInt(rs);
                if(rating<1||rating>5){showMsg(this,"Rating must be between 1 and 5.","Validation",JOptionPane.WARNING_MESSAGE);return;}
                int resId=resIds.get(resC.getSelectedIndex());
                con=DB.get();
                ps=con.prepareStatement("INSERT INTO feedback(user_id,resource_id,rating,comment) VALUES(?,?,?,?)");
                ps.setInt(1,Session.userId); ps.setInt(2,resId); ps.setInt(3,rating); ps.setString(4,comF.getText().trim());
                ps.executeUpdate();
                showMsg(this,"✅ Feedback submitted! Thank you.","Success",JOptionPane.INFORMATION_MESSAGE);
                rateF.setText(""); comF.setText(""); load();
            }catch(NumberFormatException ex){showMsg(this,"Invalid rating. Enter 1-5.","Error",JOptionPane.ERROR_MESSAGE);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                ps=con.prepareStatement("SELECT f.feedback_id,r.resource_name,f.rating,f.comment,f.feedback_date FROM feedback f JOIN resources r ON f.resource_id=r.resource_id WHERE f.user_id=? ORDER BY f.feedback_id DESC");
                ps.setInt(1,Session.userId); rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getInt(3),rs.getString(4),rs.getString(5)});
            }catch(Exception ex){}finally{DB.close(con,ps,rs);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN DASHBOARD — Dark with glow cards
    // ════════════════════════════════════════════════════════════════
    static class AdminDashboard extends JFrame {
        AdminDashboard(){
            setTitle("Admin Dashboard — Smart Community Rental");
            setSize(920,620); setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE); build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));

            // Header
            JPanel hdrP=new JPanel(new BorderLayout());
            hdrP.setBackground(BG_CARD);
            hdrP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,2,0,CYAN),
                BorderFactory.createEmptyBorder(16,24,16,24)));
            JPanel tp=new JPanel(new GridLayout(2,1,0,4)); tp.setOpaque(false);
            tp.add(lbl("\uD83D\uDEE1  Admin Dashboard",F_TITLE,TXT_WHITE));
            tp.add(lbl("Smart Community Rental System  |  Admin: "+Session.userName,F_SMALL,CYAN));
            hdrP.add(tp,BorderLayout.WEST);
            JLabel tag=lbl(" ADMIN ",F_SMALL,BG_DARK);
            tag.setOpaque(true); tag.setBackground(RED);
            tag.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
            hdrP.add(tag,BorderLayout.EAST);
            root.add(hdrP,BorderLayout.NORTH);

            // Summary cards
            JPanel cards=darkPanel(new GridLayout(1,4,14,0));
            cards.setBorder(BorderFactory.createEmptyBorder(14,22,8,22));
            Object[][] sc={
                {"Total Users","SELECT COUNT(*) FROM users WHERE role='USER'",CYAN},
                {"Resources",  "SELECT COUNT(*) FROM resources",              GREEN},
                {"Bookings",   "SELECT COUNT(*) FROM bookings",               ORANGE},
                {"Payments",   "SELECT COUNT(*) FROM payments",               PURPLE}
            };
            for(Object[] s:sc) cards.add(summCard((String)s[0],(String)s[1],(Color)s[2]));
            root.add(cards,BorderLayout.AFTER_LAST_LINE);

            // Menu
            String[][] menu={
                {"\uD83D\uDC65","Manage Users"},
                {"\uD83D\uDDC2","Manage Categories"},
                {"\uD83D\uDCE6","Manage Resources"},
                {"\uD83D\uDCCB","Manage Bookings"},
                {"\uD83D\uDCB3","Manage Payments"},
                {"\uD83D\uDD01","Manage Returns"},
                {"\uD83D\uDCB0","Manage Fines"},
                {"\u2B50",       "View Feedback"},
                {"\uD83D\uDCCA","Reports"},
                {"\uD83D\uDEAA","Logout"}
            };
            Color[] mc={CYAN,GREEN,ORANGE,PURPLE,PINK,new Color(0,180,140),RED,GOLD,new Color(80,160,255),new Color(80,80,130)};

            JPanel grid=darkPanel(new GridLayout(2,5,10,10));
            grid.setBorder(BorderFactory.createEmptyBorder(8,22,10,22));
            for(int i=0;i<menu.length;i++){
                String[] m=menu[i]; Color ac=mc[i]; int idx=i;
                HoverCard card=new HoverCard(ac);
                card.setLayout(new GridLayout(2,1,0,5));
                card.setBorder(BorderFactory.createEmptyBorder(14,8,14,8));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                JLabel ic=new JLabel(m[0],SwingConstants.CENTER);
                ic.setFont(new Font("Segoe UI Emoji",Font.PLAIN,22));
                JLabel tl=lbl(m[1],new Font("Segoe UI",Font.BOLD,11),TXT_WHITE);
                tl.setHorizontalAlignment(SwingConstants.CENTER);
                card.add(ic); card.add(tl);
                card.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){onMenu(idx);}});
                grid.add(card);
            }
            root.add(grid,BorderLayout.CENTER);

            JLabel foot=lbl("Smart Community Rental  |  Admin Panel  |  Java Swing + MySQL  |  College Project",F_SMALL,TXT_DIM);
            foot.setHorizontalAlignment(SwingConstants.CENTER);
            foot.setBorder(BorderFactory.createEmptyBorder(4,0,8,0));
            JPanel fp=darkPanel(new BorderLayout()); fp.add(foot,BorderLayout.CENTER);
            root.add(fp,BorderLayout.SOUTH);
            setContentPane(root);
        }

        JPanel summCard(String title, String query, Color ac){
            JPanel p=new JPanel(new BorderLayout());
            p.setBackground(BG_CARD2);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,0,0,BORDER_COL),
                BorderFactory.createEmptyBorder(14,10,14,10)));
            // top accent bar
            JPanel bar=new JPanel(); bar.setBackground(ac); bar.setPreferredSize(new Dimension(0,3));
            p.add(bar,BorderLayout.NORTH);
            String val="0";
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery(query);if(rs.next())val=rs.getString(1);}
            catch(Exception ig){} finally{DB.close(con,st,rs);}
            JLabel vl=lbl(val,new Font("Segoe UI",Font.BOLD,28),ac);
            vl.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel tl=lbl(title,F_SMALL,TXT_GRAY);
            tl.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(vl,BorderLayout.CENTER); p.add(tl,BorderLayout.SOUTH);
            return p;
        }

        void onMenu(int i){
            switch(i){
                case 0:new ManageUsersFrame(this).setVisible(true);      setVisible(false);break;
                case 1:new ManageCategoriesFrame(this).setVisible(true); setVisible(false);break;
                case 2:new ManageResourcesFrame(this).setVisible(true);  setVisible(false);break;
                case 3:new ManageBookingsFrame(this).setVisible(true);   setVisible(false);break;
                case 4:new ManagePaymentsFrame(this).setVisible(true);   setVisible(false);break;
                case 5:case 6:new ManageReturnsFrame(this).setVisible(true);setVisible(false);break;
                case 7:new ViewFeedbackFrame(this).setVisible(true);     setVisible(false);break;
                case 8:new ReportsFrame(this).setVisible(true);          setVisible(false);break;
                case 9:
                    if(JOptionPane.showConfirmDialog(this,"Logout?","Confirm",JOptionPane.YES_NO_OPTION)==0){
                        Session.logout(); new LoginFrame().setVisible(true); dispose();
                    }
                    break;
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MANAGE USERS
    // ════════════════════════════════════════════════════════════════
    static class ManageUsersFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl; JTextField srch;

        ManageUsersFrame(JFrame p){
            parent=p; setTitle("Manage Users — Admin");
            setSize(950,510); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load(null);
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Manage Users","View, add, search and delete users"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,10,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            srch=tf(18);
            RippleButton sb=btn("Search",CYAN.darker()),rb=btn("↻ Refresh",new Color(40,100,60)),
                         ab=btn("+ Add",GREEN.darker()),db=btn("✖ Delete",RED.darker());
            top.add(lbl("Search:",F_LABEL,TXT_GRAY)); top.add(srch);
            top.add(sb);top.add(rb);top.add(ab);top.add(db);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"User ID","Name","Email","Phone","Role","Created At"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            sb.addActionListener(e->load(srch.getText().trim()));
            rb.addActionListener(e->{srch.setText("");load(null);});
            ab.addActionListener(e->addUser());
            db.addActionListener(e->delUser());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(String q){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                String sql="SELECT user_id,name,email,phone,role,created_at FROM users";
                if(q!=null&&!q.isEmpty()){sql+=" WHERE name LIKE ? OR email LIKE ?";ps=con.prepareStatement(sql);ps.setString(1,"%"+q+"%");ps.setString(2,"%"+q+"%");}
                else ps=con.prepareStatement(sql+" ORDER BY user_id DESC");
                rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }

        void addUser(){
            JTextField nf=tf(16),ef=tf(16),phf=tf(16); JPasswordField pwf=pf(16);
            JComboBox<String> rc=combo("USER","ADMIN");
            JPanel p=new JPanel(new GridLayout(5,2,8,8)); p.setBackground(BG_CARD2);
            p.add(new JLabel("Name:")); p.add(nf);
            p.add(new JLabel("Email:")); p.add(ef);
            p.add(new JLabel("Phone:")); p.add(phf);
            p.add(new JLabel("Password:")); p.add(pwf);
            p.add(new JLabel("Role:")); p.add(rc);
            if(JOptionPane.showConfirmDialog(this,p,"Add New User",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
            String name=nf.getText().trim(),email=ef.getText().trim(),phone=phf.getText().trim(),
                   pass=new String(pwf.getPassword()).trim(),role=(String)rc.getSelectedItem();
            if(name.isEmpty()||email.isEmpty()||pass.isEmpty()){showMsg(this,"Name, Email & Password required.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            if(!validEmail(email)){showMsg(this,"Invalid email.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null;
            try{
                con=DB.get(); ps=con.prepareStatement("INSERT INTO users(name,email,phone,password,role) VALUES(?,?,?,?,?)");
                ps.setString(1,name);ps.setString(2,email);ps.setString(3,phone);ps.setString(4,pass);ps.setString(5,role);
                ps.executeUpdate(); showMsg(this,"✅ User added successfully!","Success",JOptionPane.INFORMATION_MESSAGE); load(null);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void delUser(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a user to delete.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int uid=(int)mdl.getValueAt(row,0);
            if(uid==Session.userId){showMsg(this,"Cannot delete your own account.","Not Allowed",JOptionPane.WARNING_MESSAGE);return;}
            if(JOptionPane.showConfirmDialog(this,"Delete user #"+uid+"?","Confirm",JOptionPane.YES_NO_OPTION)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{
                con=DB.get(); ps=con.prepareStatement("DELETE FROM users WHERE user_id=?"); ps.setInt(1,uid); ps.executeUpdate();
                showMsg(this,"User deleted.","Done",JOptionPane.INFORMATION_MESSAGE); load(null);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage()+"\n(Cannot delete users with active bookings)","Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MANAGE CATEGORIES
    // ════════════════════════════════════════════════════════════════
    static class ManageCategoriesFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl;

        ManageCategoriesFrame(JFrame p){
            parent=p; setTitle("Manage Categories — Admin");
            setSize(720,460); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Manage Categories","Add, update, delete categories"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,10,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            RippleButton ab=btn("+ Add",GREEN.darker()),eb=btn("✎ Update",ORANGE.darker()),
                         db=btn("✖ Delete",RED.darker()),rb=btn("↻ Refresh",CYAN.darker());
            top.add(ab);top.add(eb);top.add(db);top.add(rb);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"ID","Category Name","Description","Created At"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            ab.addActionListener(e->add()); eb.addActionListener(e->edit());
            db.addActionListener(e->del()); rb.addActionListener(e->load());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT * FROM categories ORDER BY category_name");
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)});}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,st,rs);}
        }

        void add(){
            JTextField nf=tf(18),df=tf(18);
            JPanel p=new JPanel(new GridLayout(2,2,8,8)); p.setBackground(BG_CARD2);
            p.add(new JLabel("Name:")); p.add(nf); p.add(new JLabel("Description:")); p.add(df);
            if(JOptionPane.showConfirmDialog(this,p,"Add Category",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=0)return;
            if(nf.getText().trim().isEmpty()){showMsg(this,"Category name required.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("INSERT INTO categories(category_name,description) VALUES(?,?)");
                ps.setString(1,nf.getText().trim());ps.setString(2,df.getText().trim());ps.executeUpdate();
                showMsg(this,"✅ Category added!","Success",JOptionPane.INFORMATION_MESSAGE);load();}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void edit(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a category.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int cid=(int)mdl.getValueAt(row,0);
            JTextField nf=tf(18),df=tf(18);
            nf.setText((String)mdl.getValueAt(row,1)); df.setText((String)mdl.getValueAt(row,2));
            JPanel p=new JPanel(new GridLayout(2,2,8,8)); p.setBackground(BG_CARD2);
            p.add(new JLabel("Name:")); p.add(nf); p.add(new JLabel("Description:")); p.add(df);
            if(JOptionPane.showConfirmDialog(this,p,"Update Category",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("UPDATE categories SET category_name=?,description=? WHERE category_id=?");
                ps.setString(1,nf.getText().trim());ps.setString(2,df.getText().trim());ps.setInt(3,cid);ps.executeUpdate();
                showMsg(this,"✅ Updated!","Success",JOptionPane.INFORMATION_MESSAGE);load();}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void del(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a category.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int cid=(int)mdl.getValueAt(row,0);
            if(JOptionPane.showConfirmDialog(this,"Delete this category?","Confirm",JOptionPane.YES_NO_OPTION)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("DELETE FROM categories WHERE category_id=?");ps.setInt(1,cid);ps.executeUpdate();
                showMsg(this,"Deleted!","Done",JOptionPane.INFORMATION_MESSAGE);load();}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage()+"\n(Remove resources in this category first)","Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MANAGE RESOURCES
    // ════════════════════════════════════════════════════════════════
    static class ManageResourcesFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl; JTextField srch;

        ManageResourcesFrame(JFrame p){
            parent=p; setTitle("Manage Resources — Admin");
            setSize(1080,540); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load(null);
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Manage Resources","Add, edit, delete and search resources"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,8,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            srch=tf(16);
            RippleButton sb=btn("Search",CYAN.darker()),rb=btn("↻",new Color(40,100,60)),
                         ab=btn("+ Add",GREEN.darker()),eb=btn("✎ Edit",ORANGE.darker()),
                         db=btn("✖ Del",RED.darker()),stb=btn("Status",PURPLE.darker());
            top.add(lbl("Search:",F_LABEL,TXT_GRAY));top.add(srch);
            top.add(sb);top.add(rb);top.add(ab);top.add(eb);top.add(db);top.add(stb);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"ID","Resource Name","Category","Qty","Avail Qty","Rent/Day","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            sb.addActionListener(e->load(srch.getText().trim()));
            rb.addActionListener(e->{srch.setText("");load(null);});
            ab.addActionListener(e->addRes()); eb.addActionListener(e->editRes());
            db.addActionListener(e->delRes()); stb.addActionListener(e->changeSt());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(String q){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                String sql="SELECT r.resource_id,r.resource_name,c.category_name,r.quantity,r.available_quantity,r.rent_per_day,r.status FROM resources r JOIN categories c ON r.category_id=c.category_id";
                if(q!=null&&!q.isEmpty()){sql+=" WHERE r.resource_name LIKE ? OR c.category_name LIKE ?";ps=con.prepareStatement(sql);ps.setString(1,"%"+q+"%");ps.setString(2,"%"+q+"%");}
                else ps=con.prepareStatement(sql+" ORDER BY r.resource_name");
                rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5),rs.getString(6),rs.getString(7)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }

        String[] getCatNames(){
            java.util.List<String> l=new java.util.ArrayList<>();
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT category_name FROM categories ORDER BY category_name");while(rs.next())l.add(rs.getString(1));}
            catch(Exception ig){}finally{DB.close(con,st,rs);}
            return l.toArray(new String[0]);
        }
        int[] getCatIds(){
            java.util.List<Integer> l=new java.util.ArrayList<>();
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT category_id FROM categories ORDER BY category_name");while(rs.next())l.add(rs.getInt(1));}
            catch(Exception ig){}finally{DB.close(con,st,rs);}
            return l.stream().mapToInt(Integer::intValue).toArray();
        }

        void addRes(){
            String[] names=getCatNames(); int[] ids=getCatIds();
            JTextField nf=tf(14),df=tf(14),qf=tf(8),rf=tf(8);
            JComboBox<String> cc=combo(names), sc=combo("AVAILABLE","UNAVAILABLE","MAINTENANCE");
            JPanel p=new JPanel(new GridLayout(6,2,8,8)); p.setBackground(BG_CARD2);
            p.add(new JLabel("Name:")); p.add(nf); p.add(new JLabel("Category:")); p.add(cc);
            p.add(new JLabel("Desc:"));  p.add(df); p.add(new JLabel("Qty:"));  p.add(qf);
            p.add(new JLabel("Rent/Day:")); p.add(rf); p.add(new JLabel("Status:")); p.add(sc);
            if(JOptionPane.showConfirmDialog(this,p,"Add Resource",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=0)return;
            if(nf.getText().trim().isEmpty()||qf.getText().trim().isEmpty()||rf.getText().trim().isEmpty()){showMsg(this,"Name, Qty & Rent required.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Connection con=null; PreparedStatement ps=null;
            try{
                int qty=Integer.parseInt(qf.getText().trim()); double rent=Double.parseDouble(rf.getText().trim());
                int catId=ids[cc.getSelectedIndex()];
                con=DB.get();ps=con.prepareStatement("INSERT INTO resources(category_id,resource_name,description,quantity,available_quantity,rent_per_day,status) VALUES(?,?,?,?,?,?,?)");
                ps.setInt(1,catId);ps.setString(2,nf.getText().trim());ps.setString(3,df.getText().trim());
                ps.setInt(4,qty);ps.setInt(5,qty);ps.setDouble(6,rent);ps.setString(7,(String)sc.getSelectedItem());
                ps.executeUpdate(); showMsg(this,"✅ Resource added!","Success",JOptionPane.INFORMATION_MESSAGE); load(null);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void editRes(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a resource.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int rid=(int)mdl.getValueAt(row,0);
            JTextField nf=tf(14),df=tf(14),qf=tf(8),aqf=tf(8),rf=tf(8);
            nf.setText((String)mdl.getValueAt(row,1));
            qf.setText(mdl.getValueAt(row,3).toString()); aqf.setText(mdl.getValueAt(row,4).toString());
            rf.setText(mdl.getValueAt(row,5).toString());
            JPanel p=new JPanel(new GridLayout(5,2,8,8)); p.setBackground(BG_CARD2);
            p.add(new JLabel("Name:")); p.add(nf); p.add(new JLabel("Desc:")); p.add(df);
            p.add(new JLabel("Qty:")); p.add(qf); p.add(new JLabel("Avail Qty:")); p.add(aqf);
            p.add(new JLabel("Rent/Day:")); p.add(rf);
            if(JOptionPane.showConfirmDialog(this,p,"Edit Resource",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{
                con=DB.get();ps=con.prepareStatement("UPDATE resources SET resource_name=?,description=?,quantity=?,available_quantity=?,rent_per_day=? WHERE resource_id=?");
                ps.setString(1,nf.getText().trim());ps.setString(2,df.getText().trim());
                ps.setInt(3,Integer.parseInt(qf.getText().trim()));ps.setInt(4,Integer.parseInt(aqf.getText().trim()));
                ps.setDouble(5,Double.parseDouble(rf.getText().trim()));ps.setInt(6,rid);
                ps.executeUpdate(); showMsg(this,"✅ Updated!","Success",JOptionPane.INFORMATION_MESSAGE); load(null);
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void delRes(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a resource.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int rid=(int)mdl.getValueAt(row,0);
            if(JOptionPane.showConfirmDialog(this,"Delete resource #"+rid+"?","Confirm",JOptionPane.YES_NO_OPTION)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("DELETE FROM resources WHERE resource_id=?");ps.setInt(1,rid);ps.executeUpdate();
                showMsg(this,"Deleted!","Done",JOptionPane.INFORMATION_MESSAGE);load(null);}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage()+"\n(Active bookings prevent deletion)","Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }

        void changeSt(){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a resource.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int rid=(int)mdl.getValueAt(row,0);
            JComboBox<String> sc=combo("AVAILABLE","UNAVAILABLE","MAINTENANCE");
            sc.setSelectedItem(mdl.getValueAt(row,6));
            if(JOptionPane.showConfirmDialog(this,sc,"Change Status",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("UPDATE resources SET status=? WHERE resource_id=?");
                ps.setString(1,(String)sc.getSelectedItem());ps.setInt(2,rid);ps.executeUpdate();
                showMsg(this,"✅ Status updated!","Success",JOptionPane.INFORMATION_MESSAGE);load(null);}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MANAGE BOOKINGS
    // ════════════════════════════════════════════════════════════════
    static class ManageBookingsFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl;

        ManageBookingsFrame(JFrame p){
            parent=p; setTitle("Manage Bookings — Admin");
            setSize(1100,530); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Manage Bookings","View and manage all community bookings"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,10,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            RippleButton ab=btn("✔ Approve",GREEN.darker()),rb=btn("✖ Reject",RED.darker()),
                         cb=btn("✖ Cancel",ORANGE.darker()),ref=btn("↻ Refresh",CYAN.darker());
            top.add(ab);top.add(rb);top.add(cb);top.add(ref);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"Booking ID","User","Resource","Booking Date","Start Date","End Date","Qty","Amount (₹)","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            // Status renderer
            tbl.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer(){
                public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                    JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    l.setBackground(sel?new Color(0,210,255,40):BG_CARD);
                    String s=v!=null?v.toString():"";
                    switch(s){case"PENDING":l.setForeground(ORANGE);break;case"APPROVED":l.setForeground(GREEN);break;case"REJECTED":l.setForeground(RED);break;case"CANCELLED":l.setForeground(PINK);break;case"COMPLETED":l.setForeground(CYAN);break;default:l.setForeground(TXT_WHITE);}
                    return l;
                }
            });
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            ab.addActionListener(e->upd("APPROVED")); rb.addActionListener(e->upd("REJECTED"));
            cb.addActionListener(e->upd("CANCELLED")); ref.addActionListener(e->load());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{
                con=DB.get();st=con.createStatement();
                rs=st.executeQuery("SELECT b.booking_id,u.name,r.resource_name,b.booking_date,b.start_date,b.end_date,b.quantity,b.total_amount,b.status FROM bookings b JOIN users u ON b.user_id=u.user_id JOIN resources r ON b.resource_id=r.resource_id ORDER BY b.booking_id DESC");
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getInt(7),rs.getString(8),rs.getString(9)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,st,rs);}
        }

        void upd(String st){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a booking.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int bid=(int)mdl.getValueAt(row,0);
            if(JOptionPane.showConfirmDialog(this,"Set Booking #"+bid+" → "+st+"?","Confirm",JOptionPane.YES_NO_OPTION)!=0)return;
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("UPDATE bookings SET status=? WHERE booking_id=?");ps.setString(1,st);ps.setInt(2,bid);ps.executeUpdate();
                showMsg(this,"✅ Booking #"+bid+" → "+st,"Done",JOptionPane.INFORMATION_MESSAGE);load();}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MANAGE PAYMENTS
    // ════════════════════════════════════════════════════════════════
    static class ManagePaymentsFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl;

        ManagePaymentsFrame(JFrame p){
            parent=p; setTitle("Manage Payments — Admin");
            setSize(980,510); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Manage Payments","View and manage all payment records"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,10,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            RippleButton sb=btn("✔ Mark Success",GREEN.darker()),pb=btn("⏳ Mark Pending",ORANGE.darker()),
                         rb=btn("↩ Refund",PURPLE.darker()),ref=btn("↻ Refresh",CYAN.darker());
            top.add(sb);top.add(pb);top.add(rb);top.add(ref);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"Payment ID","Booking ID","Amount (₹)","Date","Method","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            sb.addActionListener(e->upd("SUCCESS")); pb.addActionListener(e->upd("PENDING"));
            rb.addActionListener(e->upd("REFUNDED")); ref.addActionListener(e->load());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT payment_id,booking_id,amount,payment_date,payment_method,payment_status FROM payments ORDER BY payment_id DESC");
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)});}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,st,rs);}
        }

        void upd(String st){
            int row=tbl.getSelectedRow();
            if(row<0){showMsg(this,"Select a payment.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
            int pid=(int)mdl.getValueAt(row,0);
            Connection con=null; PreparedStatement ps=null;
            try{con=DB.get();ps=con.prepareStatement("UPDATE payments SET payment_status=? WHERE payment_id=?");ps.setString(1,st);ps.setInt(2,pid);ps.executeUpdate();
                showMsg(this,"✅ Payment #"+pid+" → "+st,"Done",JOptionPane.INFORMATION_MESSAGE);load();}
            catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MANAGE RETURNS & FINES
    // ════════════════════════════════════════════════════════════════
    static class ManageReturnsFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl;

        ManageReturnsFrame(JFrame p){
            parent=p; setTitle("Manage Returns & Fines — Admin");
            setSize(980,500); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("Manage Returns & Fines","All resource returns and fine records"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,10,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            RippleButton ref=btn("↻ Refresh",CYAN.darker()); top.add(ref);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"Return ID","Booking ID","User","Resource","Return Date","Fine (₹)","Status"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            ref .addActionListener(e->load());
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(){
            mdl.setRowCount(0);
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{
                con=DB.get();st=con.createStatement();
                rs=st.executeQuery("SELECT ret.return_id,ret.booking_id,u.name,r.resource_name,ret.actual_return_date,ret.fine_amount,ret.return_status FROM returns ret JOIN bookings b ON ret.booking_id=b.booking_id JOIN users u ON b.user_id=u.user_id JOIN resources r ON b.resource_id=r.resource_id ORDER BY ret.return_id DESC");
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,st,rs);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // VIEW FEEDBACK (ADMIN)
    // ════════════════════════════════════════════════════════════════
    static class ViewFeedbackFrame extends JFrame {
        JFrame parent; JTable tbl; DefaultTableModel mdl; JTextField srch;

        ViewFeedbackFrame(JFrame p){
            parent=p; setTitle("View Feedback — Admin");
            setSize(1000,510); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            build(); load(null);
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,0));
            root.add(hdr("View Feedback","All user ratings and comments"),BorderLayout.NORTH);

            JPanel top=cardPanel(new FlowLayout(FlowLayout.LEFT,12,10));
            top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COL));
            srch=tf(22);
            RippleButton sb=btn("Search",CYAN.darker()),rb=btn("↻ Refresh",new Color(40,100,60));
            top.add(lbl("Search:",F_LABEL,TXT_GRAY));top.add(srch);top.add(sb);top.add(rb);
            root.add(top,BorderLayout.AFTER_LAST_LINE);

            String[] cols={"Feedback ID","User","Resource","Rating","Comment","Date"};
            mdl=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(mdl); styleTable(tbl);
            tbl.getColumnModel().getColumn(4).setPreferredWidth(300);
            JScrollPane sp=darkScroll(tbl); sp.setBorder(BorderFactory.createEmptyBorder(8,16,4,16));
            root.add(sp,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton back=btn("← Back",new Color(55,55,90)); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            sb.addActionListener(e->load(srch.getText().trim()));
            rb.addActionListener(e->{srch.setText("");load(null);});
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        void load(String q){
            mdl.setRowCount(0);
            Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
            try{
                con=DB.get();
                String sql="SELECT f.feedback_id,u.name,r.resource_name,f.rating,f.comment,f.feedback_date FROM feedback f JOIN users u ON f.user_id=u.user_id JOIN resources r ON f.resource_id=r.resource_id";
                if(q!=null&&!q.isEmpty()){sql+=" WHERE u.name LIKE ? OR r.resource_name LIKE ? OR f.comment LIKE ?";ps=con.prepareStatement(sql);ps.setString(1,"%"+q+"%");ps.setString(2,"%"+q+"%");ps.setString(3,"%"+q+"%");}
                else ps=con.prepareStatement(sql+" ORDER BY f.feedback_id DESC");
                rs=ps.executeQuery();
                while(rs.next()) mdl.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5),rs.getString(6)});
            }catch(Exception ex){showMsg(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{DB.close(con,ps,rs);}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // REPORTS
    // ════════════════════════════════════════════════════════════════
    static class ReportsFrame extends JFrame {
        JFrame parent;
        ReportsFrame(JFrame p){
            parent=p; setTitle("Reports & Analytics — Admin");
            setSize(960,640); setLocationRelativeTo(null); setDefaultCloseOperation(DISPOSE_ON_CLOSE); build();
        }

        void build(){
            JPanel root=darkPanel(new BorderLayout(0,8));
            root.add(hdr("Reports & Analytics","System-wide statistics and data summary"),BorderLayout.NORTH);

            // 8 stat cards
            JPanel cards=darkPanel(new GridLayout(2,4,10,10));
            cards.setBorder(BorderFactory.createEmptyBorder(10,20,4,20));
            Object[][] sc={
                {"Total Users",   "SELECT COUNT(*) FROM users WHERE role='USER'",CYAN},
                {"Resources",     "SELECT COUNT(*) FROM resources",GREEN},
                {"All Bookings",  "SELECT COUNT(*) FROM bookings",ORANGE},
                {"Pending",       "SELECT COUNT(*) FROM bookings WHERE status='PENDING'",RED},
                {"Completed",     "SELECT COUNT(*) FROM bookings WHERE status='COMPLETED'",new Color(0,200,140)},
                {"Payment Total", "SELECT CONCAT('Rs ',IFNULL(FORMAT(SUM(amount),0),0)) FROM payments WHERE payment_status='SUCCESS'",GOLD},
                {"Fines Total",   "SELECT CONCAT('Rs ',IFNULL(FORMAT(SUM(fine_amount),0),0)) FROM returns",PINK},
                {"Categories",    "SELECT COUNT(*) FROM categories",PURPLE}
            };
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{
                con=DB.get(); st=con.createStatement();
                for(Object[] s:sc){
                    String val="0";
                    try{rs=st.executeQuery((String)s[1]);if(rs.next())val=rs.getString(1);if(rs!=null)rs.close();}catch(Exception ig){}
                    Color ac=(Color)s[2];
                    JPanel card=cardPanel(new BorderLayout());
                    card.setBorder(BorderFactory.createEmptyBorder(12,8,12,8));
                    JPanel bar=new JPanel(); bar.setBackground(ac); bar.setPreferredSize(new Dimension(0,3));
                    card.add(bar,BorderLayout.NORTH);
                    JLabel vl=lbl(val,new Font("Segoe UI",Font.BOLD,22),ac);
                    vl.setHorizontalAlignment(SwingConstants.CENTER);
                    JLabel tl=lbl((String)s[0],F_SMALL,TXT_GRAY);
                    tl.setHorizontalAlignment(SwingConstants.CENTER);
                    card.add(vl,BorderLayout.CENTER); card.add(tl,BorderLayout.SOUTH);
                    cards.add(card);
                }
            }catch(Exception ex){}finally{DB.close(con,st,rs);}
            root.add(cards,BorderLayout.AFTER_LAST_LINE);

            // Tabbed tables
            JTabbedPane tabs=new JTabbedPane();
            tabs.setFont(F_LABEL);
            tabs.setBackground(BG_CARD); tabs.setForeground(TXT_WHITE);
            tabs.addTab("📋 Recent Bookings",   bookingsTab());
            tabs.addTab("📦 Resources Overview", resourcesTab());
            root.add(tabs,BorderLayout.CENTER);

            JPanel bp=darkPanel(new FlowLayout(FlowLayout.CENTER,15,12));
            RippleButton ref=btn("↻ Refresh",CYAN.darker());
            RippleButton back=btn("← Back",new Color(55,55,90));
            bp.add(ref); bp.add(back);
            root.add(bp,BorderLayout.SOUTH);

            ref .addActionListener(e->{dispose();new ReportsFrame(parent).setVisible(true);});
            back.addActionListener(e->{parent.setVisible(true);dispose();});
            addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){parent.setVisible(true);}});
            setContentPane(root);
        }

        JScrollPane bookingsTab(){
            String[] cols={"Booking ID","User","Resource","Amount (₹)","Status"};
            DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable t=new JTable(m); styleTable(t);
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT b.booking_id,u.name,r.resource_name,b.total_amount,b.status FROM bookings b JOIN users u ON b.user_id=u.user_id JOIN resources r ON b.resource_id=r.resource_id ORDER BY b.booking_id DESC LIMIT 100");
                while(rs.next()) m.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)});}
            catch(Exception ig){} finally{DB.close(con,st,rs);}
            return darkScroll(t);
        }

        JScrollPane resourcesTab(){
            String[] cols={"ID","Name","Category","Total Qty","Avail Qty","Rent/Day","Status"};
            DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable t=new JTable(m); styleTable(t);
            Connection con=null; Statement st=null; ResultSet rs=null;
            try{con=DB.get();st=con.createStatement();rs=st.executeQuery("SELECT r.resource_id,r.resource_name,c.category_name,r.quantity,r.available_quantity,r.rent_per_day,r.status FROM resources r JOIN categories c ON r.category_id=c.category_id ORDER BY r.resource_name");
                while(rs.next()) m.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5),rs.getString(6),rs.getString(7)});}
            catch(Exception ig){} finally{DB.close(con,st,rs);}
            return darkScroll(t);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MAIN — Entry Point
    // ════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        // Use system L&F as base, then override colors
        try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }catch(Exception ig){}

        // Force dark backgrounds for dialogs
        UIManager.put("OptionPane.background",         BG_CARD);
        UIManager.put("Panel.background",              BG_CARD);
        UIManager.put("OptionPane.messageForeground",  Color.WHITE);
        UIManager.put("Button.background",             BG_CARD2);
        UIManager.put("Button.foreground",             Color.WHITE);

        SwingUtilities.invokeLater(()->{
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║  Smart Community Resource Rental & Booking  ║");
            System.out.println("║  Dark Theme Edition  |  Java Swing + MySQL  ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("  Connecting to database...");
            initDB();
            System.out.println("  Launching Login Screen...");
            new LoginFrame().setVisible(true);
        });
    }

} // ── END OF SmartCommunityResourceRental ──────────────────────────
