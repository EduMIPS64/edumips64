/* ErrorOutput.java
 *
 * This class provides a Class to print errors.
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
package edumips64.utils;

import java.lang.*;

public class ErrorOutput{
	protected static String ROW,COLUMN,LINE,DESCRIPTION;
	protected static boolean isError;
	
	public ErrorOutput(){
		ROW = new String();
		COLUMN = new String();
		LINE = new String();
		DESCRIPTION = new String();
	}
	public ErrorOutput(String ROW, String COLUMN, String LINE,String DESCRIPTION,boolean isError){
		this.ROW = ROW;
		this.COLUMN = COLUMN;
		this.LINE = LINE;
		this.DESCRIPTION = DESCRIPTION;
		this.isError = isError;
	}
	public String[] getStringArray(){
		System.out.println(ROW + COLUMN + LINE + DESCRIPTION);
		return new String[]{	ROW,
					COLUMN,
					LINE,
					DESCRIPTION };
	}
	public String toString(){
		return ROW + COLUMN + LINE + DESCRIPTION;
	}
}
