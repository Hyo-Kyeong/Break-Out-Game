package 과제5;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;



interface Size {
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 900;
	public static final int SCREEN_WIDTH = 784;
	public static final int SCREEN_HEIGHT = 861;
	public static final int WIDTH_OFFSET = 30;
	public static final int HEIGHT_OFFSET = 30;
	public static final int BALL_RADIUS = 10;
	public static final int WIDTH_CENTER = SCREEN_WIDTH/2;
	public static final int PADDLE_WIDTH = 200;
	public static final int PADDLE_HEIGHT = 20;
	public static final int PADDLE_SPEED = 30;
	public static final int FONT_SIZE = 30;
}


abstract class Item implements Size{
	int px, py;
	abstract void draw(Graphics g);
	void setX(int x) {
		px=x;
	}
	void setY(int y) {
		py=y;
	}
	void setP(int x, int y) {
		px=x; py=y;
	}
}

class MyBall extends Item{
	int r;
	int vx, vy;
	boolean alive=true;
	MyBall() {
		px=SCREEN_WIDTH/2;
		py=SCREEN_HEIGHT-HEIGHT_OFFSET*10;
		r = BALL_RADIUS;
		vx=1; vy=-3;
	}
	MyBall(int x, int y, int vx1, int vy1) {
		px=x; py=y; vx=vx1; vy=vy1; r=BALL_RADIUS;
	}
	void draw(Graphics g) {
		g.setColor(Color.white);
		if(alive) {
			g.fillOval((int)(px-r), (int)(py-r), (int)r*2, (int)r*2);
		}
	}
	void update(float t) {
		px = px + (int)(vx*t); py = py + (int)(vy*t);
		hit();
	}
	void changeVx() {
		vx = -vx;
	}
	void changeVy() {
		vy = -vy;
	}
	void setVx(int v) {
		vx = v;
	}
	void setVy(int v) {
		vy = v;
	}
	void hit() {
		if(px+r > SCREEN_WIDTH-WIDTH_OFFSET) {
			vx = -vx;
			px = SCREEN_WIDTH-WIDTH_OFFSET-r;
		}
		if(px-r < WIDTH_OFFSET) {
			vx = -vx;
			px = WIDTH_OFFSET+r;
		}
		if(py+r > SCREEN_HEIGHT-HEIGHT_OFFSET) {
			alive=false;
		}
		if(py-r < HEIGHT_OFFSET) {
			vy = -vy;
			py = HEIGHT_OFFSET+r;
		}
	}
}

class MyBlock extends Item {
	int width, height;
	boolean specialBlock=false;
	boolean visible = false;
	MyBlock(int x, int y, int w, int h) {
		px = x;
		py = y;
		width = w;
		height = h;
		visible = true;
	}
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setStroke(new BasicStroke(3));
		GradientPaint gp = new GradientPaint(px+width/2, py, new Color(0, 153, 76), px+width/2, py+height, new Color(0, 204, 0));
		
		g2.setPaint(gp);
		if(specialBlock) {
			gp = new GradientPaint(px+width/2, py, new Color(153, 0, 0), px+width/2, py+height, new Color(255, 0, 0));
			
			g2.setPaint(gp);
		}
		if(visible) {
			g2.fillRoundRect(px, py, width, height, 10, 10);
			g2.setColor(Color.black);
			g2.drawRoundRect(px, py, width, height, 10, 10);
		}
	}
	boolean hit(MyBall b) {
		int x = b.px;
		int y = b.py;
		int r = b.r;
		if((x > px && x < px+width && y-r < py+height && y+r > py+height) //bottom hit
		 || (x-r < px+width && x+r > px+width && y > py && y < py+height) //right hit
		 || (x-r < px && x+r > px && y > py && y < py+height) //left hit
		 ||(x > px && x < px+width && y-r < py && y+r > py)) //top hit 
		 {
			b.changeVx();
			b.changeVy();
			return true;
		 }
		return false;
	}
	
}

class MyPaddle extends Item {
	int width, height;
	MyPaddle() {
		width = PADDLE_WIDTH;
		height = PADDLE_HEIGHT;
		px = WIDTH_CENTER-width/2;
		py = SCREEN_HEIGHT - 3*HEIGHT_OFFSET - height;
	}
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gp = new GradientPaint(px+width/2, py, new Color(204, 255, 153), px+width/2, py+height, new Color(0, 204, 0));
		g2.setPaint(gp);
		g2.fillRoundRect(px, py, width, height, 10, 10);
	}
	boolean hit(MyBall b) {
		int x = b.px;
		int y = b.py;
		int r = b.r;
		if(x > px && x < px+width && y-r < py && y+r > py) //top hit 
		 {
			if(x < px + width*0.3)
				b.setVx(-2);
			else if(x < px + width*0.45)
				b.setVx(-1);
			if(x > px + width*0.7)
				b.setVx(2);
			else if(x > px + width*0.55)
				b.setVx(1);
			b.changeVy();
			return true;
		 }
		return false;
	}
	void moveLeft() {
		px -= PADDLE_SPEED;
		if(px < WIDTH_OFFSET) {
			px=WIDTH_OFFSET;
		}
	}
	void moveRight() {
		px += PADDLE_SPEED;
		if(px+width > SCREEN_WIDTH-WIDTH_OFFSET) {
			px=SCREEN_WIDTH-WIDTH_OFFSET-width;
		}
	}
}

abstract class GameMenu extends JPanel implements Size, Runnable, KeyListener{
	JFrame jframe;
	GameMenu next;
	ImageIcon icon;
	Image img;
	ArrayList<Clip> clips;
	int time;
	static int maxScore=0;
	int score;
	GameMenu(JFrame jf) {
		setVisible(false);
		jframe=jf;
		addKeyListener(this);
		clips = new ArrayList<Clip> ();
	}
	void isMain() {
		setVisible(true);
		requestFocus();
		requestFocusInWindow(true);
		setFocusable(true);
	}
	void playMusic(String fileName) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
			Clip clip = AudioSystem.getClip();
			clip.stop();
			clip.open(ais);
			clip.start();
			clips.add(clip);
		}
		catch(Exception e) {
			
		}
	}
	void nextPage(GameMenu next) {
		jframe.remove(this);
		jframe.add(next);
		jframe.setVisible(true);
		next.isMain();
		next.score=this.score;
		for(Clip c: clips) {
			c.close();
		}
		clips.clear();
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
		g.setFont(font);
		g.drawImage(img,  0,  0,  SCREEN_WIDTH, SCREEN_HEIGHT, null);
		
	}
	@Override
	public void run() {
		setVisible(true);
		requestFocus();
		setFocusable(true);
	};
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			nextPage(next);
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
}

class GameIntro extends GameMenu{
	
	GameIntro(JFrame JF) {
		super(JF);
		Thread T = new Thread(this);
		T.start();
		//requestFocusInWindow();
		time=0;
	}
	public void paintComponent(Graphics g) {
		icon = new ImageIcon("GameStart.jpg");
		img = icon.getImage();
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3));
		
		String s ="GameStart"+time%2+".png";
		ImageIcon i = new ImageIcon(s);
		Image imgStart = i.getImage();
		g.drawImage(imgStart, 200, SCREEN_HEIGHT/2-200, 400, 200, null);

		if(time%2==0) {
			g.setColor(Color.white);
			String press ="Press Spacebar to Play";
			int lenP = getFontMetrics(getFont()).stringWidth(press);
			g.drawString(press, 240, SCREEN_HEIGHT/2+50);
		
		}
	}
	public void run() {
		super.run();
		score=0;
		playMusic("Intro.wav");
		while(true) {
			try {
				Thread.sleep(500);
				time++;
				repaint();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
			nextPage(new GameRun(jframe));
	}
}
class GameOver extends GameMenu {
	GameOver(JFrame JF) {
		super(JF);
		Thread T = new Thread(this);
		T.start();
		time=0;
	}
	void storeScore() {
		if(score > maxScore) {
			maxScore=score;
		}
	}
	public void paintComponent(Graphics g) {
		icon = new ImageIcon("GameOver.jpg");
		img = icon.getImage();
		super.paintComponent(g);
		g.setColor(Color.white);
		String s ="GAME OVER";
		int len1 = getFontMetrics(getFont()).stringWidth(s);
		g.drawString(s, 300, SCREEN_HEIGHT/2 - 100);
		String maxS = "High Score : " + Integer.toString(maxScore);
		int len2 = getFontMetrics(getFont()).stringWidth(maxS);
		g.drawString(maxS, 280, SCREEN_HEIGHT/2);
		String curS = "Your Score : " + Integer.toString(score);
		int len3 = getFontMetrics(getFont()).stringWidth(curS);
		g.drawString(curS, 280, SCREEN_HEIGHT/2+40);
		
		if(time%2==0) {
			String press ="Press Spacebar to Restart";
			int lenP = getFontMetrics(getFont()).stringWidth(press);
			g.drawString(press, 200, SCREEN_HEIGHT/2+100);
		
		}
	}
	public void run() {
		super.run();
		playMusic("End.wav");
		storeScore();
		while(true) {
			try {
				Thread.sleep(500);
				time++;
				repaint();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
			nextPage(new GameIntro(jframe));
	}
}
class GameRun extends GameMenu {
	ArrayList<MyBall> ball = new ArrayList<MyBall> ();
	ArrayList<MyBlock> block = new ArrayList<MyBlock> ();
	MyBall addB;
	MyPaddle paddle;
	int level=1, blockCnt, hit=0;
	boolean process=false;
	boolean addBall=false;
	boolean isOver=false;
	Thread T;
	GameRun(JFrame JF) {
		super(JF);
		T = new Thread(this);
		T.start();
		isOver=false;
		process=false;
		addBall=false;
		paddle = new MyPaddle();
		score=0;
		time=0;
	}
	public void paintComponent(Graphics g) {
		icon = new ImageIcon("GameRun.png");
		img = icon.getImage();
		super.paintComponent(g);
		paddle.draw(g);
		for(MyBlock b : block) {
			b.draw(g);
		}
		for(MyBall b : ball) {
			b.draw(g);
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		playMusic("WeWishYouaMerryChristmas.wav");
		while(!isOver){
			setLevel();
			while(process) {
				try {
					Thread.sleep(10);
					time++;
					check();
					if(addBall) {
						for(int i=0; i<3;i++) {
							MyBall b = new MyBall(addB.px, addB.py, addB.vx*-1-(i-1), addB.vy);
							ball.add(b);
							addBall=false;
						}
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				repaint();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nextPage(new GameOver(jframe));
	}
	void check() {
		int deathB=0;
		for(MyBall b:ball) {
			if(!b.alive) deathB++;
			Iterator<MyBlock> it = block.iterator();
			while(it.hasNext()) {
				MyBlock bk = it.next();
				if(bk.visible && bk.hit(b)) {
					hit++;
					score+=20;
					playMusic("ping.wav");
					if(bk.specialBlock) {
						addBall=true;
						addB=b;
					}
					bk.visible=false;
				}
			}
			if(deathB==ball.size()) {
				isOver=true;
				process=false;
				break;
			}
			if(paddle.hit(b))
				playMusic("pong.wav");
			b.update(3f);
			if(hit==blockCnt) {
				level++;
				hit=0;
				ball.clear();
				block.clear();
				process=false;
				addBall=false;
				break;
			}
		}
	}
	void setLevel() {
		process=true;
		MyBall bl = new MyBall();
		ball.add(bl);
		paddle = new MyPaddle();
		
		int width = (int)((SCREEN_WIDTH-2*WIDTH_OFFSET)/(level*3));
		int height = (int)(width*0.3);
		
		blockCnt=level*3*level*3;
		for(int i=0; i<level*3; i++) {
			for(int j=0; j<level*3; j++) {
				int rand = (int)(Math.random()*5);
				MyBlock b = new MyBlock(j*width+WIDTH_OFFSET, i*height+HEIGHT_OFFSET, width, height);
				if(rand==0) b.specialBlock=true;
				block.add(b);
			}
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			paddle.moveLeft();
		}
		if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			paddle.moveRight();
		}
	}

}

public class Hw5 extends JFrame implements Size{

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Hw5 hw = new Hw5();
	}
	Hw5() {
		setTitle("벽돌깨기");
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setVisible(true);
		setFocusable(false);
		add(new GameIntro(this));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
	}

}
