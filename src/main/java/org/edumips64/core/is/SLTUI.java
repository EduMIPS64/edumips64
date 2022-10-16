/*
 * DADDUI.java
 *
 * 16th oct 2022
 * Instruction SLTUI of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
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

package org.edumips64.core.is;

/**
 * <pre>
 *      Syntax: SLTIU rt, rs, immediate
 * Description: Records the result of an unsigned less-than comparison with a constant
 *          comparing the contents of GPR rs and the sign-extended immediate as unsigned
 *              and recording the result in GPR rt. If GPR rs is less than immediate, rt
 *    will be 1 i.e true, else rt will be 0 i.e false.
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */

class SLTUI extends SLTIU {}

