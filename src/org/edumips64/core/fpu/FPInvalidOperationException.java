/* FPDivideByZeroException.java
 *
 * 6th may, 2007
 * (c) 2006 EduMips64 project - Trubia Massimo
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
package org.edumips64.core.fpu;
 
/** Exception thrown in some arithmetic operations which are invalid operations and that 
 * give as a result a QNaN string and that signal a QNaN exception:
 * 1) Any operation on a NaN
 * 2) Addition or subtraction: ? + (??)
 * 3) Multiplication: ± 0 × ± ?
 * 4) Division: ± 0/ ± 0 or ± ?/ ± ?
 */
public class FPInvalidOperationException  extends org.edumips64.core.SynchronousException{
	public FPInvalidOperationException() {
		super("FPINVALID");
	}
}
