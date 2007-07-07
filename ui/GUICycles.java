/* GUICycles.java
 *
 * This class draw the cycles component. It gives a representation of the timing 
 * behaviour of the pipeline.
 * (c) 2006 Filippo Mondello
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

import edumips64.utils.Config;
import edumips64.core.*;
import edumips64.core.is.*;
import java.awt.*;
import javax.swing.*;
import javax.accessibility.*;
import java.awt.event.*;
import java.util.*;

/** This class draw the cycles component. It gives a representation of the timing 
* behaviour of the pipeline.
* @author Filippo Mondello
*/
public class GUICycles extends GUIComponent {
		
	Panel1 pannello;
	Panel2 pannello2;
	
	JScrollPane jsp1,jsp2;
	private JSplitPane splitPane;
	int conta,tempo,oldTime,n_instr;
	Instruction [] instr;
	boolean flag[]=new boolean[5];
	JButton bottone;


	Map <CPU.PipeStatus,Instruction> pipeline;
	Map<CPU.PipeStatus,Color> colore;
	Dimension dim,dim2;
	
	java.util.List<ElementoCiclo> lista;
	
	public GUICycles(){
		super();
		lista=Collections.synchronizedList(new LinkedList<ElementoCiclo>());
		pannello=new Panel1();
		
		jsp1=new JScrollPane(pannello);//pannello di destra
		dim=new Dimension(20,30);
		pannello.setPreferredSize(dim);
		
		pannello2=new Panel2();
		
		jsp2=new JScrollPane(pannello2);//pannello di sinistra
		dim2=new Dimension(10,30);
		pannello2.setPreferredSize(dim2);
		
		jsp1.setVerticalScrollBar(jsp2.getVerticalScrollBar());

		jsp1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jsp1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,jsp2,jsp1);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		
		pipeline=new HashMap<CPU.PipeStatus,Instruction>();
		tempo=0;
		n_instr=0;
		oldTime=0;
		instr=new Instruction[5];
		
		for(int i=0;i<5;i++)
			flag[i]=false;
	}
	
	public void setContainer(Container co){
		super.setContainer(co);
		cont.add(splitPane);
		draw();
	}
	
	
	public synchronized void update(){
		synchronized(pannello) {
			synchronized(pannello2) {
				pipeline=cpu.getPipeline();

				tempo=cpu.getCycles();

				int firstIndex;

				if(oldTime!=tempo){
					if(tempo>0){

						int indice = lista.size();

						instr[0]=pipeline.get(CPU.PipeStatus.IF);
						instr[1]=pipeline.get(CPU.PipeStatus.ID);
						instr[2]=pipeline.get(CPU.PipeStatus.EX);
						instr[3]=pipeline.get(CPU.PipeStatus.MEM);
						instr[4]=pipeline.get(CPU.PipeStatus.WB);

						if( (tempo>4) && (instr[4]!=null) ){

							if(instr[4].getName()!=" "){

								for(firstIndex=java.lang.Math.min(4,n_instr);firstIndex>0;firstIndex--){
									if(lista.get(indice-firstIndex).getStato().getLast()=="MEM"){
										lista.get(indice-firstIndex).addStato("WB");
										break;
									}
								}
								flag[4]=false;
							}
							else
								flag[4]=true;
						}

						if((tempo>3)&&(instr[3] != null)){

							if(instr[3].getName()!=" "){

								for(firstIndex=java.lang.Math.min(3,n_instr);firstIndex>0;firstIndex--){
									if(lista.get(indice-firstIndex).getStato().getLast()=="EX"){
										lista.get(indice-firstIndex).addStato("MEM");
										break;
									}

								}
								flag[3]=false;
							}
							else
								flag[4]=true;
						}

						if( (tempo>2) && (instr[2]!=null) ){

							if(instr[2].getName()!=" "){
								for(firstIndex=java.lang.Math.min(2,n_instr);firstIndex>0;firstIndex--){
									if((lista.get(indice-firstIndex).getStato().getLast()=="ID")|| (lista.get(indice-firstIndex).getStato().getLast()=="Raw")){
										lista.get(indice-firstIndex).addStato("EX");
										break;
									}
								}
								flag[2]=false;
							}
							else
								flag[2]=true;
						}

						//si deve scandire lista e per le istruzioni floating point in ID o in RAW o in WAW o in stallo strutturale aggiungere lo stato successivo (a seconda che sia addizione,moltipl, o divisione)

					
						

						if( (tempo>1) && (instr[1]!= null) ){

							if(instr[1].getName()!=" "){

								if(flag[2]==false){
									lista.get(indice-1).addStato("ID");
									flag[1]=false;
								}
								else
									if( (lista.get(indice-2).getStato().getLast()=="ID") || (lista.get(indice-2).getStato().getLast()=="Raw")){
										lista.get(indice-2).addStato("Raw");
										flag[1]=true;
									}
									else if(lista.get(indice-1).getStato().getLast()=="IF"){
										lista.get(indice-1).addStato("ID");
										flag[1]=false;
									}
									else if((lista.get(indice-1).getStato().getLast()=="ID") || (lista.get(indice-1).getStato().getLast()=="Raw")){
										lista.get(indice-1).addStato("Raw");
										flag[1]=true;
									}
							}
							else
								flag[1]=true;
						}

						if(instr[0]!=null){

							if(flag[1]==false){
								lista.add(new ElementoCiclo(instr[0].getFullName(),tempo));
								n_instr++;
							}
							else{
								if(flag[2]==true)
									lista.get(indice-1).addStato(" ");
								else{
									if(!instr[2].getName().toUpperCase().equals("HALT")){
										lista.get(indice-1).setFlagCancella(true);
										lista.add(new ElementoCiclo(instr[0].getFullName(),tempo));
										n_instr++;
									}
									else
										lista.get(indice-1).addStato(" ");

								}
							}
						}
						
					}
					else{
						lista.clear();
						oldTime=0;
						n_instr=0;
						for(int i=0;i<5;i++)
							flag[i]=false;
					}
					oldTime=tempo;
				}
			}
		}
	}
	
	
	public synchronized void draw(){

		dim.setSize(20 + tempo*30,30+n_instr*15);
		if(30+n_instr*15>pannello2.getHeight())
		dim2.setSize(splitPane.getDividerLocation(),30+n_instr*15);
		else
		dim2.setSize(splitPane.getDividerLocation(),pannello2.getHeight());
		jsp1.getViewport().setViewSize(dim);
		jsp2.getViewport().setViewSize(dim2);
		jsp2.getViewport().setViewPosition(new Point(0,n_instr*15));
		jsp1.getViewport().setViewPosition(new Point(tempo*30,n_instr*15));
		
		cont.repaint();
	}
	
	
	class Panel1 extends JPanel{
	
		public synchronized void paintComponent(Graphics g){
			super.paintComponent(g); // va fatto sempre
			setBackground(Color.white); // fondo bianco
			
			g.setColor(Color.black);

			Font f1 = new Font("Arial", Font.PLAIN, 11); 
			FontMetrics fm1 = g.getFontMetrics(f1); 
			g.setFont(f1); 
			
			riempi(g);
			
		}	
		
		
		public synchronized void riempi(Graphics g){
			int i=0;
			for(ElementoCiclo el:lista){
				int j=0;
				String pre="IF";
				String ext_st="";
				int tempo=el.getTime();
				for(String st:el.getStato()){

					ext_st="";
					if(st.equals("IF"))
						g.setColor((Color)Config.get("IFColor"));
					else if(st.equals("ID"))
						g.setColor((Color)Config.get("IDColor"));
					else if(st.equals("EX"))
						g.setColor((Color)Config.get("EXColor"));
					else if(st.equals("MEM"))
						g.setColor((Color)Config.get("MEMColor"));
					else if(st.equals("WB"))
						g.setColor((Color)Config.get("WBColor"));
					else if(st.equals("Raw")){
						if(pre.equals("ID")){
							ext_st="Raw";
							g.setColor((Color)Config.get("IDColor"));
						}
					}
					else if(st.equals(" ")){
						if(pre.equals("IF")){
							ext_st=" ";
							g.setColor((Color)Config.get("IFColor"));
						}
					}
					g.fillRect(10+(tempo+j-1)*30,9+i*15,30,13);
					g.setColor(Color.black);
					g.drawRect(10+(tempo+j-1)*30,9+i*15,30,13);
					g.drawString(st, 15+(tempo+j-1)*30,20+i*15);
					j++;
					if((!st.equals(" "))&&(!st.equals("Raw")))
						pre=st;
				}/*
				if((ext_st==" ")&&(el.getFlagCancella()==false))
					g.setColor((Color)Config.get("SAMEIFColor"));
				else if(ext_st=="Raw")
					g.setColor((Color)Config.get("RAWColor"));
				else
					g.setColor(Color.black);*/
				i++;
			}
		}
	}
	
	
	class Panel2 extends JPanel{
	
		public synchronized void paintComponent(Graphics g){
			super.paintComponent(g); // va fatto sempre
			setBackground(Color.white); // fondo bianco
			
			g.setColor(Color.black);

			
			Font f1 = new Font("Arial", Font.PLAIN, 11); 
			FontMetrics fm1 = g.getFontMetrics(f1); 
			g.setFont(f1); 
			
			int i=0;
			for(ElementoCiclo el:lista){
				g.drawString(el.getName(), 5,20+i*15);
				i++;
			}
		}
	}
	
}
