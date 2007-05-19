/* GUIAbout.java
 *
 * This class draws a simple animation for EduMips64 credits.
 * (c) 2006 EduMIPS64 project - Rizzo Vanni G.
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edumips64.ui;
import edumips64.img.*;
import java.net.URL;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;

/**
* This class draws the "about us" animation.
*/
public class GUIAbout extends JDialog implements Runnable  {
	int x=0,y=0, clock;
	Thread animazione;
	Display lavagna;
	boolean running;
	boolean click = true;
	Container cp;
	
	protected static Image logo;

	static String members[] = 
	{ 
		"EduMips64 Project",
			"http://www.edumips.org",
			"MIPS64 Instruction set simulator",
		" ",	
		"Developers:", " ",
						
		"Andrea Spadaccini",
		 	"andrea@edumips.org",
			"Project Leader - Main GUI",
		"Antonella Scandura",
			"anto.tuny@hotmail.it",
			"Main GUI - Documentation",
		"Salvatore Scellato",
			"thetarro@hotmail.com",
			"CPU - Core classes",
		"Simona Ullo",
			"simonat.u@gmail.com",
			"CPU - Documentation",
		"Vanni Rizzo",
			"ascoltalatuasete@gmail.com",
			"Art Director - Parser",
		"Andrea Milazzo",
			"mancausoft@edumips.org",	
			"Wiki admin - Parser - Bug Hunter",
		"Massimo Trubia",
			"massimotrubia83@libero.it",
			"Instruction set",
		"Daniele Russo",
			"ManOfOnor@hotmail.it",
			"Instruction set",
		"Mirko Musumeci",
			"mirkochip84@ngweb.it",
			"GUI Widgets", 
		"Alessandro Nicolosi",
			"alenico84@hotmail.com",
			"GUI Widgets",
		"Filippo Mondello (Timmy)",
			"filworld@hotmail.com",
			"GUI Widgets",
		"Erik Urzi'",
			"jesky@hotmail.it",
			"MIPS32 instruction set",
		"Lorenzo Sciuto",
			"lorenzos84@hotmail.com",
			"MIPS32 instruction set",
		"Giorgio Scibilia",
			"giorgioscibilia@gmail.com",
			"MIPS32 instruction set",
		" ","Special thanks to: "," ",
		"Fabrizio Fazzino",
			"fabrizio@fazzino.it",
			"The Professor"
	};
	int width=500,height=400;
	
	public GUIAbout(final JFrame owner){
		super(owner,"Credits", true);
		try{
			//MediaTracker mt = new MediaTracker();
			logo = IMGLoader.getImage("logo.png");
			//mt.addImage(logo,0);
			//mt.waitForAll();
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		//owner.setEnabled(false);
		//MediaTracker mt = new MediaTracker(this);

			lavagna = new Display(width,height);
			getGlassPane().addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					setVisible(false);
					running = false;
					//owner.setEnabled(true);
					//dispose();
				}
			});
			//setLocation((int)(owner.getSize().getWidth() - width)/2,(int)(owner.getSize().getHeight() - height)/2);  
			setSize(width,height);
			setLocation((getScreenWidth() - getWidth()) / 2, (getScreenHeight() - getHeight()) / 2);
			getContentPane().add(lavagna);
			//getGlassPane().setVisible(true);
			//setAlwaysOnTop(true);
			x = 0; y = 10; 
			running = true;

			// setVisible(true);
			repaint();
		start();
	}
	public static int getScreenWidth() {
		return (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	}

	public static int getScreenHeight() {
		return (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	}
	public void start() {
		if (animazione == null) {
			animazione = new Thread(this);
			animazione.start();
		}
	}
	/**Here start the animation. Setting location for the text. A infinite cycle decrement only the y value and wait for 40ms
	 */
	public void run() {
		x = 120;
		y = 300;
		while(running) {
			if(y<-1100) y = 300;	//when the text finish reset y value
			y -= 2;			//decrementing y	
			lavagna.repaint();
			try { Thread.sleep(40);}
			catch (InterruptedException e) { }
		}
	}
	/*In this class there is the Panel that draws the animation.
	 */
	class Display extends JPanel{
		int width,height,head,xg = 0,yg = 0;
		Graphics2D G;
		BufferedImage I;

		Font font_name = new Font("Verdana",Font.BOLD, 20);
		Font font_email = new Font("Verdana",Font.ITALIC + Font.BOLD, 15);
		Font font_role = new Font("Verdana",Font.BOLD, 13);

		GradientPaint gradient;
		boolean bigger;
		/* The Contructor of the Panel.
		 * w is the Panel Wiidth, y the Height
		 */
		public Display(int w,int h){	
			setBackground(Color.yellow);
			setBounds(0,0,w,h);
			width = w;
			height = h;
			xg=width;
			I = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB); //this is a Buff.Image that give a temp graphics
			G = (Graphics2D) I.getGraphics();
			head = 98;//logo.getHeight(this);
			gradient = new GradientPaint(width/4, height/4, Color.white, width, height, Color.yellow);
		}

		public void  paintComponent(Graphics g) {
			
			super.paintComponent(g);

			//setting the Rendering Hints to Antialias
			G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
			//G.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			//sfondo 
			G.setPaint(gradient);
			G.fillRect(0,0,width,height);
			//logo image
			G.drawImage(logo,new AffineTransform(1,0,0,1,50,0),this);
			//HEAD: the width value of the logo image. I use it to know when the text must disappear
			G.setColor(new Color(0,0,0));
			G.setFont(new Font("Verdana",Font.BOLD, 15));
			G.drawString("Version " + edumips64.Main.VERSION,150,92);
			head = logo.getHeight(this);
			G.setTransform(new AffineTransform(1,0,0,1,0,0));
			//inizio stringhe
			int alpha,line;
		
			for(int i= 0 ; i < members.length; i++){
				//incrementing the line value for printing (and increment i too) and getting the alpha value to use
				line = y + i * 25 +  head;
				alpha = getAlpha(line);
				G.setColor(new Color(0,0,0,alpha)); 

				//selecting only the visible text
				if(alpha > 0){		
	       				//NAME drawing it whith his colour and font				
					
					G.setFont(font_name);
					G.drawString(members[i] ,x, line);
						
					//incrementing the line value for printing (and increment i too)
						line = y + ++i * 25 +  head;
					//EMAIL
					G.setFont(font_email);
					G.drawString(members[i],x,line  -5);
						//incrementing the line value for printing (and increment i too)
						line = y + ++i * 25 +  head;
					//ROLE
					G.setFont(font_role);
					G.drawString(members[i],x,line -10);
				}else i +=2;
			}

			//per evitare lo sfarfallio applico la grafica appena creata in quella del paint

			Graphics2D g2=(Graphics2D)g;
			g2.drawImage(I,0,0,null);


		}
		/** Give the Alpha transparency value knowing the line to draw 
		 * @return (int)  the alpha value for the selected line
		 */
		public int getAlpha(int line){
			//border is the value of the height of you wrap of passage from visible to transparent
			int border=100;
			return (line <= head || line >= height)?		 	// we are out of border?
				0 							//  nothing to draw
				: (line> head && line <=border +  head)?		// else, under text but over the upper border?
					(int)((line -head)*255/border) 			// the transparency depends of the posizion
					: (line>=height-border && line<height) ? 	// on the down border?
						(int)((height-line) * 255 / border) 	// the transparency depends of the posizion
						:255;					// in the borders: the text must be visible		
		}
	}
}


